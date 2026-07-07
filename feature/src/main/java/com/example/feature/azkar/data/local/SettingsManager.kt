package com.example.feature.azkar.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val FONT_SIZE = floatPreferencesKey("font_size")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        val MADHAB = stringPreferencesKey("madhab")
        val USE_AUTO_LOCATION = booleanPreferencesKey("use_auto_location")
        val MANUAL_LOCATION_CITY = stringPreferencesKey("manual_location_city")
        val MANUAL_LOCATION_LAT = doublePreferencesKey("manual_location_lat")
        val MANUAL_LOCATION_LNG = doublePreferencesKey("manual_location_lng")
        val PRE_PRAYER_NOTIFICATION_MINUTES = intPreferencesKey("pre_prayer_notification_minutes")
        val IQAMAH_NOTIFICATION_MINUTES = intPreferencesKey("iqamah_notification_minutes")
        val NOTIFICATION_SOUND_TYPE = stringPreferencesKey("notification_sound_type")
    }

    val fontSizeFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[FONT_SIZE] ?: 24f
    }

    val vibrationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: false
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
}
