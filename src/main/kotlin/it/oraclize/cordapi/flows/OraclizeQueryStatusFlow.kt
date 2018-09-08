package it.oraclize.cordapi.flows

import co.paralleluniverse.fibers.Suspendable
import it.oraclize.cordapi.OraclizeUtils
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.identity.Party
import net.corda.core.utilities.unwrap

@InitiatingFlow(version = 1)
class OraclizeQueryStatusFlow (val queryId: String) : FlowLogic<Boolean>() {

    @Suspendable
    override fun call(): Boolean {
        val oraclize = OraclizeUtils.getPartyNode(serviceHub)

        val session = initiateFlow(oraclize)

        val untrustedStatus = session.sendAndReceive<Boolean>(queryId)

        return untrustedStatus.unwrap { status -> status }
    }
}