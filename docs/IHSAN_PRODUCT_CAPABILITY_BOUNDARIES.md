# Ihsan Product Capability Boundaries

## Core production capabilities

* Local Quran reading, audio, and download
* Local prayer calculation and notifications (Part 1 facade)
* Local Qibla
* Local Azkar / Tasbih
* Local Dua / Hadith / Asma content
* Local profile (display name / preferences — not secure authentication)
* Local charity / request board (Ehsan listings)
* Local statistics and preferences
* Public live streams

## Capabilities not currently present

* Server account
* OTP authentication
* Cross-device synchronization
* Charity verification backend
* Beneficiary approval workflow
* Payment processing
* Donation settlement
* Remote moderation
* Messaging / Inbox
* Cloud backup of sensitive profile data
* Multi-device conflict resolution
* Backend-verified charity trust layer

## Release wording rules

The UI must **not** imply:

* Verified charity cases
* Secure authentication
* Cloud synchronization
* Guaranteed donation delivery
* Platform-controlled payments
* Official beneficiary validation

Local profile wording must remain honest: this is on-device preference storage, not an authenticated account.

Charity board wording must remain local-only unless a real verification backend ships.

IhsanPlus demo / trust scaffolding must stay unreachable in production until Stage 4 approval.
