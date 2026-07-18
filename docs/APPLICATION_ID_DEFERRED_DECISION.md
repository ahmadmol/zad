# Application ID — Deferred Decision

**Status:** Deferred (not a blocker for internal use)

## Current

```text
applicationId = com.example.mol
namespace = com.example.mol
```

## Why unchanged

No owner-approved production package name exists in project documentation. Inventing a domain/company ID is forbidden.

## Impact

| Use case | Impact |
|----------|--------|
| Internal sideload / QA | **None** — current ID works |
| Emulator / device testing | **None** |
| Google Play public listing | **Blocked** until approved ID + signing |

## Proposals (require owner approval — not applied)

1. `com.ihsan.app`
2. `org.ihsan.android`
3. Keep `com.example.mol` for internal builds only

## Rule

Do not rename after any public store listing under a given ID without understanding Android treats it as a different app.
