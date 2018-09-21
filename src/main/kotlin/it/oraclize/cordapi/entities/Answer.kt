
/*
Copyright (c) 2015-2016 Oraclize SRL
Copyright (c) 2016 Oraclize LTD
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT.  IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

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