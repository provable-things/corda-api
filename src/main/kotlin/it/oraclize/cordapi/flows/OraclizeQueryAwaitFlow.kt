package it.oraclize.cordapi.flows

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.Suspendable
import it.oraclize.cordapi.OraclizeUtils
import it.oraclize.cordapi.entities.Answer
import it.oraclize.cordapi.examples.flows.Example
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import java.time.Duration

@InitiatingFlow
@StartableByRPC
class OraclizeQueryAwaitFlow(val datasource: String,
                             val query: Any,
                             val proofType: Int = 0,
                             val delay: Int = 0) : FlowLogic<Answer>() {

    companion object {
        object PROCESSING : ProgressTracker.Step("Wait for the results.")

        @JvmStatic
        fun tracker() = ProgressTracker(PROCESSING)
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): Answer {

        val oracle = serviceHub.identityService
                .wellKnownPartyFromX500Name(OraclizeUtils.getNodeName()) as Party

        val queryId = subFlow(OraclizeQueryFlow(datasource, query, proofType))

        while (!subFlow(OraclizeQueryStatusFlow(queryId)))
            sleep(Duration.ofSeconds(5))

        return subFlow(OraclizeQueryResultFlow(queryId))
    }
}