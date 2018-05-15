package it.oraclize.cordapi.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import net.corda.core.flows.FlowSession
import net.corda.core.node.ServiceHub
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.toBase58String
import org.apache.commons.codec.binary.Hex

/**
 * Represents an Oraclize query
 */
@CordaSerializable
data class Query(
        val datasource: String,
        val query: Any,
        @get:JsonProperty("when") val delay: Int = 0,
        @get:JsonProperty("proof_type") val proofType: Int = 0) {

    // Constants
//    @get:JsonIgnore val CONTEXT_NAME = "corda_r3_testnet_1"
    @get:JsonIgnore val CONTEXT_NAME = ""
    var context : Any? = null

    fun addContext(session: FlowSession, serviceHub: ServiceHub) {
        context = object {
            val protocol = "corda"
            val name = CONTEXT_NAME
            val type = "dlt"
            val relative_timestamp = serviceHub.clock.millis().div(1000).toInt()
            val creator = object {
                val uuid = session.counterparty.owningKey.toBase58String()
                val name = serviceHub.networkMapCache.getNodeByLegalName(session.counterparty.name).toString()
                val method = "corda_flow_direct"
            }
        }
    }

    fun json() = ObjectMapper().writeValueAsString(this)

    override fun toString() = json()
}