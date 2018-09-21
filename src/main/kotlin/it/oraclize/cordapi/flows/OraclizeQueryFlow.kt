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