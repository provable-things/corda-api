package it.oraclize.cordapi.flows

import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor

@InitiatingFlow
class OraclizeQueryStatusFlow (val datasource: String, val query: String, val proofType: Int = 0, val delay: Int = 0) : FlowLogic<Boolean>() {

    override val progressTracker: ProgressTracker?
        get() = ProgressTracker(
                ProgressTracker.Step("OraclizeQueryStatusFlow")
        )

    companion object {
        @JvmStatic
        val console = loggerFor<OraclizeQueryStatusFlow>()
    }

    override fun call(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}