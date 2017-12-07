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

@InitiatingFlow
@StartableByRPC
class OraclizeQueryWaitFlow (val datasource: String, val query: String, val proofType: Int = 0, val delay: Int = 0) : FlowLogic<Answer>() {

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

        val queryID = subFlow(OraclizeQueryFlow(
                datasource = "URL",
                query = "json(https://min-api.cryptocompare.com/data/price?fsym=USD&tsyms=GBP).GBP",
                proofType = 16
        ))

        while (!subFlow(OraclizeQueryStatusFlow(queryID)))
            Fiber.sleep(5000)

        return subFlow(OraclizeQueryResultFlow(queryID))
    }
}