# Content source register — final editions

**2026-10-17 update:** the asset-level inventory and current restrictions are in [EXTERNAL_ASSET_REGISTER.csv](EXTERNAL_ASSET_REGISTER.csv), [MEDIA_LINK_AUDIT.csv](MEDIA_LINK_AUDIT.csv), and [EXTERNAL_QA_RESULTS.md](EXTERNAL_QA_RESULTS.md). All 86 video titles/channels match fresh metadata, but the owner reported audible sound with no visible picture during the physical-phone batch. Metadata coverage below must not be read as visual playback acceptance or recording permission.

The definitive permission/attribution analysis is [CONTENT_SOURCES_AND_LICENSES](CONTENT_SOURCES_AND_LICENSES.md). This coverage register supersedes the initial five-excerpt/baseline Quran provenance audit; original audit evidence is retained separately.

| Dataset | Final coverage | Source / edition | State |
|---|---|---|---|
| Quran text |114surahs /6236ayahs |Tanzil Uthmani1.1, downloaded2026-10-14, verbatim XML |VERIFIED_SOURCE; exact-source unit test and existing-install Room repair pass |
| Page/juz anchors |604pages /30juz |Official Tanzil metadata XML v1.0 |Exact boundaries and complete verse partition verified |
| Quran structural metadata retained from original dataset |Surah display/English-name metadata |Original quran-json3.1.2 / Risan Bagja Pradana; CC-BY-SA4.0 notice retained in `licenses/quran-json-CC-BY-SA-4.0.txt` |Metadata attribution; Quran **text** now comes from Tanzil, not a claim that old Risan text is Tanzil |
| Hadith |3582publisher records; all9original fields |HadeethEnc Arabic v1.7.0; publisher update2025-11-12; update-check confirmed current2026-10-14 |VERIFIED_SOURCE; grade/takhrij are publisher values, not generated classifications |
| Duas |39readings; all7navigation sections |30whole Tanzil ayahs +9complete-context HadeethEnc records |VERIFIED_SOURCE, exact-source comparison; no invented timing/virtue/likes |
| Azkar |Existing13seed counters/readings; repaired known truncations and references |Tanzil2:255; precise Muslim591/2723b references where applicable |PARTIAL_REVIEW; broader verified sleep/other corpus and every virtue/count not certified complete |
| Asma |99existing entries |Editorial meanings' exact source not established |PENDING_SOURCE_REVIEW, disclosure in details |
| Tafsir |No licensed complete edition supplied |None approved |UNAVAILABLE; unverified sample removed from presentation; stored old values retained |
| Recitations |3reader selections; global player controls/state, local metadata retained |EveryAyah recording rights not established; future Quran Foundation provider |PENDING_LICENSE / PENDING_PROVIDER_CONFIGURATION; real recordings/downloads disabled |
| Technical player fixture |1neutral synthetic WAV,6seconds |Locally generated test signal, test APK only |Actual MediaSession play/pause/seek PASS; never a Quran content item |
| Learning media |86distinct videos:28Sanhya+29Nabi+29Fahm |Existing official YouTube source catalogs; official iframe/external view |All86metadata available; actual Android per-video results are in media report/manifest |
| Direct live HLS |2configured sources |SBA distribution rights not supplied |PENDING_LICENSE; HLS off; official channel iframe/external route used |
| Community |12deterministic fictional examples |Project-generated DEMO records; existing illustrations |DEMO, blank phones/no contacts; separate owner-bound USER and unknown LEGACY records |

No backend credentials, reciter permission, production signing key or religious-review approval has been fabricated. Rights, accessibility, source integrity, actual decoding and physical validation remain separate columns in the release decision.
