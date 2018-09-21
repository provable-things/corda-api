/*
Copyright (c) 2015-2016 Oraclize SRL
Copyright (c) 2016 Oraclize LTD
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT.  IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

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