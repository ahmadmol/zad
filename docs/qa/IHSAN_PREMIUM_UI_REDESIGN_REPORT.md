# IHSAN — Modern Islamic Premium Design System

Task date: 2026-11-02, Asia/Damascus. Branch: `codex/premium-ui-20261102`. Preserved starting HEAD: `ac904e3e456f995e525195b46e3e8bba1b00c9a8`. This is the authorized premium redesign/product expansion, not a replay of Sprint01 or Batch01–05.

Source implementation, host verification and visual acceptance are separate. The accompanying [74-surface matrix](IHSAN_PREMIUM_UI_SCREEN_MATRIX.csv) covers the original 70 states plus Services, Daily Goals, Support and Haramain Visit. **VISUAL NOT VERIFIED applies to every row.** Shared-system inheritance and individually changed layouts are distinguished in `implementation_scope`; an IMPLEMENTED flag does not assert that every leaf layout received a bespoke redesign.

## Before / after

| Area | Before | Implemented behavior |
|---|---|---|
| Foundation | Green night canvas/cards, scattered fixed green accents, competing scenic headers | Charcoal/graphite hierarchy; semantic accents; Emerald/Gold, Sand/Copper, Midnight/Gold; two header geometries through compatibility adapters; shared service/icon/progress/card primitives |
| Brand | Renderer recolored the Kaaba body along with the lettering; density-scaled full bitmap per composition | Original wordmark/composition retained; emerald/ivory letters, neutral dark Kaaba, original gold identity; unscaled decode, cropped in-memory render and a bounded two-theme cache; no source artwork replaced |
| Startup | Native launch followed by a Compose scenic splash, simulated progress and a fixed 1.5-second delay | Native Android 12+ splash held only for actual startup/appearance readiness; restored routes release it; short system-aware exit; older APIs use one Compose loading surface; read errors remain recoverable |
| Onboarding | Three crowded feature grids, paging and start controls together, unavailable audio/public-service promises | Four focused pages: welcome, prayer/time, Quran/dhikr, daily journey; original scalable illustrations; one next/start CTA, skip, animated indicator, RTL, scrollable large-type content; saved completion |
| Permissions | Standalone location permission page | Optional contextual location + Android 13+ notification setup after the value pages; coarse/fine foreground requests only; manual city via existing settings; denial/settings/continue; resumed permission state |
| Home | Crowded scenic header, layered/offset content, mixed services and progress | Separated wordmark/greeting/actions/date/location; original Dome of the Rock illustration explicitly labeled; prayer hero from existing facade; six core services; real all-services page; reading continuation; responsive rings; real explicit-goal summary; Qibla entry; five-prayer timeline; local journal/community/live entries |
| Prayer accuracy | Upcoming time matched to today's list by prayer name | Upcoming time and highlight use the facade's exact next instant, including tomorrow's Fajr; no new calculator or inferred prayer completion |
| Navigation | Full services was a dismissed dialog; child Back returned to Home; tab pop anchor could refer to removed Splash | Services is a real destination with saved search/grid state; child routes push and Back pops; tab switching anchors to Home; shared fade/subtle RTL-aware transitions; restored-route splash policy |
| Worship | Inconsistent prayer/Qibla headers; scenic prayer layout; list items lacked stable identities in some touched lists | Shared prayer/Qibla headers; original prayer/Quran artwork; neutral prayer hero; explicit refresh/location control; stable prayer/surah/search keys; common azkar header/action colors; neutral widget night colors; **paper Quran unchanged** |
| Learning | Separate Fahm scenic header, small image rows, duplicated live WebView owner | Common Fahm/Nabi/Sanhya header system; roomier shared media cards with crop/fallback; existing favorites/search/learning progress preserved; consistent name **الفهم عن الله** without changing IDs/routes/keys |
| Media errors | Generic errors and separate live WebView lifecycle code | One restricted native WebView owner for learning/live; shared pause/dispose/fullscreen/renderer/HTTP handling, provider-specific errors (100/101/150/153), retry and official external fallback; playback is recognized from provider PLAYING, not HTTP/onReady |
| Community | Narrow fit images and dense side-by-side cards/forms | Uniform full-width 16:9 crop frames with error/category fallback; clear status metadata, readable details/contact actions, 48dp tabs, larger form padding/fields; owned local records, drafts and image references retained |
| Profile/settings | Scenic profile header; image replacement without preview/removal; sparse settings grouping | Quiet personal header; photo preview and confirmed removal saved as a recoverable draft; persisted palettes independent of Light/Dark/System; language, prayer/location, privacy/local-data and support groups |
| Support | No dedicated support route | Usage, expandable FAQ, local problem/suggestion draft, explicit copy, about/version; email action configurable only after a public address is approved; no invented contact or submitted-ticket state |
| Local expansion | No general personal-goal ledger or visitor-services hub | Optional daily/weekday goals, templates/custom titles, numeric targets, edit/archive, completion/undo/counts and local history; nearby-mosque map intent with browser fallback; official Nusuk guide/service links |

