package it.oraclize.cordapi.examples.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.SignTransactionFlow
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.loggerFor

@InitiatedBy(SubmitFlow::class)
class ResponderFlow(val session: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(session) {
            override fun checkTransaction(stx: SignedTransaction) {
                loggerFor<ResponderFlow>().info("Checking the transaction")
            }
        }

        subFlow(signTransactionFlow)
    }
}