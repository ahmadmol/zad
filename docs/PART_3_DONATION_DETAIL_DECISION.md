# Part 3 — Donation Details Decision

**Date:** 2026-07-18  
**Branch:** `refactor/product-surface-ownership-cleanup`

---

## Canonical surface

**Keep `IhsanDetails`** (`ihsan_details/{id}` → `IhsanDetailsScreen` / `IhsanDetailsViewModel`)

---

## Evidence

| Criterion | DonationDetail | IhsanDetails |
|-----------|----------------|--------------|
| Production callers | None | Ehsan list `onDonationClick` |
| Phone dial | No-op | `ACTION_DIAL` |
| WhatsApp | No-op | Works with stored phone |
| Missing ID | Weak blank UI | Explicit error message |
| Trust copy | **"عضو موثق"** (misleading) | **"صاحب الفرصة"** (honest) |
| Data path | `GetDonationByIdUseCase` | `EhsanRepository.getDonationById` (same DB) |

---

## Consolidation

- All production navigation uses `Screen.IhsanDetails`.
- `DonationDetailScreen` / `DonationDetailViewModel` removed.
- Legacy route `donation_detail_screen/{id}` retained as **`Screen.LegacyDonationDetail`** redirect-only (no obsolete UI).
- No IhsanPlus trust/verification badges introduced.

---

## Rollback

Restore DonationDetail files from git, re-register ViewModel, and restore the old composable if needed. Prefer keeping the redirect until deep-link compatibility is confirmed unused.