## Design system and implementation boundaries

The existing `IhsanTheme`, `IhsanButton`, `IhsanTopBar`, `IhsanBottomNavigation`, `IhsanContentCard`, empty/loading/error/capability states, form controls and Liquid Glass modifier remain canonical. `IhsanPrimaryButton` delegates to the existing filled button. `IhsanSkylineHeader` adapts old call sites to the standard/large top bars; it does not introduce a third visual header system.

New reusable primitives: `IhsanIconContainer`, `IhsanServiceCard`, `IhsanSectionHeader`, `IhsanGlassCard`, `IhsanProgressIndicator`, `IhsanDailyGoalCard`, `IhsanPrayerTimeline` and `IhsanIllustration`. Material 3 dialogs, sheets and chips inherit the shared theme instead of adding competing wrappers. Existing typography/spacing/shape tokens are reused. New core icons use the installed Material icon family; existing selected states and feature-specific graphics remain where appropriate.

Glass uses an opaque backing plus a thin border and static sheen. No GPU blur, new bitmap dependency, continuous gradient or new animation library was added. Animation settings are re-read on resume; pager/selection/navigation/progress durations reduce to zero when system animations are disabled, and shimmer uses a static fill. Card/list content grows with text; service grids adapt to width/font scale; journey icons sit inside a dedicated 64dp ring region. Root status-bar protection and an opaque bottom bar prevent content/gesture-area interference. These are source measures, not measured frame/TalkBack results.

Compose previews exist for onboarding, the prayer/Home hero and shared components (including dark and 200% text scenarios). The pinned project has no configured host screenshot/render task. Installed Studio IDE integration jars are not an executable project renderer. **Previews were compiled, not rendered; no screenshot, phone, emulator, AVD or ADB execution is claimed.** Physical layout, all 200% text paths, TalkBack/focus, sensor behavior, startup flash/fade, OS permission dialogs and performance still need separately authorized Android acceptance.

## Goals and data preservation

`DailyGoalRepository` uses the existing `UserPreferences` DataStore through the additive `personal_daily_goals_v1` key. A versioned validated ledger contains definitions and explicit dated entries. Atomic `edit` transforms serialize concurrent increments, bound counts and refuse stale-day writes. Reading/opening/timers never write completion. Weekday scheduling and created-on dates control which goals are due. Deletion archives the definition and retains recorded history. Historical entry targets remain intact after edits. The local UI supports 50 active goals and targets from 1 to 10,000.

Invalid/corrupt/future-version goal data raises a recoverable error and is never silently replaced with an empty ledger. Failed writes retain the editor/confirmation and original records. The subscribed date flow only refreshes display state and stops with its subscriber. The prayer/fasting/Quran/dhikr journals and learning progress are independent.

Palette selection uses additive `appearance_palette_v1` in the existing settings store. Historical `theme_mode` / `dark_mode` and all other preferences are preserved. All three identities support Light/Dark/System. The logo and Quran paper are independent of palette selection. Semantic and Material role pairs are checked for AA contrast by JVM tests; those checks do not certify every legacy text/image combination.

**Room remains version 11.** No table/entity/migration/schema, canonical religious asset, application version, pinned dependency version, phone database or stored route/learning ID was changed. There is no destructive migration. No Backend/authentication/public sync/payment service was introduced. Historical Git privacy contamination remains unresolved; current-tree privacy checks do not repair published history.

## Navigation verification

`serviceDestinationRoute` maps the complete catalog, with explicit owners for the live chooser and external mosque search. Services remains on the stack when opening Tasbih or another child. Its query and lazy-grid state use Compose saveable state. Home's scroll state is saveable through `rememberLazyListState`; existing child search/learning state owners are preserved. Bottom tabs keep single-top/save/restore behavior with a real Home anchor.

JVM checks cover service mapping/preservation, route identity, startup/restoration policy and facade parity across tomorrow's Fajr. `ServicesBackStackRegressionTest` uses the real Services surface with Compose Navigation to exercise Home → Services → Tasbih → Back → Services (query retained) → Back → Home. A second test uses `StateRestorationTester` to restore the Services query. Both are **compiled only**, because Android execution is excluded. They do not prove full app navigation/device acceptance.

## References, source checks and rights

