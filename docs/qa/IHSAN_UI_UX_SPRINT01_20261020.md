# IHSAN — UI/UX Modernization Sprint 01

2026-10-20 — Asia/Damascus. Source implementation complete; physical visual acceptance pending.

Base: `7d31389` on `fix/audio-runtime-adhan-quran`. The working tree was clean at the start. Existing recent UI, navigation, migration and G0-03 fixes were retained. No commit, push, installation, emulator or user-data operation was performed.

## Implemented screens and components

| Area | Completed source changes |
|---|---|
| Home dashboard | Full-width prayer name/time/countdown and daily verse replace the cramped three-column hero. Existing image and prayer inputs remain. Continue-reading action has a 48dp minimum height. Daily cards clamp progress, describe recorded values, and omit the progress ring when counts are unavailable. |
| Services | Both preview and full services dialog adapt column count to width and system font scale. Consistent bordered cards, emerald icon tiles and expanded spacing. Dialog visibility survives recreation; all existing routes are preserved. |
| Navigation | Shared bottom bar uses a selectable tab group with one selection owner. Tasbih replaces nonfunctional imitation tabs with a working Return Home action. Existing destination resolution and back-stack policy remain. |
| Quran browsing | Search and selected tab survive recreation; Android Back closes search. Helpful initial-search, no-results, bookmarks-empty and error states provide real actions. Search cancellation releases loading; errors terminate searching and retry clears stale errors. |
| Quran audio | Unavailable recordings explain the restriction without a dead timeline. Removed a fabricated fallback duration and surah title; seek input is bounded and has a 48dp minimum height. Licensing gates remain enforced. |
| Azkar | Larger filter/increment targets, readable counter text in both themes, and explicit counter state semantics. Existing increment limits, values and preferences are retained. |
| Tasbih | Reset confirmation targets the selected stored ID and leaves statistics history intact. Counter identifies its increment action and announces its current state. Larger selector targets, selected semantics and bottom system-bar padding. Statistics still opens the existing statistics destination. |
| Settings / Profile | Explicit System/Light/Dark radio choices, accessible full-row switches with shared contrasting thumb/track colors, readable loading color and bottom navigation-bar padding. Profile rows merge their semantics and omit empty subtitles. |
| Shared UI | Consistent bordered action cards and icon tiles, weighted top-bar titles with heading semantics, 48dp buttons/search fields, quieter secondary buttons, labeled search, wrapping empty-state actions, and polite error announcements. |

## Light and Dark

Light app surfaces use a warm `#FAF8F3` canvas and a subdued emerald secondary container. Dark keeps the existing charcoal/deep-emerald surface hierarchy and restrained accents. Cards, fields, selection, progress and controls use existing semantic tokens. New contrast checks cover switch thumb/track and selected radio content in both palettes.

Quran text, verse numbering, cream Mushaf paper, decorative frame, religious content, prayer/Qibla calculations, licenses, schemas and production databases were not changed. Existing preference keys and persistence ownership remain.

## Validation and QA artifact

Final validation command (JBR 21.0.3; existing Gradle/toolchain and dependencies):

```powershell
.\gradlew.bat :app:testDebugUnitTest :feature:testDebugUnitTest :designsystem:testDebugUnitTest :app:lintDebug :app:assembleRelease -PihsanPhysicalQa=true --max-workers=4 --console=plain
```

**BUILD SUCCESSFUL — 5m 21s.** Release remains minified and resource-shrunk.

| Module | JVM tests | Failures / errors / skipped |
|---|---:|---|
| app | 33 | 0 / 0 / 0 |
| feature | 429 | 0 / 0 / 0 |
| designsystem | 30 | 0 / 0 / 0 |
| Total | **492** | **0 / 0 / 0** |

Regression coverage includes Quran content integrity and licensing boundaries; search cancellation/error recovery; Home services preservation and responsive columns; navigation boundaries; Tasbih persistence/actions; design-system contrast; database and release architecture boundaries. `git diff --check` passed. Source geometry review accounts for Home's existing 16dp overlap with 24dp hero bottom padding. Device rendering was not tested.

Android lint: **0 errors, 118 warnings**. The warning count matches the pre-sprint report; warnings remain. Release vital lint passed. Logs and test summaries are archived beside the APK.

