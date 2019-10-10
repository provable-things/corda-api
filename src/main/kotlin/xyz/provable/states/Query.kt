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
        @get:JsonProperty("when") val delay: Int = 0,
        @get:JsonProperty("proof_type") val proofType: Int = 0) {

    // Constants
//    @get:JsonIgnore val CONTEXT_NAME = "corda_r3_testnet_1"
//    //    @get:JsonIgnore val CONTEXT_NAME = ""
    var context : Any? = null

//    fun addContext(session: FlowSession, serviceHub: ServiceHub) {
//        context = object {
//            val protocol = "corda"
//            val name = CONTEXT_NAME
//            val type = "dlt"
//            val relative_timestamp = serviceHub.clock.millis().div(1000).toInt()
//            val creator = object {
//                val uuid = session.counterparty.owningKey.toBase58String()
//                val name = serviceHub.networkMapCache.getNodeByLegalName(session.counterparty.name).toString()
//                val method = "corda_flow_direct"
//            }
//        }
//    }

    fun json() = ObjectMapper().writeValueAsString(this)

    override fun toString() = json()
}