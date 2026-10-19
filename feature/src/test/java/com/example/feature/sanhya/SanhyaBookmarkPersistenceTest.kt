package com.example.feature.sanhya

import com.example.feature.learning.data.*
import com.example.feature.sanhya.data.repository.SanhyaRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SanhyaBookmarkPersistenceTest {
    private class Saved : LearningBookmarkStore {
        val state = LearningCollection.entries.associateWith { MutableStateFlow(emptySet<String>()) }
        override fun observe(collection: LearningCollection): Flow<Set<String>> = state.getValue(collection)
        override suspend fun toggle(collection: LearningCollection, id: String) {
            val flow = state.getValue(collection)
            flow.value = if (id in flow.value) flow.value - id else flow.value + id
        }
    }
    @Test fun `saved selections survive repository recreation and collections stay independent`() = runTest {
        val saved = Saved()
        val first = SanhyaRepositoryImpl(saved)
        val id = first.observeStories().first().first().id
        first.toggleFavorite(id)
        first.toggleWatchLater(id)
        val reopened = SanhyaRepositoryImpl(saved)
        assertEquals(id, reopened.observeFavorites().first().single().id)
        assertEquals(id, reopened.observeWatchLater().first().single().id)
        reopened.toggleFavorite(id)
        assertTrue(first.observeFavorites().first().isEmpty())
        assertEquals(id, first.observeWatchLater().first().single().id)
        assertTrue(saved.state.getValue(LearningCollection.NABI_FAVORITES).value.isEmpty())
        first.toggleFavorite("missing-story")
        assertTrue(saved.state.getValue(LearningCollection.SANHYA_FAVORITES).value.isEmpty())
    }
}
