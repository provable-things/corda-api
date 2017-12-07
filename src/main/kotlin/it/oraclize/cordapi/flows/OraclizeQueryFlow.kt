package it.oraclize.cordapi.flows

import it.oraclize.cordapi.OraclizeUtils
import it.oraclize.cordapi.entities.Query

import co.paralleluniverse.fibers.Suspendable

import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC

import net.corda.core.identity.Party
import net.corda.core.flows.FlowLogic
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import net.corda.core.utilities.unwrap

@InitiatingFlow
@StartableByRPC
class OraclizeQueryFlow (val datasource: String, val query: String, val proofType: Int = 0, val delay: Int = 0) : FlowLogic<String>() {

    override val progressTracker: ProgressTracker?
        get() = ProgressTracker(
                ProgressTracker.Step("OraclizeQueryFlow")
        )

    companion object {
        @JvmStatic
        val console = loggerFor<OraclizeQueryFlow>()
    }


    // start OraclizeQueryFlow datasource: "URL", query: "json(https://min-api.cryptocompare.com/data/price?fsym=USD&tsyms=GBP).GBP", delay: 0, proof_type: 16
    // start OraclizeQueryFlow datasource: identity, query: hello, proofType: 0, delay: 0
    @Suspendable
    override fun call(): String {
        val oraclize = serviceHub.identityService
                .wellKnownPartyFromX500Name(OraclizeUtils.getNodeName()) as Party

        val session = initiateFlow(oraclize)

        val untrustedString = session.sendAndReceive<String>(Query(datasource, query, delay, proofType))

        return untrustedString.unwrap { queryID -> queryID }
    }
}