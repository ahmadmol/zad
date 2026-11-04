package com.example.feature.quran.util

/**
 * Sources reviewed for verse-accurate recitation. None is enabled: this build has no
 * recording grant, and a successful HTTP response is not treated as proof of verse alignment.
 */
object RecitationSourceCatalog {
    data class Candidate(
        val name: String,
        val documentationUrl: String,
        val verseAddressing: String,
        val decision: String
    )

    val reviewed: List<Candidate> = listOf(
        Candidate(
            name = "Quran.com API v4",
            documentationUrl = "https://api-docs.quran.foundation/docs/quickstart",
            verseAddressing = "chapter_number and verse_number on recitations/{reciter_id}/by_ayah/{verse_key}",
            decision = "Official documented API. Audio files are copyrighted; in-app playback needs a per-reciter grant and owner approval. The app may only open the public verse page externally."
        ),
        Candidate(
            name = "EveryAyah",
            documentationUrl = "https://everyayah.com/data",
            verseAddressing = "Static MP3 paths of the form {reciter}/{sss}{aaa}.mp3",
            decision = "Predictable verse keys, but redistribution terms are per reciter and are not granted for this beta."
        ),
        Candidate(
            name = "mp3quran.net",
            documentationUrl = "https://mp3quran.net/api/v3",
            verseAddressing = "Surah-level MP3 files, not independent ayah files",
            decision = "Rejected for ayah playback because a surah file cannot prove which ayah is sounding."
        )
    )
}
