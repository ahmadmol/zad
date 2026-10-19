# Visible functions and callback audit

The final source scan enumerates **355 callback sites** across app/feature/designsystem. This is an inventory of code sites, including previews/reusable components, not 355 executed UI gestures. Exact files/lines/expressions: [button-audit-final.json](../../.artifacts/release-final-20261014/button-audit-final.json). Initial inventory had341sites before functional additions.

| Function family | Evidence | Honest boundary |
|---|---|---|
| Bottom navigation/theme/back | Actual MainActivity Light/Dark navigation test, saved DataStore theme | Production navigation tested; dynamic accessibility/window states still follow matrix evidence. |
| Quran search/bookmark/edition/preferences | Real Room tests and460unit suite; exact canonical text/604partition | Bookmarks/search are data tests, not a claim that every passage was manually read. |
| Quran page/reciter/zoom/collapse/audio | Real UI journey; selected reciter and zoom persist; collapse/reopen works; transport controls disabled pending rights | Actual recordings/downloads unavailable; synthetic MediaSession playback is labeled technical QA. |
| Hadith/dua search/filter/favorite/copy/share | Source data/VM tests, actual Room ingestion/favorite persistence, route rendering | Full3582hadith+39dua integrity is source verification, not independent religious grading. External share applications are OS-dependent. |
| Community create/edit/status/delete/reset | Repository ownership/migration tests; actual management UI edit/save/status/delete-cancel/confirm journey; existing form draft and album tests | GUI draft plus repository create is not a blanket assertion that every image-picker/provider gesture was exercised. |
| Profile/bio/learning save | Actual DataStore reopen, actual keyboard save/cancel visibility, saved-instance restoration | Device-local presentation, not cloud identity or secure multi-account isolation. |
| Learning/video/search | Repository/UI tests; actual official-player runtime manifest linked in media report | Metadata availability, actual PLAYING state, fallback dispatch and full external playback are separate statuses. |
| Prayer/reminders/qibla | Unit logic, actual Android notification-channel tests, production routes/manual location | Physical direction/calibration/exact alarm/reboot timing remain PHYSICAL DEVICE PENDING. |
| DEMO contacts/reset | Explicit DEMO/no-contact state and reset ownership tests | No real persons or completed charitable acts are implied. |

Empty callback occurrences reviewed: Home/global-search previews are non-production; Tasbih header's unused search/notification parameters do not render those controls; IhsanPlus prayer-assist quick actions belong to an unregistered/gated experimental surface. They are not counted as verified visible CTAs. Unapproved tafsir/recording actions expose an explicit unavailable state rather than pretending to execute.

**100% exhaustive manual visible-button coverage is not claimed.** The release decision must retain this acceptance limit. Unit, repository, route and selected UI evidence must not be collapsed into a fabricated all-buttons PASS.
