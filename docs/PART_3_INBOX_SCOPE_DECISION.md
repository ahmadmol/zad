# Part 3 — Inbox Scope Decision

**Date:** 2026-07-18  
**Branch:** `refactor/product-surface-ownership-cleanup`

---

## Decision

Inbox is not implemented.  
No user data or workflow depends on it.  
The route is removed from production navigation definitions until a real messaging contract exists.

---

## Evidence

| Artifact | Status |
|----------|--------|
| `Screen.Inbox` (`inbox_screen`) | Removed |
| Composable / screen | Never existed |
| Message entity / DAO / repository | None |
| Notification → inbox | None |
| Navigation callers | None |
| Bottom nav | Never included Inbox |

---

## Rules

- Do not add an empty placeholder Inbox screen.
- Do not wire IhsanPlus messaging fakes as Inbox.
- Reintroduce only with a real repository contract, UI, and product workflow.
