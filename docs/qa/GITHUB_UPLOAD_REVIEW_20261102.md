# GitHub upload review — 2026-11-02

## Scope and history

The owner authorized reviewing, reorganizing and publishing the local work in
four commits. The original tip is `b683996ecf5d06540d71c7cffd2156933efe37aa`.
The original branches and all their commits remain local; a verified all-refs
bundle and file hashes are retained under the ignored
`.project-preservation/github-upload-20261102/` directory.

The upload branch is `codex/reviewed-github-upload-20261102`, based on
`origin/main` at `a5b0fb89843edfd0506aa5521a47bf045b140104`.
It imports the reviewed final file contents without merging the old feature
branch history. This includes the accumulated application changes relative to
main, so the resulting source matches the original final tip.

The existing remote `fix/audio-runtime-adhan-quran` history contains previously
documented personal-data exposure. It is not an ancestor of the upload branch.
This upload does not clean that remote history, change main, or perform a force
push. The historical no-push statements in older QA reports describe their
original sessions; this review records the owner's later upload authorization.

## Four commit groups

1. Repository privacy, build configuration, CI and maintenance scripts.
2. Application features, UI, assets, database schemas and recovery behavior.
3. Unit and instrumented regression tests and synthetic test resources.
4. Planning, QA, privacy, licensing and handoff documentation.

## Exclusions and preservation

Private QA captures, UI dumps, runtime logs, extracted databases and sidecars,
APK/AAB outputs, signing keys, local settings, environment secrets, caches and
preservation bundles remain ignored and local. The runtime scratch helper
`_runtime_qa_helper.py` and the one-off agent prompt
`IHSAN_MOL_BOTTOM_NAVIGATION_FIX_AGENT_PROMPT.md` are also excluded from the new
branch, while their local contents and original commits are preserved.
Required application assets, Gradle wrapper and synthetic test fixtures remain.

## Verification

- All seven main-history commit trees and the final source tree were scanned
  for secret patterns, SQLite headers and known profile identity values without
  disclosing those values. No findings were reported.
- Gitleaks 8.30.1 was obtained from its official release and SHA-256 verified.
  Its main-history scan reported two IDE approval-map entries containing UUIDs;
  inspection confirmed metadata false positives rather than credentials.
- The source privacy gate passed against the original final index with the
  local known-profile database; its five regression tests passed.
- All four new commit trees passed the identity/secret and private-artifact
  checks. Gitleaks scanned their outgoing history (22.92 MB) with no findings.
- The Android unit-test and debug lint Gradle tasks completed successfully.
  The test reports contain 673 tests with zero failures/errors/skips; debug lint
  has zero errors and 63 existing warnings. The five privacy tests also passed.
- A hash comparison covered 15,035 original files: no missing files and no
  unexpected source/private-data changes. Only the three intentional repository
  policy changes and a regenerated Python bytecode cache differed. Both excluded
  scratch files still exist locally. Git connectivity validation passed.
- Existing whitespace in imported source and third-party license texts was
  preserved. Automated scans cannot certify image pixels or copies of data
  already published elsewhere.

Existing Android QA evidence is retained as historical evidence. Fresh local
verification results for this upload are recorded separately; physical-device
acceptance remains pending as described in the handoff.
