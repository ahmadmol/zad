# Content Rating Questionnaire — Prepared Answers

**App:** إحسان (Ihsan) · **Category:** Lifestyle (secondary: Books & Reference)
**Status:** prepared answers for the Play Console IARC questionnaire.
**Not yet submitted** — submission requires a Play Console account and the final
`applicationId` (see `docs/APPLICATION_ID_DEFERRED_DECISION.md`).

---

## 1. App category selection

**Reference / Lifestyle — not a game.**

Ihsan is a local-first Muslim daily companion: prayer times, Qibla, Quran text and audio,
Azkar, Tasbih, Duas, Hadith, and a local community help board.

---

## 2. Questionnaire answers

| Question area | Answer | Justification |
|---|---|---|
| Violence (realistic / fantasy / cartoon) | **No** | No violent content of any kind. |
| Sexuality / nudity | **No** | None. |
| Profanity or crude humour | **No** | Religious and reference text only. |
| Controlled substances (drugs, alcohol, tobacco) | **No** | None. |
| Gambling / simulated gambling | **No** | No gambling mechanics, no loot boxes, no virtual currency. |
| Horror / fear content | **No** | None. |
| User-generated content shared between users | **No — see §3** | Listings are stored on the device only and are never transmitted to other users or to a server. |
| Users can interact / communicate | **No in-app messaging** | The app has no messaging system. Contact happens by handing a phone number to the device's own dialer or WhatsApp, outside the app. |
| Shares user location with other users | **No** | Location is used locally for prayer times and Qibla. Listing location is a user-typed city, not coordinates, and is not transmitted. |
| Allows purchase of digital goods | **No** | No in-app purchases, no payment processing. |
| Contains ads | **No** | No advertising SDK. |
| Digital purchases / real money | **No** | Charity coordination is entirely offline between neighbours; the app never handles money. |

---

## 3. Note on the "user-generated content" question

This deserves an explicit answer rather than a bare "No", because the app does contain a
listing board.

* Listings are written to the device's local Room database.
* There is **no backend, no sync, and no network transmission** of listing content.
* A listing is visible only on the device that created it.
* Therefore there is no moderation surface, no reporting-to-server flow, and no exposure
  of one user's content to another.

If a future release adds a real backend that shares listings between devices, this answer
**must be revisited** and a moderation/reporting policy added before that release ships.

---

## 4. Religious content note

The app contains Quran text, Hadith, and Islamic supplications. This is reference
religious content and is not a rating factor in IARC, but it is stated here so the
reviewer has accurate context.

---

## 5. Expected outcome

Everyone / PEGI 3 / USK 0 equivalent, subject to IARC's own determination.

---

## 6. Blocking prerequisites

1. **PRODUCT OWNER DECISION REQUIRED** — production `applicationId`.
2. Play Console account and developer verification.
3. Public privacy-policy URL hosting `docs/store/PRIVACY_POLICY.md`.
