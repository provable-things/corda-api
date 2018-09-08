package it.oraclize.cordapi.example

import co.paralleluniverse.fibers.Suspendable
import it.oraclize.cordapi.OraclizeUtils
import it.oraclize.cordapi.flows.*
import it.oraclize.cordapi.entities.*
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import java.util.function.Predicate

object Example {

    @InitiatingFlow
    @StartableByRPC
    @Suppress("UNUSED")
    class Initiator(val amount: Int) : FlowLogic<SignedTransaction>() {
        companion object {
            object QUERYING_ORACLE : ProgressTracker.Step("Sending query to Oraclize")
            object VERIFYING_PROOF: ProgressTracker.Step("Verifying the authenticity proof backing the result")
            object CREATING_TX : ProgressTracker.Step("Creating the transaction")
            object VERIFYING_TX : ProgressTracker.Step("Verifying the transaction")
            object SIGNING_ORACLIZE : ProgressTracker.Step("Gathering the Oraclize signature")
            object SIGNING_OURIDENTITY : ProgressTracker.Step("Signing the transaction")
            object FINALIZING_TX : ProgressTracker.Step("Finalizing the transaction") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            val console = loggerFor<Example>()
        }


        override val progressTracker = ProgressTracker(
                QUERYING_ORACLE,
                VERIFYING_PROOF,
                CREATING_TX,
                VERIFYING_TX,
                SIGNING_ORACLIZE,
                SIGNING_OURIDENTITY,
                FINALIZING_TX
        )

        @Suspendable
        override fun call(): SignedTransaction {

            // This will check if the Oraclize node exists within the network
            val oracle = OraclizeUtils.getPartyNode(serviceHub)

            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            progressTracker.currentStep = QUERYING_ORACLE

            val answer = subFlow(OraclizeQueryAwaitFlow(
                    dataSource = "URL",
                    query = "json(https://min-api.cryptocompare.com/data/price?fsym=USD&tsyms=GBP).GBP",
                    proofType = ProofType.TLSNOTARY
            ))

            progressTracker.currentStep = VERIFYING_PROOF
            val proofVerificationTool = OraclizeUtils.ProofVerificationTool()

            require(proofVerificationTool.verifyProof(answer.proof as ByteArray))

            progressTracker.currentStep = CREATING_TX

            val issueState = DummyState(amount, ourIdentity)
            val issueCommand = Command(DummyContract.Commands.Issue(), ourIdentity.owningKey)
            val answerCommand = Command(answer, oracle.owningKey)

            val txBuilder = TransactionBuilder(notary).withItems(
                    StateAndContract(issueState, DummyContract.CONTRACT_ID),
                    issueCommand, answerCommand)

            progressTracker.currentStep = VERIFYING_TX

            txBuilder.verify(serviceHub)

            progressTracker.currentStep = SIGNING_ORACLIZE

            val filtering = OraclizeUtils()::filtering
            val ftx = txBuilder.toWireTransaction(serviceHub).buildFilteredTransaction(
                    Predicate { filtering(oracle.owningKey, it) }
            )

            val oracleSigned = subFlow(OraclizeSignFlow(ftx))

            progressTracker.currentStep = SIGNING_OURIDENTITY

            val fullySignedTx = serviceHub.signInitialTransaction(txBuilder) + oracleSigned

            progressTracker.currentStep = FINALIZING_TX

            return subFlow(FinalityFlow(fullySignedTx, FINALIZING_TX.childProgressTracker()))
        }
    }
}
