# Phase 1A Preservation Report — إحسان

**Date:** 2026-07-12 (local)  
**Phase:** 1A — Working Tree Preservation and Safe Branch Gate  
**Source content modified:** **No** (except this report as allowed documentation)

---

## 1. Executive Result

The Phase 0 verified dirty working tree was preserved without discarding or rewriting project source.

| Outcome | Value |
|---------|--------|
| WIP branch | `wip/phase0-preserved-baseline` |
| Preservation commit | **Yes** (see §9) |
| Patches | Binary-safe staged + unstaged |
| Untracked archive | `phase0-untracked-human-authored.zip` (5 files) |
| SHA-256 | Verified OK for all listed artifacts |
| Working-tree loss | **None detected** |
| Controlled Integration | **REMAINS BLOCKED** |

**Phase 1A decision:** `PHASE 1A PASSED — WORKING TREE SAFELY PRESERVED`  
**Source-fix decision:** `SOURCE FIXES MAY BEGIN` (on recommended strategy only)  
**Controlled Integration:** `CONTROLLED INTEGRATION REMAINS BLOCKED`

---

## 2. Pre-Preservation Git State

| Item | Value |
|------|--------|
| Date/time | 2026-07-12 13:43:09 (capture start) |
| Repository | `C:\Users\WIN 10\Desktop\New folder (2)\Sdk\Sdk\mol` |
| Branch before | `main` |
| HEAD before | `a5b0fb89843edfd0506aa5521a47bf045b140104` |
| Upstream | `origin/main` |
| Dirty | Yes |

### Path counts (pre-preservation)

| Metric | Count |
|--------|------:|
| Total `git status --short` lines | **151** |
| Group A (docs/ + ihsanplus/) | **66** |
| Group B (production outside Add-Only) | **85** |
| Untracked (`??`) | **5** human-authored (+ later local preservation artifacts) |
| Approx. `A` / `AM` / `M` / `D` / `R` | 64 / 17 / 65 / 0 / 0 (XY heuristic) |

Deleted / renamed: **0**.

---

## 3. Phase 0 Baseline Reference

| Phase 0 claim | Phase 1A observation |
|---------------|----------------------|
| Group A 64 / Group B 85 / Total 149 | Group A **66** / Group B **85** / Total **151** |
| Delta | **+2** documentation files from Phase 0 itself: `docs/PHASE_0_BASELINE_REPORT.md`, `docs/PHASE_0_FILE_INVENTORY.md` |
| Build SUCCESS | Not re-run (no source change required) |
| Tests / lint | Not re-run |
| Critical Room risk | Still present in working tree |
| Part 5 not started | Unchanged |
| Controlled Integration blocked | Unchanged |

Delta is expected and does **not** indicate loss of Phase 0 product changes.

---

## 4. Preservation Directory Location

```text
.project-preservation/phase-1a/
```

- Outside Android source packages (`app/`, `feature/`, `designsystem/`).
- **Not** listed in `.gitignore` at time of creation; treated as **local-only**.
- Must **not** be committed or pushed.
- Appears as untracked under `.project-preservation/` in `git status` (expected).

---

## 5. Patch Artifacts

| File | Size (bytes) | Contents |
|------|-------------:|----------|
| `phase0-working-tree.patch` | 539,156 | `git diff --binary` (unstaged tracked) |
| `phase0-staged.patch` | 2,378,842 | `git diff --cached --binary` (staged) |

Both files exist. Headers verified (`diff --git` / `GIT binary patch`).  
Staged patch is **non-empty** (81 staged paths including binaries).  
Patches were **not** applied; index was **not** altered by export.

Export method (Windows):

```text
cmd /c "git diff --binary > .project-preservation\phase-1a\phase0-working-tree.patch"
cmd /c "git diff --cached --binary > .project-preservation\phase-1a\phase0-staged.patch"
```

---

## 6. Untracked File Preservation

### Human-authored untracked (5)

| Path | Classification |
|------|----------------|
| `docs/PHASE_0_BASELINE_REPORT.md` | Documentation |
| `docs/PHASE_0_FILE_INVENTORY.md` | Documentation |
| `designsystem/.../IhsanEmptyState.kt` | Production source |
| `feature/.../DailyActivityIds.kt` | Production source |
| `feature/.../LiveStreamSources.kt` | Production source |

### Archive

- **Created:** `phase0-untracked-human-authored.zip` (~11,285 bytes)
- **Included:** the 5 paths above (relative paths preserved)
- **Excluded:** `.gradle/`, `**/build/`, `.idea/`, `local.properties`, APKs, logs, `.project-preservation/**`

Inventory files: `phase0-untracked-files.txt`, `phase0-untracked-classification.txt`.

---

## 7. SHA-256 Verification

File: `.project-preservation/phase-1a/SHA256SUMS.txt`

All listed artifacts re-hashed and verified **OK** after final inventory refresh (read-back match).

Do not hash Gradle `build/` outputs (none included).

---

## 8. WIP Branch Created

| Item | Value |
|------|--------|
| Preferred name | `wip/phase0-preserved-baseline` |
| Already existed? | **No** |
| Command | `git switch -c wip/phase0-preserved-baseline` |
| Branch after | `wip/phase0-preserved-baseline` |
| HEAD after switch (pre-commit) | `a5b0fb89843edfd0506aa5521a47bf045b140104` (same as main) |
| Working tree after switch | Still dirty — changes preserved |

---

## 9. Preservation Commit Decision

### Conditions checklist

| Condition | Met? |
|-----------|------|
| Human-authored untracked inventoried | Yes |
| Generated/local excluded from commit | Yes (`.project-preservation/` excluded) |
| No secrets/keystores/`local.properties` | Yes (scan of status paths) |
| Patches + pre-state stored | Yes |
| Message states unreviewed WIP snapshot | Yes |
| Semantic content unchanged | Yes |

