package it.oraclize.cordapi

import com.eclipsesource.v8.*
import net.corda.core.flows.FlowException

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.loggerFor

import java.io.PrintWriter
import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths

class OraclizeUtils {
    companion object {
        @JvmStatic
        val console = loggerFor<OraclizeUtils>()

        @JvmStatic
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
            val pathToBundle = Paths.get(".")
                    .toAbsolutePath()
                    .resolve("pvtBundle.js")
                    .normalize()

            if (!pathToBundle.toFile().exists()) {

                val bundle = ClassLoader
                        .getSystemResourceAsStream("bundleNode.js")
                        .bufferedReader()

                val pw = PrintWriter(pathToBundle.toFile())

                pw.use {
                    for (line in bundle.readLines())
                        it.println(line)
                }

                console.info("File pvtBundle.js has been written on disk.")
            }

            return pathToBundle
        }

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

            return isVerified
        }

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
}