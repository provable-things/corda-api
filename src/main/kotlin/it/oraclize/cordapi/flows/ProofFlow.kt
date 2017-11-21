package it.oraclize.cordapi.flows

import co.paralleluniverse.fibers.Suspendable
import it.oraclize.cordapi.OraclizeUtils
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import java.nio.file.Files
import java.nio.file.Paths

@InitiatingFlow
@StartableByRPC
class ProofFlow() : FlowLogic<Unit>() {
    override val progressTracker: ProgressTracker?
        get() = ProgressTracker(
                ProgressTracker.Step("Resource")
        )
    @Suspendable
    override fun call() {
        val proof = Files.readAllBytes(Paths.get("/Users/mauro/Desktop/proof.proof"))

        val value = OraclizeUtils.verifyProof(proof)
        loggerFor<ProofFlow>().info("verifiedProof is ${value}")
        return Unit
    }
}