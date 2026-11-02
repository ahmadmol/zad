package com.example.feature.learning.data

import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.flow.map
import java.time.Clock

data class LearningEpisodeProgress(
    val id: String, val parentId: String, val seconds: Int = 0,
    val completed: Boolean = false, val updatedAt: Long = 0
)

/** Metadata for existing Nabi/Sanhya episodes; never writes on page-open or playback intent. */
class LearningProgressStore(private val preferences: UserPreferences, private val clock: Clock = Clock.systemUTC()) {
    fun observe(series: String) = preferences.learningIds(key(series)).map { rows ->
        rows.mapNotNull(::decode).associateBy { it.id }
    }
    suspend fun save(series: String, id: String, parentId: String, seconds: Int, completed: Boolean) {
        require(id.isNotBlank() && parentId.isNotBlank() && '|' !in id && '|' !in parentId)
        require(seconds in 0..86400)
        val row = LearningEpisodeProgress(id, parentId, seconds, completed, clock.millis())
        preferences.updateLearningIds(key(series)) { existing ->
            // Retain unrelated and unknown-format rows. Existing favorite keys are untouched.
            existing.filterNot { decode(it)?.id == id }.toSet() + encode(row)
        }
    }
    private fun key(series: String): String {
        require(series in setOf("nabi", "sanhya"))
        return "${series}_progress_v1"
    }
    internal companion object {
        fun encode(row: LearningEpisodeProgress) = "1|${row.id}|${row.parentId}|${row.seconds}|${row.completed}|${row.updatedAt}"
        fun decode(raw: String): LearningEpisodeProgress? {
            val parts = raw.split('|')
            if (parts.size != 6 || parts[0] != "1" || parts[1].isBlank() || parts[2].isBlank()) return null
            val seconds = parts[3].toIntOrNull()?.takeIf { it in 0..86400 } ?: return null
            val completed = parts[4].toBooleanStrictOrNull() ?: return null
            val time = parts[5].toLongOrNull() ?: return null
            return LearningEpisodeProgress(parts[1], parts[2], seconds, completed, time)
        }
    }
}
