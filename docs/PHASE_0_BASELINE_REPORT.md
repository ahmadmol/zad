# Phase 0 Baseline Report — إحسان

**Date:** 2026-07-12  
**Mode:** Diagnostic / documentation only (no source repairs)  
**Companion inventory:** `docs/PHASE_0_FILE_INVENTORY.md`

---

## 1. Executive Result

| Question | Answer |
|----------|--------|
| Does the current working tree build? | **Yes** — `:app:assembleDebug` **BUILD SUCCESSFUL** (~10m 23s); APK produced |
| Do unit tests pass? | **Yes** — feature + app unit tests **0 failures**; root `test` **BUILD SUCCESSFUL** |
| Does lint pass? | **Yes** — `:app:lintDebug` **BUILD SUCCESSFUL** with **78 warnings**, **0 errors** |
| Changed paths (baseline) | **149** total → **Group A 64** / **Group B 85** / **Group C 0** |
| Add-Only compliant? | **No** — 85 production paths outside allowed Add-Only roots |
| Safe base for Phase 1? | **Yes, only with preservation strategy** (branch/commit groups) — not as “clean Add-Only main” |
| Controlled Integration? | **REMAINS BLOCKED** |

**Phase 0 decision:** `PHASE 0 PASSED — READY FOR PHASE 1`  
**Controlled Integration decision:** `CONTROLLED INTEGRATION REMAINS BLOCKED`

---

## 2. Environment Baseline

| Item | Value | Evidence |
|------|-------|----------|
| Repository path | `C:\Users\WIN 10\Desktop\New folder (2)\Sdk\Sdk\mol` | Shell `Get-Location` |
| OS | Windows NT 10.0.18362.0 | PowerShell |
| Java | 21.0.1 LTS (HotSpot) | `java -version` |
| Gradle wrapper | Present (`gradlew.bat`, `gradlew`) | Filesystem |
| Gradle | **8.10.2** | `.\gradlew.bat --version` |
| AGP | **8.7.3** | `gradle/libs.versions.toml` |
| Kotlin | **2.1.0** | `gradle/libs.versions.toml` |
| compileSdk | **35** | `app/build.gradle.kts` |
| targetSdk | **35** | `app/build.gradle.kts` |
| minSdk | **25** | `app/build.gradle.kts` |
| Modules | `:app`, `:feature`, `:designsystem` | `settings.gradle.kts` |
| Root project name | إحسان | `settings.gradle.kts` |

---

## 3. Git Branch and Working Tree State

| Item | Value |
|------|-------|
| Current branch | `main` |
| Upstream | `origin/main` |
| Tracking | `## main...origin/main` (no ahead/behind digits shown at capture) |
| Working tree | **Dirty** — mixed staged (`A`/`AM`) and unstaged (` M`) + untracked (`??`) |
| Destructive git actions | **Not executed** (per Phase 0 rules) |

Commands executed (read-only):

```text
git status -sb
git status --short
git branch --show-current
git rev-parse --abbrev-ref --symbolic-full-name @{u}
git diff --stat
git diff --name-status
git diff --cached --name-status
git ls-files --others --exclude-standard
```

Unstaged diffstat summary (partial aggregate): **82 files changed, ~2188 insertions, ~751 deletions** (plus large staged Add-Only / asset sets).

---

## 4. Complete Changed-File Classification

See **`docs/PHASE_0_FILE_INVENTORY.md`** for the full path list.

| Group | Definition | Count |
|-------|------------|------:|
| **A** | `docs/` + `feature/.../ihsanplus/` | **64** |
| **B** | All other changed paths (app, feature non-ihsanplus, designsystem, Gradle, resources) | **85** |
| **C** | Generated/local (`.gradle/`, `build/`, `.idea/`, `local.properties`) | **0** |

Untracked (Group B):

- `designsystem/.../IhsanEmptyState.kt`
- `feature/.../core/preferences/DailyActivityIds.kt`
- `feature/.../live/LiveStreamSources.kt`

---

## 5. Add-Only Compliance Result

Documented rules: `docs/IHSAN_ADD_ONLY_RULES.md` forbid modifying navigation, DB, Gradle, existing screens/VMs, etc., and require logging needed edits in `APPEND_ONLY_INTEGRATION_REQUEST.md`.

