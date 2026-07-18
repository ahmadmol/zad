# Internal Release Completion Report

**Classification:**

```text
Complete — Ready for Internal Use
Store Publishing — Deferred by Owner
```

## 1–2. Branches / commits

| Item | Value |
|------|--------|
| Starting branch | `release/final-controlled-integration` |
| Starting commit | `8297bba` |
| Final branch | `qa/final-runtime-hardening` |
| Final tip | *(updated after commits)* |

## 3. Baseline

See `docs/FINAL_RUNTIME_HARDENING_BASELINE.md`.

* Debug/Release assemble + unit tests: PASS (R8 off at baseline)
* Lint: PASS later in this branch

## 4–5. Device used

| Field | Value |
|-------|--------|
| Model | Xiaomi 23129RAA4G |
| Manufacturer | Xiaomi |
| Android | 15 |
| API | 35 |
| Timezone | Asia/Damascus |
| Battery | ~67% (USB powered during session) |
| adb | `C:\android_sdk\platform-tools\adb.exe` |
| Emulator | Not required (physical device used) |

## 6–8. Runtime scenarios / defects

### Executed

* Debug APK install (`adb install -r`) — Success
* Debug cold start `MainActivity` — process alive, no FATAL
* UI dump: Ehsan local-board notice visible; bottom nav Home / إحسان / حسابي; Inbox absent
* R8 Release assemble — Success (`app-release.apk` ~6.8 MB, mapping produced)
* R8 Release install — Success
* R8 Release start — process started, no `FATAL EXCEPTION` observed

### Limited / interrupted

* Extended UI automation contested by WhatsApp Business / YouTube PiP focus theft
* USB disconnect ended session before full font-scale / dark-mode scripted matrix
* `connectedDebugAndroidTest` not completed (device disconnect)

### Defects

See `docs/FINAL_RUNTIME_DEFECTS.md` — **no critical app defects fixed** (none verified).

## 9–16. Feature verification (honest scope)

| Area | Evidence |
|------|----------|
| Prayer | Unit/architecture retained; device deep prayer matrix partial (focus/USB limits) |
| Home | Debug UI showed production shell; Daily entry expected in debug — not fully re-confirmed after focus loss |
| Settings / Statistics / Tasbih | Automated unit/ownership tests PASS; manual device deep pass incomplete |
| Ehsan | Device UI showed **لوحة إحسان المحلية** honest notice; no Verified/trust strings in that dump |
| Daily / Prayer Assist | Flags remain debug-on / release-off; production adapters only in DI |
| Dark / font / RTL / a11y | Automated design/localization tests PASS; full device matrix interrupted |

## 17–20. Quran / Room / migrations

* Migration matrix unit tests + instrumented suite remain in tree
* Instrumented connected run not finished this session (device disconnect)
* Quran audio: service remains non-exported; no crash attributed during smoke

## 21–26. Release hardening

| Item | Status |
|------|--------|
| R8 + shrinkResources | **Enabled and release build verified** |
| Mapping | `app/build/outputs/mapping/release/` present |
| Signing | External env/`local.properties`; no secrets in Git; `.gitignore` adds `*.jks`/`*.keystore` |
| applicationId | Remains `com.example.mol` — deferred (`docs/APPLICATION_ID_DEFERRED_DECISION.md`) |
| Store publish | **Not requested / not performed** |
| Privacy docs | Draft under `docs/store/*` — not published |

## 27–30. Quality gates

| Gate | Result |
|------|--------|
| `:app:assembleDebug` | PASS |
| `:app:assembleRelease` (R8) | PASS |
| Unit tests (designsystem/feature/app) | PASS |
| Architecture / minify config tests | PASS |
| Lint debug | PASS |
| Instrumented connected | Not completed (device disconnect) |
| CI workflow | Updated for `qa/**` + R8 release assemble |

## 31–32. Remaining / deferred

**Non-store technical**

* Reconnect device for full dark-mode / font-scale / prayer notification fire matrix
* Optional second older API device when available
* Owner-supplied release keystore for true signed production artifacts

**Store (explicitly deferred)**

* Application ID approval
* Play Console / AAB upload
* Public privacy URL / Data Safety form
* Legal counsel certification

## 33. Rollback

* R8: revert `isMinifyEnabled`/`isShrinkResources` in `app/build.gradle.kts`
* Daily/Prayer Assist: feature flags already release-disabled
* Charity notice: remove `LocalCharityBoardNotice` without touching Room
* Never remove shipped Room migrations

## 34. Acceptance checklist

| Criterion | Status |
|-----------|--------|
| Internal debug usable on device | Yes |
| R8 release builds & installs | Yes |
| No critical FATAL in smoke | Yes |
| Demo Trust not in production | Yes |
| Store publishing avoided | Yes |
| Full UI/a11y device matrix | Partial (ENV interruption) |

## Git snapshot

```text
(see post-commit log)
```
