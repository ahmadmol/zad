# Part 4 — Design, Localization & Accessibility Inventory

**Branch:** `refactor/design-localization-accessibility`  
**Baseline commit:** `19bf2ff`  
**Date:** 2026-07-18

---

## Baseline commands

| Command | Result |
|---------|--------|
| `git status -sb` | Clean except untracked `.project-preservation/` |
| `git rev-parse --short HEAD` | `19bf2ff` |
| `:app:assembleDebug` | PASS |
| `:feature:testDebugUnitTest` | PASS |
| `:app:testDebugUnitTest` | PASS |
| `:app:lintDebug` | PASS |

---

## 1. Hardcoded production colors (high impact)

| Color | Role in screens | Files (sample) |
|-------|-----------------|----------------|
| `#0D4D3D` | De-facto brand green (not theme primary) | Home, Tasbih, Ehsan, Profile, EditProfile, IhsanDetails, Dua, GlobalSearch, DailyActivityCard |
| `#073028` | Canonical `PrimaryTeal` / `colorScheme.primary` | `designsystem/.../Color.kt` only |
| `#F9F9F9` / `#F8F9FA` | Screen backgrounds | Profile, Ehsan, Dua, Prayer, Tasbih |
| `#F1F8F6` | Mint card surface | Home, DailyActivities, Tasbih, DailyActivityCard |
| `#2E7D32` / `#E65100` | Offer / request chips | Ehsan, IhsanDetails |
| `#25D366` | WhatsApp button | IhsanDetails (**intentional brand**) |
| `#C66927` | Accent / incomplete | Tasbih, DailyActivityCard |
| `#146C7A` / `#D32F2F` | Location permission UI | MainActivity |
| Favorites pinks | Favorite toggles | Azkar, Hadith, Asma |

---

## 2. Screen-local color constants

- `TasbihScreen`: `BrandGreen`, `MintSurface`, `ScreenBackground`, `AccentOrange`
- Elsewhere: inline `Color(0x…)` without named locals

---

## 3. Repeated dimensions / radii

- Card radius `12.dp` / `16.dp` / `20.dp` / `24.dp` repeated
- Control heights `48.dp` / `56.dp`
- Icon sizes `16.dp` / `18.dp` / `24.dp` / `48.dp` / `64.dp` / `80.dp`
- Padding `16.dp` / `24.dp` / `32.dp` (Spacing exists but underused)
- Min touch target inconsistently applied (IconButton default OK; some custom clickables smaller)

---

## 4. Repeated screen-local patterns

- CircularProgressIndicator with hardcoded green
- Scaffold + CenterAlignedTopAppBar / TopAppBar
- Offer/request status chips
- Contact Call / WhatsApp button rows
- Empty/error Text blocks without shared components

---

## 5–6. Hardcoded / high-risk strings

| Area | Examples | Resources today |
|------|----------|-----------------|
| Settings | «صوت الأذان», share CTA | Partial (`settings_title`, toggles) |
| Prayer | Retry, reschedule, location settings | Mostly hardcoded |
| Profile / Edit | Local profile honesty, validation | Mostly hardcoded |
| Ehsan / Details | Offer/request labels, call/WhatsApp, report, missing case | Mostly hardcoded |
| MainActivity | Location permission copy | Hardcoded |
| Feature strings.xml | Azkar/settings/dashboard subset | No `values-ar` |
| App strings.xml | `app_name` only | No `values-ar` |

**No `values-ar` directories exist** — Arabic is embedded as default `values`.

---

## 7. Loading / empty / error presentations

| Pattern | Where |
|---------|-------|
| `CircularProgressIndicator` | Statistics, Ehsan, IhsanDetails, Dua, … |
| `IhsanEmptyState` | Exists in designsystem; underused |
| Inline error Text | Statistics, Prayer, IhsanDetails |
| Capability / permission | MainActivity location screen; Prayer location status |
| Home section isolation | Part 2 — preserve |

---

## 8–9. Accessibility gaps

- Many `contentDescription = null` on actionable icons (Ehsan ~11, IhsanDetails ~8, Profile, Settings back is OK with "Back" English)
- Tasbih counter lacks semantics for count/target
- Settings switches rely on adjacent Text (OK) but icons need Arabic descriptions
- Small custom clickables without 48dp target in places

---

## 10. Forced RTL / bidi

- `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)` on IhsanDetails, Tasbih, others
- Phone numbers / WhatsApp URLs may need LTR isolation
- Media timestamps / verse numbers (Quran) — verify, avoid breaking specialized typography

---

## 11. Design-system components available

`IhsanButton`, `IhsanSecondaryButton`, `IhsanBottomNavigation`, `IhsanEmptyState`, `IhsanSearchBar`, `IhsanActionCard`, `DailyActivityCard`, `LastReadCard`, `DashboardHeader`, `AyahItem`, `QiblaCompass`, `Shimmer`

---

## 12. Candidates to promote / add

- `IhsanLoadingState`
- `IhsanErrorState` (title, message, retry)
- Enhance `IhsanEmptyState` with optional actions
- Semantic color / dimen tokens (not screen-named)
- Optional: status chip (offer/request) if reused ≥2 screens

---

## 13. Duplicate themes

| Package | Usage |
|---------|-------|
| `designsystem` `IhsanTheme` | **Canonical** (MainActivity) |
| `app/.../ui/theme` | Unused (template purple) |
| `feature/.../ui/theme` | Unused (template purple) |

Safe to remove after confirming zero imports (confirmed: only self-package files).

---

## Canonical brand decision (for Slice 2)

- **Canonical brand / primary:** `#073028` (`PrimaryTeal` → `MaterialTheme.colorScheme.primary`)
- **`#0D4D3D`:** treat as legacy alias of brand; replace with theme primary / semantic brand token
- **WhatsApp `#25D366`:** intentional third-party brand exception
