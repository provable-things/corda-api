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

package xyz.provable.flows

import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.utilities.unwrap
import co.paralleluniverse.fibers.Suspendable

import xyz.provable.utils.ProvableUtils
import xyz.provable.states.Answer

@InitiatingFlow
class ProvableQueryResultFlow (val queryId : String) : FlowLogic<Answer>() {
    @Suspendable
    override fun call(): Answer {
        val provable = ProvableUtils.getPartyNode(serviceHub)
        val session = initiateFlow(provable)
        val untrustedAnswer = session.sendAndReceive<Answer>(queryId)

        return untrustedAnswer.unwrap { answer -> answer }
    }
}