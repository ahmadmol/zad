# Production Identity Decision

## Current identity

| Field | Value |
|-------|--------|
| applicationId | `com.example.mol` |
| namespace (app) | `com.example.mol` |
| namespace (feature) | `com.example.feature` |
| Decision status | **Unapproved for Play production** — release blocker |

## Proposed production ID options (not selected)

1. `com.ihsan.app` (if brand/domain owned)
2. `org.ihsan.android`
3. Keep `com.example.mol` only for internal demos

**No rename performed in Part 5** — requires explicit product approval.

## Migration implications

* Changing `applicationId` creates a **new** Play Store listing identity unless Play App Signing / transfer process is used carefully.
* Existing installs on `com.example.mol` will not auto-upgrade to a new ID.
* FileProvider authorities, deep links, and backup keys tied to package name must be updated together.

## Play Store implications

* `com.example.*` is unsuitable for a public production listing.
* Final ID must be reserved and owned before store submission.

## Signing decision required

* Release signing configuration is not committed (correct).
* See `docs/RELEASE_SIGNING_GUIDE.md`.

## Decision status

**BLOCKER:** Application identity remains `com.example.mol` until product approves a production package name.
