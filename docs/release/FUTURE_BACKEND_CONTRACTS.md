# Future backend boundaries — no backend implemented

## R7-01 decision briefs — 2026-11-02

These five studies are **DEFERRED / NO-GO for implementation** until separately
commissioned. No provider, spending, data transfer, key, service, or rights approval
is implied. The product owner must appoint the specialist owners below and approve
a capped budget before a pilot. Unpriced vendor quotes are required; no cost is invented.

| Feature | Accuracy / go-no-go evidence | Privacy / retention / license | Operating owner and cost gate |
|---|---|---|---|
| Recitation voice evaluation | Qualified Quran reviewers define error categories and a consented benchmark across accents, reciters and recording conditions; measure false corrections and missed errors before any claim; never edit canonical text | Explicit microphone/upload consent; optional local-only evaluation study; no training or retention by default; deletion SLA and lawful consented audio/provider license required | Religious review + speech/ML + security owners; quote inference per minute, storage, evaluation and human review; reject unsupported accuracy claims |
| Quran assistant | Cite a reviewed edition and exact references; evaluate unsupported answers and refusal behavior on a scholar-reviewed set; no autonomous religious rulings | Licensed retrieval corpus and reviewer sign-off; no personal worship history in prompts by default; processor terms, region, minimal logs and deletion schedule before service | Religious/editorial + privacy + service owners; cap tokens, latency and review/support costs; fail closed on missing sources |
| Hajj / Umrah guide | A named specialist approves versioned references, jurisdictions and ritual instructions; review accessibility/offline journeys; no generated rulings or unverified emergency guidance | Prove text/image/map licenses and attribution; no pilgrim tracking; explicit permission and retention contract for any later location use | Qualified religious editor + product owner; fund translations, annual source updates, accessibility and specialist review; remain unavailable without reviewed material |
| Real community | Test server ownership, idempotency, pagination, conflicts, abuse, report delivery and account/media deletion on synthetic fixtures before rollout | Verified account identity distinct from local profile; opt-in linking; demo/legacy exclusion; contact disclosure controls; processor/region/retention policies; image validation | Moderation/safeguarding + security + on-call owners; staff queues, appeals, rate limits and incident response; quote auth/storage/egress/moderation/support |
| Real donations | Licensed payment provider and legal jurisdiction approved; test signed webhooks, replay prevention, reconciliation, refunds and audit; local listing completion is never payment proof | No card handling in this app; separate explicit financial consent and legally required retention; licensed provider and charitable entity contracts | Legal/finance + payment/security + support owners; approve fees, refunds, fraud losses and reconciliation staffing; no payment UI/service activation before sign-off |

A pilot requires written scope, named accountable owners, numerical evaluation
budgets derived from evidence, consent/deletion tests, security review, operational
runbooks, and an approved cost ceiling. Otherwise all five stay deferred. Existing
local Quran, learning and community records remain local; activating a future
backend must not silently upload them.

## Provider study

| Concern | Firebase | Laravel API |
|---|---|---|
| Mobile identity | Firebase Auth; server-verified UID required | Token/session identity with revocation and verified contact workflows |
| Community storage | Firestore rules enforce ownership and allowed transitions; pagination/index costs | SQL constraints and policies enforce ownership/transitions; explicit pagination |
| Media | Storage rules, MIME/size checks, server image validation | Signed object-storage upload URLs, validation jobs |
| Moderation / abuse | Trusted server functions plus queues and human review | Jobs/queues/policies plus human review |
| Operations | Managed services; cost/rule/emulator discipline | Hosting, backups, patching, observability owned by operator |
| Religious/media credentials | Backend secret storage, never APK | Server secret storage, never APK |

No provider selected. Choice requires budget, operator capacity, data residency/privacy review, moderation operations, and deployment ownership. Neither a device-local author ID nor a phone string is server authentication.

## Domain contracts

- Keep `EhsanRepository`, `UserRepository`, Quran repositories, prayer calculation, and preference owners at domain/data boundaries. Composables dispatch actions to ViewModels; they do not call HTTP services.
- `LocalProfile`: local opaque ID, display name, optional contact/city/bio/avatar. Cloud `Account`: server UID, verified-contact claims, consent/version, deletion state. Explicit migration/linking consent is required; never infer identity from display name.
- `Listing`: client-generated UUID, server ID (nullable until synchronized), owner UID, type OFFER/REQUEST, category, city/approximate location, description, ordered media references, timestamps, version/etag, moderation state, local lifecycle state. Local `COMPLETED` is not a verified delivery/payment.
- `ListingOrigin`: USER / DEMO / LEGACY. DEMO never enters sync/outbox, contacts, notifications to other people, analytics about real charity, or production totals. LEGACY requires explicit owner reconciliation; no name-based backfill.
- `ListingMedia`: stable key, local persisted URI, remote object key (after upload), MIME, dimensions, byte size, checksum, ownership, upload status. Album order must survive retries. Orphan cleanup must never delete referenced images.
- `ContributionEvent`: event ID, listing ID, actor UID, client operation ID, transition, device timestamp and server timestamp. Immutable server audit separate from editable display status. No financial claims without a financial system.
- `ContentEdition`: publisher, edition/version, language/script, license/attribution, canonical item ID, source URL, checksum, retrieved/verified/reviewed timestamps, reviewer, update policy. Source text is immutable; normalized search index is derived.
- `MediaResource`: publisher/video/reciter ID, source URL, platform kind, rights scope, availability status, verification time, duration if verified, official thumbnail, embed policy. Credentials remain server-side. HTTP reachability is not playback success.

## Future API operations

`GET /listings` uses cursor/category/city/type filters. `POST /listings` uses an idempotency key and validates ownership on the server. `PATCH /listings/{id}` requires version and authorization. Delete supports an auditable policy and media cleanup. `POST /media/uploads` returns narrowly scoped upload authorization. Reporting/blocking and moderation are required before real multiuser rollout. Auth, contact disclosure, consent, rate limits, and abuse prevention precede chat or coordination.

Local Room stays authoritative for offline UI; a future sync adapter can implement repository interfaces with outbox/inbox and conflict rules. Do not upload religious activity, phone/address/avatar, bookmarks, or device content without explicit product consent. Demo reset touches only DEMO data. No automatic upload on backend activation.

## Test seams

Repository contracts: authorization with same-name different-owner records, idempotent create/retry, pagination, optimistic conflicts, offline queue, demo exclusion, media failure cleanup, account deletion, migration preservation. Use server/emulator integration tests after a backend is actually implemented. Nothing in this document represents a deployed or secured service.
