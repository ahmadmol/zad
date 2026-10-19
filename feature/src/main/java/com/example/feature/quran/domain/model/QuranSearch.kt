package com.example.feature.quran.domain.model

import com.example.feature.core.search.ArabicSearch

/** Matches chapter names without changing the source text or treating سورة as verse text. */
object QuranSearch {
    /** A second search key restores dagger alifs such as عَٰلَمِين; source text stays immutable. */
    fun expandedVerseKey(text: String): String = ArabicSearch.key(text.replace('\u0670', 'ا'))

    fun matchesSurah(name: String, query: String): Boolean {
        val words = ArabicSearch.key(query).split(' ').filter(String::isNotBlank)
        val nameWords = words.dropWhile { it == "سورة" || it == "سوره" }
        return nameWords.isNotEmpty() && ArabicSearch.matches(name, nameWords.joinToString(" "))
    }
}