**Decision: create one WIP preservation commit** (allowed by Phase 1A Step 8).

Commit message:

```text
chore(wip): preserve Phase 0 verified working tree

- BUILD SUCCESSFUL at Phase 0
- Tests passed
- Lint passed with 78 warnings and 0 errors
- Contains pre-existing production changes outside Add-Only
- Not approved for release
- Controlled Integration remains blocked
```

**Not pushed** to remote.

---

## 10. Post-Preservation Integrity Verification

| Check | Result |
|-------|--------|
| Branch | `wip/phase0-preserved-baseline` |
| HEAD (preservation commit) | `223c540f25a9edc11f32afa4067715a69decd128` |
| Parent HEAD (former main tip) | `a5b0fb89843edfd0506aa5521a47bf045b140104` |
| Files in commit | 152 paths; 6433 insertions / 539 deletions |
| Working tree after commit | Clean of product files; only `?? .project-preservation/` remains |
| Preservation patches on disk | Present (unchanged by commit) |
| `.project-preservation/` committed? | **No** |
| Source semantic loss | **None** — WIP tree captured in commit; patches remain as dual backup |

`git status -sb` after commit:

```text
## wip/phase0-preserved-baseline
?? .project-preservation/
```

---

## 11. Files Excluded for Safety

Excluded from Git commit and from untracked zip:

- `.project-preservation/**` (local preservation store)
- `.gradle/`, `**/build/`, `.idea/`
- `local.properties`
- APK / intermediate build outputs
- Temporary logs

---

## 12. Secrets and Local-File Check

| Check | Result |
|-------|--------|
| `local.properties` in commit? | No |
| Keystore / `*.jks` / `google-services.json`? | Not in status list |
| Hardcoded API secrets in paths? | No new secret files observed |
| Public HLS/YouTube constants in `LiveStreamSources.kt` | Public URLs only (not credentials) |

---

## 13. Recommended Phase 1 Branch Strategy

### Selected recommendation: **Strategy A — Fix from preservation snapshot**

Create (later, not in Phase 1A):

```text
fix/room-migration-safety
```

from the WIP preservation commit on `wip/phase0-preserved-baseline`.

### Why A (not B or C first)

- Phase 0 build already depends on the **full** Group B production WIP (nav, Media3 HLS, tasbih, Quran audio, DB version bump). Separating all 85 paths before P1-05 is slower and riskier for “known green” state.
- A preservation commit gives a **named rollback point** for Strategy A.
- Strategy C (logical commits) remains valuable **after** P1-05 or in parallel review, not as a blocker to starting Room safety.
- Strategy B (fix on dirty WIP without commit) is weaker now that a snapshot commit is available.

**Do not execute** `fix/room-migration-safety` in Phase 1A.

---

## 14. Rollback and Recovery Instructions

**Do not apply patches onto an unclean tree without creating another backup first.**

### Recover unstaged tracked changes

```powershell
git switch wip/phase0-preserved-baseline
# ensure clean or backed-up tree first
git apply --check .project-preservation/phase-1a/phase0-working-tree.patch
git apply .project-preservation/phase-1a/phase0-working-tree.patch
```

### Recover staged tracked changes

```powershell
git apply --check .project-preservation/phase-1a/phase0-staged.patch
git apply .project-preservation/phase-1a/phase0-staged.patch
# then re-stage intentionally if needed:
# git add -A -- <paths>
```

### Recover human-authored untracked files

```powershell
Expand-Archive -Path .project-preservation/phase-1a/phase0-untracked-human-authored.zip -DestinationPath . -Force
```

(Or extract with Explorer / `tar` preserving relative paths.)

### Recover WIP branch / commit

```powershell
git switch wip/phase0-preserved-baseline
git rev-parse HEAD   # preservation commit hash
git log -1
```

### Verify artifact integrity

```powershell
Get-Content .project-preservation/phase-1a/SHA256SUMS.txt | ForEach-Object {
  # compare Get-FileHash for each listed file
}
```

---

## 15. Phase 1A Exit Criteria

| # | Criterion | Met? |
|---|-----------|------|
| 1 | Pre-preservation state recorded | Yes |
| 2 | Binary-safe tracked patches | Yes |
| 3 | Untracked human files inventoried + archived | Yes |
| 4 | SHA-256 generated + verified | Yes |
| 5 | Dedicated WIP branch created safely | Yes |
| 6 | No current source change lost | Yes |
| 7 | No generated/local secret committed | Yes |
| 8 | Phase 1 strategy recommended | Yes (A) |
| 9 | This report created | Yes |
| 10 | Controlled Integration remains blocked | Yes |

---

## 16. Final Decision

### PHASE 1A PASSED — WORKING TREE SAFELY PRESERVED

### SOURCE FIXES MAY BEGIN

(Only Room/safety Phase 1 tasks on a branch derived from the preservation commit; not Controlled Integration.)

### CONTROLLED INTEGRATION REMAINS BLOCKED

---

## Appendix — Post-Commit Fields

| Field | Value |
|-------|--------|
| Branch before | `main` |
| Branch after | `wip/phase0-preserved-baseline` |
| HEAD before | `a5b0fb89843edfd0506aa5521a47bf045b140104` |
| HEAD after | `223c540f25a9edc11f32afa4067715a69decd128` |
| Preservation commit | `223c540f25a9edc11f32afa4067715a69decd128` |
| `git status -sb` after | `## wip/phase0-preserved-baseline` + `?? .project-preservation/` |
| Remote push | **Not performed** |