| Check | Result |
|-------|--------|
| Group A only? | **Fail** — 85 Group B paths |
| Sensitive files touched? | **Yes** — `DatabaseModule.kt`, `IhsanDatabase.kt`, `Screen.kt`, `AppNavHost.kt`, `ViewModelModule.kt`, Manifest, Gradle |
| Neutral interpretation | Group B appears to be a **parallel production enhancement batch** (live streams, tasbih, Quran audio, daily activities, prayer sounds, icons), not accidental noise |

**Conclusion:** Current tree **does not** respect Add-Only. It is still a valid Phase 1 base **if** preserved as an intentional WIP branch/commit set.

---

## 6. High-Risk Production Diffs

Inspected via `git diff` / `git diff --cached` (no repairs).

### `DatabaseModule.kt` — **Critical risk**

| Aspect | Finding |
|--------|---------|
| What changed | **Added** `.fallbackToDestructiveMigration()`; expanded onCreate azkar seed into category `"تسبيح"` |
| Complete? | Appears intentional for local schema jumps |
| Compilation risk | Low |
| DB risk | **High/Critical** — unsupported upgrades can wipe `ihsan_master_db` |
| Overlap IhsanPlus | No |
| Documented in Add-Only log? | No |

### `IhsanDatabase.kt` — **Critical risk**

| Aspect | Finding |
|--------|---------|
| What changed | `version = 3` → `version = 5`; `exportSchema = false` unchanged |
| Migrations present | Only `MIGRATION_2_3` in DI (hadiths.explanation) — **no 3→4 or 4→5** |
| DB risk | Version bump without migrations **forces** reliance on destructive fallback |

### `Screen.kt` / `AppNavHost.kt` — **Navigation risk**

| Aspect | Finding |
|--------|---------|
| What changed | Routes: `tasbih_screen`, `haram_live`, `nabawi_live`; Live + Tasbih composables; last-read navigation; shared `HomeDashboardViewModel` for Daily Activities |
| Complete? | Appears wired end-to-end for those features |
| Compilation risk | Low if LiveStreamSources / TasbihViewModel present (they are) |
| Overlap IhsanPlus | **No** IhsanPlus routes added |

### `ViewModelModule.kt`

- Registers `TasbihViewModel`. Complete for tasbih DI. No IhsanPlus modules.

### `AndroidManifest.xml`

- Adds `INTERNET` (needed for live/Quran download). Security surface unchanged re: exported `QuranAudioService` (still exported; lint Warning).

### Gradle (`feature/build.gradle.kts`, `libs.versions.toml`)

- Adds `media3-exoplayer-hls`. Build-risk if catalog mismatch — **resolved** (assemble succeeded).

### Dashboard / Quran / Prayer / Profile / Ehsan / Design system

| Area | Nature of change | Risk notes |
|------|------------------|------------|
| Dashboard + UserPreferences + DailyActivityIds | Last-read + daily activity tracking | Medium runtime/logic; not DB schema |
| Quran VM/reader/audio/handler | Playback / progress | Medium runtime (media lifecycle) |
| Prayer workers/receiver/scheduler | Adhan sound path | Medium runtime (alarms/notifications) |
| Profile / Ehsan details / Duas / Hadith | UI + explanation polish | Lower |
| Design system cards + IhsanEmptyState | Visual consistency | Low |
| Icons / mipmaps | Branding | Low |

None of the Group B diffs implement IhsanPlus Controlled Integration.

---

## 7. Build Baseline

| Item | Result |
|------|--------|
| Command | `.\gradlew.bat :app:assembleDebug --stacktrace` |
| Start | Gradle Daemon started; Kotlin daemon connection failed repeatedly, then **fallback in-process compile** |
| Final | **BUILD SUCCESSFUL in 10m 23s** |
| Artifact | `app/build/outputs/apk/debug/app-debug.apk` (~28.8 MB, timestamp 2026-07-12) |
| Failing task | None |
| First actionable “error” noise | `Could not connect to Kotlin compile daemon` (environment/tooling), **not** source compile failure |
| Warning observed | `QuranReaderScreen.kt` FlowPreview `debounce` usage |

**Build baseline trustworthiness:** Yes for this machine/tree (APK produced). Kotlin daemon instability is an environment note for future CI.

---

## 8. Unit Test Baseline

| Command | Result |
|---------|--------|
| `.\gradlew.bat :feature:testDebugUnitTest --stacktrace` | **BUILD SUCCESSFUL** (~48s) |
| `.\gradlew.bat :app:testDebugUnitTest --stacktrace` | **BUILD SUCCESSFUL** (~11s) |
| `.\gradlew.bat test --stacktrace` | **BUILD SUCCESSFUL** (~2m 12s) |

