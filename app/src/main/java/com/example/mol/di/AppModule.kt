package com.example.mol.di

import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.azkar.util.DateProvider
import com.example.feature.azkar.util.DateProviderImpl
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
}

val appModule = module {
    includes(
        coreModule,
        databaseModule,
        repositoryModule,
        useCaseModule,
        viewModelModule,
        quranModule
    )
}
