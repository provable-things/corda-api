package it.oraclize.cordapi.entities

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
}