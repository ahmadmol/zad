package com.example.feature.ihsanplus.integration.di

import com.example.feature.ihsanplus.integration.adapter.ProductionCharitySourceAdapter
import com.example.feature.ihsanplus.integration.adapter.ProductionDailySourceAdapter
import com.example.feature.ihsanplus.integration.adapter.ProductionPrayerSourceAdapter
import com.example.feature.ihsanplus.integration.contract.IhsanPlusCharitySource
import com.example.feature.ihsanplus.integration.contract.IhsanPlusDailySource
import com.example.feature.ihsanplus.integration.contract.IhsanPlusPrayerSource
import com.example.feature.ihsanplus.integration.presentation.ControlledDailyViewModel
import com.example.feature.ihsanplus.integration.presentation.ControlledPrayerAssistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Production-only IhsanPlus bindings. Never register Demo* sources here.
 */
val ihsanPlusProductionModule = module {
    single<IhsanPlusDailySource> {
        ProductionDailySourceAdapter(
            profileUseCase = get(),
            quranUseCase = get(),
            dhikrUseCase = get(),
            dailyActivityRepository = get(),
            prayerFacade = get(),
            userPreferences = get()
        )
    }
    single<IhsanPlusPrayerSource> {
        ProductionPrayerSourceAdapter(prayerFacade = get())
    }
    single<IhsanPlusCharitySource> {
        ProductionCharitySourceAdapter(
            ehsanRepository = get(),
            userPreferences = get()
        )
    }
    viewModel { ControlledDailyViewModel(get()) }
    viewModel { ControlledPrayerAssistViewModel(get()) }
}
