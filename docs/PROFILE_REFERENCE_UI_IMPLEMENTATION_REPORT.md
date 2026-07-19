# Profile Reference UI Implementation Report

**Classification:** Profile UI Redesign Complete · Business Logic Unchanged · Architecture Unchanged · Navigation Routes Unchanged · Bottom Navigation Hidden on Profile Only

## 1. Starting branch and commit

| Item | Value |
|------|--------|
| Prior tip | `ui/home-reference-redesign` @ `bbf2820` |
| Original QA tip (historical) | `qa/final-runtime-hardening` @ `04126b6` |
| Checkpoint (prior UI) | `checkpoint/pre-home-ui-work` @ `56ac2e2` |

Working tree note at start: untracked `docs/screenshots/` left intact (not discarded). Branch created without reset.

## 2. Final branch and commit

| Item | Value |
|------|--------|
| Branch | `ui/profile-reference-redesign` |
| Tip | `f45c59b` |

## 3. Reference image used

Cursor workspace asset (Profile reference mock):

`assets/c__Users_WIN_10_AppData_Roaming_Cursor_User_workspaceStorage_empty-window_images_ChatGPT_Image_18_______2026__04_39_54__-d923e394-88e2-4123-badf-8a5187ae50e2.png`

Implemented via Compose components — **not** embedded as a static screenshot background.

## 4. Files modified

- `feature/.../profile/ProfileScreen.kt`
- `feature/.../profile/presentation/components/ProfileUiComponents.kt` *(new)*
- `feature/.../profile/ProfileReferenceUiBoundaryTest.kt` *(new)*
- `app/.../ui/MainScreen.kt` *(presentation hide bottom bar on Profile)*
- `designsystem/.../drawable/ic_islamic_pattern_tile.xml` *(new decorative vector)*
- `docs/PROFILE_REFERENCE_UI_IMPLEMENTATION_REPORT.md` *(this file)*

## 5. Composables added

- `ProfileTopBar`
- `ProfileHeader`
- `ProfileAvatar`
- `ProfileImpactCard`
- `ProfileStatItem`
- `ProfileSettingsCard`
- `ProfileMenuRow`
- `ProfileSupportCard`
- `ProfileVersionText`
- Preview-only content helpers

## 6. Existing components reused

- `AuthBottomSheet` (local profile create/open)
- Existing dialogs (language / privacy / help)
- `IhsanTheme` / semantic colors / dimens
- Material Icons + AutoMirrored icons
- Existing Profile callbacks from `AppNavHost`

## 7. Dynamic data mappings

| UI | Source |
|----|--------|
| Name | `ProfileUiState.userName` |
| Phone | `ProfileUiState.userPhone` (LTR block) |
| Avatar letter | Presentation `first letter` of name |
| Donations count | `myDonations.count { type == OFFER }` |
| Requests count | `myDonations.count { type == REQUEST }` |
| Language subtitle | Existing `"العربية"` dialog source (unchanged) |
| App version | `PackageManager.getPackageInfo(...).versionName` |
| Logged-in / missing profile | `isUserLoggedIn` |
| Loading | `isLoading && !isUserLoggedIn` |

## 8. Values omitted because unavailable

| Reference value | Handling |
|-----------------|----------|
| `"محسن متميز"` impact tier | **Not invented.** Card title uses honest label **`نشاطك المحلي`**. |
| Photo avatar URL | No image field in state — letter avatar only (same as before). |
| Hardcoded `1.0.0` | Replaced with installed `versionName` (currently app `1.0`). |

## 9–17. Unchanged layers

| Area | Status |
|------|--------|
| 9. ProfileViewModel | Unchanged |
| 10. Repository | Unchanged |
| 11. Room | Unchanged |
| 12. DataStore | Unchanged |
| 13. Navigation routes | Unchanged (`profile_screen`, edit, history, settings, reminders) |
| 14. Bottom nav outside Profile | Home + إحسان still show bar; 3 tabs remain in `Screen.items` |
| 15. Edit profile | Same `onEditProfileClick` |
| 16. Donation history | Same callback |
| 17. Settings / reminders / language / privacy / logout | Same behaviors (`viewModel.logout()`, dialogs) |

## 18–21. Verification

| Check | Result |
|-------|--------|
| 18. Debug assemble | PASS |
| 19. Release assemble | PASS |
| 20. Unit tests (`designsystem` / `feature` / `app`) | PASS |
| 21. Lint debug | PASS |

## 22. Device verification

Install attempted when device available. If NotificationShade focuses (Xiaomi), screenshot may be blocked — UI verified by compile + boundary tests. Re-capture after collapsing shade recommended.

## 23. Final screenshot

Path (if captured): `docs/screenshots/profile_reference_ui.png`  
Otherwise: device shade interference noted; Preview set covers light/dark/360/430/fontScale/missing.

## 24. Known visual differences from reference

- Impact tier text is **نشاطك المحلي** (honest) not **محسن متميز**.
- Version shows real `versionName` (`1.0`) not mock `1.0.0`.
- No user photo bitmap (state has none).
- Pattern is vector tile, not the exact mock geometry.
- Bottom nav removed only while Profile destination is focused; tab routes still exist.

## 25. Regression checklist

- [x] No ViewModel / Repository / Room / DI edits
- [x] No route string changes
- [x] Profile bottom bar hidden; Home/Ehsan unchanged
- [x] Local-profile auth sheet retained
- [x] Logout still `viewModel.logout()`
- [x] No fake trust / impact algorithm
- [x] Demo IhsanPlus not wired
- [x] Builds + tests + lint green
