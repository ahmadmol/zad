package com.example.feature.nabiihsan

import com.example.feature.learning.data.*
import com.example.feature.nabiihsan.data.repository.NabiIhsanRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class NabiBookmarkPersistenceTest {
    private class Saved : LearningBookmarkStore {
        val state = LearningCollection.entries.associateWith { MutableStateFlow(emptySet<String>()) }
        override fun observe(collection: LearningCollection): Flow<Set<String>> = state.getValue(collection)
        override suspend fun toggle(collection: LearningCollection, id: String) {
            val flow = state.getValue(collection)
            flow.value = if (id in flow.value) flow.value - id else flow.value + id
        }
    }
    @Test fun `episode selections survive recreation without inventing recipe bookmarks`() = runTest {
        val saved = Saved()
        val first = NabiIhsanRepositoryImpl(saved)
        val id = first.observeEpisodes().first().first().id
        first.toggleEpisodeFavorite(id)
        first.toggleEpisodeWatchLater(id)
        val reopened = NabiIhsanRepositoryImpl(saved)
        assertEquals(id, reopened.observeFavoriteEpisodes().first().single().id)
        assertEquals(id, reopened.observeWatchLaterEpisodes().first().single().id)
        reopened.toggleEpisodeFavorite(id)
        assertTrue(first.observeFavoriteEpisodes().first().isEmpty())
        assertEquals(id, first.observeWatchLaterEpisodes().first().single().id)
        first.toggleRecipeFavorite("missing-recipe")
        assertTrue(saved.state.getValue(LearningCollection.NABI_RECIPE_FAVORITES).value.isEmpty())
        assertTrue(saved.state.getValue(LearningCollection.SANHYA_FAVORITES).value.isEmpty())
    }
}