### Feature `testDebugUnitTest` (from JUnit XML)

| Class | Tests | Failures | Errors | Kind |
|-------|------:|---------:|-------:|------|
| `AsmaTodayResolverTest` | 2 | 0 | 0 | Meaningful |
| `GetQiblaDirectionUseCaseTest` | 2 | 0 | 0 | Meaningful |
| `GetSurahUseCaseTest` | 2 | 0 | 0 | Meaningful |
| `ExampleUnitTest` | 1 | 0 | 0 | Placeholder |

**Feature total:** 7 tests, **0 failures**.

### App

| Class | Tests | Failures |
|-------|------:|---------:|
| `ExampleUnitTest` | 1 | 0 |

### Notes

- No IhsanPlus unit tests.
- Instrumented `QuranDataIngestionTest` uses legacy `QuranDatabase` — **not executed** in this unit-test baseline.
- Failures attributable to working-tree changes: **None observed**.

---

## 9. Lint and Static Analysis Baseline

| Item | Result |
|------|--------|
| Command | `.\gradlew.bat :app:lintDebug --stacktrace` |
| Result | **BUILD SUCCESSFUL** (~3m 2s) |
| Report | `app/build/reports/lint-results-debug.html` (+ XML) |
| Issues | **78** total — all **Warning** severity; **0 errors** |

Top issue IDs:

| Count | Id |
|------:|----|
| 60 | GradleDependency |
| 8 | UnusedResources |
| 6 | AndroidGradlePluginVersion |
| 1 | **ExportedService** (Security) — `QuranAudioService` |
| 1 | OldTargetApi |
| 1 | RedundantLabel |
| 1 | ModifierParameter |

Most dependency/version warnings look **pre-existing / catalog-wide**, not unique to this WIP. `ExportedService` confirms prior security finding.

---

## 10. Database Safety Snapshot

| Item | Current working-tree value |
|------|----------------------------|
| Active DB class | `IhsanDatabase` |
| DB file name | `ihsan_master_db` |
| Version | **5** (diff from HEAD **3** → **5**) |
| `exportSchema` | **false** |
| Entities (10) | Zikr, DailyStat, Dua, Donation, User, Hadith, Surah, Ayah, Bookmark, DownloadedAyah |
| DAOs exposed | azkar, dua, donation, user, hadith, quran, download |
| Registered migrations | `MIGRATION_2_3` only (add `hadiths.explanation`) |
| Missing paths | **3→4**, **4→5** (and any non-2→3 jump) |
| `fallbackToDestructiveMigration()` | **Active in working tree** (added vs HEAD) |
| Migration tests | **None** found |
| Production data wipe risk | **Yes** on unsupported upgrade |

Docs still claim version 4 in places (`IHSAN_EXISTING_PROJECT_MAP.md`) — documentation drift.

---

## 11. IhsanPlus Isolation Snapshot

| Package | Layers | Data | Local Koin | Root Koin | Nav | Room | Entry | Runtime | Tests |
|---------|--------|------|------------|-----------|-----|------|-------|---------|-------|
| daily | data/domain/presentation/preview/di | Fake | Yes | **No** | **No** | No | Preview / unwired screen | **No** | No |
| prayerassist | data/domain/presentation | Fake | **No** | **No** | **No** | No | Screen + inline use case | **No** | No |
| charitytrust | data/domain/presentation/preview/di | Fake | Yes | **No** | **No** | No | Preview / unwired screen | **No** | No |
| integration | notes object only | N/A | No | No | No | No | Marker | N/A | No |

`IhsanPlusIntegrationNotes.STATUS = "Isolated enhancement scaffold only"`.

Docs present for Parts 1–4 under `docs/`. No production entry from `AppNavHost` / `appModule`.

---

## 12. Part 5 Status

**Not started.**

- No `docs/IHSAN_PART_5_*`
- No `ihsanplus` social/community package
- No integration contracts, mappers, readiness evaluator, or checklist code
- Only forward references in Part 4 report / backlog / append-only log

---

## 13. Part 5 Scope Conflict

| Source | Claimed Part 5 |
|--------|----------------|
| `docs/IHSAN_PART_4_EXECUTION_REPORT.md` | Isolated **Social / Community** under `ihsanplus` |
| `docs/IHSAN_ENHANCEMENT_BACKLOG.md` | **UI Consistency and Localization Audit** |
| Verification recommendation (prior) | Integration **contracts + safety** before more features |

### Recommendation (requires product approval)

