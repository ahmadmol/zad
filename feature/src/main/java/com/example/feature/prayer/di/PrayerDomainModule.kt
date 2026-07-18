package com.example.feature.prayer.di

import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.data.diagnostics.DataStorePrayerEventRepository
import com.example.feature.prayer.data.location.PrayerLocationRepositoryImpl
import com.example.feature.prayer.data.scheduler.AndroidPrayerAlarmGateway
import com.example.feature.prayer.data.settings.PrayerSettingsRepositoryImpl
import com.example.feature.prayer.domain.calculator.PrayerCalculator
import com.example.feature.prayer.domain.clock.PrayerClock
import com.example.feature.prayer.domain.facade.DefaultPrayerTimesFacade
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.repository.PrayerAlarmGateway
import com.example.feature.prayer.domain.repository.PrayerEventRepository
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.example.feature.prayer.domain.repository.PrayerSettingsRepository
import com.example.feature.prayer.domain.usecase.ObserveNextPrayerUseCase
import com.example.feature.prayer.domain.usecase.ObservePrayerDayUseCase
import com.example.feature.prayer.domain.usecase.ObservePrayerSystemStatusUseCase
import com.example.feature.prayer.domain.usecase.PrayerSystemStatusStore
import com.example.feature.prayer.domain.usecase.ReconcilePrayerScheduleUseCase
import com.example.feature.prayer.domain.usecase.RefreshPrayerLocationUseCase
import com.example.feature.prayer.domain.usecase.UpdatePrayerSettingsUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val prayerDomainModule = module {
    single { PrayerClock() }
    single<PrayerCalculator> { AdhanPrayerCalculator() }
    single<PrayerSettingsRepository> { PrayerSettingsRepositoryImpl(get()) }
    single<PrayerLocationRepository> {
        PrayerLocationRepositoryImpl(androidContext(), get(), get())
    }
    single<PrayerAlarmGateway> { AndroidPrayerAlarmGateway(androidContext()) }
    single<PrayerEventRepository> { DataStorePrayerEventRepository(androidContext()) }
    single { PrayerSystemStatusStore() }

    single { RefreshPrayerLocationUseCase(get()) }
    single { UpdatePrayerSettingsUseCase(get()) }
    single {
        ReconcilePrayerScheduleUseCase(
            locationRepository = get(),
            settingsRepository = get(),
            calculator = get(),
            alarmGateway = get(),
            eventRepository = get(),
            clock = get(),
            systemStatusStore = get()
        )
    }
    single { ObservePrayerDayUseCase(get(), get(), get(), get()) }
    single { ObserveNextPrayerUseCase(get(), get(), get(), get()) }
    single { ObservePrayerSystemStatusUseCase(get(), get(), get()) }

    single<PrayerTimesFacade> {
        DefaultPrayerTimesFacade(
            locationRepository = get(),
            settingsRepository = get(),
            calculator = get(),
            clock = get(),
            refreshLocationUseCase = get(),
            updateSettingsUseCase = get(),
            reconcileUseCase = get(),
            systemStatusFlow = get<ObservePrayerSystemStatusUseCase>().invoke()
        )
    }
}
