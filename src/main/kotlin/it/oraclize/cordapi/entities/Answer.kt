package it.oraclize.cordapi.entities

import net.corda.client.jackson.JacksonSupport
import net.corda.core.contracts.CommandData
import net.corda.core.flows.FlowException
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

/**
 * Enclose an Oraclize answer
 *
 * @property queryId id of the query performed
 * @property rawValue binary or string representation of the result
 * @property proof hex string used as authenticity proof
 *
 * @return a [CommandData] enclosing the answer
 */
@CordaSerializable
data class Answer(val queryId: String, val rawValue: Any, val proof: ByteArray? = null) : CommandData {

    companion object {
        @JvmStatic
        fun empty() = Answer("", "")

    }
    // Depends on the rawValue
    val type: String
    val value: String

    init {
        if (rawValue is ByteArray) {
            type = "hex"
            value = Hex.encodeHexString(rawValue)
        }
        else if (rawValue is String) {
            type = "str"
            value = rawValue
        }
        else
            throw FlowException("The value given must be a ByteArray or a String.")
    }

    override fun equals(other: Any?): Boolean {
        if (other == this) return true
        if (other !is Answer) return false

        val ans: Answer = other

        return EqualsBuilder().append(queryId, ans.queryId)
                .append(rawValue, ans.rawValue)
                .append(proof, ans.proof)
                .isEquals
    }
    override fun hashCode(): Int {
        return HashCodeBuilder(17, 31)
                .append(queryId)
                .append(rawValue)
                .append(proof)
                .toHashCode()
    }
    override fun toString(): String = JacksonSupport.createNonRpcMapper().writeValueAsString(this)

    fun isEmpty() = ( value == "" && queryId == "")
}