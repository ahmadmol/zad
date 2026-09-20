package com.example.feature.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    private val activityDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val userName: Flow<String> = context.dataStore.data
        .map { it[KEY_USER_NAME] ?: DEFAULT_USER_NAME }

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_HAS_COMPLETED_ONBOARDING] ?: false }

    val lastReadSurahId: Flow<Int?> = context.dataStore.data
        .map { it[KEY_LAST_READ_SURAH_ID] }

    val lastReadAyahNumber: Flow<Int?> = context.dataStore.data
        .map { it[KEY_LAST_READ_AYAH_NUMBER] }

    val userLatitude: Flow<Double?> = context.dataStore.data
        .map { it[KEY_LATITUDE] }

    val userLongitude: Flow<Double?> = context.dataStore.data
        .map { it[KEY_LONGITUDE] }

    val adhanSoundUri: Flow<String?> = context.dataStore.data
        .map { it[KEY_ADHAN_SOUND_URI] }

    val userAvatarUri: Flow<String?> = context.dataStore.data
        .map { it[KEY_USER_AVATAR_URI] }

    val favoriteAsmaIds: Flow<Set<String>> = context.dataStore.data
        .map { it[KEY_FAVORITE_ASMA_IDS] ?: emptySet() }

    val dailyActivityDate: Flow<String> = context.dataStore.data
        .map { it[KEY_DAILY_ACTIVITY_DATE] ?: DEFAULT_ACTIVITY_DATE }

    val dailyActivityCounts: Flow<Map<String, Int>> = context.dataStore.data
        .map { prefs ->
            DailyActivityIds.ALL.associateWith { id ->
                prefs[keyForActivity(id)] ?: 0
            }
        }

    suspend fun ensureDailyActivitiesForDate(date: String) {
        context.dataStore.edit { prefs ->
            val existingDate = prefs[KEY_DAILY_ACTIVITY_DATE] ?: ""
            if (existingDate != date) {
                prefs[KEY_DAILY_ACTIVITY_DATE] = date
                DailyActivityIds.ALL.forEach { id ->
                    prefs[keyForActivity(id)] = 0
                }
            }
        }
    }

    suspend fun setDailyActivityCount(id: String, count: Int) {
        val today = todayDate()
        context.dataStore.edit { prefs ->
            ensureTodayLocked(prefs, today)
            val max = DailyActivityIds.targetFor(id)
            prefs[keyForActivity(id)] = count.coerceIn(0, max)
        }
    }

    /**
     * Increments a daily activity for today, rolling the date forward if needed,
     * and never exceeding the activity target.
     */
    suspend fun incrementDailyActivityCount(id: String, delta: Int = 1) {
        val today = todayDate()
        val max = DailyActivityIds.targetFor(id)
        context.dataStore.edit { prefs ->
            ensureTodayLocked(prefs, today)
            val current = prefs[keyForActivity(id)] ?: 0
            if (current >= max) return@edit
            prefs[keyForActivity(id)] = (current + delta).coerceIn(0, max)
        }
    }

    /** Marks a once-per-day activity as done (sets count to target if still 0). */
    suspend fun markDailyActivityComplete(id: String) {
        val today = todayDate()
        val max = DailyActivityIds.targetFor(id)
        context.dataStore.edit { prefs ->
            ensureTodayLocked(prefs, today)
            val current = prefs[keyForActivity(id)] ?: 0
            if (current < max) {
                prefs[keyForActivity(id)] = max
            }
        }
    }

    suspend fun resetDailyActivityCounts(date: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DAILY_ACTIVITY_DATE] = date
            DailyActivityIds.ALL.forEach { id ->
                prefs[keyForActivity(id)] = 0
            }
        }
    }

    suspend fun setUserName(value: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = value
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun saveLastRead(surahId: Int, ayahNumber: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_READ_SURAH_ID] = surahId
            prefs[KEY_LAST_READ_AYAH_NUMBER] = ayahNumber
        }
    }

    suspend fun saveLocation(latitude: Double, longitude: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LATITUDE] = latitude
            prefs[KEY_LONGITUDE] = longitude
        }
    }

    suspend fun setAdhanSoundUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ADHAN_SOUND_URI] = uri
        }
    }

    suspend fun setUserAvatarUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri.isNullOrBlank()) {
                prefs.remove(KEY_USER_AVATAR_URI)
            } else {
                prefs[KEY_USER_AVATAR_URI] = uri
            }
        }
    }

    suspend fun setFavoriteAsmaIds(ids: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FAVORITE_ASMA_IDS] = ids
        }
    }

    private fun todayDate(): String = activityDateFormatter.format(Date())

    private fun ensureTodayLocked(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        today: String
    ) {
        val existingDate = prefs[KEY_DAILY_ACTIVITY_DATE] ?: ""
        if (existingDate != today) {
            prefs[KEY_DAILY_ACTIVITY_DATE] = today
            DailyActivityIds.ALL.forEach { id ->
                prefs[keyForActivity(id)] = 0
            }
        }
    }

    private companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_AVATAR_URI = stringPreferencesKey("user_avatar_uri")
        val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val KEY_LAST_READ_SURAH_ID = intPreferencesKey("last_read_surah_id")
        val KEY_LAST_READ_AYAH_NUMBER = intPreferencesKey("last_read_ayah_number")
        val KEY_LATITUDE = doublePreferencesKey("user_latitude")
        val KEY_LONGITUDE = doublePreferencesKey("user_longitude")
        val KEY_ADHAN_SOUND_URI = stringPreferencesKey("adhan_sound_uri")
        val KEY_FAVORITE_ASMA_IDS = stringSetPreferencesKey("favorite_asma_ids")
        val KEY_DAILY_ACTIVITY_DATE = stringPreferencesKey("daily_activity_date")

        fun keyForActivity(id: String) = intPreferencesKey("daily_activity_count_$id")

        const val DEFAULT_USER_NAME = "مستخدم إحسان"
        const val DEFAULT_ACTIVITY_DATE = "1970-01-01"
    }
}
