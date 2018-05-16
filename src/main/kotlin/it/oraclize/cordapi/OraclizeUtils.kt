package it.oraclize.cordapi

import co.paralleluniverse.fibers.Suspendable
import com.eclipsesource.v8.*
import com.eclipsesource.v8.utils.MemoryManager
import com.fasterxml.jackson.databind.ObjectMapper
import it.oraclize.cordapi.entities.Answer
import net.corda.core.contracts.Command
import net.corda.core.flows.FlowException

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.loggerFor

import java.io.PrintWriter
import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths
import java.security.PublicKey
import java.util.concurrent.TimeoutException

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
        private fun verify(proofs: List<ByteArray>, timer: Long? = null): Boolean  {

            val bundleFile = setBundleFile().toFile()

            console("Bundle defined at $bundleFile")

            val nodeJS = NodeJS.createNodeJS()
            val memV8 = MemoryManager(nodeJS.runtime)

            val module = nodeJS.require(bundleFile)

            var v8Object : V8Object? = null
            val verifiedProofs = mutableListOf<Boolean>()
            var i = 0

            val callback = V8Function(
                    nodeJS.runtime,
                    { _, parameters: V8Array? ->
                        v8Object = parameters?.getObject(0)

                        val mainProof = v8Object?.getObject("mainProof") as V8Object // if is null then TypeCastException
                        val isVerified = mainProof.getBoolean("isVerified")

                        val proofNumber = verifiedProofs.size
                        console(if (isVerified) "${proofNumber} is verified" else "${proofNumber} verification failed")
                        verifiedProofs.add(isVerified)
                        ""
                    }
            )

            val v8proofs = proofs.map { toV8TypedArray(nodeJS, it) }

            console("Required variables defined")

            try {

                for (v8Proof in v8proofs) {
                    console("Verifying proof ${i++}")
                    module.executeJSFunction("verifyProof", v8Proof, callback) as V8Object
                }

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
                    console("ProofVerificationTool: ${Thread.currentThread().name} interrupted.")
                }
            }

            timeout.start()

            // When isRunning is false until NodeJS.createNodeJS()
            // is called or handleMessage is called
            do {
                nodeJS.handleMessage()
            } while (nodeJS.isRunning)


            return try {
                while (v8Object == null && timeout.isAlive && i < proofs.size)
                    continue

                verifiedProofs.all { true }

            } catch (e: TypeCastException) {
                val msg = v8Object?.getObject("message").toString()
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
        fun verifyProof(proof: ByteArray, timer: Long? = null) : Boolean { return verify(listOf(proof), timer) }

        /**
         * Verify a list of Oraclize's proofs
         */
        @Suspendable
        fun verifyProofs(proofs: List<ByteArray>, timer: Long? = null) : Boolean { return verify(proofs, timer) }
    }

}