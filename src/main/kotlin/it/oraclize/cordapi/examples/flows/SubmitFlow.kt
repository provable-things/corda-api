package it.oraclize.cordapi.examples.flows

import co.paralleluniverse.fibers.Suspendable
import it.oraclize.cordapi.OraclizeUtils
import it.oraclize.cordapi.entities.Answer
import it.oraclize.cordapi.examples.contracts.CashIssueContract
import it.oraclize.cordapi.examples.states.CashOwningState
import it.oraclize.cordapi.flows.OraclizeQueryAwaitFlow
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.awt.color.ProfileDataException

@InitiatingFlow
@StartableByRPC
class SubmitFlow(val amount: Int, val party: Party) : FlowLogic<SignedTransaction>() {
    companion object {
        object ORACLIZE_ANSWER : ProgressTracker.Step("Oraclize answer")
        object TX_BUILDING : ProgressTracker.Step("Transaction building")
        object TX_VERIFYING : ProgressTracker.Step("Verifying")
        object TX_SIGNATURES : ProgressTracker.Step("Signatures")
        object TX_FINAL : ProgressTracker.Step("Finalizing")

        fun tracker() = ProgressTracker(ORACLIZE_ANSWER, TX_BUILDING, TX_VERIFYING, TX_SIGNATURES, TX_FINAL)
    }

    override val progressTracker: ProgressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val oracle = serviceHub.identityService.wellKnownPartyFromX500Name(OraclizeUtils.getNodeName()) as Party

        progressTracker.currentStep = ORACLIZE_ANSWER
        val issueState = CashOwningState(amount, ourIdentity)
        val answer = subFlow(OraclizeQueryAwaitFlow("identity", "hello world"))

        val issueCmd = Command(CashIssueContract.Commands.Issue(), listOf(ourIdentity.owningKey, party.owningKey))
        val answerCmd = Command(answer, oracle.owningKey)

        progressTracker.currentStep = TX_BUILDING
        val tx = TransactionBuilder(notary).withItems(
                StateAndContract(issueState, CashIssueContract.TEST_CONTRACT_ID),
                issueCmd,
                answerCmd
        )

        progressTracker.currentStep = TX_VERIFYING
        tx.verify(serviceHub)


        progressTracker.currentStep = TX_SIGNATURES
        val signedOnce = serviceHub.signInitialTransaction(tx)

        val session = initiateFlow(party)

        val signedTwice = subFlow(CollectSignaturesFlow(signedOnce, listOf(session)))

        progressTracker.currentStep = TX_FINAL
        return subFlow(FinalityFlow(signedTwice))

    }
}