**Prefer Part 5 = Integration Readiness & Safe Connection Contracts** (docs + isolated contract/mapper/readiness types under `ihsanplus/integration/`, no nav wiring yet).

Rationale: Group B already delivers substantial UI/features; Controlled Integration is blocked by missing contracts and DB safety—not by lack of another user-facing module.

**Do not implement** until approved. This is a recommendation only.

---

## 14. Runtime Checks Not Executed

- Device install / cold start
- Prayer notification end-to-end
- Live HLS / YouTube playback
- Quran audio MediaSession controls / external bind of exported service
- Auto Backup contents on API 31+
- Room upgrade from installed v3/v4 DB → v5 on device
- RequestHelp phone placeholder behavior on device
- Instrumented tests (`connectedAndroidTest`)

---

## 15. Confirmed Blockers

1. **Destructive Room migration** active + version 3→5 without migrations (**Critical** for upgrades).  
2. **Add-Only policy violated** — 85 unclassified-as-allowed production paths (classified here, but not isolated).  
3. **Part 5 / integration contracts absent.**  
4. **Local auth / RequestHelp phone / backup PII / exported service** remain open (Phase 1 candidates; not fixed in Phase 0).  
5. Kotlin compile **daemon instability** (build still succeeded via fallback).

---

## 16. Preservation and Branch Strategy Recommendation

**Do not continue unbounded work on dirty `main` without preservation.**

### Recommended strategy (do not execute in Phase 0)

1. **Create a WIP branch from the current working tree** (keeps all local changes):

```powershell
git switch -c wip/phase0-baseline-production-edits
```

2. **Optionally commit logical groups** (only when explicitly approved):

```text
- docs + ihsanplus (Add-Only)
- icons/resources
- live + media3 HLS
- tasbih + nav/DI
- quran audio / daily activities / prayer sounds
```

3. **Create a patch backup** without committing:

```powershell
git diff > backup/phase0-unstaged.patch
git diff --cached > backup/phase0-staged.patch
git status --short > backup/phase0-status.txt
```

4. **Avoid** `git reset --hard`, `git clean -fd`, or discard without a verified backup copy of the tree.

**Not recommended:** Pretend Add-Only is intact; integrate IhsanPlus onto this tree before Phase 1 DB safety.

---

## 17. Phase 0 Exit Criteria Result

| Criterion | Met? |
|-----------|------|
| Build result recorded | **Yes** (SUCCESS + APK) |
| Test result recorded | **Yes** (0 failures on executed unit suites) |
| Lint result recorded | **Yes** (78 warnings) |
| Full path classification | **Yes** (inventory doc) |
| Add-Only vs production partition | **Yes** |
| DB safety snapshot | **Yes** |
| IhsanPlus + Part 5 status | **Yes** |
| Preservation recommendation | **Yes** |
| No source code modified for green build | **Yes** |

---

## 18. Final Readiness Decision

### PHASE 0 PASSED — READY FOR PHASE 1

Baseline is trustworthy for this working tree: build and unit tests succeed; risks and file groups are documented.

### CONTROLLED INTEGRATION REMAINS BLOCKED

Reasons (any one is sufficient):

- Database destructive migration risk remains  
- Large production WIP not preserved/isolated as a formal base  
- No Part 5 integration contracts  
- Rollback/preservation strategy recommended but **not yet executed** by product/team  

---

## 19. Exact Recommended Phase 1 Order

Execute **after** preservation branch/patch (Section 16). One concern per change set:

| Order | Task ID | Focus | Notes |
|------:|---------|-------|-------|
| 1 | P1-PRESERVE | Branch or patch backup | Non-code; unlocks safe fixes |
| 2 | P1-05 | Room: remove/replace destructive fallback; supply real migrations or explicit version strategy | Critical; alone |
| 3 | P1-01 | RequestHelp phone placeholder | Confirmed High product bug |
| 4 | P1-03 | Backup / data-extraction excludes for PII | Manifest/XML only |
| 5 | P1-04 | QuranAudioService export/permission review | Manifest + device test |
| 6 | P1-02 | Auth copy honesty (local profile) | Minimal strings/docs |

**Do not** combine with IhsanPlus nav/DI wiring or Part 5 feature UI in the same change set.

---

## Appendix — Command Exit Codes (this machine)

| Command | Exit |
|---------|------|
| `:app:assembleDebug` | 0 (SUCCESS) |
| `:feature:testDebugUnitTest` | 0 |
| `:app:testDebugUnitTest` | 0 |
| `test` | 0 |
| `:app:lintDebug` | 0 |
