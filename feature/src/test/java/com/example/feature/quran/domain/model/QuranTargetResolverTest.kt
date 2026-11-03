package com.example.feature.quran.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranTargetResolverTest {

    @Test
    fun `explicit navigation target takes precedence over bookmark and last read`() {
        val target = QuranTargetResolver.resolve(
            explicitSurahId = 4,
            explicitAyahNumber = 50,
            bookmarkTarget = 2 to 255,
            journeyTarget = 3 to 100,
            lastReadTarget = 1 to 7
        )

        assertTrue(target is QuranTarget.Explicit)
        assertEquals(4, target.surahId)
        assertEquals(50, target.ayahNumber)
    }

    @Test
    fun `explicit surah without ayah defaults to ayah 1 of that surah`() {
        val target = QuranTargetResolver.resolve(
            explicitSurahId = 18,
            explicitAyahNumber = null,
            lastReadTarget = 2 to 10
        )

        assertTrue(target is QuranTarget.Explicit)
        assertEquals(18, target.surahId)
        assertEquals(1, target.ayahNumber)
    }

    @Test
    fun `bookmark target takes precedence when explicit target is absent or zero`() {
        val target = QuranTargetResolver.resolve(
            explicitSurahId = -1,
            explicitAyahNumber = -1,
            bookmarkTarget = 36 to 1,
            journeyTarget = 2 to 50,
            lastReadTarget = 1 to 1
        )

        assertTrue(target is QuranTarget.Bookmark)
        assertEquals(36, target.surahId)
        assertEquals(1, target.ayahNumber)
    }

    @Test
    fun `reading journey target takes precedence when explicit and bookmark are absent`() {
        val target = QuranTargetResolver.resolve(
            explicitSurahId = null,
            explicitAyahNumber = null,
            bookmarkTarget = null,
            journeyTarget = 67 to 1,
            lastReadTarget = 2 to 282
        )

        assertTrue(target is QuranTarget.ReadingJourney)
        assertEquals(67, target.surahId)
        assertEquals(1, target.ayahNumber)
    }

    @Test
    fun `last saved reading position is used when no explicit target or bookmark is present`() {
        val target = QuranTargetResolver.resolve(
            explicitSurahId = null,
            explicitAyahNumber = null,
            bookmarkTarget = null,
            journeyTarget = null,
            lastReadTarget = 4 to 120
        )

        assertTrue(target is QuranTarget.LastRead)
        assertEquals(4, target.surahId)
        assertEquals(120, target.ayahNumber)
    }

    @Test
    fun `default fallback is used only when all targets are absent`() {
        val target = QuranTargetResolver.resolve(
            explicitSurahId = null,
            explicitAyahNumber = null,
            bookmarkTarget = null,
            journeyTarget = null,
            lastReadTarget = null
        )

        assertTrue(target is QuranTarget.DefaultFallback)
        assertEquals(1, target.surahId)
        assertEquals(1, target.ayahNumber)
    }

    @Test
    fun `invalid zero or negative values fall back correctly`() {
        val target = QuranTargetResolver.resolve(
            explicitSurahId = 0,
            explicitAyahNumber = 0,
            bookmarkTarget = -1 to -1,
            journeyTarget = 0 to 0,
            lastReadTarget = 0 to 0
        )

        assertTrue(target is QuranTarget.DefaultFallback)
        assertEquals(1, target.surahId)
        assertEquals(1, target.ayahNumber)
    }

    @Test
    fun `requestIdentity distinguishes explicit surah and ayah requests from implicit requests`() {
        val nisa50 = QuranTargetResolver.requestIdentity(4, 50)
        val nisa100 = QuranTargetResolver.requestIdentity(4, 100)
        val nisaDefault = QuranTargetResolver.requestIdentity(4, null)
        val nisa1 = QuranTargetResolver.requestIdentity(4, 1)
        val implicitNull = QuranTargetResolver.requestIdentity(null, null)
        val implicitZero = QuranTargetResolver.requestIdentity(0, -1)

        assertEquals("explicit:4:50", nisa50)
        assertEquals("explicit:4:100", nisa100)
        assertNotEquals(nisa50, nisa100)
        assertEquals(nisaDefault, nisa1)
        assertEquals("implicit", implicitNull)
        assertEquals(implicitNull, implicitZero)
    }
}
