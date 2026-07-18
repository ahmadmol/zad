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
import org.koin.dsl.module
import com.example.feature.settings.presentation.SettingsViewModel
import com.example.feature.statistics.presentation.StatisticsViewModel
import com.example.feature.tasbih.presentation.TasbihViewModel

val viewModelModule = module {
    viewModelOf(::AzkarViewModel)
    viewModelOf(::DuaViewModel)
    viewModelOf(::AsmaViewModel)
    // HomeDashboardViewModel registered in homeDashboardModule
    viewModelOf(::TasbihViewModel)
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
