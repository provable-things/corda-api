package it.oraclize.cordapi

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.strands.SuspendableCallable
import com.eclipsesource.v8.*
import com.eclipsesource.v8.utils.MemoryManager
import net.corda.core.flows.FlowException

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.loggerFor

import java.io.PrintWriter
import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths
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

//    data class Timeout(val nodeJS: NodeJS) : Thread() {
//        override fun run() {
//            try {
//                Thread.sleep(3000)
//            } catch (e : InterruptedException) {
//                console.info(e.stackTrace.toString())
//            }
//
//            println("inside the thread")
//            if (nodeJS.isRunning) {
//                nodeJS.runtime.terminateExecution()
//                throw FlowException("Proof verification tool: execution timeout expired.")
//            }
//        }
//
//    }

    class TimeoutException : Exception()

    class ProofVerificationTool {
        companion object {
            @JvmStatic
            private val VERIFY_FUNCTION_TIMEOUT: Long = 300000 // 5 minutes
        }


        private fun setBundleFile() : Path {
            val pathToBundle = Paths.get(".")
                    .toAbsolutePath()
                    .resolve("pvtBundle.js")
                    .normalize()

            if (!pathToBundle.toFile().exists()) {

                val bundle = ClassLoader
                        .getSystemResourceAsStream("bundleNode.js")
                        .bufferedReader()

                loggerFor<ProofVerificationTool>().info(pathToBundle.toFile().toString())
                val pw = PrintWriter(pathToBundle.toFile())

                pw.use {
                    for (line in bundle.readLines())
                        it.println(line)
                }

                console.info("Proof-verification-tool stored.")
            }

            return pathToBundle
        }

        private fun toV8TypedArray(nodeJS: NodeJS, proof: ByteArray) : V8TypedArray {

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

        private fun verify(proof: ByteArray) : Boolean {

            // Check if the bundle exists, otherwise it'll save it to disk
            val pathToBundle = setBundleFile()

            // Loading the module, preparing the required objects
            val nodeJS = NodeJS.createNodeJS()
            val memV8 = MemoryManager(nodeJS.runtime)

            // Exits the current flow when expired
            val timeout = Thread {
                try {
                    Thread.sleep(VERIFY_FUNCTION_TIMEOUT)
                } catch (e : InterruptedException) {
                    console.info("$e")
                }
            }

            try {
                val proofVerificationToolModule = nodeJS.require(pathToBundle.toFile())

                var v8Object : V8Object? = null
                val callback = V8Function(nodeJS.runtime,
                        { _, parameters: V8Array? -> v8Object = parameters?.getObject(0); Unit }
                )

                // Converts the proof into a valid V8 byte array
                val proofV8 = toV8TypedArray(nodeJS, proof)

                // verifyProof() in js code
                proofVerificationToolModule
                        .executeJSFunction("verifyProof", proofV8, callback) as V8Object


                timeout.start()

                // Must be done in this way as when the loop is stopped
                // isRunning will be set to false, and will remain false until
                // NodeJS.createNodeJS() is called or handleMessage is called
                do {
                    nodeJS.handleMessage()
                } while (nodeJS.isRunning && timeout.isAlive)


                if (!timeout.isAlive) {
                    nodeJS.runtime.terminateExecution()
                    throw FlowException("Error: verifyProof() timeout expired.")
                }


                // Wait for the callback's value
                while (v8Object == null)
                    continue

                // Explore the object returned
                val mainProof = v8Object?.getObject("mainProof") as V8Object

                return mainProof.getBoolean("isVerified")

            } catch (e : RuntimeException) {
                throw FlowException(e)
            } finally {

                // Stop the timeout if not expired
                if (timeout.isAlive) {
                    timeout.interrupt()
                    console.info("timeout released: ${timeout.isAlive}")
                }

                console.info("Releasing V8 resources")
                // Release resources
                memV8.release()
            }
        }

        fun verifyProof(proof: ByteArray) : Boolean { return verify(proof) }
    }

}