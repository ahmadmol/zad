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
Production Release now fails at `preReleaseBuild` when complete signing credentials
are absent. It never falls back to the debug certificate. Internal QA must explicitly
use `-PihsanPhysicalQa=true`; that selects `com.example.mol.referenceqa` and the existing
debug QA certificate, even on a host with production credentials. CI uses this QA mode.


## Part 5 status

* No release signing config is present in the repository (intentional).
* Signed release verification was **not** claimed in Part 5.
* The historical Part 5 fallback is superseded by the 2026-11-02 guard above.
* Providing credentials does not grant production distribution approval; D-02/D-10 remain open.

## Do not

* Commit `.jks` / `.keystore` files
* Commit passwords in Gradle scripts
* Enable Play publishing workflows in this phase


## Batch 04 internal QA — 2026-10-21

The campaign uses `-PihsanPhysicalQa=true` with the existing `com.example.mol.referenceqa` package. The resulting release QA build is checked with aapt/apksigner and SHA-256; see `docs/qa/IHSAN_PREBACKEND_BATCH04_20261021.md` for the exact artifact. The existing Android Debug QA certificate is for internal verification only. It is not an approved production signing identity. D-02 and public distribution remain pending; no Gradle signing configuration, package identity, key or permission was changed in this batch.


## Batch 05 internal QA — 2026-10-21

Continue using the existing `-PihsanPhysicalQa=true` package and certificate. D07 authorizes only the RemoteViews widget receiver/actions, not a distribution identity change. Package, APK certificate, permission set and SHA are verified against the exact Batch05 candidate; see `docs/qa/IHSAN_PREBACKEND_BATCH05_20261021.md`. No production signing enforcement or public distribution decision is inferred while D02 remains pending.
