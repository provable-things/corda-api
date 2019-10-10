/*
Copyright (c) 2015-2016 provable SRL
Copyright (c) 2016 provable LTD
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

package xyz.provable.utils

import net.corda.core.contracts.Command
import net.corda.core.flows.FlowException
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub
import net.corda.core.utilities.loggerFor
import co.paralleluniverse.fibers.Suspendable
import java.security.PublicKey

import xyz.provable.states.Answer

class ProvableUtils {
    companion object {
        @JvmStatic
        val console = loggerFor<ProvableUtils>()

        @JvmStatic
        fun getPartyNode(service: ServiceHub): Party {
            val provableLegalName = CordaX500Name(
                    null,
                    null,
                    "Provable Things Ltd",
                    "London",
                    null,
                    "GB"
            )

            val provableTestnetName = CordaX500Name(
                    null,
                    "C14cf3f0e-8409-400d-89be-568f54cac309",
                    "TESTNET_Provable Things Ltd",
                    "London",
                    null,
                    "GB"
            )

            val peer = service.networkMapCache.getPeerByLegalName(provableLegalName)
                    ?: service.networkMapCache.getPeerByLegalName(provableTestnetName)

            return peer
                ?: throw FlowException(
                        "Unable to reach Provable: check you are connected to Testnet."
                )
        }
    }

    @Suspendable
    fun filtering(oracleKey: PublicKey, elem: Any): Boolean {
        return when (elem) {
            is Command<*> -> oracleKey in elem.signers && elem.value is Answer
            else -> false
        }
    }
}