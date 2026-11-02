package com.example.feature.ihsanplus.daily.di

import com.example.feature.ihsanplus.daily.data.DemoIhsanPlusDailyDataSource
import com.example.feature.ihsanplus.daily.data.DemoIhsanPlusDailyRepository
import com.example.feature.ihsanplus.daily.domain.repository.IhsanPlusDailyRepository
import com.example.feature.ihsanplus.daily.domain.usecase.GetIhsanPlusDailyExperienceUseCase
import com.example.feature.ihsanplus.daily.presentation.IhsanPlusDailyViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Demo-only Koin module. Must NOT be included in production [com.example.mol.di.appModule].
 */
val ihsanPlusDailyModule = module {
    single { DemoIhsanPlusDailyDataSource() }
    single<IhsanPlusDailyRepository> { DemoIhsanPlusDailyRepository(get()) }
    single { GetIhsanPlusDailyExperienceUseCase(get()) }
    viewModel { IhsanPlusDailyViewModel(get()) }
}
