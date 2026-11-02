package com.example.feature.prayer.data.diagnostics

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.feature.prayer.domain.model.PrayerOperationalEvent
import com.example.feature.prayer.domain.model.PrayerOperationalEventType
import com.example.feature.prayer.domain.model.PrayerReconciliationReason
import com.example.feature.prayer.domain.repository.PrayerEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.prayerEventsStore: DataStore<Preferences> by preferencesDataStore(name = "prayer_operational_events")

/**
 * Bounded local event buffer (max [MAX_EVENTS]) via DataStore JSON.
 * Tradeoff: avoids Room migration risk for diagnostics; no coordinates/PII stored.
 */
class DataStorePrayerEventRepository(
    private val context: Context
) : PrayerEventRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun record(event: PrayerOperationalEvent) {
        context.prayerEventsStore.edit { prefs ->
            val current = decode(prefs[KEY_EVENTS]).toMutableList()
            current.add(0, event.toDto())
            while (current.size > MAX_EVENTS) current.removeAt(current.lastIndex)
            prefs[KEY_EVENTS] = json.encodeToString(current)
        }
    }

    override fun observeRecent(limit: Int): Flow<List<PrayerOperationalEvent>> =
        context.prayerEventsStore.data.map { prefs ->
            decode(prefs[KEY_EVENTS]).take(limit).map { it.toDomain() }
        }

    override suspend fun clear() {
        context.prayerEventsStore.edit { it.remove(KEY_EVENTS) }
    }

    private fun decode(raw: String?): List<EventDto> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<EventDto>>(raw) }.getOrDefault(emptyList())
    }

    @Serializable
    private data class EventDto(
        val type: String,
        val reason: String? = null,
        val summary: String,
        val epochMillis: Long
    )

    private fun PrayerOperationalEvent.toDto() = EventDto(
        type = type.name,
        reason = reason?.name,
        summary = summary,
        epochMillis = epochMillis
    )

    private fun EventDto.toDomain() = PrayerOperationalEvent(
        type = runCatching { PrayerOperationalEventType.valueOf(type) }
            .getOrDefault(PrayerOperationalEventType.SCHEDULE_FAILED),
        reason = reason?.let { runCatching { PrayerReconciliationReason.valueOf(it) }.getOrNull() },
        summary = summary,
        epochMillis = epochMillis
    )

    companion object {
        private val KEY_EVENTS = stringPreferencesKey("events_json")
        private const val MAX_EVENTS = 40
    }
}
