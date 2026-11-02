# IHSAN — Pre-backend implementation campaign

## Final closure — 2026-11-02

The final mission supersedes the historical no-commit/no-read-only-device rules
below within its explicit scope. Sprint01 and Batch01–05 are preserved on
`codex/final-prebackend-closure-20261102`. Current findings, artifact identity,
40-task disposition and70-screen acceptance are in
[the final report](../qa/IHSAN_FINAL_PREBACKEND_COMPLETION_REPORT.md);
resume from [the final handoff](IHSAN_FINAL_HANDOFF.md), not a new campaign.

Final closure fixes reading-journey read gating/deletion failure handling and
Quran quick-jump restoration/asset errors/keyboard sizing. Production Release
now rejects missing signing credentials while explicit QA keeps its separate
identity. R7-01 documentation-only studies are prepared; future services stay
deferred. Room remains11. The final report contains current test counts rather
than carrying forward the historical619 result as a new run.

Private raw QA artifacts are removed from current tracking with their originals
preserved; current candidates are scanned. Published GitHub history is still
contaminated: no push, history rewrite or repository privacy change. Read-only
Xiaomi preflight was performed; no install or physical acceptance was performed.
Source/UI/local whole-product completion remains PARTIAL; device and production
acceptance remain pending/blocked. The following sections are historical checkpoints.

2026-10-21 — latest continuation: **Batch 05 functional recovery/polish and D07 RemoteViews widget implemented; physical acceptance pending**. Retain all Sprint 01 and Batch 01/02/03/04/05 working-tree changes. Do not restart the campaign.

Current Room target: **11**, additive 10→11. D-03 approved by the user for the fasting scope (explicit fasted/not-fasted, optional personal type, isolated confirmed deletion, no sign-out deletion). F6-01/02 are locally implemented. F6-03 source now includes Fahm and Nabi/Sanhya manual progress/resume/restoration/error handling; unavailable content mappings and physical acceptance remain separate. Batch04/05 add no schema or religious content. D07 is approved; Batch05 activates the existing RemoteViews provider using the canonical facade, with safe prayer/reader actions. See [Batch05 QA](../qa/IHSAN_PREBACKEND_BATCH05_20261021.md). See [Batch04 QA](../qa/IHSAN_PREBACKEND_BATCH04_20261021.md). See [Batch 03 QA](../qa/IHSAN_PREBACKEND_BATCH03_20261021.md). Matrix now has 70 cases: previous69 preserved plus the approved launcher-widget surface. Full product UI/LOCAL completion remains NO.

Historical Batch 02 checkpoint adds F5-04/05 memorization/manual review, F5-02/03 monthly Hijri calendar and F4-05 monthly prayer/Home summaries. Room target at that checkpoint was **10**, additive from 9. D-09 is approved for -2..+2 days/default 0/civil midnight; events intentionally empty pending sourced review. See [Batch 02 QA](../qa/IHSAN_PREBACKEND_BATCH02_20261020.md). Full product UI/LOCAL completion remains NO.

The following table retains the historical Batch 01 boundary. Current states are in the ledger and Batch 02 report.

The authoritative task ledger is [IHSAN_PREBACKEND_CAMPAIGN_PROGRESS.csv](IHSAN_PREBACKEND_CAMPAIGN_PROGRESS.csv). It retains all **40** original IDs and distinguishes source work from acceptance. The original master plan and screen matrix remain the scope references.

| Capability | Status after batch 01 |
|---|---|
| Sprint 01 source and existing local Quran/prayer/azkar/learning services | COMPLETED for previously delivered source; physical acceptance remains separate |
| Reading plans, page confirmation, private notes/export and manual prayer journal | PARTIAL: functional source implemented; remaining acceptance listed in task ledger |
| Full-screen accessibility/visual acceptance | PARTIAL |
| Memorization/self-review F5-04/05 | NOT IMPLEMENTED |
| Hijri calendar F5-02/03; fasting/Ramadan F6-01/02 | NOT IMPLEMENTED |
| Community local forms/images/ownership and saved learning | PARTIAL: existing functionality retained, further campaign work remains |
| Verified phone authentication, public listing publication, report delivery, real moderation, cross-device sync and transactions | BACKEND DEPENDENT: no new backend/provider/server implemented |

## Product completion labels

- UI COMPLETE: **NO**.
- LOCAL FUNCTIONALITY COMPLETE: **NO**.
- BACKEND INTEGRATION: **PENDING**, intentionally outside this campaign's implementation scope.
- PHYSICAL QA: **PENDING**. No emulator, APK installation or phone data operation is authorized by this campaign.

