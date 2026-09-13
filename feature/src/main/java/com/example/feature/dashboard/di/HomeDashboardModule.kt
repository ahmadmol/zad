package com.example.feature.dashboard.di

import com.example.feature.dashboard.data.DailyActivityRepositoryImpl
import com.example.feature.dashboard.domain.repository.DailyActivityRepository
import com.example.feature.dashboard.domain.usecase.ObserveHomeAsmaSummaryUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomeCharitySummaryUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomeDailyActivitiesUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomeDhikrSummaryUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomePrayerSummaryUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomeProfileSummaryUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomeQuranSummaryUseCase
import com.example.feature.dashboard.domain.usecase.RefreshHomeDashboardUseCase
import com.example.feature.dashboard.presentation.HomeDashboardViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeDashboardModule = module {
    single<DailyActivityRepository> { DailyActivityRepositoryImpl(get()) }

    single { ObserveHomeProfileSummaryUseCase(get()) }
    single { ObserveHomePrayerSummaryUseCase(get()) }
    single { ObserveHomeQuranSummaryUseCase(get(), get()) }
    single { ObserveHomeDailyActivitiesUseCase(get()) }
    single { ObserveHomeDhikrSummaryUseCase(get()) }
    single { ObserveHomeAsmaSummaryUseCase(get()) }
    single { ObserveHomeCharitySummaryUseCase(get()) }
    single { RefreshHomeDashboardUseCase(get(), get()) }

    viewModel {
        HomeDashboardViewModel(
            observeHomeProfile = get(),
            observeHomePrayer = get(),
            observeHomeQuran = get(),
            observeDailyActivities = get(),
            observeHomeDhikr = get(),
            observeHomeAsma = get(),
            observeHomeCharity = get(),
            refreshHome = get(),
            updatePrayerSettings = get(),
            prayerLocationRepository = get()
        )
    }
}
