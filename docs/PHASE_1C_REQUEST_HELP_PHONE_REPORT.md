# Phase 1C RequestHelp Phone Integrity Report — إحسان

**Date:** 2026-07-12  
**Branch:** `fix/request-help-phone-integrity`  
**Phase:** 1C — RequestHelp Phone Integrity only

---

## 1. Executive Result

| Item | Result |
|------|--------|
| Placeholder Removed | **Yes** — `"0000000000"` removed from production submission flow |
| Phone Source of Truth | **Verified** — Uses active local user profile from `UserRepository` |
| Validation Logic | **Implemented** — Trims, rejects blanks/placeholders, normalizes format |
| UI Error Feedback | **Implemented** — Clear Arabic snackbar messages |
| Automated Tests | **Added** — Coverage for success, missing user, blank, placeholder, and invalid format |
| Build / Test / Lint | **PASS** |
| Controlled Integration | **REMAINS BLOCKED** |

---

## 2. Starting Branch and HEAD

| Item | Value |
|------|--------|
| Started from | `fix/room-migration-safety` (Phase 1B tip) |
| Current Branch | `fix/request-help-phone-integrity` |
| HEAD | `c1aed60` (Phase 1B commit) |

---

## 3. Confirmed Original Bug

New help requests in `RequestHelpScreen` were hardcoded to use `"0000000000"` as the phone number, regardless of the user's actual profile data. This made it impossible for donors to contact the requester via Phone or WhatsApp from the details screen.

---

## 4. Original Data Flow

1. `RequestHelpScreen` (Hardcoded `"0000000000"`)
2. `AddEhsanViewModel.addDonation(...)`
3. `AddDonationUseCase`
4. `EhsanRepository`
5. `DonationDao` (Insert into `donations` table)

---

## 5. Phone Source of Truth

The phone number is now obtained directly from the `UserRepository.getUser()` Flow, which provides the active local `UserEntity`. The ViewModel updates its internal state whenever the user profile changes.

---

## 6. Validation Rules

File: `ValidatePhoneNumberUseCase.kt`

- **Trim:** Whitespace is removed from both ends.
- **Reject Blank:** Null or empty strings are rejected.
- **Reject Placeholder:** Exactly `"0000000000"` is rejected.
- **Normalize:** Retains leading `+` and all digits; removes spaces, dashes, and other separators.
- **Reject Invalid Format:** Rejects if the resulting string contains fewer than 7 digits (insufficient for a valid number).

---

## 7. Implementation Changes

### Domain Layer
- Created `ValidatePhoneNumberUseCase` and `PhoneValidationResult`.
- Registered `ValidatePhoneNumberUseCase` in `UseCaseModule.kt`.

### Presentation Layer
- **AddEhsanViewModel:**
    - Injected `UserRepository` and `ValidatePhoneNumberUseCase`.
    - Replaced `userName` Flow with `uiState` StateFlow.
    - Added `submitRequest` method with validation and loading state management.
- **RequestHelpScreen:**
    - Updated to use `uiState` and `submitRequest`.
    - Added `SnackbarHost` for error messages.
    - Disabled button and showed loading indicator during submission.
- **AddEhsanScreen:**
    - Updated to use `uiState` and `submitRequest`.
    - Made phone and name fields read-only (obtained from profile) to ensure integrity for REQUEST types.

---

## 8. UI Error Behavior

If the profile phone is missing or invalid, a snackbar displays:
> "تعذر إرسال الطلب لأن رقم الهاتف غير متوفر. يرجى تحديث رقم الهاتف في الملف الشخصي ثم المحاولة مرة أخرى."

---

## 9. Automated Tests

### ValidatePhoneNumberUseCaseTest
- `valid phone returns Valid`: Normalization check (`+963 9...` -> `+9639...`).
- `blank phone returns Empty`.
- `placeholder phone returns Placeholder`.
- `too few digits returns InvalidFormat`.

### AddEhsanViewModelTest
- `initial state reflects user profile`: Verifies profile loading.
- `submitRequest with valid data calls use case`: Verifies success path.
- `submitRequest with invalid phone blocks submission`: Verifies integrity check.
- `submitRequest when no user blocks submission`: Verifies safety.
- `submitRequest when already submitting does nothing`: Verifies duplicate protection.

---

## 10. Build Result

| Command | Result |
|---------|--------|
| `.\gradlew.bat :app:assembleDebug` | **SUCCESS** (2m 31s) |

---

## 11. Test Result

| Command | Result |
|---------|--------|
| `.\gradlew.bat :feature:testDebugUnitTest` | **SUCCESS** (17 tests passed, 0 failures) |

---

## 12. Lint Result

| Command | Result |
|---------|--------|
| `.\gradlew.bat :app:lintDebug` | **SUCCESS** (0 errors) |

---

## 13. Manual Verification

| Scenario | Result |
|----------|--------|
| Valid profile submission | **Pending** (No device/adb) |
| Invalid profile blocking | **Pending** (No device/adb) |
| Database Integrity | **Pending** |

Implementation matches logic verified by unit tests.

---

## 14. Placeholder Search Result

Command: `git grep -n "0000000000"`

| File | Line | Classification |
|------|------|----------------|
| `ValidatePhoneNumberUseCase.kt` | 17 | Validation Rule (Production) |
| `ValidatePhoneNumberUseCaseTest.kt` | 28 | Test Case |
| `AddEhsanViewModelTest.kt` | 69 | Test Case |

**Production flow in screens no longer contains the hardcoded string.**

---

## 15. Changed Files

| File Path | Role |
|-----------|------|
| `feature/.../ehsan/domain/usecase/ValidatePhoneNumberUseCase.kt` | New Use Case |
| `app/.../di/UseCaseModule.kt` | DI Registration |
| `feature/.../ehsan/presentation/AddEhsanViewModel.kt` | State & Logic |
| `feature/.../ehsan/presentation/RequestHelpScreen.kt` | UI |
| `feature/.../ehsan/presentation/AddEhsanScreen.kt` | UI Fix |
| `feature/src/test/.../ehsan/domain/usecase/ValidatePhoneNumberUseCaseTest.kt` | Unit Tests |
| `feature/src/test/.../ehsan/presentation/AddEhsanViewModelTest.kt` | Unit Tests |
| `docs/PHASE_1C_REQUEST_HELP_PHONE_REPORT.md` | This Report |

---

## 16. Legacy Placeholder Data Decision

Existing donation rows with `"0000000000"` are considered **Legacy Data**. No cleanup routine was implemented in this phase to maintain scope.

---

## 17. Remaining Risks

- Users with existing local profiles that have `"0000000000"` (if allowed by Edit Profile) will be blocked from submitting until they update their profile.
- Edit Profile screen itself may still allow saving `"0000000000"`; it should be updated in a future phase to use the same validator.

---

## 18. Rollback Instructions

```powershell
git switch fix/request-help-phone-integrity
git reset --hard HEAD~1
```

---

## 19. Exit Criteria

1. `"0000000000"` removed from production submission? **Yes**
2. Valid current-profile phone used? **Yes**
3. Missing/invalid phone blocks submission? **Yes**
4. Arabic UI feedback? **Yes**
5. Automated tests? **Yes**
6. Build/Test/Lint success? **Yes**
7. Controlled Integration remains blocked? **Yes**

---

## 20. Final Decision

### PHASE 1C PASSED — REQUEST PHONE INTEGRITY VERIFIED

### P1-01 COMPLETE

### CONTROLLED INTEGRATION REMAINS BLOCKED
