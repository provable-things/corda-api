package it.oraclize.cordapi.examples.flows

import co.paralleluniverse.fibers.Suspendable
import it.oraclize.cordapi.OraclizeUtils
import it.oraclize.cordapi.entities.Answer
import it.oraclize.cordapi.examples.contracts.CashIssueContract
import it.oraclize.cordapi.examples.states.CashOwningState
import it.oraclize.cordapi.flows.*
import it.oraclize.cordapi.entities.*
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
    class Initiator(val amount: Int) : FlowLogic<SignedTransaction>() {
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
        override fun call(): SignedTransaction {

            // Parties involved
            val oracle = serviceHub.identityService
                    .wellKnownPartyFromX500Name(OraclizeUtils.getNodeName()) as Party

            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            progressTracker.currentStep = QUERYING_ORACLE

            val answer = subFlow(OraclizeQueryAwaitFlow(
                    datasource = "URL",
                    query = "json(https://min-api.cryptocompare.com/data/price?fsym=USD&tsyms=GBP).GBP",
                    proofType = ProofType.TLSNOTARY
            ))

            console.info(answer.toString())

            progressTracker.currentStep = RESULTS_RECEIVED

            console.info("Oraclize: ${answer.queryId} proccessed")

            progressTracker.currentStep = PROOF
            val proofVerificationTool = OraclizeUtils.ProofVerificationTool()
            proofVerificationTool.verifyProof(answer.proof as ByteArray)

            progressTracker.currentStep = CREATING_TX
            // States + commands + contract = raw transaction <- it can be modified
            val issueState = CashOwningState(amount, ourIdentity)
            val issueCommand = Command(CashIssueContract.Commands.Issue(),
                    issueState.participants.map { it.owningKey })
            val answerCommand = Command(answer, oracle.owningKey)
            val txBuilder = TransactionBuilder(notary).withItems(
                    StateAndContract(issueState, CashIssueContract.TEST_CONTRACT_ID),
                    issueCommand, answerCommand)

            progressTracker.currentStep = VERIFYING_TX
            txBuilder.toLedgerTransaction(serviceHub).verify() // <- it cannot be modified

            // Give to the oracle only the appropriate
            // commands inside the tx
            fun filtering(elem: Any): Boolean {
                return when (elem) {
                    is Command<*> -> oracle.owningKey in elem.signers && elem.value is Answer
                    else -> false
                }
            }

            progressTracker.currentStep = GATHERING_SIGNS
            val ftx = txBuilder.toWireTransaction(serviceHub).buildFilteredTransaction(Predicate { filtering(it) })

            val fullySignedTx = serviceHub.signInitialTransaction(txBuilder)
                    .withAdditionalSignature(subFlow(OraclizeSignFlow(ftx)))

            // Catch also the notary signature and further verifications
            progressTracker.currentStep = FINALIZING_TX
            return subFlow(FinalityFlow(fullySignedTx, FINALIZING_TX.childProgressTracker()))

        }
    }

}