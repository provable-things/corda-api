package it.oraclize.cordapi.entities

<<<<<<< HEAD
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.codec.binary.Hex

/**
     * Build the query to send to the api.oraclize
     */
@CordaSerializable
data class Query(val datasource: String, val query: Any,
                 val delay: Int = 0,     val proofType: Int = 0) {

    // TODO(prevent code injection by using an external library to manage jsons like json stringify)
    fun json() : String {

        if (datasource.isEmpty())
            throw IllegalArgumentException("Fields \"datasource\" and \"query\" must not be empty.")

        var str = "{\"datasource\" : \"$datasource\""

        if (query is ByteArray)
            str += ",\"query\":{\"type\":\"hex\",\"value\":\"${Hex.encodeHexString(query)}\"}"

        if (query is String)
            str += ",\"query\":\"$query\""

        if (delay > 0)
            str += ",\"delay:\":$delay"

        if (proofType > 0)
            str += ",\"proof_type\":$proofType"
        str += "}"

        return str
    }

    override fun toString(): String {
        return "Query:\n" +
                "  ds: $datasource\n" +
                "  query: $query\n" +
                "  delay: $delay\n" +
                "  proofType: $proofType\n"
    }
=======
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
    @get:JsonIgnore val CONTEXT_NAME = "corda_r3_testnet_1"

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
>>>>>>> EXPORT
}