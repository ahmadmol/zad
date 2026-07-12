# Phase 0 File Inventory

**Captured:** 2026-07-12 (local)  
**Branch:** `main` → `origin/main`  
**Total changed paths (pre–Phase 0 report files):** 149 (`Group A` 64 + `Group B` 85 + `Group C` 0)  
**Note:** After this Phase 0 documentation was added, `docs/` gains additional untracked/new files. Counts below describe the working tree **before** counting `PHASE_0_*.md`.

Status legend: ` M` modified unstaged, `A ` added staged, `AM` added staged + unstaged edits, `??` untracked.

---

## Group A — Allowed Add-Only paths (64)

### docs/ (14)

| Status | Path | Type | Add-Only OK? |
|--------|------|------|--------------|
| A | docs/APPEND_ONLY_INTEGRATION_REQUEST.md | documentation | Yes |
| A | docs/IHSAN_ADD_ONLY_RULES.md | documentation | Yes |
| A | docs/IHSAN_ENHANCEMENT_BACKLOG.md | documentation | Yes |
| A | docs/IHSAN_EXISTING_PROJECT_MAP.md | documentation | Yes |
| A | docs/IHSAN_PART_1_EXECUTION_REPORT.md | documentation | Yes |
| A | docs/IHSAN_PART_2_DAILY_EXPERIENCE_FOUNDATION.md | documentation | Yes |
| A | docs/IHSAN_PART_2_EXECUTION_REPORT.md | documentation | Yes |
| A | docs/IHSAN_PART_3_EXECUTION_REPORT.md | documentation | Yes |
| A | docs/IHSAN_PART_3_FUTURE_INTEGRATION_REQUEST.md | documentation | Yes |
| A | docs/IHSAN_PART_3_PRAYER_QIBLA_FOUNDATION.md | documentation | Yes |
| A | docs/IHSAN_PART_4_CHARITY_TRUST_FOUNDATION.md | documentation | Yes |
| AM | docs/IHSAN_PART_4_EXECUTION_REPORT.md | documentation | Yes |
| A | docs/IHSAN_PART_4_FUTURE_INTEGRATION_REQUEST.md | documentation | Yes |
| A | docs/IHSAN_SENSITIVE_FILES.md | documentation | Yes |

### ihsanplus/ (50)

All under `feature/src/main/java/com/example/feature/ihsanplus/` — source, Add-Only OK.

| Package | Approx. files | Notes |
|---------|---------------|-------|
| charitytrust | ~20 | Fake DS, local Koin module, preview screen |
| daily | ~16 | Fake DS, local Koin module, preview screen; some `AM` |
| prayerassist | ~13 | Fake DS, no Koin module, inline use case in screen |
| integration | 1 | `IhsanPlusIntegrationNotes.kt` marker only |

---

## Group B — Production paths outside Add-Only (85)

### Violates documented Add-Only policy: **Yes** (all Group B)

| Status | Path | Kind | Likely purpose |
|--------|------|------|----------------|
| M | app/src/main/AndroidManifest.xml | configuration | Added `INTERNET` permission |
| A | app/src/main/ic_launcher-playstore.png | resource | Launcher / store icon |
| M | app/src/main/java/com/example/mol/di/DatabaseModule.kt | source | Destructive migration + tasbih seed |
| M | app/src/main/java/com/example/mol/di/ViewModelModule.kt | source | Register `TasbihViewModel` |
| M | app/src/main/java/com/example/mol/navigation/AppNavHost.kt | source | Tasbih/Live routes, last-read, shared VM |
| M | app/src/main/java/com/example/mol/navigation/Screen.kt | source | Tasbih, HaramLive, NabawiLive routes |
| M | app/src/main/res/drawable/ic_launcher_foreground.xml | resource | Icon |
| M/A | app/.../mipmap-* (many) | resource | Launcher assets refresh |
| M | designsystem/.../DailyActivityCard.kt | source | UI polish |
| M | designsystem/.../DashboardHeader.kt | source | UI polish |
| M | designsystem/.../LastReadCard.kt | source | Optional ayah display |
| ?? | designsystem/.../IhsanEmptyState.kt | source | New empty-state component |
| M | feature/build.gradle.kts | configuration | Media3 HLS dependency |
| A | feature/src/main/ic_launcher-playstore.png | resource | Icon |
| M | feature/.../asma/.../AsmaViewModel.kt | source | Daily activity / prefs wiring |
| M | feature/.../azkar/AzkarScreen.kt | source | Minor UI |
| M | feature/.../azkar/.../AzkarViewModel.kt | source | Daily activity prefs |
| M | feature/.../core/.../IhsanDatabase.kt | source | Version 3 → 5 |
| M | feature/.../core/preferences/UserPreferences.kt | source | Last-read + daily activity APIs |
| ?? | feature/.../core/preferences/DailyActivityIds.kt | source | Activity id constants |
| M | feature/.../dashboard/* (5 files) | source | Home / daily activities / search |
| M | feature/.../duas/* (3 files) | source | Bookmark/favorite wiring |
| M | feature/.../ehsan/.../IhsanDetailsScreen.kt | source | Share / contact UI |
| M | feature/.../hadith/* (3 files) | source | Explanation cleanup |
| AM | feature/.../live/LiveStreamScreen.kt | source | HLS + YouTube fallback |
| ?? | feature/.../live/LiveStreamSources.kt | source | Stream URL constants |
| M | feature/.../prayer/util/PrayerNotificationScheduler.kt | source | Notification scheduling |
| M | feature/.../prayer/worker/AdhanWorker.kt | source | Adhan worker sound |
| M | feature/.../prayer/worker/PrayerNotificationReceiver.kt | source | Receiver + sound |
| M | feature/.../profile/ProfileScreen.kt | source | Settings/reminders wiring |
| M | feature/.../quran/* (7 files) | source | Audio, reader, list, download, DI |
| M | feature/.../settings/SettingsScreen.kt | source | Settings tweaks |
| AM/A | feature/.../tasbih/* (3 files) | source | New tasbih feature UI/VM |
| M/A | feature/.../res/mipmap-* + drawables | resource | Icons / image_app |
| M | gradle/libs.versions.toml | configuration | media3-exoplayer-hls library |

---

## Group C — Generated or local (0)

No changed paths classified as `.gradle/`, `build/`, `.idea/`, or `local.properties` in `git status --short`.

---

## Untracked only (`git ls-files --others --exclude-standard`)

Prior to Phase 0 report files:

```text
designsystem/src/main/java/com/example/designsystem/component/IhsanEmptyState.kt
feature/src/main/java/com/example/feature/core/preferences/DailyActivityIds.kt
feature/src/main/java/com/example/feature/live/LiveStreamSources.kt
```

---

## Summary counts

| Group | Count | Add-Only compliant? |
|-------|------:|---------------------|
| A | 64 | Yes |
| B | 85 | No (policy violation by definition) |
| C | 0 | N/A |
| **Total** | **149** | Mixed |
