# Community demo manifest — 2026-10-14

The beta is device-local. A saved phone string is not verified ownership, cloud authentication, an online user or a delivered donation.

`CommunityDemoManager` supplies **12 deterministic examples**: six OFFER and six REQUEST records, six categories, varied cities and AVAILABLE/PENDING/COMPLETED local states. IDs occupy the reserved negative range beginning -8400001. Every row is `origin=DEMO`, with a visible `[DEMO]` title/description and a blank phone number. Images are existing project illustrations. Contact is unavailable for these fictional rows.

Opening the community list ensures examples exist without duplicating them. The community reset action requires confirmation and transactionally removes/recreates **only DEMO** rows. USER records, local profiles, bookmarks, preferences and counters are not reset. The same-name/different-owner Android test exercises repeated reset with personal records present.

Real local creates receive `origin=USER` and `ownerId=local-profile:<Room user ID>` from the repository, never from editable form fields. Only the active local profile may manage its records. Reopening the same local profile retains its stable ID. This is an offline beta boundary, not a security substitute for server authentication.

Schema6→7 preserves all legacy rows and media references. Unknown old authors remain LEGACY with no inferred owner. Only three exact known bundled sample fingerprints are reclassified/neutralized; the former title/description heuristic deletion has been removed. The migration test includes an arbitrary legacy title `تت`, description `عع`, image reference, counters and `prayer_log` to prove they survive.

Deleting one listing protects files still referenced by another listing and does not run blanket orphan deletion over the shared avatar directory. Account-wide cleanup has a separate contract. No demo row is eligible for future automatic cloud sync.
