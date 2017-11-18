package it.oraclize.cordapi

import it.oraclize.cordapi.flows.OraclizeQueryFlow
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.loggerFor

class OraclizeUtils {
    companion object {
        @JvmStatic
        val console = loggerFor<OraclizeQueryFlow>()

        @JvmStatic
        private fun proofVerificationTool(proof: ByteArray) : Boolean {
            return true
        }

        @JvmStatic
        fun verifyProof(proof: ByteArray) : Boolean {
            console.info("Proof called!")
            return true
        }

        @JvmStatic
        fun getNodeName() = CordaX500Name(
                "Oraclize",
                "London",
                "GB"
        )
    }
}