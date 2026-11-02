# Runtime Bottom Navigation Implementation Report

## Summary

Root bottom navigation uses an explicit allowlist resolver (`BottomBarDestination` /
`resolveBottomBarDestination`). Selection is a per-item green pill with a white icon
(no empty white circle / sliding absolute-offset halo).

## Route visibility policy (authoritative)

| Destination | Bottom Navigation |
|-------------|-------------------|
| **Home root** (`home_screen`) | **Visible** — Home selected |
| **Ehsan root** (`donations_screen`) | **Visible** — Ehsan selected |
| **Profile root** (`profile_screen`) | **Visible** — Profile selected |
| Child / Detail routes | **Hidden** |
| Unknown routes | **Hidden** — **no fallback to Home** |

## Visual behavior

- Selected: deep-green pill behind icon; white filled icon; dark label.
- Unselected: outline icon + muted label.
- Exactly one selected item when a root is active.
- Compact bar height (~70dp) with horizontal margins (~14dp).
- Ehsan list content uses Scaffold FAB/`contentPadding` so filters and actions clear
  the FAB and the outer MainScreen bottom-bar insets (no fixed 88dp spacer).

## AppNavHost note

Earlier drafts claimed AppNavHost was untouched. That is no longer accurate for the
Home services restore path:

- **Included with Home UI commit:** `onNavigateToStatistics` only
  (restores the Statistics service entry from Home).
- **Not included in navigation-ui / home-ui commits:** `onClearError` (Quran reader)
  remains uncommitted for a later Quran/Audio commit.

## Related commits on this branch (split)

1. `fix(database): migrate Ihsan database to version 6`
2. `fix(navigation-ui): restore compact root bottom navigation`
3. `refactor(home-ui): compact dashboard while preserving services`
4. `fix(splash): prevent logo cropping`
5. `docs(navigation-ui): document root bottom navigation policy` (this file)

## Out of scope for these commits

- Quran / Audio / Auth / Connectivity / Workers
- Runtime screenshots, logcat dumps, helper scripts
- Agent prompts and `.artifacts`
