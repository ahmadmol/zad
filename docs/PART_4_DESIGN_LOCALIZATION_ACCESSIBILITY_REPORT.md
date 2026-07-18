# Part 4 — Design, Localization & Accessibility Report

**Date:** 2026-07-18  
**Branch:** `refactor/design-localization-accessibility`

---

## 1–2. Starting / final

| Item | Value |
|------|--------|
| Start branch | `refactor/product-surface-ownership-cleanup` |
| Start commit | `19bf2ff` |
| Final branch | `refactor/design-localization-accessibility` |
| Final commit | *(see tip after commits)* |

---

## 3. Baseline

| Check | Result |
|-------|--------|
| assembleDebug | PASS |
| feature + app unit tests | PASS |
| lintDebug | PASS |

---

## 4–7. Colors / tokens

| Item | Decision |
|------|----------|
| Inconsistency | `#0D4D3D` (screens) vs `#073028` (`PrimaryTeal` / theme primary) |
| Canonical brand | `#073028` (`PrimaryTeal` = `colorScheme.primary`) |
| Tokens added | `IhsanSemanticColors`, `IhsanDimens`, exposed via `IhsanTheme.colors` / `.dimens` |
| Hardcoded brand greens removed | High-traffic/high-risk screens mapped to `MaterialTheme.colorScheme.primary` or semantic colors |
| Exception | WhatsApp `#25D366` kept as `IhsanTheme.colors.whatsapp` |

Light/dark semantic mappings defined for mint surface, charity chips, progress, accent, favorite.

---

## 8–11. Localization

| Strategy | Arabic default in `values/`; explicit `values-ar/` mirrors for high-risk/shared keys |
| Module ownership | App shell (location permission) in `:app`; feature UI in `:feature` |
| High-risk migrated | Location permission, prayer retry/reschedule, Ehsan details/report/contact, settings adhan/share, profile local notice, statistics empty |
| Shared actions | `common_*`, `cd_*` content descriptions |
| Format strings | `statistics_total_count_value`, `tasbih_count_semantics`, `ehsan_share_template` |
| Full English | **Not claimed / not implemented** |

---

## 12–13. Shared states / screens

**Added/updated in `:designsystem`:**
- `IhsanLoadingState` / `IhsanInlineLoading`
- `IhsanErrorState`
- `IhsanCapabilityState`
- Enhanced `IhsanEmptyState` (optional actions + semantics)

**Screens migrated (incremental):**
- Settings, Statistics, MainActivity location, Tasbih, IhsanDetails, Prayer (retry copy), Home/Ehsan/Profile/EditProfile/Dua/DailyActivities/GlobalSearch (brand color), DailyActivityCard

**Intentionally light / deferred:**
- Quran specialized typography untouched
- Full Azkar/Hadith/Asma/Live string sweeps deferred
- IhsanPlus packages not touched

---

## 14–17. Components / themes

| Reused | IhsanButton, IhsanEmptyState, Material Scaffold/TopAppBar, existing cards |
| Promoted/new | Loading, Error, Capability states; semantic colors/dimens |
| Duplicate themes | Removed unused `app/.../ui/theme` and `feature/.../ui/theme` (template purple) |

---

## 18–22. Accessibility / RTL / scale

| Fix | Detail |
|-----|--------|
| Content descriptions | Back, share, vibration, call, WhatsApp, report, tasbih increment/reset |
| Touch targets | Settings/Tasbih/IhsanDetails IconButtons use `minTouchTarget` / control heights |
| Semantics | Tasbih counter exposes count/target state description |
| RTL | Existing Rtl providers retained; no media-control reversal |
| Large text / dark | **Not device-verified** in this pass |
| Contrast | Brand via theme primary; semantic dark mappings added |

---

## 23–24. Tests / commands

- `IhsanSemanticColorsTest` (designsystem)
- `Part4LocalizationResourcesTest` (feature)
- `Part4AppLocalizationResourcesTest` (app)
- Existing Parts 1–3 tests retained

Commands: assembleDebug, designsystem/feature/app unit tests, lintDebug.

---

## 25–27. Regression

| Part | Status |
|------|--------|
| Part 1 prayer tests | Covered by feature unit tests |
| Part 2 dashboard tests | Covered |
| Part 3 ownership/nav tests | Covered |
| Tasbih canonical / SettingsVM / StatisticsVM / IhsanDetails / no Inbox / no IhsanPlus | Preserved |

---

## 28–29. Lint / device

| Lint | PASS |
| Device/emulator | **Not executed** |

---

## 30–32. Risks / deferred / rollback

**Risks:** Remaining hardcoded greens/strings on lower-traffic screens; visual shift where `#0D4D3D` became darker `#073028`.

**Deferred:** Full English, language switching, complete string migration, Compose UI instrumented a11y suite, Quran typography changes, screenshot tests.

**Rollback:** `git checkout refactor/product-surface-ownership-cleanup`

---

## 33. Acceptance checklist

| Criterion | Status |
|-----------|--------|
| Canonical theme in designsystem | Pass |
| High-impact brand colors replaced | Pass (priority screens) |
| Semantic tokens | Pass |
| Duplicate themes removed | Pass |
| High-risk strings in resources | Pass |
| Shared states | Pass |
| Screens reused | Pass |
| A11y descriptions / touch on priority controls | Pass (targeted) |
| Device font-scale / dark verification | Not run |
| assemble + unit tests + lint | Pass |
| No IhsanPlus / no Part 5 | Pass |
