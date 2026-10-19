# QA bug report — 2026-10-14

Severity describes the completion brief's acceptance impact. Implementation fixes are followed by evidence in the final candidate report; source scans alone do not prove end-to-end behavior.

| ID | Severity | Finding / repair | State |
|---|---|---|---|
| REL-01 | P0 | Only5 hadith excerpts with unsourced editorial explanations. Imported publisher's3582 Arabic records v1.7.0, exact fields/grade/takhrij/source, preserve favorites. | IMPLEMENTED; source integrity verified; Android persistence test listed in candidate report. |
| REL-02 | P0 | Truncated AyatKursi and azkar; exact known legacy strings repaired, counters/favorites unchanged, source supplied. | IMPLEMENTED; broader corpus review remains separate. |
| REL-03 | P0 | Quran source ambiguous and search vocalization-sensitive. Exact Tanzil1.1 edition,114/6236/604/30 integrity; Arabic derived search keys; existing installs updated without ID changes. | IMPLEMENTED; exact-source unit and Room edition test. |
| REL-04 | P1 | Font/reciter preferences transient and jump validation incomplete. Persisted, page/juz/ayah bounds, meaningful actual reading-position label. | IMPLEMENTED. |
| REL-05 | P0 | Daily counter stuck next day; zero-target cannot increment. Actual date rollover, mutex, keep favorites and lifetime tasbih. | VERIFIED Gate1 real SQLite. |
| REL-06 | P0 | Declination reflection wrong; no sensor/lifecycle boundary. Getter, availability, lifecycle, independent valid0bearing state. | IMPLEMENTED; PHYSICAL DEVICE PENDING for directional accuracy. |
| REL-07 | P1 | General reminder toggle did not reconcile schedules. Coordinator wiring, permission request, clock/boot reconciliation, stale worker guard, replace schedules. | IMPLEMENTED; physical exact-delivery pending. |
| COM-01 | P0 | Plausible fake phone numbers in seeds. Blank contact, DEMO origin/marking and detail contact block; exact legacy sample neutralization. | IMPLEMENTED. |
| COM-02 | P0 | No persisted author; manage/history nonfunctional. Stable local ID, repository enforcement, actual CRUD and screens, logout/reopen stable profile. | IMPLEMENTED; migration/owner boundary Android tests. |
| COM-03 | P0 | Read-side title/description heuristic deleted arbitrary rows. Removed; schema migration preserves arbitrary legacy rows/images/prayer_log. | IMPLEMENTED. |
| COM-04 | P1 | REQUEST AddDonation route always offered donation. Route now opens existing request form. | IMPLEMENTED; layout matrix includes variant. |
| MED-01 | P0 | Public MP3/HLS interpreted as approved use. Playback/download and queued workers gated pending recording grant; direct HLS off, official YouTube primary. | CONTROLLED PENDING_LICENSE, not media completion. |
| MED-03 | P1 | An IFrame could report onReady yet remain black without PLAYING for40seconds. Initial-start timeout now requires a real PLAYING event; a later user pause does not trigger this initial timeout. | IMPLEMENTED; all86clips must be rechecked on the rebuilt source. |
| MED-02 | P1 | Native overlays covered YouTube; incorrect app referrer; HTML load reported player ready. Controls uncovered, real IFrameAPI readiness/errors, app-package base URL, timeout/fallback. | IMPLEMENTED; actual media results separately reported. |
| AUD-01 | P1 | More than one back-stack Quran VM advanced tracks. One global handler owns completion/next/prev, track metadata separate from reading page, buffering/duration reset/error state. | IMPLEMENTED; synthetic transport test only until reciters approved. |
| STAT-01 | P1 | Tasbih totals33/625/532 and month=week×3 fabricated progress. Defaults0, actual SQLite dated totals and real calendar windows. | IMPLEMENTED; no claim of imported historical activity. |
| SEARCH-01 | P1 | Global categories/results lacked actual learning/community details; some callbacks opened generic lists. Real datasets, typed targets, exact detail route, unsupported Prayer result filter removed, URI encoded. | IMPLEMENTED; exhaustive interaction audit still tracked. |
| SAVE-01 | P1 | Local-profile logout silently removed all `learning_*` saved-library keys. Presentation reset now clears only presentation fields and keeps the device's learning bookmarks/watch-later IDs. Added real DataStore assertions to existing persistence test. | IMPLEMENTED; targeted final runtime regression required. |
| SEARCH-02 | P1 | Browse-Dua CTA still targeted obsolete sample1 after the verified edition replaced visible samples. Added explicit library navigation callback; results still open exact record IDs. | IMPLEMENTED; actual AppNavHost regression test added. |
| UI-01 | P1 | Category labels visibly ellipsized at360dp/font1.3. Use3columns with accessibility fonts below500dp and allow2label lines; default grid and shared typography retained. | IMPLEMENTED; targeted8configuration search rerun required. |
| REM-01 | P0 | Complete sourced tafsir missing. Unverified sample excluded; clearly unavailable action. | UNRESOLVED SOURCE. |
| REM-02 | P0 | Full azkar/dua and Asma editorial meanings lack complete source/religious review. Existing content disclosed; no generated filler. | UNRESOLVED REVIEW. |
| REM-03 | P0 | Every advertised video has not been decoded on Android; metadata is insufficient. | UNRESOLVED PLAYBACK COVERAGE. |
| REM-04 | P0 | Exhaustive all visible-button and physical sensor/alarm journeys require evidence. | UNRESOLVED QA until exact checks documented. |

Known boundary: device-local profiles do not offer secure cross-device identity. Backend requirements are in FUTURE_BACKEND_CONTRACTS; this work does not deploy Firebase/Laravel. Existing personal/global preferences must not be advertised as secure multi-account isolation.
