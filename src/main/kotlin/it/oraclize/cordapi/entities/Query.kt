package it.oraclize.cordapi.entities

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import jdk.nashorn.internal.ir.ObjectNode
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.codec.binary.Hex

/**
     * Build the query to send to the api.oraclize
     */
@CordaSerializable
data class Query(
        val datasource: String,
        val query: Any,
        @get:JsonProperty("when") var delay: Int = 0,
        val proof_type: Int = 0) {

    fun json() = ObjectMapper().writeValueAsString(this)

    override fun toString(): String {
        return json().toString()
    }
}