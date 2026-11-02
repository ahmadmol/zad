# Charity Integration Final Decision

**Date:** 2026-07-18  
**Branch:** `release/final-controlled-integration`

## Decision

```text
A. Local-board enhancement
```

## Why not the other options

| Option | Selected? | Reason |
|--------|-----------|--------|
| A. Local-board enhancement | **Yes** | Matches current `EhsanRepository` / `IhsanDetails` product model |
| B. Demo-only trust screen | No for production | Demo trust claims remain debug/scaffold only |
| C. Real backend and verification | **No** | No backend or verification workflow exists |
| D. Do not integrate | Rejected | Safe local-board clarity still valuable |

## Implemented safely

* `LocalCharityBoardNotice` on `EhsanScreen`
* `ProductionCharitySourceAdapter` remains `CharityCapability.LocalBoard`
* Canonical screens: `EhsanScreen`, `IhsanDetailsScreen`
* No verification badges, trust scores, impact guarantees, or payment flows
* Charity Trust demo module stays unregistered and `IhsanPlusFeatureFlags.charityTrustEnabled = false`

## Explicit non-claims

The UI must not imply verified cases, secure auth, cloud sync, guaranteed delivery, or platform payments.
