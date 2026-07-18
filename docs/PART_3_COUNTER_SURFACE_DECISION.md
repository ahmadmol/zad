# Part 3 — Counter Surface Decision

**Date:** 2026-07-18  
**Branch:** `refactor/product-surface-ownership-cleanup`

---

## 1. Selected production surface

**Tasbih** (`Screen.Tasbih` → `tasbih_screen` → `TasbihScreen` / `TasbihViewModel`)

Sebha is legacy and removed from DI and the codebase UI entry points.

---

## 2. Evidence

| Signal | Finding |
|--------|---------|
| Active route | Only `Screen.Tasbih` is registered in `AppNavHost` |
| Home | `onNavigateToTasbih` / action `"tasbih"` |
| Azkar | `onOpenSebha` already navigated to `Screen.Tasbih.route` |
| Daily activities | `DailyActivityMapper` route `"tasbih_screen"` |
| Sebha route | **None** — `SebhaScreen` had no composable destination |
| Koin | Both registered before cleanup; only Tasbih was reachable |
| Persistence | Both used Azkar Room; Tasbih filters `category == "تسبيح"` |

---

## 3. Data ownership

| Concern | Owner |
|---------|-------|
| Counter counts | Azkar Room (`ZikrEntity`) via `IncrementCounterUseCase` / `ResetCounterUseCase` |
| Production UI mutations | `TasbihViewModel` only |
| Vibration preference | Shared `SettingsManager` (read/write from Tasbih) |
| Daily checklist progress | `UserPreferences` / `DailyActivityIds.TASBEEH` |

---

## 4. Behavior differences

| | Tasbih | Sebha (legacy) |
|--|--------|----------------|
| Category | Exact `"تسبيح"` | Contains `"سبح"` (e.g. `"سبحة"`) |
| CRUD custom beads | No | Yes (edit/delete) |
| Daily activity | `DailyActivityIds.TASBEEH` | String `"tasbeeh"` on target complete |
| UX | Circular progress counter | List + dialogs |

---

## 5. Migration / compatibility

- No Room schema change.
- Existing `"تسبيح"` row progress is preserved automatically (same use cases / DB).
- Custom `"سبحة"` rows remain in Room but are not shown in production Tasbih (safe; no deletion).
- No Sebha route existed to redirect; DI registration and legacy screen/ViewModel files removed.
- Azkar CTA label still says «فتح المسبحات» but opens Tasbih (behavior unchanged from Part 2).

---

## 6. Files

| Action | Files |
|--------|-------|
| Retain | `TasbihScreen`, `TasbihViewModel`, `TasbihUiState`, route `Screen.Tasbih` |
| Remove | `SebhaScreen.kt`, `SebhaViewModel.kt`, Koin `SebhaViewModel` |

---

## 7. Rollback

1. Restore deleted Sebha files from git history.
2. Re-register `viewModelOf(::SebhaViewModel)`.
3. Optionally add a Sebha composable route if product requires a second surface (not recommended).
