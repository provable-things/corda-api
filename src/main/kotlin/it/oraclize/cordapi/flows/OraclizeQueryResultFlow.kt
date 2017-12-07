package it.oraclize.cordapi.flows

import it.oraclize.cordapi.entities.Answer
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor

@InitiatingFlow
class OraclizeQueryResultFlow (val datasource: String, val query: String, val proofType: Int = 0, val delay: Int = 0) : FlowLogic<Answer>() {

    override val progressTracker: ProgressTracker?
        get() = ProgressTracker(
                ProgressTracker.Step("OraclizeQueryResultFlow")
        )

    companion object {
        @JvmStatic
        val console = loggerFor<OraclizeQueryResultFlow>()
    }

    override fun call(): Answer {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}