package com.example.mol.di

import com.example.feature.asma.presentation.AsmaViewModel
import com.example.feature.azkar.presentation.AzkarViewModel
import com.example.feature.duas.presentation.DuaViewModel
import com.example.feature.hadith.presentation.HadithViewModel
import com.example.feature.components.AuthViewModel
import com.example.feature.ehsan.presentation.*
import com.example.feature.profile.presentation.*
import com.example.feature.qibla.presentation.QiblaViewModel
import com.example.feature.prayer.presentation.PrayerViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.example.feature.settings.presentation.SettingsViewModel
import com.example.feature.statistics.presentation.StatisticsViewModel
import com.example.feature.tasbih.presentation.TasbihViewModel
import com.example.feature.dashboard.presentation.GlobalLearningSearchViewModel
import com.example.feature.quran.presentation.ReadingJourneyViewModel

val viewModelModule = module {
    viewModel { com.example.feature.goals.DailyGoalsViewModel(get()) }
    viewModel { com.example.feature.splashScreen.StartupViewModel(get()) }
    viewModel { com.example.feature.fasting.presentation.FastingViewModel(get(), get()) }
    viewModel { com.example.feature.ramadan.RamadanViewModel(get(), get(), get(), get()) }
    viewModel { com.example.feature.calendar.IslamicCalendarViewModel(get()) }
    viewModel { com.example.feature.quran.presentation.MemorizationViewModel(get()) }
    viewModelOf(::ReadingJourneyViewModel)
    viewModel { com.example.feature.prayer.presentation.ManualPrayerTrackerViewModel(get(), savedState = get()) }
    viewModelOf(::AzkarViewModel)
    viewModelOf(::DuaViewModel)
    viewModelOf(::AsmaViewModel)
    // HomeDashboardViewModel registered in homeDashboardModule
    viewModelOf(::TasbihViewModel)
    viewModelOf(::GlobalLearningSearchViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::StatisticsViewModel)
    viewModelOf(::PrayerViewModel)
    viewModelOf(::QiblaViewModel)
    viewModelOf(::HadithViewModel)
    viewModelOf(::EhsanViewModel)
    viewModelOf(::AddEhsanViewModel)
    viewModelOf(::IhsanDetailsViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::EditProfileViewModel)
    viewModelOf(::AuthViewModel)
}
