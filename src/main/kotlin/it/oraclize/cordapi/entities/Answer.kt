package it.oraclize.cordapi.entities

import net.corda.core.contracts.CommandData
import net.corda.core.flows.FlowException
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

/**
 * Wrap the rawValue given by the Oracle
 */
@CordaSerializable
data class Answer(val queryId: String, val rawValue: Any, val proof: ByteArray? = null) : CommandData {

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
    override fun toString(): String {
        var str = "Answer:\n" +
                "  id: $queryId\n"

        str += "  rawValue: $rawValue"
        str += "  value: $value"
        if (proof != null)
            str += "  verifyProof: ${Hex.encodeHexString(proof)}\n"
        else
            str += "  verifyProof: null\n"

        return str
    }
}