# IHSAN — Final handoff

2026-11-02 — Asia/Damascus. Source finalization of the approved local scope; **do not recreate Batch01–05**.

- Branch: `codex/final-prebackend-closure-20261102`.
- Preserved baseline: `9698045`, `5e7a1ea`, `c93ca88`; new source/test commit `c5e8bcc485876f2c78b71978b244c6ed5928764c`. The documentation commit is final HEAD (`git log -2 --oneline`).
- Source implementation and Host local functionality: **COMPLETE within the reviewed approved local scope**; UI source review **70/70 COMPLETE**. Exact deferred services/content are itemized in the completion report.
- Device/visual acceptance: **PENDING, excluded**. Backend: **PENDING, not started**. Production: **BLOCKED** by device, D01/D02/D08/D10 and exposed-history gates.
- 30 fixed defect groups; **673 JVM PASS**, 0 failures/errors/skipped. Six new Android/Compose tests **compiled only**, none executed.
- Final Lint counts by module/variant are in the completion report; **zero errors**. Debug/Release QA builds, R8, package and signature verification PASS.
- Release QA: `.artifacts/product-finalization-20261102/IHSAN-PRODUCT-FINAL-20261102-release-qa.apk`; SHA-256 `057df90e3a997efad11a7188690acce3b71487b072d02024589fb395168c5a4c`.
- Debug QA: `.artifacts/product-finalization-20261102/IHSAN-PRODUCT-FINAL-20261102-debug-qa.apk`; SHA-256 `9d4ca5be5c75215668899f27b4836eff07f6a7eb693cc0deabe83ec12492376e`.
- Package `com.example.mol.referenceqa`, version 1/1.0, minSdk25/targetSdk35; historical QA certificate `870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63`. No production key selected.
- Room **11**, historical schemas/migrations/religious assets/versions/permissions unchanged. No user data reset/deleted; no device or private known-profile database read.

## Restart point

Read `docs/qa/IHSAN_FINAL_PREBACKEND_COMPLETION_REPORT.md` and its exact unavailable/deferred-capability table. The existing 40-task and 70-case CSVs retain separate acceptance gates. No source defect found in this mission is left half-fixed; future newly demonstrated regressions should receive a targeted fix/test. Do not turn absent reviewed content into invented metadata or activate Backend/payment/auth/media providers.

The present authorization expressly excludes **ADB, Xiaomi interaction/preflight, emulator/AVD, installation/uninstallation, device tests and device-data changes**. Do not request phone acceptance to continue independent source work. Physical QA stays PENDING until a separate instruction authorizes it. Android-test compilation is not execution or execution authorization.

No Push/Force Push/Reset/Clean/main merge/history rewrite. Published history is still contaminated; current source scans and local commits do not repair it. Keep all local originals and preservation bundles. Do not publish the source archive or religious assets as evidence of distribution rights.

## Reproducible Host checks (PowerShell, repository root)

Keep the existing JDK 21.0.11, Gradle 8.10.2, AGP 8.7.3 and pinned Kotlin/dependencies. Existing SDK is `C:/android_sdk`; keep `local.properties` private and do not overwrite it.

```powershell
$env:JAVA_HOME = Join-Path $env:USERPROFILE '.jdks\jbr-21.0.11'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :designsystem:testDebugUnitTest --rerun :feature:testDebugUnitTest --rerun :app:testDebugUnitTest --rerun -PihsanPhysicalQa=true --offline --console=plain --max-workers=2
.\gradlew.bat :app:lintDebug :app:lintRelease :feature:lintDebug :feature:lintRelease :designsystem:lintDebug :designsystem:lintRelease :feature:compileDebugAndroidTestKotlin :app:compileDebugAndroidTestKotlin :designsystem:compileDebugAndroidTestKotlin :app:assembleDebug :app:assembleRelease -PihsanPhysicalQa=true --offline --console=plain --max-workers=2
$ihsanPython = Join-Path $env:USERPROFILE '.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
& $ihsanPython scripts/test_source_privacy.py
& $ihsanPython scripts/verify_source_privacy.py --index
& $ihsanPython scripts/verify_counter_transaction.py
& $ihsanPython scripts/verify_batch03_migration.py
& $ihsanPython scripts/verify_bookmark_projection.py
git diff --check
git status --short
```

Full JVM emits the actual migration SQL consumed by the synthetic SQLite checks. `verify_counter_transaction.py` checks actual DAO SQL and generated transaction wrapping; it does not execute Android Room. There is no need to read a private/device database for these checks.

## Local evidence and recovery

Final logs, all module Lint reports, JUnit XML, package/signature/permission/SHA metadata, source-byte manifest and per-screen source hashes are in ignored `.artifacts/product-finalization-20261102/`. Earlier failed integration logs are retained. The final source-only recovery archive is `.artifacts/product-finalization-20261102/IHSAN-product-source-20261102.zip`; extract only into a new empty local folder. It includes the current tracked tree, not Git history or ignored raw artifacts. Keep the original workspace and all previous archives intact.

No device performance result, visual acceptance, production signing approval, rights grant or GitHub-history certification is claimed.
