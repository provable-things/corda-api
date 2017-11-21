## Oraclize `corda-api` repo

Clone this repository with the `--recursive` flag, due the presence of submodules.

#### Examples

How to run the examples:

```bash
gradle deployNodes
./buid/nodes/runnodes
```

In the **`crash`** shell:

```bash
>>> start Example amount: 10
>>> run vaultQuery contractStateType: it.oraclize.cordapi.examples.states.CashOwningState
```

#### Add this to your cordapp

In the gradle put the following:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compile "com.github.oraclize:corda-api:master-SNAPSHOT"
}
```

### Use case

We want to _issue_ cash to a _party_ only if an the change USD/GBP is above a certain value. A query asking for the USD/GBP rate to the Oraclize module will be performed.

The final transaction commited to the ledger will have:
  
  1. an _issue state_ which represents the fact of issuing some cash to a party
  2. an _issue command_ which wraps the issue state
  3. an _answer command_ which wraps the answer obtained by Oraclize
  
The transaction is depicted in the following figure:
![](docs/imgs/transaction.png)