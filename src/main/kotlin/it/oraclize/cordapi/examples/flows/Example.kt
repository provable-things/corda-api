package it.oraclize.cordapi.examples.flows

import co.paralleluniverse.fibers.Suspendable
import com.sun.org.apache.regexp.internal.RESyntaxException
import it.oraclize.cordapi.OraclizeUtils
import it.oraclize.cordapi.entities.Answer
import it.oraclize.cordapi.examples.contracts.CashIssueContract
import it.oraclize.cordapi.examples.states.CashOwningState
import it.oraclize.cordapi.flows.OraclizeQueryFlow
import it.oraclize.cordapi.flows.OraclizeSignFlow
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import java.util.function.Predicate


object Example {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(val amount: Int) : FlowLogic<Unit>() {
        companion object {
            object QUERYING_ORACLE : ProgressTracker.Step("Sending query to Oraclize")
            object RESULTS_RECEIVED : ProgressTracker.Step("Waiting for the result from Oraclize")
            object PROOF: ProgressTracker.Step("Verifying the authenticity proof backing the result")
            object CREATING_TX : ProgressTracker.Step("Creating the transaction")
            object VERIFYING_TX : ProgressTracker.Step("Verifying the transaction")
            object GATHERING_SIGNS : ProgressTracker.Step("Gathering signatures")
            object FINALIZING_TX : ProgressTracker.Step("Finalizing the transaction") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            val console = loggerFor<Example>()
        }


        override val progressTracker = ProgressTracker(QUERYING_ORACLE, RESULTS_RECEIVED, PROOF,
                CREATING_TX, VERIFYING_TX, GATHERING_SIGNS, FINALIZING_TX)

        @Suspendable
        override fun call(): Unit {

            // Parties involved
            val oracle = serviceHub.identityService
                    .wellKnownPartyFromX500Name(OraclizeUtils.getNodeName()) as Party

            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            progressTracker.currentStep = QUERYING_ORACLE
            progressTracker.currentStep = RESULTS_RECEIVED
            val queryID = subFlow(OraclizeQueryFlow(
                    datasource = "URL",
                    query = "json(https://min-api.cryptocompare.com/data/price?fsym=USD&tsyms=GBP).GBP",
                    proofType = 16
            ))

            console.info("QueryID : $queryID")

        }
    }

}