package com.example.feature.prayer.domain.usecase

import com.example.feature.prayer.domain.calculator.NextPrayerSelector
import com.example.feature.prayer.domain.calculator.PrayerCalculator
import com.example.feature.prayer.domain.clock.PrayerClock
import com.example.feature.prayer.domain.model.NextPrayer
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.example.feature.prayer.domain.repository.PrayerSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ObservePrayerDayUseCase(
    private val locationRepository: PrayerLocationRepository,
    private val settingsRepository: PrayerSettingsRepository,
    private val calculator: PrayerCalculator,
    private val clock: PrayerClock
) {
    operator fun invoke(date: LocalDate = clock.today()): Flow<PrayerDay?> =
        combine(
            locationRepository.observeLocation(),
            settingsRepository.observeSettings()
        ) { locationState, settings ->
            when (locationState) {
                is PrayerLocationState.Available -> calculator.calculate(
                    date = date,
                    location = locationState.location,
                    settings = settings,
                    timeZoneId = clock.zoneId().id,
                    nowEpochMillis = clock.nowEpochMillis()
                )
                else -> null
            }
        }
}

class ObserveNextPrayerUseCase(
    private val locationRepository: PrayerLocationRepository,
    private val settingsRepository: PrayerSettingsRepository,
    private val calculator: PrayerCalculator,
    private val clock: PrayerClock
) {
    operator fun invoke(): Flow<NextPrayer?> =
        combine(
            locationRepository.observeLocation(),
            settingsRepository.observeSettings()
        ) { locationState, settings ->
            when (locationState) {
                is PrayerLocationState.Available -> {
                    val zone = clock.zoneId().id
                    val now = clock.nowEpochMillis()
                    val today = calculator.calculate(clock.today(), locationState.location, settings, zone, now)
                    val tomorrow = calculator.calculate(
                        clock.today().plusDays(1),
                        locationState.location,
                        settings,
                        zone,
                        now
                    )
                    NextPrayerSelector.select(today, tomorrow, now)
                }
                else -> null
            }
        }
}

class RefreshPrayerLocationUseCase(
    private val locationRepository: PrayerLocationRepository
) {
    suspend operator fun invoke() = locationRepository.refreshLocation()
}

class UpdatePrayerSettingsUseCase(
    private val settingsRepository: PrayerSettingsRepository
) {
    suspend fun updateMethod(method: com.example.feature.prayer.domain.model.PrayerCalculationMethod) =
        settingsRepository.updateCalculationMethod(method)

    suspend fun updateMadhhab(madhhab: com.example.feature.prayer.domain.model.PrayerMadhhab) =
        settingsRepository.updateMadhhab(madhhab)

    suspend fun updatePrePrayer(minutes: Int) =
        settingsRepository.updatePrePrayerMinutes(minutes)

    suspend fun updateIqamah(minutes: Int) =
        settingsRepository.updateIqamahMinutes(minutes)

    suspend fun updateUseAutoLocation(enabled: Boolean) =
        settingsRepository.updateUseAutoLocation(enabled)
}
