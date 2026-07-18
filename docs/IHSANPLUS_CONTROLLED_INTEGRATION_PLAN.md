# IhsanPlus Controlled Integration Plan

Part 5 establishes contracts only. **Do not start Controlled Integration** until Stage gates pass.

## Stage 0 — Contracts only (Part 5)

* Snapshot + source contracts exist
* Route / DI specs exist but are unwired
* Demo modules isolated and renamed
* No production routes
* Architecture tests prevent accidental wiring

## Stage 1 — Daily read-only adapter

* Register `ProductionDailySourceAdapter` behind an explicit feature flag
* One hidden/internal entry point only
* No demo data
* Do not replace Home
* Rollback: remove flag + DI binding

## Stage 2 — Prayer assist read-only adapter

* Consume Part 1 `PrayerTimesFacade` only
* No duplicate calculator / scheduler
* Existing Prayer / Qibla screens remain canonical

## Stage 3 — Charity capability decision

Choose exactly one:

1. Local-board enhancement
2. Demo-only trust screen (debug/internal only)
3. Real backend and verification
4. Do not integrate

Charity Trust must not ship as verified production UI without real backing.

## Stage 4 — Production approval

Requires:

* Product approval
* Security review
* Data-contract review
* Device testing
* Navigation approval
* DI approval
* Release rollback plan

## Rollback strategy (Part 5 artifacts)

| Change | Rollback |
|--------|----------|
| Integration contracts | Delete `ihsanplus/integration/**` — production screens unaffected |
| Demo classification / rename | Keep Demo naming even if other slices roll back |
| CI workflow | Remove `.github/workflows/android-quality.yml` |
| R8 changes | Revert `isMinifyEnabled` (currently remains false) |
| Manifest changes | None performed in Part 5 |
| Observability | Remove logger usage; keep NoOp bindings |
| Migration tests / docs | Keep tests; never remove released Room migrations |
| Release docs | Documentation-only |

**Hard rules**

* Released Room migrations must never be removed
* Production route behavior unchanged in Part 5
* Parts 1–4 commits remain recoverable via git
* Fake/demo isolation must survive partial rollbacks
