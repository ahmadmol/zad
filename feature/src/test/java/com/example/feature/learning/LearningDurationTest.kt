package com.example.feature.learning

import com.example.feature.learning.data.LearningDuration
import com.example.feature.nabiihsan.data.repository.NabiIhsanRepositoryImpl
import com.example.feature.nabiihsan.domain.model.NabiIhsanFilterState
import com.example.feature.sanhya.data.repository.SanhyaRepositoryImpl
import com.example.feature.sanhya.domain.model.StoryFilterState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LearningDurationTest {
    @Test fun `timestamps support hours and Arabic numerals and reject malformed values`() {
        assertEquals(3601L,LearningDuration.seconds("1:00:01"))
        assertEquals(1200L,LearningDuration.seconds("٢٠:٠٠"))
        listOf(null,"","unknown","-1:00","10:60","1:60:00","1:00:99","999999999999999:00").forEach {
            assertNull(it,LearningDuration.seconds(it))
        }
    }
    @Test fun `duration boundaries include seconds and do not classify unknown as short`() {
        val short=LearningDuration.filters[1]; val medium=LearningDuration.filters[2]; val long=LearningDuration.filters[3]
        assertTrue(LearningDuration.matches("19:59",short))
        assertFalse(LearningDuration.matches("20:00",short))
        assertTrue(LearningDuration.matches("20:00",medium))
        assertTrue(LearningDuration.matches("40:00",medium))
        assertFalse(LearningDuration.matches("40:01",medium))
        assertTrue(LearningDuration.matches("40:01",long))
        assertTrue(LearningDuration.matches("1:00:00",long))
        assertFalse(LearningDuration.matches(null,short))
        assertTrue(LearningDuration.matches(null,"الكل"))
    }
    @Test fun `sanhya applies the selected filter to at least one known episode and reset restores catalog`() = runTest {
        val repo=SanhyaRepositoryImpl()
        val all=repo.observeStories().first()
        for (filter in LearningDuration.filters.drop(1)) {
            val actual=repo.filterStories("",StoryFilterState(selectedDurationFilter=filter))
            val expected=all.filter { story -> story.episodes.any { LearningDuration.matches(it.durationLabel,filter) } }
            assertEquals(expected.map { it.id }.toSet(),actual.map { it.id }.toSet())
        }
        assertEquals(all.size,repo.filterStories("",StoryFilterState()).size)
        assertTrue(repo.filterStories("",StoryFilterState(selectedDurationFilter=LearningDuration.filters[1])).size < all.size)
    }
    @Test fun `nabi duration partitions contain each known catalog episode exactly once`() = runTest {
        val repo=NabiIhsanRepositoryImpl()
        val all=repo.observeEpisodes().first()
        val partition=LearningDuration.filters.drop(1).flatMap { filter ->
            repo.search("",NabiIhsanFilterState(selectedDurationFilter=filter)).episodes
        }
        assertEquals(all.size,partition.size)
        assertEquals(all.map { it.id }.toSet(),partition.map { it.id }.toSet())
    }
}
