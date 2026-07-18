# Release Signing Guide

## Rules

* Never commit keystores, passwords, or `signingConfigs` secrets.
* Debug builds continue using the Android debug keystore.
* Release signing must be supplied locally or via CI secrets / environment variables.

## Recommended local setup

1. Create a release keystore **outside** the repository.
2. Add to `local.properties` (gitignored) or environment:

```properties
IHSAN_STORE_FILE=C:/path/to/ihsan-release.jks
IHSAN_STORE_PASSWORD=***
IHSAN_KEY_ALIAS=ihsan
IHSAN_KEY_PASSWORD=***
```

3. Wire `signingConfigs` in `app/build.gradle.kts` to read those properties only when present.
4. Leave unsigned/default when properties are absent so PR CI does not need secrets.

## Part 5 status

* No release signing config is present in the repository (intentional).
* Signed release verification was **not** claimed in Part 5.
* `assembleRelease` may still produce an unsigned or debug-signed artifact depending on Gradle defaults — treat as build-smoke only, not store-ready.

## Do not

* Commit `.jks` / `.keystore` files
* Commit passwords in Gradle scripts
* Enable Play publishing workflows in this phase
