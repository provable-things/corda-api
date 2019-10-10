
## Oraclize `corda-api`

### External references

[Documentation](https://docs.provable.xyz/)

Gitter public support channel: 
[![Join the chat at https://gitter.im/provable/corda-api](https://gitter.im/provable/corda-api?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)]

### Setup instructions

```bash
git clone --recursive https://github.com/oraclize/corda-api.git
cd corda-api
./setup
```


#### Build the project

```bash
./gradlew build [-Pos=[macos, win32, linux]]
```

`-Pos` is optional and specify the architecture you want to build against to. This is useful if you want
to export the jar produced in a machine with a different operating system.
If the `-Pos` argument is not given, the local architecture is automatically detected as well as the relative
J2V8 dependency.

### Use the service in a cordapp

 1. Join testnet as explained [here](https://docs.corda.net/head/corda-testnet-intro.html)
 2. Add the following dependencies to your cordapp `build.gradle` file

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compile "com.github.oraclize:corda-api:linux_x86_64-SNAPSHOT"
}
```

**Note:** Choose a dependency from which architecture you are intended to use:

  * *Linux:* com.github.oraclize:corda-api:linux_x86_64-SNAPSHOT
  * *Windows:* com.github.oraclize:corda-api:win32_x86_64-SNAPSHOT
  * *macOS:* com.github.oraclize:corda-api:macosx_x86_64-SNAPSHOT
 
 
Follow [this](https://github.com/gitmp01/corda-example) example to see how it works  

