package it.oraclize.cordapi.flows

import co.paralleluniverse.fibers.Suspendable
import it.oraclize.cordapi.OraclizeUtils
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.identity.Party
import net.corda.core.utilities.loggerFor
import net.corda.core.utilities.unwrap

@InitiatingFlow
class OraclizeQueryStatusFlow (val queryId: String) : FlowLogic<Boolean>() {

    @Suspendable
    override fun call(): Boolean {
        val oraclize = serviceHub.identityService
                .wellKnownPartyFromX500Name(OraclizeUtils.getNodeName()) as Party

        val session = initiateFlow(oraclize)

        val untrustedStatus = session.sendAndReceive<Boolean>(queryId)

        return untrustedStatus.unwrap { status -> status }
    }
}