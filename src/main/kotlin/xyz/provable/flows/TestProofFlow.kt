package xyz.provable.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import xyz.provable.states.ProofType
import xyz.provable.utils.ProofVerificationTool

@StartableByRPC
@InitiatingFlow
class TestProofFlow  : FlowLogic<Boolean>() {
    companion object {
        object QUERY : ProgressTracker.Step("Querying Provable")
        object VERIFYING : ProgressTracker.Step("Veryifying proof")
        object SUCCESS : ProgressTracker.Step("Flow finished")

        @JvmStatic
        fun tracker() = ProgressTracker(QUERY, VERIFYING, SUCCESS)
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): Boolean {
        progressTracker.currentStep = QUERY
        val answer = subFlow(ProvableQueryAwaitFlow(
                "URL",
                "xml(https://www.fueleconomy.gov/ws/rest/fuelprices).fuelPrices.diesel",
                ProofType.TLSNOTARY,
                0
        ))

        progressTracker.currentStep = VERIFYING

        val pvTool = ProofVerificationTool()
        val isProofVerified = pvTool.verifyProof(answer.proof!!)

        if (!isProofVerified)
            throw FlowException("The proof is not valid")

        progressTracker.currentStep = SUCCESS

        return true
    }
}