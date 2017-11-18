## Oraclize Corda-api repo

#### Examples

How to run the examples:

```$xslt
gradle deployNodes
./buid/nodes/runnodes
```

In the **`crash`** shell:

```$xslt
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