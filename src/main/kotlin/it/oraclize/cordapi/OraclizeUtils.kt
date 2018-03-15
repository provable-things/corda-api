package it.oraclize.cordapi

import co.paralleluniverse.fibers.Suspendable
import com.eclipsesource.v8.*
import com.eclipsesource.v8.utils.MemoryManager
import com.sun.xml.internal.fastinfoset.algorithm.BooleanEncodingAlgorithm
import it.oraclize.cordapi.entities.Answer
import net.corda.core.contracts.Command
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic.Companion.sleep

import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub
import net.corda.core.utilities.loggerFor

import java.io.PrintWriter
import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths
import java.security.PublicKey
import java.time.Duration
import java.util.concurrent.TimeoutException
import kotlin.concurrent.timer

class OraclizeUtils {



    companion object {
        @JvmStatic
        val console = loggerFor<OraclizeUtils>()

        @JvmStatic
        fun getNodeName() = CordaX500Name(
                "Oraclize",
                "London",
                "GB"
        )


    }

    @Suspendable
    fun filtering(oracleKey: PublicKey, elem: Any): Boolean {
        return when (elem) {
            is Command<*> -> oracleKey in elem.signers && elem.value is Answer
            else -> false
        }
    }

    /**
     * Wrapper class to our nodeJS proof
     * verification tool.
     */
    class ProofVerificationTool {

        private fun console(a: Any) = loggerFor<ProofVerificationTool>().info(a.toString())

        /**
         * Save to disk the NodeJS bundle containing
         * all the proof verify methods.
         */
        private fun setBundleFile() : Path {
            val pathToBundle = Paths.get(".")
                    .toAbsolutePath()
                    .resolve("pvtBundle.js")
                    .normalize()

            if (!pathToBundle.toFile().exists()) {

                // Loads the file from the resources
                val bundle = ClassLoader
                        .getSystemResourceAsStream("bundleNode.js")
                        .bufferedReader()

                console(pathToBundle.toFile().toString())

                val writer = PrintWriter(pathToBundle.toFile())

                writer.use {
                    for (line in bundle.readLines())
                        it.println(line)
                }

                console.info("Bundle stored in $pathToBundle.")
            }

            return pathToBundle
        }

        /**
         * Converts a read proof into a V8TypedArray, ready to be passed
         * as a parameter to a V8Function.
         */
        private fun toV8TypedArray(nodeJS: NodeJS, proof: ByteArray): V8TypedArray {

            var proofArray : V8ArrayBuffer? = null

            try {
                val proofBB = ByteBuffer.allocateDirect(proof.size)
                proofBB.put(proof)

                proofArray = V8ArrayBuffer(nodeJS.runtime, proofBB)

                return V8TypedArray(nodeJS.runtime, proofArray,
                        V8Value.UNSIGNED_INT_8_ARRAY, 0, proof.size)
            } finally {
                proofArray?.release()
            }
        }

        /**
         * Calls the relative NodeJS function
         * verifying the proof.
         */
        @Suspendable
        private fun verify(proof: ByteArray, timer: Long? = null) : Boolean {

            val bundleFile = setBundleFile().toFile()

            val nodeJS = NodeJS.createNodeJS()
            val memV8 = MemoryManager(nodeJS.runtime)

            val module = nodeJS.require(bundleFile)

            var v8Object : V8Object? = null
            val callback = V8Function(
                    nodeJS.runtime,
                    { _, parameters: V8Array? ->
                        v8Object = parameters?.getObject(0)
                        Unit
                    }
            )

            val proofV8 = toV8TypedArray(nodeJS, proof)

            try {
                module.executeJSFunction("verifyProof", proofV8, callback) as V8Object
            } catch (e: V8ScriptExecutionException) {
                FlowException("Proof verification failed due to the following error: \n\n ${e.message}",
                        e.cause)
            }

            val timeToSleep = timer ?: 300000L

            val timeout = Thread {
                try {
                    Thread.sleep(timeToSleep)
                    throw TimeoutException("ProofVerificationTool: Timeout expired.")
                } catch (e : InterruptedException) {
                    console.info("ProofVerificationTool: ${Thread.currentThread().name} interrupted.")
                }
            }

            timeout.start()


            // [isRunning] is false until NodeJS.createNodeJS()
            // is called or handleMessage is called
            do {
                nodeJS.handleMessage()
            } while (nodeJS.isRunning)

            return try {
                while (v8Object == null && timeout.isAlive)
                    continue

                val mainProof = v8Object?.getObject("mainProof") as V8Object // if is null then TypeCastException
                mainProof.getBoolean("isVerified")

            } catch (e: TypeCastException) {
                val msg = v8Object?.getObject("message") as String
                throw TimeoutException("ProofVerificationTool: $msg.")
            } finally {
                if (timeout.isAlive)
                    timeout.interrupt()
                memV8.release()
            }
        }

        /**
         * Verify the Oraclize's proof
         */
        @Suspendable
        fun verifyProof(proof: ByteArray, timer: Long? = null) : Boolean { return verify(proof, timer) }
    }

}