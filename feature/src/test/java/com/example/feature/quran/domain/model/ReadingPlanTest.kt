package com.example.feature.quran.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import kotlinx.serialization.json.*

class ReadingPlanTest {
    private val today = LocalDate.of(2026, 10, 20)
    private fun plan(days: Int = 30, first: Int = 1, last: Int = 604) =
        ReadingPlan("p", today.toEpochDay(), today.plusDays(days.toLong() - 1).toEpochDay(), first, last)

    @Test fun `full khatma uses 604 actual pages with inclusive dates`() {
        assertEquals(604, plan().pageCount)
        assertEquals(21, plan().suggestedPages(today, emptySet()).size)
        assertEquals(604, plan(1).suggestedPages(today, emptySet()).size)
    }
    @Test fun `completion affects only confirmed pages and is idempotent`() {
        val completed = setOf(1, 3, 5)
        assertEquals(listOf(2, 4, 6), plan(2, 1, 8).suggestedPages(today, completed))
        assertEquals(5, plan(2, 1, 8).remaining(completed).size)
    }
    @Test fun `overdue plans propose only remaining pages without a missed count`() {
        assertEquals(listOf(3, 4), plan(1, 1, 4).suggestedPages(today.plusDays(20), setOf(1, 2)))
        assertTrue(plan(1, 1, 2).suggestedPages(today, setOf(1, 2)).isEmpty())
        assertTrue(plan().suggestedPages(today.minusDays(1), emptySet()).isEmpty())
    }
    @Test fun `range validation rejects reversed or nonexistent pages`() {
        listOf(0 to 20, 2 to 1, 1 to 605).forEach { (first, last) ->
            assertThrows(IllegalArgumentException::class.java) { plan(first = first, last = last) }
        }
    }
    @Test fun `extension redistributes remaining pages without altering confirmation history`() {
        val original = plan(1, 1, 10)
        val completed = setOf(1, 2, 3, 4)
        val extended = original.copy(dueEpochDay = today.plusDays(2).toEpochDay())
        assertEquals(listOf(5, 6), extended.suggestedPages(today, completed))
        assertEquals(6, original.remaining(completed).size)
    }
    @Test fun `private export includes only selected notes and safely escapes text`() {
        val notes = listOf(PrivateAyahNote("a", 2, 3, "سطر\n\"خاص\"", 1, 2), PrivateAyahNote("b", 3, 4, "غير محدد", 3, 4))
        val encoded = PrivateNotesExport.encode(notes, setOf("a"))
        val payload = Json.parseToJsonElement(encoded).jsonObject
        assertEquals(1, payload.getValue("version").jsonPrimitive.int)
        val rows = payload.getValue("notes").jsonArray
        assertEquals(1, rows.size)
        assertEquals(notes[0].text, rows[0].jsonObject.getValue("text").jsonPrimitive.content)
        assertFalse(encoded.contains("غير محدد"))
        assertThrows(IllegalArgumentException::class.java) { PrivateNotesExport.encode(notes, emptySet()) }
        assertThrows(IllegalArgumentException::class.java) { PrivateNotesExport.encode(notes, setOf("missing")) }
    }
}