- APK: `.artifacts/uiux-sprint01-20261020/IHSAN-UIUX-SPRINT01-20261020-release-qa.apk`
- Absolute APK path: `<LOCAL_USER_HOME>/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/uiux-sprint01-20261020/IHSAN-UIUX-SPRINT01-20261020-release-qa.apk`
- Size: 31,664,162 bytes.
- SHA-256: `3F0BA6DCBE8BB098A3BD8C06A6D9CDB0D5DA9325D567970CD4CF6369E1880C92`
- Identity verified with aapt: `com.example.mol.referenceqa`; versionCode 1 / versionName 1.0; minSdk 25 / targetSdk 35.
- apksigner: **Verifies**, APK Signature Scheme **v2**, one signer, `CN=Android Debug, O=Android, C=US` (existing internal QA signing configuration).
- Certificate SHA-256: `870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63`.
- Archived APK SHA-256 equals the final Gradle APK output.
- Evidence directory: `.artifacts/uiux-sprint01-20261020/` contains `validation.log`, `lint-results-debug.txt`, `unit-test-summary.json`, `apk-badging.txt`, `signature-verification.txt`, and `SHA256.txt`.

## Acceptance status and remaining work

- **Completed:** the source changes above, JVM regression tests, source whitespace checks and verified Release QA artifact.
- **Partially completed:** RTL and accessibility verification consists of source review, layout constraints, semantics and contrast/layout unit checks. These do not establish rendered-device acceptance.
- **Not executed:** Xiaomi 23129RAA4G visual/TalkBack checks in Light/Dark and font scales 1.0/1.3/2.0, clipping and scrolling inspection, gesture/three-button system bars, search Back/recreation, theme selection, reset cancel/confirm, navigation and unavailable audio. No APK was installed; separate authorization is required for installation.
- **Existing external restriction:** Quran recording playback/download remains unavailable pending licensing approval. This sprint does not bypass it.
- The approved Mushaf reader page and overall Profile composition were preserved; this is not a redesign of those layouts.

## Modified files

- `designsystem/src/main/java/com/example/designsystem/component/IhsanActionCard.kt`
- `designsystem/src/main/java/com/example/designsystem/component/IhsanBottomNavigation.kt`
- `designsystem/src/main/java/com/example/designsystem/component/IhsanButton.kt`
- `designsystem/src/main/java/com/example/designsystem/component/IhsanEmptyState.kt`
- `designsystem/src/main/java/com/example/designsystem/component/IhsanErrorState.kt`
- `designsystem/src/main/java/com/example/designsystem/component/IhsanLoadingState.kt`
- `designsystem/src/main/java/com/example/designsystem/component/IhsanSearchBar.kt`
- `designsystem/src/main/java/com/example/designsystem/component/IhsanSwitch.kt`
- `designsystem/src/main/java/com/example/designsystem/component/IhsanTopBar.kt`
- `designsystem/src/main/java/com/example/designsystem/theme/Color.kt`
- `designsystem/src/test/java/com/example/designsystem/theme/IhsanContrastTest.kt`
- `feature/src/main/java/com/example/feature/azkar/AzkarScreen.kt`
- `feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeContextualHero.kt`
- `feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeDailyJourneyCard.kt`
- `feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeDiscoverServicesGrid.kt`
- `feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeQuranContinueCard.kt`
- `feature/src/main/java/com/example/feature/profile/presentation/components/ProfileUiComponents.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/QuranListScreen.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/QuranViewModel.kt`
- `feature/src/main/java/com/example/feature/settings/SettingsScreen.kt`
- `feature/src/main/java/com/example/feature/tasbih/presentation/TasbihScreen.kt`
- `feature/src/main/res/values/strings_sprint.xml`
- `feature/src/test/java/com/example/feature/dashboard/HomeFeaturePreservationTest.kt`
- `feature/src/test/java/com/example/feature/dashboard/ServiceGridLayoutTest.kt`
- `feature/src/test/java/com/example/feature/quran/presentation/QuranSearchUiRecoveryTest.kt`
- `docs/qa/IHSAN_UI_UX_SPRINT01_20261020.md`
