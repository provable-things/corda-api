package it.oraclize.cordapi.flows

import it.oraclize.cordapi.OraclizeUtils
import it.oraclize.cordapi.entities.Answer
import it.oraclize.cordapi.entities.Query

import co.paralleluniverse.fibers.Suspendable

import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.Party
import net.corda.core.utilities.unwrap

@InitiatingFlow
class OraclizeQueryResultFlow (val queryId : String) : FlowLogic<Answer>() {

    @Suspendable
    override fun call(): Answer {
        val oraclize = serviceHub.identityService
                .wellKnownPartyFromX500Name(OraclizeUtils.getNodeName()) as Party

        val session = initiateFlow(oraclize)

        val untrustedAnswer = session.sendAndReceive<Answer>(queryId)

        return untrustedAnswer.unwrap { answer -> answer }
    }
}