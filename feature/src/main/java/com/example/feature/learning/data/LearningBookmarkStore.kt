package com.example.feature.learning.data

import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow

enum class LearningCollection(val key: String) {
    SANHYA_FAVORITES("sanhya_favorites"), SANHYA_LATER("sanhya_later"),
    NABI_FAVORITES("nabi_favorites"), NABI_RECIPE_FAVORITES("nabi_recipe_favorites"), NABI_LATER("nabi_later")
}

interface LearningBookmarkStore {
    fun observe(collection: LearningCollection): Flow<Set<String>>
    suspend fun toggle(collection: LearningCollection, id: String)
}

class PreferenceLearningBookmarkStore(private val preferences: UserPreferences) : LearningBookmarkStore {
    override fun observe(collection: LearningCollection) = preferences.learningIds(collection.key)
    override suspend fun toggle(collection: LearningCollection, id: String) = preferences.toggleLearningId(collection.key, id)
}
