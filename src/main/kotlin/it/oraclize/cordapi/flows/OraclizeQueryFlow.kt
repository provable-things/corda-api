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

@StartableByRPC
@InitiatingFlow(version = 1)
class OraclizeQueryFlow (val datasource: String, val query: Any, val proofType: Int = 0, val delay: Int = 0) : FlowLogic<String>() {

    companion object {

        object PROCESSING : ProgressTracker.Step("Submitting the query.")

        @JvmStatic
        fun tracker() = ProgressTracker(PROCESSING)

        @JvmStatic
        val console = loggerFor<OraclizeQueryFlow>()
    }

    override val progressTracker = tracker()

    fun console(a: Any) = loggerFor<OraclizeQueryFlow>().info(a.toString())

    @Suspendable
    override fun call(): String {

        val oraclize = OraclizeUtils.getPartyNode(serviceHub)

        progressTracker.currentStep = PROCESSING
        val session = initiateFlow(oraclize)

        val query = Query(datasource, query, delay, proofType)
        val queryId = session.sendAndReceive<String>(query).unwrap { it }

        console("Query id: $queryId")

        return queryId
    }
}