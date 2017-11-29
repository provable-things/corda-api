package it.oraclize.cordapi.entities

import com.fasterxml.jackson.databind.ObjectMapper
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.codec.binary.Hex

/**
     * Build the query to send to the api.oraclize
     */
@CordaSerializable
data class Query(val datasource: String, val query: Any,
                 val delay: Int = 0,     val proof_type: Int = 0) {

    fun json() = ObjectMapper().writeValueAsString(this)

    override fun toString(): String {
        return "Query:\n" +
                "  ds: $datasource\n" +
                "  query: $query\n" +
                "  delay: $delay\n" +
                "  proof_type: $proof_type\n"
    }
}