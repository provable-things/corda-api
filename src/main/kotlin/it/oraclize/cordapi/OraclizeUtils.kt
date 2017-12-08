package it.oraclize.cordapi

<<<<<<< HEAD
import com.eclipsesource.v8.*
import net.corda.core.flows.FlowException

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.loggerFor
=======
import co.paralleluniverse.fibers.Fiber
import com.eclipsesource.v8.*
import com.eclipsesource.v8.utils.MemoryManager
import it.oraclize.cordapi.flows.OraclizeQueryStatusFlow
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.loggerFor
import java.io.Closeable
>>>>>>> EXPORT

import java.io.PrintWriter
import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths

class OraclizeUtils {
    companion object {
        @JvmStatic
        val console = loggerFor<OraclizeUtils>()

        @JvmStatic
<<<<<<< HEAD
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

        @JvmStatic private fun setBundleFile() : Path {
=======
        fun getNodeName() = CordaX500Name(
                "Oraclize",
                "London",
                "GB"
        )
    }

    class ProofVerificationTool {


        private fun setBundleFile() : Path {
>>>>>>> EXPORT
            val pathToBundle = Paths.get(".")
                    .toAbsolutePath()
                    .resolve("pvtBundle.js")
                    .normalize()

            if (!pathToBundle.toFile().exists()) {

                val bundle = ClassLoader
<<<<<<< HEAD
                        .getSystemResourceAsStream("proof-verification-tool/bundleNode.js")
=======
                        .getSystemResourceAsStream("bundleNode.js")
>>>>>>> EXPORT
                        .bufferedReader()

                val pw = PrintWriter(pathToBundle.toFile())

                pw.use {
                    for (line in bundle.readLines())
                        it.println(line)
                }

<<<<<<< HEAD
                console.info("File pvtBundle.js has been written on disk.")
=======
                console.info("Proof-verification-tool stored.")
>>>>>>> EXPORT
            }

            return pathToBundle
        }

<<<<<<< HEAD
        @JvmStatic private fun proofVerificationTool(proof: ByteArray) : Boolean {

            val pathToBundle = setBundleFile()
            console.info(pathToBundle.toString())

            val nodeJS = NodeJS.createNodeJS()
            val proofV8 = toV8TypedArray(nodeJS, proof)
            val proofVerifier = nodeJS.require(pathToBundle.toFile())
            var obj : V8Object? = null

            val callback = V8Function(nodeJS.runtime,
                    { _, parameters: V8Array? -> obj = parameters?.getObject(0); Unit }
            )

            // You must release the obj returned
            val tmp = proofVerifier.executeJSFunction("verifyProof", proofV8, callback) as V8Object

            while (nodeJS.isRunning) {
                nodeJS.handleMessage()
            }

            // Loop until the object returned has been set
            while (obj == null)
                continue

            val mainProof = obj?.getObject("mainProof") as V8Object
//            for (key in mainProof.keys) {
//                console.info("$key : ${mainProof.get(key)}")
//            }
            val isVerified = mainProof.getBoolean("isVerified")

            // TODO(make it more readable)
            mainProof.release()
            obj?.release()
            tmp.release()
            callback.release()
            proofVerifier.release()
            proofV8.release()
            nodeJS.release()
=======
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
            val proofVerificationToolModule = nodeJS.require(pathToBundle.toFile())

            var v8Object : V8Object? = null
            val callback = V8Function(nodeJS.runtime,
                    { _, parameters: V8Array? -> v8Object = parameters?.getObject(0); Unit }
            )

            // Converts the proof into a valid V8 byte array
            val proofV8 = toV8TypedArray(nodeJS, proof)

            // js verifyProof call
            proofVerificationToolModule
                    .executeJSFunction("verifyProof", proofV8, callback) as V8Object

            // Must be done in this way, because when the loop is done
            // isRunning will be set to false, and will remain false until
            // NodeJS.createNodeJS() is called or handleMessage is called
            do {
                nodeJS.handleMessage()
            } while (nodeJS.isRunning)

            // Wait for the callback's rawValue
            while (v8Object == null)
                continue

            // Explore the object returned
            val mainProof = v8Object?.getObject("mainProof") as V8Object
            val isVerified = mainProof.getBoolean("isVerified")

            // Release resources
            memV8.release()
>>>>>>> EXPORT

            return isVerified
        }

<<<<<<< HEAD
        @JvmStatic
        fun verifyProof(proof: ByteArray?) : Boolean {
            if (proof != null)
                return proofVerificationTool(proof)
            else
                throw FlowException("Proof is null")
        }

        @JvmStatic
        fun getNodeName() = CordaX500Name(
                "Oraclize",
                "London",
                "GB"
        )
    }
=======
        fun verifyProof(proof: ByteArray) : Boolean { return verify(proof) }
    }

>>>>>>> EXPORT
}