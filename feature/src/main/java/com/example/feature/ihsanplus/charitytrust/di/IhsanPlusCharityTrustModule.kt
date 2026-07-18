package com.example.feature.ihsanplus.charitytrust.di

import com.example.feature.ihsanplus.charitytrust.data.DemoIhsanPlusCharityTrustRepository
import com.example.feature.ihsanplus.charitytrust.domain.repository.IhsanPlusCharityTrustRepository
import com.example.feature.ihsanplus.charitytrust.domain.usecase.BuildIhsanPlusTrustMessageUseCase
import com.example.feature.ihsanplus.charitytrust.domain.usecase.EvaluateIhsanPlusCaseTrustLevelUseCase
import com.example.feature.ihsanplus.charitytrust.domain.usecase.GetIhsanPlusCharityTrustDashboardUseCase
import com.example.feature.ihsanplus.charitytrust.presentation.IhsanPlusCharityTrustViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Demo-only Koin module with fabricated trust claims.
 * Must NOT be included in production [com.example.mol.di.appModule].
 */
val ihsanPlusCharityTrustModule = module {
    single<IhsanPlusCharityTrustRepository> { DemoIhsanPlusCharityTrustRepository() }

    factory { GetIhsanPlusCharityTrustDashboardUseCase(get()) }
    factory { BuildIhsanPlusTrustMessageUseCase() }
    factory { EvaluateIhsanPlusCaseTrustLevelUseCase() }

    viewModel { IhsanPlusCharityTrustViewModel(get()) }
}
