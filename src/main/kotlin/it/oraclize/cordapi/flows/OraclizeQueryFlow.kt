package it.oraclize.cordapi.flows

<<<<<<< HEAD
import co.paralleluniverse.fibers.Suspendable
import it.oraclize.cordapi.OraclizeUtils
import it.oraclize.cordapi.entities.Answer
import it.oraclize.cordapi.entities.Query
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
=======
import it.oraclize.cordapi.OraclizeUtils
import it.oraclize.cordapi.entities.Query

import co.paralleluniverse.fibers.Suspendable

import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC

import net.corda.core.identity.Party
import net.corda.core.flows.FlowLogic
>>>>>>> EXPORT
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import net.corda.core.utilities.unwrap

<<<<<<< HEAD
// TODO(remove StartableByRPC and the progress tracker)
@InitiatingFlow
@StartableByRPC
class OraclizeQueryFlow (val datasource: String, val query: String,
                         val delay: Int = 0, val proofType: Int = 0) : FlowLogic<Answer>() {

    companion object {

=======
@InitiatingFlow
@StartableByRPC
class OraclizeQueryFlow (val datasource: String, val query: String, val proofType: Int = 0, val delay: Int = 0) : FlowLogic<String>() {

    companion object {

        object PROCESSING : ProgressTracker.Step("Submitting the query.")

        @JvmStatic
        fun tracker() = ProgressTracker(PROCESSING)
>>>>>>> EXPORT

        @JvmStatic
        val console = loggerFor<OraclizeQueryFlow>()
    }

<<<<<<< HEAD

    // start OraclizeQueryFlow datasource: "URL", query: "json(https://min-api.cryptocompare.com/data/price?fsym=USD&tsyms=GBP).GBP", delay: 0, proofType: 16
    @Suspendable
    override fun call(): Answer {
        console.info("Called!")

        val oraclize = serviceHub.identityService
                .wellKnownPartyFromX500Name(OraclizeUtils.getNodeName()) as Party

        val session = initiateFlow(oraclize)

        val untrustedAnswer = session.sendAndReceive<Answer>(Query(datasource, query, delay, proofType))

        return untrustedAnswer.unwrap { answ -> answ }
=======
    override val progressTracker = tracker()

    // start OraclizeQueryFlow datasource: "URL", query: "json(https://min-api.cryptocompare.com/data/price?fsym=USD&tsyms=GBP).GBP", proofType: 16, delay: 0
    // start OraclizeQueryFlow datasource: identity, query: hello, proofType: 0, delay: 0
    @Suspendable
    override fun call(): String {
        val oraclize = serviceHub.identityService
                .wellKnownPartyFromX500Name(OraclizeUtils.getNodeName()) as Party

        progressTracker.currentStep = PROCESSING
        val session = initiateFlow(oraclize)

        val untrustedString = session.sendAndReceive<String>(Query(datasource, query, delay, proofType))

        return untrustedString.unwrap { queryID -> queryID }
>>>>>>> EXPORT
    }
}