## Exact continuation

1. Read the latest batch-05 QA report and the 40-ID ledger; retain all uncommitted Sprint 01 and batch 01/02/03/04/05 changes.
2. Do not recreate F4/F5 campaign work or F6-01/02. Continue the existing Fahm implementation rather than creating a learning library. Their local source/JVM work is implemented; physical tests remain separate. Use the memorization screen and existing reader/repeat controls.
3. Final integration closure follows Batch05, not a new broad audit. F4-06 source is implemented under approved D07; do not recreate the widget/provider/calculator. Run the remaining same-artifact G0-03/04/05/06 and F6-04/UX physical cases only after separate Xiaomi installation authorization. D01 media acceptance, D02 production identity and D10 rights remain owner/external gates. PS-01 device baseline and D08 budgets precede final PS-02 performance acceptance. Host metrics are not device acceptance. Keep unavailable content/events explicit rather than generating mappings. Continue any reproducible source defect with targeted regression; do not repeat Batch01–05 implementations.
4. Database current target is **11**, with compiler-exported schemas 9/10/11 preserved. The new migration is 10→11; historical steps are unchanged. Any subsequent schema change starts from actual current state and preserves historical schemas. No destructive fallback, downgrade or data cleanup.
5. Use the existing QuranGoalCalculator/manual prayer repository/ArabicSearch. The reader's last-read percentage is a resume-position measure, never manual completion. Private notes are not tafsir.
6. Physical acceptance requires the Xiaomi and separate installation authorization; source tests do not close G0-03/04/05/06 or R7-03.

No commit, push, backend or new dependency/toolchain upgrade has been performed.

## Historical Batch 04 guard — continue from Batch 05 above

- Continue from `docs/qa/IHSAN_PREBACKEND_BATCH04_20261021.md` and the existing working tree. Full JVM ran once (594 tests); final targeted regression and exact APK evidence are in that report.
- Room stays **11**; schemas9/10/11 and previous migrations are unchanged. New bookmark projection/transaction queries require no migration.
- Existing learning favorite keys remain unchanged; new Nabi/Sanhya metadata uses `learning_nabi_progress_v1` and `learning_sanhya_progress_v1` in UserPreferences. Sign-out preserves them and community/worship records.
- UI COMPLETE **NO**; LOCAL FUNCTIONALITY COMPLETE **NO**; BACKEND INTEGRATION **PENDING**; PHYSICAL QA **PENDING**. Counts alone do not close visual/device/content/product gates.

## Batch 05 final functional checkpoint — 2026-10-21

- Validation: Full JVM once **619 PASS**, final targeted **65 PASS**, Lint **0 errors / 114 warnings** (four fewer than Batch04; no new diagnostics), Release QA and Android-test compilation **PASS**. Package `com.example.mol.referenceqa` uses the existing QA certificate; exact APK/SHA and all75 changed/new paths are in the Batch05 QA report. These are source/build results, not phone acceptance.
- Current Room remains **11**; no new migration or dependency/toolchain upgrade. Historical schemas and worship/community/learning keys are preserved.
- D-07 **APPROVED**. Existing PrayerWidgetProvider, widget_prayer layout and factory are active; next prayer comes from PrayerTimesFacade. No second calculator, history display, network polling or foreground service. Native countdown is approximate, last-updated is visible, and boundary/midnight refresh is inexact. Prayer alarm scheduling classes remain unchanged.
- Non-learning search/read/write retries, shared form CTA sizing/bidi, atomic favorites, clock-at-edit DataStore updates, Quran validation/IO transaction, root Settings/Stats/Home recovery and shared media callback cleanup are implemented. Source tests and exact artifacts are in Batch05 QA.
- Final closure: (1) select internal QA versus production identity under D02 and resolve D01/D10 media/rights scope; (2) separately authorize Xiaomi installation of a specific SHA; (3) execute the70-case/integrated data/media/widget/accessibility matrix; (4) measure device performance, approve D08 budgets and fix only measured remaining defects; (5) re-evaluate completion labels from those results. No permission is inferred for phone operations or publishing.
- UI COMPLETE **NO**; LOCAL FUNCTIONALITY COMPLETE **NO**; BACKEND INTEGRATION **PENDING**; PHYSICAL QA **PENDING**. Core source delivery is not the same as complete device acceptance, content rights or all40 task closure.
