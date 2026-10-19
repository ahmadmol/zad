# Gate 0 — baseline and audit

Status: **baseline PASS; functional audit PARTIAL, explicitly not beta acceptance**.

- Preserved the dirty working tree. Snapshot SHA-256 and before-copies under `.artifacts/release-final-20261014`; no commit/push/reset.
- Existing Dark and reference-refresh reports read. Fresh baseline `assembleDebug`, module unit tests and module lint succeeded in [gate0-baseline.log](../../.artifacts/release-final-20261014/gate0-baseline.log). Baseline remains 452 unit tests, 0 failures/errors/skips, 149 lint warnings and 0 errors.
- Static scan read 386 production Kotlin files. Declared navigation: 58 route patterns, including legacy redirect and conditional debug route. Previous 59 screenshot cases include variant/missing states and are not a contradictory route total.
- [SCREEN_INVENTORY](SCREEN_INVENTORY.md) includes route registrations and modal/state declarations; [341 callback sites](../../.artifacts/release-final-20261014/button-audit.json) include reusable components and previews, not 341 proven user actions. Runtime proof is separately tracked.
- Quran provenance/mapping checked independently: exact structural comparison of the full pinned source and every page boundary. [Integrity evidence](../../.artifacts/release-final-20261014/quran-integrity.json).
- No large hadith/dua corpus, reciter reuse grant, publisher stream permission, religious-review signoff, or supplied-artwork provenance has been fabricated. See [CONTENT_SOURCE_REGISTER](CONTENT_SOURCE_REGISTER.md).

All initial findings: [EXECUTION_STATUS](EXECUTION_STATUS.md) and [FUNCTIONAL_GAPS](FUNCTIONAL_GAPS.md). Complete function-by-function acceptance remains open until the named journeys are actually run.