The source pages for [Sadiq](https://gtaf.org/apps/sadiq/), [Nusuk](https://www.nusuk.sa/) and its [official service directory](https://www.nusuk.sa/all-services) were reviewed for information hierarchy and official-service boundaries. Their visual assets, copy and branding were not copied. New illustrations are original Compose Canvas geometry authored for this change, with no remote images or raster-generation assets.

The reproducible [media inventory](IHSAN_PREMIUM_MEDIA_INVENTORY.csv) contains **89 entries**: 87 existing learning slots plus the two supplied live candidates. Public oEmbed probes on 2026-11-02 returned matching title/author metadata for **86 learning videos**, one learning slot has **no approved mapping**, and both supplied live URLs returned HTML-only metadata without title/author identity. Those two are labeled `OEMBED_HTML_ONLY; PUBLISHER_UNCONFIRMED`. Metadata is not proof of runtime playback, current availability, permission to distribute or continued embedding permission. Catalog IDs, episode mappings and approved source files were not replaced.

- Makkah candidate: [Cm1v4bteXbI](https://www.youtube.com/watch?v=Cm1v4bteXbI). Direct web fetch was throttled; oEmbed supplied no publisher/title. Exact live status and internal playback remain unconfirmed.
- Madinah candidate: [9A1S0xAPVIs](https://www.youtube.com/watch?v=9A1S0xAPVIs). Web search returned an attributed verified Sunnah-channel listing, but the opened page lacked live-state details and oEmbed supplied no publisher/title. Internal playback remains unconfirmed.
- Official channel entry points: [Saudi Quran TV](https://www.youtube.com/@SaudiQuranTv), [Saudi Sunnah TV](https://www.youtube.com/@SaudiSunnahTv). The SBA site fetch timed out; this session does not independently recertify all channel identities/current policy.
- [YouTube embedded-player requirements](https://developers.google.com/youtube/terms/required-minimum-functionality) and [official IFrame error-code definitions](https://developers.google.com/youtube/iframe_api_reference#onError) were reviewed. The player retains referrer/origin, official controls, playback callbacks and restricted navigation. No stream URL was extracted, rehosted or bypassed. Direct HLS remains disabled by its existing approval flag.

All inventory entries retain **CONTENT/RIGHTS PENDING** and **VISUAL NOT VERIFIED**. Existing absent Sanhya mappings, station editorial data, Nabi recipes, licensed audio/tafsir and approved religious-event data remain under the prior report's specific content gates. No HTTP/oEmbed result was relabeled WORKING in app.

## Deliberately unavailable capabilities

| Capability | Precise remaining requirement |
|---|---|
| Public community/auth/sync/moderation/payment delivery | Real Backend and approved contracts; private local CRUD remains available |
| Internal live playback and changing third-party videos | Authorized Android playback acceptance plus publisher/embedding/rights confirmation; retry/external official fallback implemented |
| Missing religious/editorial/audio/recipe content | Reviewed source mappings and distribution rights; no unrelated substitutions |
| Email contact/developer biography | Owner-approved public address/identity information; the configurable interface and local draft/copy are implemented |
| Nusuk bookings, permits, availability and travel documents | Explicitly excluded; official external links only |
| Full English translation | Approved complete translation; Arabic remains the honest current UI |
| New prohibited-time/late-night worship calculations | Approved jurisprudential sources/method and domain contracts; none guessed; existing Ramadan behavior retained |
| Optional personalized six-service selection | Current implementation uses a fixed curated six; all 28 services remain accessible |
| Visual/accessibility/OS execution and measured performance | Separately authorized device/Android acceptance; no actual rendering was available in the pinned host workflow |
| Production publication/history remediation | Production signing/distribution decisions and separate history cleanup; no push or history rewrite performed |

## Final verification and artifacts

| Check | Result |
|---|---|
| Full JVM Tests (Debug) | **689 PASS**: app 38 + feature 617 + designsystem 34; failures/errors/skipped **0**. |
| Targeted regressions | `ServicesBackStackRegressionTest`, `DailyGoalPersistenceTest`, `PalettePersistenceTest`, `HomeFeaturePreservationTest`, `HomeDestinationCatalogTest`, `HomePrayerParityTest`, `EditProfileViewModelTest`, `SettingsViewModelTest`, `YouTubeFailureMessageTest`. |
| Android Test Compilation | `compileDebugAndroidTestKotlin` PASS for app, feature, designsystem. |
| Lint debug | **0 errors** across app, feature, designsystem. |
| Debug QA Build | `assembleDebug` PASS. Output: `app-debug.apk` (63,038,958 bytes). |
| Release QA Build | `assembleRelease -PihsanPhysicalQa=true` PASS with R8 shrinking & minification. Output: `app-release.apk` (25,588,317 bytes). |
| Package ID Verification | Debug: `com.example.mol`; Reference QA: `com.example.mol.referenceqa`. |
| Privacy Gate | Clean current working tree; no secrets, keys, or private profiles staged. |

### Generated APK SHA-256 Hashes

- `app/build/outputs/apk/debug/app-debug.apk`:
  `F3F43AA6ED28A9437BDCA3FDCB4ABBF6651867377A50D0B7150C471286DDBA59`
- `app/build/outputs/apk/release/app-release.apk`:
  `379A0456A19252C2D94D624BEAE420A3EDEC585387543259634391678DA6296E`

### Source Commits

- Commit `e826cb8`: "Redesign Ihsan Compose experience and add local daily goals"
- Branch: `codex/premium-ui-20261102`

