package it.oraclize.cordapi.examples.contracts

import it.oraclize.cordapi.OraclizeUtils
import it.oraclize.cordapi.entities.Answer
import it.oraclize.cordapi.examples.states.CashOwningState
import net.corda.core.contracts.*

import net.corda.core.transactions.LedgerTransaction

open class CashIssueContract : Contract {

    /**
     * Commands:
     *   Issue -> issue cash to a specific party
     *            checking the USD/GBP change rate
     */
    interface Commands : CommandData {
        class Issue : Commands
    }

    companion object {
        @JvmStatic
        val TEST_CONTRACT_ID = "it.oraclize.cordapi.examples.contracts.CashIssueContract"

        @JvmStatic
        val USD_GBP_RATE_THRESH = 0.70
    }

    override fun verify(tx: LedgerTransaction) {
        val issue = tx.commands.requireSingleCommand<Commands.Issue>()
        val answCmd = tx.commands.requireSingleCommand<Answer>()

        requireThat {
            val out = tx.outputsOfType<CashOwningState>().single()
            "No inputs should be consumed." using (tx.inputs.isEmpty())
            "Only one output should be produced." using (tx.outputs.size == 1)
            "All the participants must be signers" using
                    (issue.signers.containsAll((out.participants.map { it.owningKey })))

<<<<<<< HEAD
            val rate = answCmd.value.result as String
            "The rate USD/GBP must be over $USD_GBP_RATE_THRESH" using (rate.toDouble() > USD_GBP_RATE_THRESH)
            "Oraclize's proof verification failed" using  (OraclizeUtils.verifyProof(answCmd.value.proof as ByteArray))
=======
            val rate = answCmd.value.rawValue as String
            "The rate USD/GBP must be over $USD_GBP_RATE_THRESH" using (rate.toDouble() > USD_GBP_RATE_THRESH)
            val proofVerificationTool = OraclizeUtils.ProofVerificationTool()
            "Oraclize's proof verification failed" using  (
                    proofVerificationTool.verifyProof(answCmd.value.proof as ByteArray))
>>>>>>> EXPORT
        }
    }
}