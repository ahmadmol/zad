package com.example.feature.quran.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phase 4 — Repeat Range domain rules. */
class QuranRepeatPlannerTest {

    private val ayahCount = 7

    private fun decide(config: QuranRepeatConfig, current: Int) =
        QuranRepeatPlanner.decideNext(config, current, ayahCount)

    // --- OFF (existing sequential behaviour is preserved) ------------------

    @Test
    fun `off advances sequentially`() {
        val d = decide(QuranRepeatConfig.OFF, 3)
        assertEquals(QuranRepeatDecision.Play(4, QuranRepeatConfig.OFF), d)
    }

    @Test
    fun `off stops at the last ayah`() {
        assertTrue(decide(QuranRepeatConfig.OFF, ayahCount) is QuranRepeatDecision.Stop)
    }

    @Test
    fun `empty surah stops immediately`() {
        val d = QuranRepeatPlanner.decideNext(QuranRepeatConfig.OFF, 1, 0)
        assertTrue(d is QuranRepeatDecision.Stop)
    }

    // --- SINGLE_AYAH -------------------------------------------------------

    @Test
    fun `single ayah repeats the same ayah unlimited`() {
        val config = QuranRepeatConfig.singleAyah(4, ayahCount)
        val d = decide(config, 4) as QuranRepeatDecision.Play
        assertEquals(4, d.ayah)
        assertEquals(1, d.config.completedCycles)
    }

    @Test
    fun `single ayah stops after the requested count`() {
        var config = QuranRepeatConfig.singleAyah(4, ayahCount, repeatCount = 3)
        // cycles 1 and 2 keep playing
        repeat(2) {
            val d = decide(config, 4)
            assertTrue(d is QuranRepeatDecision.Play)
            config = (d as QuranRepeatDecision.Play).config
        }
        // the third completion ends the repeat cleanly
        val last = decide(config, 4)
        assertTrue(last is QuranRepeatDecision.Stop)
        assertFalse((last as QuranRepeatDecision.Stop).config.isActive)
    }

    @Test
    fun `single ayah is clamped into the surah`() {
        assertEquals(ayahCount, QuranRepeatConfig.singleAyah(99, ayahCount).startAyah)
        assertEquals(1, QuranRepeatConfig.singleAyah(-4, ayahCount).startAyah)
    }

    // --- RANGE -------------------------------------------------------------

    @Test
    fun `range advances inside the range`() {
        val config = QuranRepeatConfig.range(2, 5, ayahCount)
        val d = decide(config, 3) as QuranRepeatDecision.Play
        assertEquals(4, d.ayah)
        assertEquals(0, d.config.completedCycles)
    }

    @Test
    fun `range wraps to its start at the end of a pass`() {
        val config = QuranRepeatConfig.range(2, 5, ayahCount)
        val d = decide(config, 5) as QuranRepeatDecision.Play
        assertEquals(2, d.ayah)
        assertEquals(1, d.config.completedCycles)
    }

    @Test
    fun `range stops after the requested number of passes`() {
        var config = QuranRepeatConfig.range(2, 3, ayahCount, repeatCount = 2)

        // pass 1 completes at ayah 3 -> wraps
        val first = decide(config, 3) as QuranRepeatDecision.Play
        assertEquals(2, first.ayah)
        config = first.config

        // walking through the range again
        config = (decide(config, 2) as QuranRepeatDecision.Play).config

        // pass 2 completes -> stop
        val end = decide(config, 3)
        assertTrue(end is QuranRepeatDecision.Stop)
        assertEquals(QuranRepeatConfig.OFF, (end as QuranRepeatDecision.Stop).config)
    }

    @Test
    fun `range re-enters when playback drifted outside it`() {
        val config = QuranRepeatConfig.range(2, 4, ayahCount)
        val below = decide(config, 1) as QuranRepeatDecision.Play
        assertEquals(2, below.ayah)
        val above = decide(config, 6) as QuranRepeatDecision.Play
        assertEquals(2, above.ayah)
    }

    @Test
    fun `inverted range bounds are normalized`() {
        val config = QuranRepeatConfig.range(5, 2, ayahCount)
        assertEquals(2, config.startAyah)
        assertEquals(5, config.endAyah)
    }

    @Test
    fun `range bounds are clamped to the surah length`() {
        val config = QuranRepeatConfig.range(0, 999, ayahCount)
        assertEquals(1, config.startAyah)
        assertEquals(ayahCount, config.endAyah)
    }

    @Test
    fun `a single ayah range behaves like single ayah repeat`() {
        val config = QuranRepeatConfig.range(3, 3, ayahCount)
        val d = decide(config, 3) as QuranRepeatDecision.Play
        assertEquals(3, d.ayah)
        assertEquals(1, d.config.completedCycles)
    }

    @Test
    fun `negative repeat count degrades to unlimited`() {
        val config = QuranRepeatConfig.range(1, 2, ayahCount, repeatCount = -5)
        assertEquals(QuranRepeatConfig.UNLIMITED, config.repeatCount)
        assertTrue(decide(config, 2) is QuranRepeatDecision.Play)
    }

    @Test
    fun `unlimited repeat never stops on its own`() {
        var config = QuranRepeatConfig.range(1, 2, ayahCount)
        repeat(50) {
            val d = decide(config, 2)
            assertTrue(d is QuranRepeatDecision.Play)
            config = (d as QuranRepeatDecision.Play).config
        }
    }

    @Test
    fun `off config reports inactive and range reports active`() {
        assertFalse(QuranRepeatConfig.OFF.isActive)
        assertTrue(QuranRepeatConfig.range(1, 2, ayahCount).isActive)
        assertTrue(QuranRepeatConfig.singleAyah(1, ayahCount).isActive)
    }
}
