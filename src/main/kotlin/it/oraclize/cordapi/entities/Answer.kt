package it.oraclize.cordapi.entities

import net.corda.core.contracts.CommandData
<<<<<<< HEAD
=======
import net.corda.core.flows.FlowException
>>>>>>> EXPORT
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

/**
<<<<<<< HEAD
 * Wrap the result given by the Oracle
 */
@CordaSerializable
data class Answer(val queryId: String, val result: Any,
                  val type: String, val proof: ByteArray? = null) : CommandData {
=======
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
>>>>>>> EXPORT

    override fun equals(other: Any?): Boolean {
        if (other == this) return true
        if (other !is Answer) return false

        val ans: Answer = other

        return EqualsBuilder().append(queryId, ans.queryId)
<<<<<<< HEAD
                .append(result, ans.result)
=======
                .append(rawValue, ans.rawValue)
>>>>>>> EXPORT
                .append(proof, ans.proof)
                .isEquals
    }
    override fun hashCode(): Int {
        return HashCodeBuilder(17, 31)
                .append(queryId)
<<<<<<< HEAD
                .append(result)
=======
                .append(rawValue)
>>>>>>> EXPORT
                .append(proof)
                .toHashCode()
    }
    override fun toString(): String {
        var str = "Answer:\n" +
                "  id: $queryId\n"
<<<<<<< HEAD
        if (result is String)
            str += "  result: $result\n"
        else if (result is ByteArray)
            str += "  result: ${Hex.encodeHexString(result)}"
=======

        str += "  rawValue: $rawValue"
        str += "  value: $value"
>>>>>>> EXPORT
        if (proof != null)
            str += "  verifyProof: ${Hex.encodeHexString(proof)}\n"
        else
            str += "  verifyProof: null\n"

        return str
    }
}