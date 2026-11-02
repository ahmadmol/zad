package com.example.feature.azkar.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager internal constructor(private val store: DataStore<Preferences>) {
    constructor(context: Context) : this(context.dataStore)

    enum class ThemeMode { SYSTEM, LIGHT, DARK }

    val paletteFlow: Flow<com.example.designsystem.theme.IhsanPalette> = store.data.map {
        com.example.designsystem.theme.IhsanPalette.fromStorage(it[stringPreferencesKey("appearance_palette_v1")])
    }
    suspend fun setPalette(palette: com.example.designsystem.theme.IhsanPalette) {
        store.edit { it[stringPreferencesKey("appearance_palette_v1")] = palette.name }
    }

    /**
     * Internal DataStore handle for code paths that need direct read/write
     * access (e.g. the Azkar seed manager storing its version key). Kept
     * package-private to avoid leaking DataStore access to the rest of the app.
     */
    internal val dataStoreInternal: DataStore<Preferences> = store

    companion object {
        val FONT_SIZE = floatPreferencesKey("font_size")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PRAYER_NOTIFICATIONS_ENABLED = booleanPreferencesKey("prayer_notifications_enabled")
        private val PRAYER_OFFSET_KEYS = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")
            .map { intPreferencesKey("prayer_offset_$it") }
        val CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        val MADHAB = stringPreferencesKey("madhab")
        val USE_AUTO_LOCATION = booleanPreferencesKey("use_auto_location")
        val MANUAL_LOCATION_CITY = stringPreferencesKey("manual_location_city")
        val MANUAL_LOCATION_LAT = doublePreferencesKey("manual_location_lat")
        val MANUAL_LOCATION_LNG = doublePreferencesKey("manual_location_lng")
        val PRE_PRAYER_NOTIFICATION_MINUTES = intPreferencesKey("pre_prayer_notification_minutes")
        val IQAMAH_NOTIFICATION_MINUTES = intPreferencesKey("iqamah_notification_minutes")
        val NOTIFICATION_SOUND_TYPE = stringPreferencesKey("notification_sound_type")
        val AZKAR_SEED_VERSION = intPreferencesKey("azkar_seed_version")
        val MORNING_REMINDER_ENABLED = booleanPreferencesKey("morning_reminder_enabled")
        val MORNING_REMINDER_HOUR = intPreferencesKey("morning_reminder_hour")
        val MORNING_REMINDER_MINUTE = intPreferencesKey("morning_reminder_minute")
        val EVENING_REMINDER_ENABLED = booleanPreferencesKey("evening_reminder_enabled")
        val EVENING_REMINDER_HOUR = intPreferencesKey("evening_reminder_hour")
        val EVENING_REMINDER_MINUTE = intPreferencesKey("evening_reminder_minute")
        val MORNING_REMINDER_LAST_RUN = longPreferencesKey("morning_reminder_last_run")
        val EVENING_REMINDER_LAST_RUN = longPreferencesKey("evening_reminder_last_run")
    }

    val fontSizeFlow: Flow<Float> = store.data.map { preferences ->
        preferences[FONT_SIZE] ?: 24f
    }

    val prayerOffsetsFlow: Flow<com.example.feature.prayer.domain.model.PrayerOffsets> = store.data.map { prefs ->
        val values = PRAYER_OFFSET_KEYS.map { (prefs[it] ?: 0).coerceIn(-120, 120) }
        com.example.feature.prayer.domain.model.PrayerOffsets(values[0], values[1], values[2], values[3], values[4], values[5])
    }
    suspend fun setPrayerOffsets(offsets: com.example.feature.prayer.domain.model.PrayerOffsets) {
        val values = listOf(offsets.fajrMinutes, offsets.sunriseMinutes, offsets.dhuhrMinutes,
            offsets.asrMinutes, offsets.maghribMinutes, offsets.ishaMinutes)
        require(values.all { it in -120..120 }) { "تعديل الموعد يجب أن يكون بين -120 و120 دقيقة" }
        store.edit { prefs -> PRAYER_OFFSET_KEYS.zip(values).forEach { (key, value) -> prefs[key] = value } }
    }

    val vibrationEnabledFlow: Flow<Boolean> = store.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    val darkModeFlow: Flow<Boolean> = store.data.map { preferences ->
        when (preferences[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT, ThemeMode.SYSTEM -> false
            null -> preferences[DARK_MODE] ?: false
        }
    }

    val themeModeFlow: Flow<ThemeMode> = store.data.map { preferences ->
        preferences[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: if (preferences.contains(DARK_MODE)) {
                if (preferences[DARK_MODE] == true) ThemeMode.DARK else ThemeMode.LIGHT
            } else {
                ThemeMode.SYSTEM
            }
    }

    val calculationMethodFlow: Flow<String> = store.data.map { preferences ->
        preferences[CALCULATION_METHOD] ?: "MUSLIM_WORLD_LEAGUE"
    }

    val madhabFlow: Flow<String> = store.data.map { preferences ->
        preferences[MADHAB] ?: "SHAFI"
    }

    val useAutoLocationFlow: Flow<Boolean> = store.data.map { preferences ->
        preferences[USE_AUTO_LOCATION] ?: true
    }

    val manualLocationCityFlow: Flow<String> = store.data.map { preferences ->
        preferences[MANUAL_LOCATION_CITY] ?: "حلب"
    }

    val manualLocationLatFlow: Flow<Double> = store.data.map { preferences ->
        preferences[MANUAL_LOCATION_LAT] ?: 36.2021
    }

    val manualLocationLngFlow: Flow<Double> = store.data.map { preferences ->
        preferences[MANUAL_LOCATION_LNG] ?: 37.1343
    }

    val prePrayerNotificationMinutesFlow: Flow<Int> = store.data.map { preferences ->
        preferences[PRE_PRAYER_NOTIFICATION_MINUTES] ?: 0 // 0 means disabled
    }

    val iqamahNotificationMinutesFlow: Flow<Int> = store.data.map { preferences ->
        preferences[IQAMAH_NOTIFICATION_MINUTES] ?: 0 // 0 means disabled
    }

    val notificationSoundTypeFlow: Flow<String> = store.data.map { preferences ->
        preferences[NOTIFICATION_SOUND_TYPE] ?: "DEFAULT_ATHAN"
    }

    val prayerNotificationsEnabledFlow: Flow<Boolean> = store.data.map { preferences ->
        preferences[PRAYER_NOTIFICATIONS_ENABLED] ?: true
    }

    val morningReminderEnabledFlow = store.data.map { it[MORNING_REMINDER_ENABLED] ?: false }
    val morningReminderHourFlow = store.data.map { it[MORNING_REMINDER_HOUR] ?: 7 }
    val morningReminderMinuteFlow = store.data.map { it[MORNING_REMINDER_MINUTE] ?: 0 }
    val eveningReminderEnabledFlow = store.data.map { it[EVENING_REMINDER_ENABLED] ?: false }
    val eveningReminderHourFlow = store.data.map { it[EVENING_REMINDER_HOUR] ?: 17 }
    val eveningReminderMinuteFlow = store.data.map { it[EVENING_REMINDER_MINUTE] ?: 0 }
    val morningReminderLastRunFlow = store.data.map { it[MORNING_REMINDER_LAST_RUN] }
    val eveningReminderLastRunFlow = store.data.map { it[EVENING_REMINDER_LAST_RUN] }

    suspend fun setFontSize(size: Float) {
        store.edit { preferences ->
            preferences[FONT_SIZE] = size
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        store.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        store.edit { preferences ->
            preferences[DARK_MODE] = enabled
            preferences[THEME_MODE] = if (enabled) ThemeMode.DARK.name else ThemeMode.LIGHT.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { preferences ->
            preferences[THEME_MODE] = mode.name
            if (mode != ThemeMode.SYSTEM) preferences[DARK_MODE] = mode == ThemeMode.DARK
        }
    }

    suspend fun setCalculationMethod(method: String) {
        store.edit { preferences ->
            preferences[CALCULATION_METHOD] = method
        }
    }

    suspend fun setMadhab(madhab: String) {
        store.edit { preferences ->
            preferences[MADHAB] = madhab
        }
    }

    suspend fun setUseAutoLocation(enabled: Boolean) {
        store.edit { preferences ->
            preferences[USE_AUTO_LOCATION] = enabled
        }
    }

    suspend fun setManualLocation(city: String, lat: Double, lng: Double) {
        store.edit { preferences ->
            preferences[MANUAL_LOCATION_CITY] = city
            preferences[MANUAL_LOCATION_LAT] = lat
            preferences[MANUAL_LOCATION_LNG] = lng
            preferences[USE_AUTO_LOCATION] = false
        }
    }

    suspend fun setPrePrayerNotificationMinutes(minutes: Int) {
        store.edit { preferences ->
            preferences[PRE_PRAYER_NOTIFICATION_MINUTES] = minutes
        }
    }

    suspend fun setIqamahNotificationMinutes(minutes: Int) {
        store.edit { preferences ->
            preferences[IQAMAH_NOTIFICATION_MINUTES] = minutes
        }
    }

    suspend fun setNotificationSoundType(type: String) {
        store.edit { preferences ->
            preferences[NOTIFICATION_SOUND_TYPE] = type
        }
    }

    suspend fun setPrayerNotificationsEnabled(enabled: Boolean) {
        store.edit { it[PRAYER_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setMorningReminder(enabled: Boolean, hour: Int, minute: Int) {
        store.edit {
            it[MORNING_REMINDER_ENABLED] = enabled
            it[MORNING_REMINDER_HOUR] = hour
            it[MORNING_REMINDER_MINUTE] = minute
        }
    }

    suspend fun setEveningReminder(enabled: Boolean, hour: Int, minute: Int) {
        store.edit {
            it[EVENING_REMINDER_ENABLED] = enabled
            it[EVENING_REMINDER_HOUR] = hour
            it[EVENING_REMINDER_MINUTE] = minute
        }
    }

    suspend fun recordReminderRun(kind: String, timestamp: Long) {
        store.edit {
            val key = if (kind == "MORNING") MORNING_REMINDER_LAST_RUN else EVENING_REMINDER_LAST_RUN
            it[key] = timestamp
        }
    }
}
