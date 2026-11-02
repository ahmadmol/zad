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

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences internal constructor(private val store: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>, private val clock: java.time.Clock?) {
    internal constructor(store: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>) : this(store, null)
    constructor(context: Context) : this(context.dataStore, null)
    val ramadanHighlightEnabled: Flow<Boolean> = store.data.map { it[booleanPreferencesKey("ramadan_highlight_enabled")] ?: false }
    suspend fun setRamadanHighlightEnabled(enabled: Boolean) {
        store.edit { it[booleanPreferencesKey("ramadan_highlight_enabled")] = enabled }
    }
    val hijriDayOffset: Flow<Int> = store.data.map {
        (it[intPreferencesKey("hijri_day_offset")] ?: 0).coerceIn(-2, 2)
    }
    suspend fun setHijriDayOffset(days: Int) {
        require(days in -2..2) { "التعديل من −2 إلى +2 يوم فقط" }
        store.edit { it[intPreferencesKey("hijri_day_offset")] = days }
    }



    val userName: Flow<String> = store.data
        .map { it[KEY_USER_NAME] ?: DEFAULT_USER_NAME }

    val hasCompletedOnboarding: Flow<Boolean> = store.data
        .map { it[KEY_HAS_COMPLETED_ONBOARDING] ?: false }

    val lastReadSurahId: Flow<Int?> = store.data
        .map { it[KEY_LAST_READ_SURAH_ID] }

    val lastReadAyahNumber: Flow<Int?> = store.data
        .map { it[KEY_LAST_READ_AYAH_NUMBER] }

    /** Both keys come from one committed DataStore snapshot; no cross-emission verse pairs. */
    val lastReadPosition: Flow<Pair<Int, Int>?> = store.data.map { prefs ->
        val surah = prefs[KEY_LAST_READ_SURAH_ID]
        val ayah = prefs[KEY_LAST_READ_AYAH_NUMBER]
        if (surah != null && surah in 1..114 && ayah != null && ayah > 0) surah to ayah else null
    }

    val dailyActivitySnapshot: Flow<Pair<String, Map<String, Int>>> = store.data.map { prefs ->
        (prefs[KEY_DAILY_ACTIVITY_DATE] ?: DEFAULT_ACTIVITY_DATE) to
            DailyActivityIds.ALL.associateWith { prefs[keyForActivity(it)] ?: 0 }
    }

    val userLatitude: Flow<Double?> = store.data
        .map { it[KEY_LATITUDE] }

    val userLongitude: Flow<Double?> = store.data
        .map { it[KEY_LONGITUDE] }

    val adhanSoundUri: Flow<String?> = store.data
        .map { it[KEY_ADHAN_SOUND_URI] }

    val quranFontSize: Flow<Float> = store.data.map { (it[androidx.datastore.preferences.core.floatPreferencesKey("quran_font_size")] ?: 28f).coerceIn(20f, 48f) }
    val quranReaderId: Flow<String?> = store.data.map { it[stringPreferencesKey("quran_reader_id")] }
    suspend fun setQuranFontSize(size: Float) { store.edit { it[androidx.datastore.preferences.core.floatPreferencesKey("quran_font_size")] = size.coerceIn(20f, 48f) } }
    suspend fun setQuranReaderId(id: String) { store.edit { it[stringPreferencesKey("quran_reader_id")] = id } }

    val userBio: Flow<String> = store.data.map { it[KEY_USER_BIO] ?: "" }

    fun learningIds(collection: String): Flow<Set<String>> = store.data
        .map { it[stringSetPreferencesKey("learning_$collection")] ?: emptySet() }

    suspend fun toggleLearningId(collection: String, id: String) {
        val key = stringSetPreferencesKey("learning_$collection")
        store.edit { prefs ->
            val current = prefs[key].orEmpty()
            prefs[key] = if (id in current) current - id else current + id
        }
    }

    suspend fun updateLearningIds(collection: String, transform: (Set<String>) -> Set<String>) {
        val key = stringSetPreferencesKey("learning_$collection")
        store.edit { prefs -> prefs[key] = transform(prefs[key].orEmpty()) }
    }

    fun learningOrder(collection: String): Flow<List<String>> = store.data.map {
        it[stringPreferencesKey("learning_${collection}_order_v1")]?.split('|')?.filter(String::isNotBlank).orEmpty()
    }
    suspend fun updateLearningOrder(collection: String, transform: (List<String>) -> List<String>) {
        val key = stringPreferencesKey("learning_${collection}_order_v1")
        store.edit { prefs ->
            val order = transform(prefs[key]?.split('|')?.filter(String::isNotBlank).orEmpty())
            require(order.none { it.contains('|') })
            prefs[key] = order.distinct().joinToString("|")
        }
    }

    suspend fun setUserBio(bio: String) { store.edit { it[KEY_USER_BIO] = bio.take(200) } }

    suspend fun clearUserPresentation() {
        store.edit { prefs ->
            prefs.remove(KEY_USER_BIO)
            // Local learning bookmarks belong to this device. Signing out of a local
            // community profile must not silently erase the saved learning library.
        }
    }

    val userAvatarUri: Flow<String?> = store.data
        .map { it[KEY_USER_AVATAR_URI] }

    val favoriteAsmaIds: Flow<Set<String>> = store.data
        .map { it[KEY_FAVORITE_ASMA_IDS] ?: emptySet() }

    val dailyActivityDate: Flow<String> = store.data
        .map { it[KEY_DAILY_ACTIVITY_DATE] ?: DEFAULT_ACTIVITY_DATE }

    val dailyActivityCounts: Flow<Map<String, Int>> = store.data
        .map { prefs ->
            DailyActivityIds.ALL.associateWith { id ->
                prefs[keyForActivity(id)] ?: 0
            }
        }

    suspend fun ensureDailyActivitiesForDate(date: String) {
        store.edit { prefs ->
            if (date == todayDate()) ensureTodayLocked(prefs, date)
        }
    }

    suspend fun setDailyActivityCount(id: String, count: Int) {
        store.edit { prefs ->
            val today = todayDate()
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
        val max = DailyActivityIds.targetFor(id)
        store.edit { prefs ->
            val today = todayDate()
            ensureTodayLocked(prefs, today)
            val current = prefs[keyForActivity(id)] ?: 0
            if (current >= max) return@edit
            prefs[keyForActivity(id)] = (current + delta).coerceIn(0, max)
        }
    }

    /** Marks a once-per-day activity as done (sets count to target if still 0). */
    suspend fun markDailyActivityComplete(id: String) {
        val max = DailyActivityIds.targetFor(id)
        store.edit { prefs ->
            val today = todayDate()
            ensureTodayLocked(prefs, today)
            val current = prefs[keyForActivity(id)] ?: 0
            if (current < max) {
                prefs[keyForActivity(id)] = max
            }
        }
    }

    suspend fun resetDailyActivityCounts(date: String) {
        store.edit { prefs ->
            prefs[KEY_DAILY_ACTIVITY_DATE] = date
            DailyActivityIds.ALL.forEach { id ->
                prefs[keyForActivity(id)] = 0
            }
        }
    }

    suspend fun setUserName(value: String) {
        store.edit { prefs ->
            prefs[KEY_USER_NAME] = value
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        store.edit { prefs ->
            prefs[KEY_HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun saveLastRead(surahId: Int, ayahNumber: Int) {
        store.edit { prefs ->
            prefs[KEY_LAST_READ_SURAH_ID] = surahId
            prefs[KEY_LAST_READ_AYAH_NUMBER] = ayahNumber
        }
    }

    suspend fun saveLocation(latitude: Double, longitude: Double) {
        store.edit { prefs ->
            prefs[KEY_LATITUDE] = latitude
            prefs[KEY_LONGITUDE] = longitude
        }
    }

    suspend fun setAdhanSoundUri(uri: String) {
        store.edit { prefs ->
            prefs[KEY_ADHAN_SOUND_URI] = uri
        }
    }

    suspend fun setUserAvatarUri(uri: String?) {
        store.edit { prefs ->
            if (uri.isNullOrBlank()) {
                prefs.remove(KEY_USER_AVATAR_URI)
            } else {
                prefs[KEY_USER_AVATAR_URI] = uri
            }
        }
    }

    suspend fun toggleFavoriteAsmaId(id: Int) {
        store.edit { prefs ->
            val existing = prefs[KEY_FAVORITE_ASMA_IDS].orEmpty()
            val key = id.toString()
            prefs[KEY_FAVORITE_ASMA_IDS] = if (key in existing) existing - key else existing + key
        }
    }

    suspend fun setFavoriteAsmaIds(ids: Set<String>) {
        store.edit { prefs ->
            prefs[KEY_FAVORITE_ASMA_IDS] = ids
        }
    }

    private fun todayDate(): String = java.time.LocalDate.now(clock ?: java.time.Clock.systemDefaultZone()).toString()

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
        val KEY_USER_BIO = stringPreferencesKey("user_bio")
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
