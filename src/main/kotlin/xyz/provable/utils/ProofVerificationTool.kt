/*
Copyright (c) 2015-2016 Provable SRL
Copyright (c) 2016 Provable LTD
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT.  IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package xyz.provable.utils

import co.paralleluniverse.fibers.Suspendable
import com.eclipsesource.v8.*
import com.eclipsesource.v8.utils.MemoryManager
import net.corda.core.flows.FlowException
import net.corda.core.utilities.loggerFor
import java.io.PrintWriter
import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeoutException

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
                    ?.bufferedReader() ?: throw FlowException("Unable to load the bundle.")

            console("Bundle stored in $pathToBundle.")

            val writer = PrintWriter(pathToBundle.toFile())

            writer.use {
                for (line in bundle.readLines())
                    it.println(line)
            }
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
    private  fun verify(proofs: List<ByteArray>, timer: Long? = null): Boolean {

        val bundleFile = setBundleFile().toFile()

        console("Bundle defined at $bundleFile")

        // TODO think to use the nodeJS.runtime.locker for thread safety

        val nodeJS = NodeJS.createNodeJS()

        val memV8 = MemoryManager(nodeJS.runtime)

        val module = nodeJS.require(bundleFile)

        var v8Object: V8Object? = null
        val verifiedProofs = mutableListOf<Boolean>()
        var i = 0

        val callback = V8Function(
                nodeJS.runtime
        ) { _, parameters: V8Array? ->
            v8Object = parameters?.getObject(0)

            val mainProof = v8Object?.getObject("mainProof") as V8Object // if is null then TypeCastException
            val isVerified = mainProof.getBoolean("isVerified")

            val proofNumber = verifiedProofs.size
            console(
                    if (isVerified) "$proofNumber is verified"
                    else "$proofNumber verification failed"
            )
            verifiedProofs.add(isVerified)
            ""
        }


        val v8proofs = proofs.map { toV8TypedArray(nodeJS, it) }

        console("Required variables defined")

        try {
            for (v8Proof in v8proofs) {
                console("Verifying proof ${i++}")
                module.executeJSFunction("verifyProof", v8Proof, callback) as V8Object
            }
        } catch (e: V8ScriptExecutionException) {
            throw FlowException("Proof verification failed due to the following error: \n\n ${e.message}",
                    e.cause)
        }

        val timeToSleep = timer ?: 300000L
        val timeout = Thread {
            try {

                Thread.sleep(timeToSleep)
                throw TimeoutException("ProofVerificationTool: Timeout expired.")
            } catch (e: InterruptedException) {
                console("ProofVerificationTool: ${Thread.currentThread().name} interrupted.")
            }
        }

        timeout.start()

        // [isRunning] is false until NodeJS.createNodeJS()
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
     * Verify the Provable's proof
     */
    @Suspendable
    fun verifyProof(proof: ByteArray, timer: Long? = null) = verify(listOf(proof), timer)

    /**
     * Verify a list of Provable's proofs
     */
    @Suspendable
    fun verifyProofs(proofs: List<ByteArray>, timer: Long? = null) = verify(proofs, timer)
}