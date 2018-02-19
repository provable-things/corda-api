package it.oraclize.cordapi

import com.eclipsesource.v8.*
import com.eclipsesource.v8.utils.MemoryManager
import com.sun.xml.internal.fastinfoset.algorithm.BooleanEncodingAlgorithm
import net.corda.core.flows.FlowException

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.loggerFor

import java.io.PrintWriter
import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths
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

    /**
     * Wrapper class to our nodeJS proof
     * verification tool.
     */
    class ProofVerificationTool {
        companion object {
            @JvmStatic
            private val VERIFY_FUNCTION_TIMEOUT: Long = 120000 // 2 minutes

        }

        fun console(a: Any) = loggerFor<ProofVerificationTool>().info(a.toString())

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
        private fun verify(proof: ByteArray) : Boolean {

            val bundleFile = setBundleFile().toFile()

            val nodeJS = NodeJS.createNodeJS()
            val memV8 = MemoryManager(nodeJS.runtime)

            val proofVerificationToolModule = nodeJS.require(bundleFile)

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
                proofVerificationToolModule
                        .executeJSFunction("verifyProof", proofV8, callback) as V8Object
            } catch (e: V8ScriptExecutionException) {
                FlowException("Proof verification failed due to the following error: \n\n ${e.message}",
                        e.cause)
            }

            val timeout = Thread {
                try {
                    Thread.sleep(VERIFY_FUNCTION_TIMEOUT)
                    throw TimeoutException("ProofVerificationTool: Timeout expired.")
                } catch (e : InterruptedException) {
                    console.info("ProofVerificationTool: Timeout interrupted.")
                }
            }


            // TL;DR isRunning is false until NodeJS.createNodeJS()
            // is called or handleMessage is called
            do {
                nodeJS.handleMessage()
            } while (nodeJS.isRunning && timeout.isAlive)

            try {
                timeout.start()

                while (v8Object == null && timeout.isAlive)
                    continue

                val mainProof = v8Object?.getObject("mainProof") as V8Object

                return mainProof.getBoolean("isVerified")

            } finally {
                if (timeout.isAlive)
                    timeout.interrupt()

                memV8.release()
            }
        }

        /**
         * Verify the Oraclize's proof
         */
        fun verifyProof(proof: ByteArray) : Boolean { return verify(proof) }
    }

}