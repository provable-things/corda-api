/*
Copyright (c) 2015-2016 Provable SRL
Copyright (c) 2016 Provable LTD
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
package xyz.provable.states

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import net.corda.core.serialization.CordaSerializable

/**
 * Represents an Provable query.
 *
 * @property datasource source type, could be [URL, Random, WolframAlpha, IPFS, computation]
 * @property query query to send (could be "Weather in London" using WolframAlpha ds)
 * @property proofType check [xyz.provable.states.ProofType]
 * @property delay milliseconds to wait before executing the query
 */
@CordaSerializable
data class Query(
        val datasource: String,
        val query: Any,
        @get:JsonProperty("proof_type") val proofType: Int = 0,
        @get:JsonProperty("when") val delay: Int = 0) {

    var context : Any? = null

    fun json() = ObjectMapper().writeValueAsString(this)

    override fun toString() = json()
}