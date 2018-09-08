package it.oraclize.cordapi.flows

import it.oraclize.cordapi.OraclizeUtils

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.crypto.TransactionSignature
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.identity.Party
import net.corda.core.transactions.FilteredTransaction
import net.corda.core.utilities.UntrustworthyData
import net.corda.core.utilities.loggerFor
import net.corda.core.utilities.unwrap

/**
 * Starts the flow to sign the filtered transaction.
 */
@InitiatingFlow(version = 1)
class OraclizeSignFlow(private val ftx: FilteredTransaction) : FlowLogic<TransactionSignature>() {
    companion object {
        @JvmStatic
        val console = loggerFor<OraclizeSignFlow>()
    }

    @Suspendable
    override fun call(): TransactionSignature {
        // TODO(change to a constant ORACLE_NAME see option sample and use serviceHub.firstIdentityByName())
        val oracle = OraclizeUtils.getPartyNode(serviceHub)
        val session = initiateFlow(oracle)

        val untrustedData: UntrustworthyData<TransactionSignature> = session.sendAndReceive(ftx)

        return untrustedData.unwrap { tx ->
            // TODO(additional checks?)
            tx
        }
    }
}