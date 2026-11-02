# Gate3 — local community and account

Implementation: schema7 introduces stable local profile ownership and DEMO/USER/LEGACY origins, with a tested non-destructive6→7migration. Local registration/opening/reopening retains profile ID. This is not server authentication or verified phone ownership.

The repository overrides client-supplied owner/origin on create and enforces stored ownership on edit/status/delete. Same display name cannot grant ownership. Management/history use actual owned rows; editing, status changes and confirmed delete are wired. Exact legacy sample fingerprints lose plausible contact numbers and become DEMO. Arbitrary legacy rows, image references, counters and `prayer_log` survive; title/description heuristic deletion was removed.

12fictional examples cover both listing types, varied categories/cities and3local states. Demo reset confirms and transactionally touches onlyDEMO. Individual image deletion protects shared album references and does not blanket-clean the avatar directory. No backend, live users, payments or verified deliveries were invented.

[Real Room tests](../../.artifacts/release-final-20261014/gate4-room-player-final.log): owner-safe create/edit/status/delete, logout/reopen, same-name denial, repeated reset preserving USER records, and exact old-schema migration all PASS. Existing photo-store/album/draft tests are tracked in the final Android report. Actual management UI journey and screenshots are recorded separately in RELEASE_CANDIDATE_REPORT; repository tests alone do not prove every form gesture.

See [demo manifest](DEMO_DATA_MANIFEST.md) and [future backend boundaries](FUTURE_BACKEND_CONTRACTS.md). Personal/global preference presentation is not advertised as secure multi-account isolation. Backend authentication/moderation and consent-based sync remain future work.
