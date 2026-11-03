package com.example.feature.quran.domain.model

/**
 * Domain representation of target positions when opening the Quran Reader.
 * Enforces a strict priority hierarchy:
 * 1. Explicit navigation target (surah/ayah) from current action
 * 2. Explicit bookmark target
 * 3. Explicit reading plan / continue reading target
 * 4. Last saved reading position
 * 5. Default fallback (Surah 1, Ayah 1)
 */
sealed interface QuranTarget {
    val surahId: Int
    val ayahNumber: Int

    data class Explicit(
        override val surahId: Int,
        override val ayahNumber: Int = 1
    ) : QuranTarget

    data class Bookmark(
        override val surahId: Int,
        override val ayahNumber: Int
    ) : QuranTarget

    data class ReadingJourney(
        override val surahId: Int,
        override val ayahNumber: Int
    ) : QuranTarget

    data class LastRead(
        override val surahId: Int,
        override val ayahNumber: Int
    ) : QuranTarget

    data object DefaultFallback : QuranTarget {
        override val surahId: Int = 1
        override val ayahNumber: Int = 1
    }
}

/**
 * Resolver that applies the strict priority hierarchy for opening the Quran.
 */
object QuranTargetResolver {
    /**
     * Generates a stable request identity for a navigation request.
     * Explicit requests carry their explicit surah/ayah identity; implicit ("continue")
     * requests resolve to a shared implicit identity so that a restored implicit session
     * keeps its scrolled page, while any new explicit request starts a fresh landing.
     */
    fun requestIdentity(explicitSurahId: Int?, explicitAyahNumber: Int?): String {
        val surah = explicitSurahId?.takeIf { it > 0 } ?: 0
        val ayah = if (surah > 0) (explicitAyahNumber?.takeIf { it > 0 } ?: 1) else 0
        return if (surah > 0) "explicit:$surah:$ayah" else "implicit"
    }

    fun resolve(
        explicitSurahId: Int?,
        explicitAyahNumber: Int?,
        bookmarkTarget: Pair<Int, Int>? = null,
        journeyTarget: Pair<Int, Int>? = null,
        lastReadTarget: Pair<Int, Int>? = null
    ): QuranTarget {
        // Priority 1: Explicit navigation target
        if (explicitSurahId != null && explicitSurahId > 0) {
            val ayah = if (explicitAyahNumber != null && explicitAyahNumber > 0) explicitAyahNumber else 1
            return QuranTarget.Explicit(explicitSurahId, ayah)
        }

        // Priority 2: Bookmark target
        if (bookmarkTarget != null && bookmarkTarget.first > 0 && bookmarkTarget.second > 0) {
            return QuranTarget.Bookmark(bookmarkTarget.first, bookmarkTarget.second)
        }

        // Priority 3: Reading plan / journey target
        if (journeyTarget != null && journeyTarget.first > 0 && journeyTarget.second > 0) {
            return QuranTarget.ReadingJourney(journeyTarget.first, journeyTarget.second)
        }

        // Priority 4: Last saved reading position
        if (lastReadTarget != null && lastReadTarget.first > 0 && lastReadTarget.second > 0) {
            return QuranTarget.LastRead(lastReadTarget.first, lastReadTarget.second)
        }

        // Priority 5: Fallback
        return QuranTarget.DefaultFallback
    }
}
