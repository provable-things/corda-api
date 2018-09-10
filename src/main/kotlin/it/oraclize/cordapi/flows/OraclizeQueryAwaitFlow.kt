package it.oraclize.cordapi.flows

import co.paralleluniverse.fibers.Suspendable
import it.oraclize.cordapi.entities.Answer
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import java.time.Duration

/**
 * Sends the query and returns the results.
 *
 * @param dataSource source type, could be [URL, Random, WolframAlpha, IPFS, computation]
 * @param query query to send (could be "Weather in London" using WolframAlpha ds)
 * @param proofType @see [it.oraclize.cordapi.entities.ProofType]
 * @param delay milliseconds to wait before executing the query
 *
 * @return an [Answer]
 */
@StartableByRPC
@InitiatingFlow(version = 1)
class OraclizeQueryAwaitFlow(val dataSource: String,
                             val query: Any,
                             val proofType: Int = 0,
                             val delay: Int = 0) : FlowLogic<Answer>() {

    companion object {
        object QUERY : ProgressTracker.Step("Querying Oraclize")
        object STATUS : ProgressTracker.Step("Waiting for the result ")
        object RESULT : ProgressTracker.Step("Giving back the result")

        @JvmStatic
        fun tracker() = ProgressTracker(QUERY, STATUS, RESULT)
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): Answer {
        progressTracker.currentStep = QUERY

        val queryId = subFlow(OraclizeQueryFlow(dataSource, query, proofType))

        progressTracker.currentStep = STATUS

        while (!subFlow(OraclizeQueryStatusFlow(queryId)))
            sleep(Duration.ofSeconds(5))

        progressTracker.currentStep = RESULT

        return subFlow(OraclizeQueryResultFlow(queryId))
    }
}