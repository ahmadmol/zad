# Release Signing Guide

## Rules

* Never commit keystores, passwords, or `signingConfigs` secrets.
* Debug builds continue using the Android debug keystore.
* Release signing must be supplied locally or via CI secrets / environment variables.

## Recommended local / CI variables

```text
IHSAN_KEYSTORE_PATH
IHSAN_KEYSTORE_PASSWORD
IHSAN_KEY_ALIAS
IHSAN_KEY_PASSWORD
```

Aliases also accepted in `local.properties`:

```text
IHSAN_STORE_FILE
IHSAN_STORE_PASSWORD
```

`app/build.gradle.kts` reads these from environment variables or `local.properties`.  
If absent, release falls back to the debug signing config so PR CI needs no secrets.


## Part 5 status

* No release signing config is present in the repository (intentional).
* Signed release verification was **not** claimed in Part 5.
* `assembleRelease` may still produce an unsigned or debug-signed artifact depending on Gradle defaults — treat as build-smoke only, not store-ready.

## Do not

* Commit `.jks` / `.keystore` files
* Commit passwords in Gradle scripts
* Enable Play publishing workflows in this phase
