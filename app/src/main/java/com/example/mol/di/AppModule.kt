package com.example.mol.di

import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.azkar.util.DateProvider
import com.example.feature.azkar.util.DateProviderImpl
import com.example.feature.core.observability.AppEventReporter
import com.example.feature.core.observability.AppLogger
import com.example.feature.core.observability.NoOpAppEventReporter
import com.example.feature.core.observability.NoOpAppLogger
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.prayer.util.PrayerNotificationScheduler
import com.example.feature.quran.quranModule
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { UserPreferences(androidContext()) }
    single { SettingsManager(androidContext()) }
    single { PrayerNotificationScheduler(androidContext()) }
    single<DateProvider> { DateProviderImpl() }
    // Vendor-neutral until an observability provider is approved.
    single<AppLogger> { NoOpAppLogger }
    single<AppEventReporter> { NoOpAppEventReporter }
}

val appModule = module {
    includes(
        coreModule,
        databaseModule,
        repositoryModule,
        useCaseModule,
        viewModelModule,
        quranModule,
        com.example.feature.prayer.di.prayerDomainModule,
        com.example.feature.dashboard.di.homeDashboardModule,
        com.example.feature.ihsanplus.integration.di.ihsanPlusProductionModule
    )
}
