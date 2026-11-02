# Audio Runtime Fix Implementation Report

## 1. Starting branch and commit

| Item | Value |
|------|--------|
| Starting branch | `ui/project-dark-mode-polish` |
| Starting HEAD | `6840c08` |
| Working tree at start | `?? docs/screenshots/` only |

## 2. Final branch and commit

| Item | Value |
|------|--------|
| Branch | `fix/audio-runtime-adhan-quran` |
| HEAD | `74a9e1a` |
| Adhan commit (alarm usage) | `23cdc67 fix(adhan): route prayer sound through alarm audio usage` |
| Adhan commit (bundled recording) | `74a9e1a fix(adhan): use bundled adhan recording for prayer alerts` |
| Quran commit | **Not created yet** (awaiting approved runtime UI play + logcat) |

## 3. Device information

| Field | Value |
|-------|--------|
| Device | Xiaomi `23129RAA4G` (sapphire_global), Android 15 / API 35 |
| App | `com.example.mol` |
| Adhan channel verify | Done via debug intent + `dumpsys notification` / `dumpsys audio` |
| Quran runtime tap-trace | Not completed in this session |

## 4. Adhan confirmed root cause

1. Notification stream muted in Vibrate while Alarm stream audible → need `USAGE_ALARM`.
2. After alarm routing, sound was still `Settings.System.DEFAULT_ALARM_ALERT_URI` → system alarm tone, not adhan.
3. Fix: keep `USAGE_ALARM`, ship bundled `R.raw.adhan_default`, new immutable channel `adhan_v3`.

## 5. Bundled adhan source / license / path

| Field | Value |
|-------|--------|
| Source file supplied by owner | `c:\Users\WIN 10\Downloads\adhan_default.mp3.mp3` |
| Project path | `feature/src/main/res/raw/adhan_default.mp3` |
| Size | ~1,995,676 bytes (~1.9 MB) |
| In APK | `res/raw/adhan_default.mp3` (confirmed via `jar tf app-debug.apk`) |
| License | Owner-supplied recording for app embedding; not downloaded by the agent |

## 6. Adhan URI

```
android.resource://<context.packageName>/<R.raw.adhan_default>
```

Observed on device:

```
android.resource://com.example.mol/2131755008
```

Built with `ContentResolver.SCHEME_ANDROID_RESOURCE` + `context.packageName` (not hard-coded).

### Sound selection order

1. Valid user custom URI (existing preference), if set  
2. Bundled `R.raw.adhan_default`  
3. `Settings.System.DEFAULT_ALARM_ALERT_URI` last resort  

## 7. Channel ID strategy (v3)

```
prayer_notifications_adhan_v3_<soundType>_<soundHash>
```

Observed:

```
prayer_notifications_adhan_v3_DEFAULT_ATHAN_1866409633
```

- `v3` required to migrate channel sound to bundled adhan (immutable channels).
- Legacy `alarm_v2` / older IDs left in place (not deleted).
- Receiver + Worker share `AdhanNotificationChannelFactory`.

## 8. AudioAttributes

| | Value |
|--|--------|
| Usage | `USAGE_ALARM` |
| Content type | `CONTENT_TYPE_SONIFICATION` |
| Importance | HIGH |
| Category | `CATEGORY_ALARM` |
| Bypass DND | Not set |
| Full-screen intent | Unchanged |

## 9. Normal mode result

- Debug intent posted notification on `adhan_v3` channel.
- Channel sound = `android.resource://com.example.mol/...`
- `USAGE_ALARM` + `CONTENT_TYPE_SONIFICATION` confirmed in `dumpsys notification`.
- Agent cannot subjectively “hear” from the PC; device dump shows alarm-usage playback path engaged (see §11).

## 10. Vibrate mode result

- Ringer `VIBRATE`: STREAM_NOTIFICATION volume **0** / muted; STREAM_ALARM volume **15**.
- Same `adhan_v3` channel / resource URI used.
- Notification still posts with `category=alarm`.

## 11. Did the adhan play (and fully)?

| Evidence | Result |
|----------|--------|
| Notification posted | Yes |
| Channel sound URI | Bundled `android.resource://…` |
| Audio focus / player attrs | `USAGE_ALARM` / `CONTENT_TYPE_SONIFICATION` seen around trigger (`dumpsys audio`) |
| Full-length playback on Xiaomi | **Not proven end-to-end by agent ear**; no ForegroundService player added. If OEM truncates long channel sounds, a separate approved FGS phase is required (not started). |

## 12. Adhan files changed (bundled commit)

- `feature/src/main/res/raw/adhan_default.mp3`
- `feature/.../prayer/util/AdhanNotificationChannelFactory.kt`
- `feature/.../AdhanNotificationChannelFactoryTest.kt`
- `feature/.../AdhanNotificationChannelFactoryInstrumentedTest.kt`

## 13–16. Quran Runtime (pending)

| Item | Status |
|------|--------|
| Runtime trace | Not captured this session |
| First failing boundary (A–J) | Unclassified |
| Quran root cause | Unconfirmed |
| Quran fix / commit | None |

Stop rule: no speculative Quran edits without play + logcat evidence.

## 17. Player state / streaming / local (Quran)

Pending.

## 18. Debug / Release / Tests / Lint

| Check | Bundled-adhan session |
|-------|------------------------|
| `:feature` adhan unit tests | PASS |
| `:app:assembleDebug` | PASS |
| `:app:installDebug` | PASS (upgrade) |
| Full release/lint suite after v3 | Not re-run in this short session (prior suite passed on `23cdc67`) |

## 19. Remaining limitations

1. Quran diagnosis/fix still required on-device.
2. Long adhan duration under OEM notification sound limits not fully certified.
3. If user has a custom `adhanSoundUri` preference, it still wins over bundled default (by design).
4. Debug trigger: `adb shell am start -n com.example.mol/.MainActivity -a com.example.mol.DEBUG_ADHAN_TEST` (DEBUG only). Prefer `am force-stop` first so `onCreate` receives the action.

## 20. Architecture / business boundaries

- Prayer calculation / scheduling / PendingIntent architecture unchanged.
- everyayah provider unchanged.
- Navigation / DI / Room / DataStore schema unchanged.
- `QuranAudioService` still `exported=false`.

## 21. Rollback

```bash
git revert 74a9e1a   # bundled adhan
git revert 23cdc67   # alarm usage (if needed)
```

---

## Git snapshot

```
74a9e1a (HEAD -> fix/audio-runtime-adhan-quran) fix(adhan): use bundled adhan recording for prayer alerts
23cdc67 fix(adhan): route prayer sound through alarm audio usage
6840c08 (ui/project-dark-mode-polish) fix(dark-ui): sync component dark checks to app theme
```

Untracked (not committed): `docs/AUDIO_RUNTIME_FIX_IMPLEMENTATION_REPORT.md`, `docs/screenshots/`

---

## Final classification (current)

| Label | Status |
|-------|--------|
| Bundled Adhan Audio Working | **Channel + resource + ALARM path verified on device; subjective full hear / full-length TBD by owner ear** |
| Quran Recitation Runtime Fixed | **No** |
| Prayer Scheduling Unchanged | Yes |
| Quran Provider Unchanged | Yes |
| Architecture Unchanged | Yes |
| Navigation Unchanged | Yes |
