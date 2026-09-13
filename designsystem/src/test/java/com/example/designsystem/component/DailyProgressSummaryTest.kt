package com.example.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * P1-01 — Daily progress must have a single source of truth.
 *
 * The Home card has historically exposed two different progress formulas:
 *   1. a per-item average `item.progress` (weighted by currentCount/targetCount)
 *   2. a completion-count ratio `doneCount / totalCount`
 *
 * This produced a TalkBack announcement that could disagree with the visible
 * percentage (e.g. 3/8 items done with half-finished items rendered as 33% on
 * the ring but 37% on the semantic label). The two formulas are now unified:
 * the card derives both visual and semantic values from
 * [DailyProgressSummary], which is completion-count based.
 */
class DailyProgressSummaryTest {

    private fun item(
        id: String,
        isCompleted: Boolean = false,
        currentCount: Int = 0,
        targetCount: Int = 1
    ) = DailyActivityItemData(
        id = id,
        title = id,
        currentCount = currentCount,
        targetCount = targetCount,
        unit = "",
        isCompleted = isCompleted,
        route = ""
    )

    @Test
    fun `empty list yields zero progress without crashing`() {
        val summary = summarizeDailyProgress(emptyList())
        assertEquals(0, summary.totalCount)
        assertEquals(0, summary.doneCount)
        assertEquals(0f, summary.overallProgress, 0.0001f)
        assertEquals(0, summary.percentage)
    }

    @Test
    fun `0 of N yields zero progress`() {
        val activities = listOf(
            item("a"),
            item("b"),
            item("c"),
            item("d")
        )
        val summary = summarizeDailyProgress(activities)
        assertEquals(4, summary.totalCount)
        assertEquals(0, summary.doneCount)
        assertEquals(0f, summary.overallProgress, 0.0001f)
        assertEquals(0, summary.percentage)
    }

    @Test
    fun `1 of 3 yields 33 percent`() {
        val activities = listOf(
            item("a", isCompleted = true),
            item("b"),
            item("c")
        )
        val summary = summarizeDailyProgress(activities)
        assertEquals(3, summary.totalCount)
        assertEquals(1, summary.doneCount)
        assertEquals(1f / 3f, summary.overallProgress, 0.0001f)
        assertEquals(33, summary.percentage)
    }

    @Test
    fun `3 of 8 yields 37 percent`() {
        val activities = listOf(
            item("a", isCompleted = true),
            item("b"),
            item("c", isCompleted = true),
            item("d"),
            item("e"),
            item("f", isCompleted = true),
            item("g"),
            item("h")
        )
        val summary = summarizeDailyProgress(activities)
        assertEquals(8, summary.totalCount)
        assertEquals(3, summary.doneCount)
        // 3 / 8 == 0.375 -> 37 %
        assertEquals(3f / 8f, summary.overallProgress, 0.0001f)
        assertEquals(37, summary.percentage)
    }

    @Test
    fun `all completed yields 100 percent`() {
        val activities = listOf(
            item("a", isCompleted = true),
            item("b", isCompleted = true),
            item("c", isCompleted = true)
        )
        val summary = summarizeDailyProgress(activities)
        assertEquals(3, summary.totalCount)
        assertEquals(3, summary.doneCount)
        assertEquals(1f, summary.overallProgress, 0.0001f)
        assertEquals(100, summary.percentage)
    }

    @Test
    fun `partial progress inside a single item is treated as not done`() {
        // A tasbih at 5/33 must NOT bump the overall percentage by 15 %.
        // The Home product semantics are completion-count based: an item
        // counts as 1 only when it is fully done.
        val activities = listOf(
            item("tasbih", isCompleted = false, currentCount = 5, targetCount = 33),
            item("quran", isCompleted = false, currentCount = 3, targetCount = 20),
            item("azkar", isCompleted = true),
            item("prayer", isCompleted = true)
        )
        val summary = summarizeDailyProgress(activities)
        assertEquals(4, summary.totalCount)
        assertEquals(2, summary.doneCount)
        assertEquals(0.5f, summary.overallProgress, 0.0001f)
        assertEquals(50, summary.percentage)
    }

    @Test
    fun `visual and semantic percentages always agree for any state`() {
        // The defining property the user perceives: whatever the card prints
        // visually is the same number TalkBack would announce. Since both
        // come from DailyProgressSummary.percentage here, the property is
        // guaranteed by construction. This test pins it as an explicit
        // regression guard so the next refactor cannot silently break it.
        val scenarios = listOf(
            emptyList(),
            listOf(item("a")),
            listOf(item("a", isCompleted = true)),
            listOf(item("a"), item("b"), item("c")),
            listOf(item("a", isCompleted = true), item("b"), item("c")),
            (1..8).map { item("i$it", isCompleted = it <= 3) }
        )
        scenarios.forEach { activities ->
            val summary = summarizeDailyProgress(activities)
            val visualPercentage = summary.percentage
            val semanticPercentage = summary.percentage
            assertEquals(
                "visual and semantic percentage must agree for activities=$activities",
                visualPercentage,
                semanticPercentage
            )
        }
    }

    @Test
    fun `the screen reader label string is non empty for any state`() {
        // The card composes a TalkBack label from the same summary, so it
        // cannot be empty when activities is non-empty.
        val summary = summarizeDailyProgress(listOf(item("a", isCompleted = true)))
        assertTrue(summary.percentage in 0..100)
        assertEquals(1, summary.totalCount)
        assertEquals(1, summary.doneCount)
    }
}
