# IHSAN video UI/UX audit

Source recording: local screen recording dated 2026-11-04, duration 191 seconds, 864×1920.
Review method: 20 still frames taken every 10 seconds from 0:00 through 3:10. Continuous gestures, pinch paths, and back-press results were not observed frame by frame, so they are not claimed below.

## Screens visible

| Time | Screen | Status |
|---|---|---|
| 0:00 | Android launcher, app not open | outside the product |
| 0:10–0:20 | Home, light, prayer card and services | CONFIRMED IN VIDEO |
| 0:30 and 3:10 | Community listings | CONFIRMED IN VIDEO |
| 0:40 | Profile, dark | CONFIRMED IN VIDEO |
| 0:50 | Quran list | CONFIRMED IN VIDEO |
| 1:00 | Quran reader, Al-Maidah page 108, chrome hidden | CONFIRMED IN VIDEO |
| 1:10 | Same reader zoomed, license text and reset chip over the page | CONFIRMED IN VIDEO |
| 1:20 | Azkar list, dark, bottom bar visible | CONFIRMED IN VIDEO |
| 1:30 | Fasting journal, mostly empty with a small progress mark | SUSPECTED loading frame |
| 1:40 | Reading-plan dialog | CONFIRMED IN VIDEO |
| 1:50 | Ramadan hub | CONFIRMED IN VIDEO |
| 2:00–2:10 | Manual prayer journal | CONFIRMED IN VIDEO |
| 2:20 | Full white frame, no app chrome | SUSPECTED; not mapped to a source screen |
| 2:30 | Daily goals | CONFIRMED IN VIDEO |
| 2:40 | All services | CONFIRMED IN VIDEO |
| 2:50–3:00 | Settings | CONFIRMED IN VIDEO |

About 12 product screens were sampled. The back path Home → all services → service → Back was not exercised on camera.

## Issues

### VID-001 — License notice and zoom reset cover Quran text

- Screen: Quran reader
- Time: 1:10
- Class: UI
- Severity: P1
- Evidence: CONFIRMED IN VIDEO and CONFIRMED IN SOURCE
- The pending-license message was drawn as an error row with “إعادة التشغيل”, and the 100% zoom chip sat on the first line of the page.
- Root: `QuranReaderScreen` always rendered `errorMessage` above the page, including `RecitationAvailability.MESSAGE`, and the reset chip was outside the reading chrome.
- Fix: do not render that license string as a retry row. Show the zoom reset only while the reading chrome is visible.
- Acceptance: the mushaf is not covered by the license sentence or the reset chip during immersive reading.
- Status: IMPLEMENTED

### VID-002 — Community action covers a listing

- Screen: Community
- Time: 0:30 and 3:10
- Class: UI
- Severity: P1
- Evidence: CONFIRMED IN VIDEO
- The “طلب مساعدة” button covered the lower corner of a card, including “سكن”.
- Root: the list drew underneath the floating action.
- Fix: the list viewport stops above the action, and the demo-reset control is on its own row instead of beside the origin chips.
- Acceptance: the action no longer paints over listing text, and the reset label does not collide with chips.
- Status: IMPLEMENTED. Not rechecked on a phone.

### VID-003 — Services back stack

- Screen: All services
- Time: 2:40 shows the screen; the back press itself was not in a sampled frame
- Class: Navigation
- Severity: P1 if broken
- Evidence: CONFIRMED IN SOURCE, NOT VERIFIED in the video
- `AllServicesScreen` back calls `popBackStack`. A service opened from that screen is a normal push with `launchSingleTop`. `ServicesBackStackRegressionTest` already expects return to services, then home.
- Status: already implemented. The Android test was not executed in this session.

### VID-004 — Fasting journal looks empty

- Screen: Fasting
- Time: 1:30
- Class: UX
- Severity: P2
- Evidence: SUSPECTED
- One frame shows the honesty sentence and a small mark, without the month grid. The next sampled frame is a different screen, so a stuck load is not proven. Source shows `IhsanLoadingState` only while `state.loading` is true, then the month grid.
- Status: NOT FIXED. Needs a device trace if the grid never appears.

### VID-005 — White frame

- Time: 2:20
- Class: UI
- Severity: P2
- Evidence: SUSPECTED
- A full white frame sits between the dark prayer journal and dark daily goals. No destination in the sampled source is a permanent white screen. It may be a system sheet, a transition, or a recorder frame.
- Status: NOT FIXED. Not mapped to a Kotlin screen.

### VID-006 — Settings switch color

- Screen: Settings
- Time: 2:50
- Class: UI
- Severity: P3
- Evidence: CONFIRMED IN VIDEO, explained by source
- The track uses `IhsanTheme.colors.accentContent` through `IhsanSwitch`. That is the approved accent, not a separate orange component.
- Status: left unchanged so the identity is not recolored.

### VID-007 — Prayer journal day controls

- Screen: Manual prayer journal
- Time: 2:00
- Class: UI
- Severity: none
- Evidence: CONFIRMED IN VIDEO and CONFIRMED IN SOURCE
- In RTL, “اليوم السابق” is at the start and “اليوم التالي” toward the end. That matches the source order.
- Status: no change.

Prayer calculation, stale location, and early-adhan rearm were not contradicted by this recording. They were not reopened.

## Files changed for this audit

- `feature/.../quran/presentation/QuranReaderScreen.kt`
- `feature/.../ehsan/EhsanScreen.kt`
- `feature/.../quran/util/RecitationSourceCatalogTest.kt`

## Verification limits

Frames are 10 seconds apart. Pinch movement, back-stack results, TalkBack, and frame rate were not measured on the phone.

Host checks after the code changes: `testDebugUnitTest` passed, then `lintDebug`, `lintVitalRelease`, `assembleDebug`, and QA `assembleRelease` passed. The Android services back-stack test was not run on a device.
