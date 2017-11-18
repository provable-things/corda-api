package it.oraclize.cordapi.entities

import net.corda.core.contracts.CommandData
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

/**
 * Wrap the result given by the Oracle
 */
@CordaSerializable
data class Answer(val queryId: String, val result: Any,
                  val type: String, val proof: ByteArray? = null) : CommandData {

    override fun equals(other: Any?): Boolean {
        if (other == this) return true
        if (other !is Answer) return false

        val ans: Answer = other

        return EqualsBuilder().append(queryId, ans.queryId)
                .append(result, ans.result)
                .append(proof, ans.proof)
                .isEquals
    }
    override fun hashCode(): Int {
        return HashCodeBuilder(17, 31)
                .append(queryId)
                .append(result)
                .append(proof)
                .toHashCode()
    }
    override fun toString(): String {
        var str = "Answer:\n" +
                "  id: $queryId\n"
        if (result is String)
            str += "  result: $result\n"
        else if (result is ByteArray)
            str += "  result: ${Hex.encodeHexString(result)}"
        if (proof != null)
            str += "  verifyProof: ${Hex.encodeHexString(proof)}\n"
        else
            str += "  verifyProof: null\n"

        return str
    }
}