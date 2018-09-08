package it.oraclize.cordapi.example

import it.oraclize.cordapi.entities.Answer
import net.corda.core.contracts.*

import net.corda.core.transactions.LedgerTransaction

open class DummyContract : Contract {

    interface Commands : CommandData { class Issue : Commands }

    companion object {
        @JvmStatic
        val CONTRACT_ID = "it.oraclize.cordapi.example.DummyContract"

        @JvmStatic
        val THRESH = 0.70
    }

    override fun verify(tx: LedgerTransaction) {
        val issue = tx.commands.requireSingleCommand<Commands.Issue>()
        val answerCommand = tx.commands.requireSingleCommand<Answer>()

        requireThat {
            val out = tx.outputsOfType<DummyState>().single()
            val rate = answerCommand.value.rawValue as String
            "No inputs should be consumed." using (tx.inputs.isEmpty())
            "Only one output should be produced." using (tx.outputs.size == 1)
            "The rate USD/GBP must be over $THRESH" using (rate.toDouble() > THRESH)
            "All the participants must be signers" using (issue.signers.containsAll(
                    (out.participants.map { it.owningKey })
            ))
        }
    }
}