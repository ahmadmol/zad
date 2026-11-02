# Mol Implementation Review Report

Date: 2026-07-22

## Scope and repository safety

- Project path: `C:\Users\WIN 10\Desktop\New folder (2)\Sdk\Sdk\mol`
- Current branch: `fix/audio-runtime-adhan-quran`
- Starting HEAD: `74a9e1a`
- Final HEAD: `74a9e1a` (no commit created)
- Worktree changes were preserved. No reset, restore, clean, stash, add, commit, merge, or rebase was used.
- Existing secondary worktree `mol-bottom-nav` was inspected only; no files were copied from it.
- No files were deleted.

## Initial and final working-tree state

The worktree was already dirty at the start, with staged, modified, and untracked files covering navigation, dark mode, audio, adhan, dashboard, and documentation. The final state remains dirty and contains those changes plus the connectivity/Quran/dashboard review additions. Unrelated changes were not reverted or replaced.

## Bottom Navigation integration

Status: integrated in the original `mol` project.

- `BottomBarDestination` resolves only the Home and Ehsan root routes.
- Profile, child/detail routes, feature routes, and unknown routes hide the bar.
- Selection is derived from the current `NavDestination` hierarchy.
- Navigation uses `popUpTo`, `saveState`, `launchSingleTop`, and `restoreState`.
- RTL, shared animation, semantic tab roles, touch targets, and design-system colors remain in the existing component.
- The alternate `mol-bottom-nav` worktree was not used as a source of whole-file replacements.

## Connectivity and Quran downloads

Status: implemented and statically reviewed.

- `ConnectivityMonitor` uses `ConnectivityManager`, `NET_CAPABILITY_INTERNET`, and validated connectivity.
- Quran download scheduling defaults to `NetworkType.CONNECTED` and supports configurable `UNMETERED` policy.
- HTTP downloads have connection/read timeouts, status classification, cancellation checks, WorkManager exponential backoff, temporary files, and atomic finalization.
- 408, 429, and 5xx errors retry; other HTTP failures return permanent failure.
- Partial files are deleted and never registered as valid local audio.
- WorkManager progress is observed through the scheduler and mapped into Quran UI state.
- Quran audio does not select remote playback while offline and no longer falls back to remote audio after connectivity is lost.
- HLS and YouTube live-stream screens expose an offline state and fallback/error path.
- `QuranWorkerFactory` is registered through the application WorkManager configuration.

## Quran audio

Status: implemented; current code was statically reviewed after the last debug build.

- `QuranViewModel` no longer owns `Context` or WorkManager.
- Audio source selection prefers a valid local file and otherwise requires online connectivity.
- `AudioPlayerHandler` has explicit `close()` cancellation/release behavior.
- `QuranAudioService` logs initialization failure without rethrowing a service-start crash.
- Existing reader, next/previous, MediaSession, and notification behavior was preserved.

## Dashboard

Status: implemented and statically reviewed.

- Sections support Loading, Empty, Error, Retry, and Content states without blanking unrelated sections.
- Refresh and location failures propagate to the UI.
- Quran repository failures remain Error instead of being silently converted to Empty.
- Hijri date calculation is separated from the one-second clock tick and refreshed only when the date key changes.
- Hardcoded volunteer counts were removed; unavailable charity data renders as an explicit section state.
- Existing home visual layout remains the source of truth for successful content.

## Prayer and lifecycle

Status: implemented and statically reviewed.

- The active-prayer window is domain-owned and documented.
- `DefaultPrayerTimesFacade` owns or receives its scope explicitly and exposes cancellation through `close()`.
- Location refresh returns `Result` and scheduling reconciliation occurs only after success.
- Prayer notification receiver cancellation is tied to its `goAsync()` completion.

## Notifications and adhan

Status: existing implementation reviewed; no broad notification rewrite was performed.

- Adhan channel versioning, `USAGE_ALARM`, sonification content type, independent notification IDs, and exact-alarm handling are present.
- PendingIntent flags use immutable/update-current combinations in the Android alarm gateway.
- Android 13 notification permission handling exists.
- Remaining legacy worker/service-locator usage outside the Quran worker was left untouched to avoid expanding scope.

## Environment

- `JAVA_HOME`: not set in the inspected shell.
- Java: Oracle JDK 21.0.1.
- Gradle: 8.10.2.
- Memory before the attempted verification: approximately 7.2 GB total, 0.45 GB free.
- Android Studio Java process was using approximately 0.7 GB and Android Studio approximately 1.2 GB.
- No heavy process was killed.

## Verification

- Debug build previously completed successfully: `:app:assembleDebug`.
- APK previously produced at `app/build/outputs/apk/debug/app-debug.apk`.
- The APK was previously installed on device `ac190123`.
- Previous runtime launch reached `com.example.mol/.MainActivity` with no observed crash.
- Focused tests previously passed for connectivity, Quran source selection, download classification/finalization, WorkManager progress, dashboard refresh failure, prayer window, and Hijri date policy.
- Full feature/design-system/app test runs previously exposed pre-existing dirty-tree failures in `ProfileReferenceUiBoundaryTest`, `DarkModeFoundationBoundaryTest`, and two navigation characterization tests.
- `lintDebug` was not run in this review because free memory was below the requested safe threshold.
- `assembleRelease` was not run because debug verification and memory safety take priority.
- The latest static fixes were not rebuilt or reinstalled because the machine remained below the safe memory threshold.

## Remaining blockers and next step

1. Close Android Studio or other heavy processes, or increase pagefile/restart the machine until at least 1.5–3 GB is free.
2. Run the staged Gradle sequence with `--no-daemon --max-workers=1` and in-process Kotlin compilation.
3. Resolve or explicitly classify the existing profile, dark-theme, and navigation test failures.
4. Run `:app:lintDebug`, then `:app:assembleRelease`.
5. Reinstall the rebuilt APK on `ac190123` and perform the runtime QA matrix for Home, Bottom Navigation, Quran audio/downloads, Offline mode, Live streams, Ehsan, notifications, and Dark Mode.
