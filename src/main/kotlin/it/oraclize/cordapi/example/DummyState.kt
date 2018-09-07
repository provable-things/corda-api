package it.oraclize.cordapi.example

import net.corda.core.contracts.CommandAndState
import net.corda.core.contracts.OwnableState
import net.corda.core.identity.AbstractParty

// Dummy state to run the Example
data class DummyState(val amount: Int, override val owner: AbstractParty) : OwnableState {

    override val participants: List<AbstractParty> = listOf(owner)

    override fun withNewOwner(newOwner: AbstractParty) = CommandAndState(
            DummyContract.Commands.Issue(), copy(amount = amount, owner = newOwner)
    )
}