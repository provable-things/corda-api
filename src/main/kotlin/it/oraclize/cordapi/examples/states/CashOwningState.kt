package it.oraclize.cordapi.examples.states

import it.oraclize.cordapi.examples.contracts.CashIssueContract

import net.corda.core.contracts.CommandAndState
import net.corda.core.contracts.OwnableState
import net.corda.core.identity.AbstractParty

/**
 * Represent the possession of cash by a specific party. Cash is supposed to be issued only
 * if the rate is above a specific threshold defined in the contract.
 */
data class CashOwningState(val amount: Int, override val owner: AbstractParty) : OwnableState {
    /**
     * We need the oracle sign for the transaction to be valid
     */
    override val participants: List<AbstractParty> = listOf(owner)

    override fun withNewOwner(newOwner: AbstractParty): CommandAndState {
        return CommandAndState(CashIssueContract.Commands.Issue(), copy(amount = amount, owner = newOwner))
    }
}