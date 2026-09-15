package com.example.feature.azkar.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    enum class ThemeMode { SYSTEM, LIGHT, DARK }

    /**
     * Internal DataStore handle for code paths that need direct read/write
     * access (e.g. the Azkar seed manager storing its version key). Kept
     * package-private to avoid leaking DataStore access to the rest of the app.
     */
    internal val dataStoreInternal: DataStore<Preferences> = context.dataStore

    companion object {
        val FONT_SIZE = floatPreferencesKey("font_size")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PRAYER_NOTIFICATIONS_ENABLED = booleanPreferencesKey("prayer_notifications_enabled")
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

    val fontSizeFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[FONT_SIZE] ?: 24f
    }

    val vibrationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        when (preferences[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT, ThemeMode.SYSTEM -> false
            null -> preferences[DARK_MODE] ?: false
        }
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: if (preferences.contains(DARK_MODE)) {
                if (preferences[DARK_MODE] == true) ThemeMode.DARK else ThemeMode.LIGHT
            } else {
                ThemeMode.SYSTEM
            }
    }

    val calculationMethodFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CALCULATION_METHOD] ?: "MUSLIM_WORLD_LEAGUE"
    }

    val madhabFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[MADHAB] ?: "SHAFI"
    }

    val useAutoLocationFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_AUTO_LOCATION] ?: true
    }

    val manualLocationCityFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[MANUAL_LOCATION_CITY] ?: "حلب"
    }

    val manualLocationLatFlow: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[MANUAL_LOCATION_LAT] ?: 36.2021
    }

    val manualLocationLngFlow: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[MANUAL_LOCATION_LNG] ?: 37.1343
    }

    val prePrayerNotificationMinutesFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PRE_PRAYER_NOTIFICATION_MINUTES] ?: 0 // 0 means disabled
    }

    val iqamahNotificationMinutesFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[IQAMAH_NOTIFICATION_MINUTES] ?: 0 // 0 means disabled
    }

    val notificationSoundTypeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_SOUND_TYPE] ?: "DEFAULT_ATHAN"
    }

    val prayerNotificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PRAYER_NOTIFICATIONS_ENABLED] ?: true
    }

    val morningReminderEnabledFlow = context.dataStore.data.map { it[MORNING_REMINDER_ENABLED] ?: false }
    val morningReminderHourFlow = context.dataStore.data.map { it[MORNING_REMINDER_HOUR] ?: 7 }
    val morningReminderMinuteFlow = context.dataStore.data.map { it[MORNING_REMINDER_MINUTE] ?: 0 }
    val eveningReminderEnabledFlow = context.dataStore.data.map { it[EVENING_REMINDER_ENABLED] ?: false }
    val eveningReminderHourFlow = context.dataStore.data.map { it[EVENING_REMINDER_HOUR] ?: 17 }
    val eveningReminderMinuteFlow = context.dataStore.data.map { it[EVENING_REMINDER_MINUTE] ?: 0 }
    val morningReminderLastRunFlow = context.dataStore.data.map { it[MORNING_REMINDER_LAST_RUN] }
    val eveningReminderLastRunFlow = context.dataStore.data.map { it[EVENING_REMINDER_LAST_RUN] }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
            preferences[THEME_MODE] = if (enabled) ThemeMode.DARK.name else ThemeMode.LIGHT.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
            if (mode != ThemeMode.SYSTEM) preferences[DARK_MODE] = mode == ThemeMode.DARK
        }
    }

    suspend fun setCalculationMethod(method: String) {
        context.dataStore.edit { preferences ->
            preferences[CALCULATION_METHOD] = method
        }
    }

    suspend fun setMadhab(madhab: String) {
        context.dataStore.edit { preferences ->
            preferences[MADHAB] = madhab
        }
    }

    suspend fun setUseAutoLocation(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_AUTO_LOCATION] = enabled
        }
    }

    suspend fun setManualLocation(city: String, lat: Double, lng: Double) {
        context.dataStore.edit { preferences ->
            preferences[MANUAL_LOCATION_CITY] = city
            preferences[MANUAL_LOCATION_LAT] = lat
            preferences[MANUAL_LOCATION_LNG] = lng
            preferences[USE_AUTO_LOCATION] = false
        }
    }

    suspend fun setPrePrayerNotificationMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PRE_PRAYER_NOTIFICATION_MINUTES] = minutes
        }
    }

    suspend fun setIqamahNotificationMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[IQAMAH_NOTIFICATION_MINUTES] = minutes
        }
    }

    suspend fun setNotificationSoundType(type: String) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_SOUND_TYPE] = type
        }
    }

    suspend fun setPrayerNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PRAYER_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setMorningReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[MORNING_REMINDER_ENABLED] = enabled
            it[MORNING_REMINDER_HOUR] = hour
            it[MORNING_REMINDER_MINUTE] = minute
        }
    }

    suspend fun setEveningReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[EVENING_REMINDER_ENABLED] = enabled
            it[EVENING_REMINDER_HOUR] = hour
            it[EVENING_REMINDER_MINUTE] = minute
        }
    }

    suspend fun recordReminderRun(kind: String, timestamp: Long) {
        context.dataStore.edit {
            val key = if (kind == "MORNING") MORNING_REMINDER_LAST_RUN else EVENING_REMINDER_LAST_RUN
            it[key] = timestamp
        }
    }
}
