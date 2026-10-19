# Future backend boundaries — no backend implemented

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
