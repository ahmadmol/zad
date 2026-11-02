# Final Runtime Hardening Baseline

**Branch:** `qa/final-runtime-hardening`  
**Starting commit:** `8297bba`  
**Date:** 2026-07-18

## Git status

```text
?? .project-preservation/
```

## Build / test (pre-change)

| Command | Result |
|---------|--------|
| `:app:assembleDebug` | PASS |
| `:app:assembleRelease` | PASS (R8 still off at baseline) |
| `:designsystem:testDebugUnitTest` | PASS |
| `:feature:testDebugUnitTest` | PASS |
| `:app:testDebugUnitTest` | PASS |
| `:app:lintDebug` | Pending this branch |
| `clean` | Deferred (Windows lock risk) |

## Environment

| Item | Value |
|------|--------|
| adb | `C:\android_sdk\platform-tools\adb.exe` |
| ANDROID_HOME | `C:\android_sdk` |
| Device | Connected (see runtime record) |
| applicationId | `com.example.mol` |
| R8 | Disabled at baseline |
| Release signing | External vars; credentials not present → debug fallback |

## Goals this branch

1. Device runtime smoke for critical flows
2. Enable + verify R8/resource shrinking
3. CI / docs for internal readiness
4. No store publishing
