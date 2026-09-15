package com.example.feature.prayer.data.settings

import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.domain.model.PrayerCalculationMethod
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerMadhhab
import com.example.feature.prayer.domain.model.PrayerOffsets
import com.example.feature.prayer.domain.repository.PrayerSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class PrayerSettingsRepositoryImpl(
    private val settingsManager: SettingsManager
) : PrayerSettingsRepository {

    override fun observeSettings(): Flow<PrayerCalculationSettings> = combine(
        settingsManager.calculationMethodFlow,
        settingsManager.madhabFlow,
        settingsManager.useAutoLocationFlow,
        settingsManager.prePrayerNotificationMinutesFlow,
        settingsManager.iqamahNotificationMinutesFlow
    ) { method, madhab, auto, pre, iqamah ->
        PrayerCalculationSettings(
            method = AdhanPrayerCalculator.parseMethod(method),
            madhhab = AdhanPrayerCalculator.parseMadhhab(madhab),
            offsets = PrayerOffsets(),
            useAutoLocation = auto,
            prePrayerNotificationMinutes = pre,
            iqamahNotificationMinutes = iqamah,
            notificationsEnabled = true
        )
    }.let { settingsFlow ->
        combine(settingsFlow, settingsManager.prayerNotificationsEnabledFlow) { settings, enabled ->
            settings.copy(notificationsEnabled = enabled)
        }
    }

    override suspend fun updateCalculationMethod(method: PrayerCalculationMethod) {
        settingsManager.setCalculationMethod(method.name)
    }

    override suspend fun updateMadhhab(madhhab: PrayerMadhhab) {
        settingsManager.setMadhab(madhhab.name)
    }

    override suspend fun updateOffsets(offsets: PrayerOffsets) {
        // Offsets persistence not present in SettingsManager yet; no-op keeps single settings source.
    }

    override suspend fun updateUseAutoLocation(enabled: Boolean) {
        settingsManager.setUseAutoLocation(enabled)
    }

    override suspend fun updateManualLocation(city: String, latitude: Double, longitude: Double) {
        settingsManager.setManualLocation(city, latitude, longitude)
    }

    override suspend fun updatePrePrayerMinutes(minutes: Int) {
        settingsManager.setPrePrayerNotificationMinutes(minutes)
    }

    override suspend fun updateIqamahMinutes(minutes: Int) {
        settingsManager.setIqamahNotificationMinutes(minutes)
    }

    override suspend fun updateNotificationSoundType(type: String) {
        settingsManager.setNotificationSoundType(type)
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        settingsManager.setPrayerNotificationsEnabled(enabled)
    }
}
