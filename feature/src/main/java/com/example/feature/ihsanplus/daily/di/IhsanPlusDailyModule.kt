package com.example.feature.ihsanplus.daily.di

import com.example.feature.ihsanplus.daily.data.IhsanPlusDailyFakeDataSource
import com.example.feature.ihsanplus.daily.data.IhsanPlusDailyRepositoryImpl
import com.example.feature.ihsanplus.daily.domain.repository.IhsanPlusDailyRepository
import com.example.feature.ihsanplus.daily.domain.usecase.GetIhsanPlusDailyExperienceUseCase
import com.example.feature.ihsanplus.daily.presentation.IhsanPlusDailyViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val ihsanPlusDailyModule = module {
    single { IhsanPlusDailyFakeDataSource() }
    single<IhsanPlusDailyRepository> { IhsanPlusDailyRepositoryImpl(get()) }
    single { GetIhsanPlusDailyExperienceUseCase(get()) }
    viewModel { IhsanPlusDailyViewModel(get()) }
}
