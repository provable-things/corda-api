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

package it.oraclize.cordapi.flows

import it.oraclize.cordapi.OraclizeUtils

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.crypto.TransactionSignature
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.identity.Party
import net.corda.core.transactions.FilteredTransaction
import net.corda.core.utilities.UntrustworthyData
import net.corda.core.utilities.loggerFor
import net.corda.core.utilities.unwrap

/**
 * Starts the flow to sign the filtered transaction.
 */
@InitiatingFlow(version = 1)
class OraclizeSignFlow(private val ftx: FilteredTransaction) : FlowLogic<TransactionSignature>() {
    companion object {
        @JvmStatic
        val console = loggerFor<OraclizeSignFlow>()
    }

    @Suspendable
    override fun call(): TransactionSignature {
        // TODO(change to a constant ORACLE_NAME see option sample and use serviceHub.firstIdentityByName())
        val oracle = OraclizeUtils.getPartyNode(serviceHub)
        val session = initiateFlow(oracle)

        val untrustedData: UntrustworthyData<TransactionSignature> = session.sendAndReceive(ftx)

        return untrustedData.unwrap { tx ->
            // TODO(additional checks?)
            tx
        }
    }
}