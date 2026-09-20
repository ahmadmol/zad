package com.example.feature.nabiihsan.di

import com.example.feature.core.network.AndroidConnectivityMonitor
import com.example.feature.core.network.ConnectivityMonitor
import com.example.feature.nabiihsan.data.repository.NabiIhsanRepositoryImpl
import com.example.feature.nabiihsan.domain.repository.NabiIhsanRepository
import com.example.feature.nabiihsan.presentation.NabiIhsanViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val nabiIhsanModule = module {
    single<ConnectivityMonitor> { AndroidConnectivityMonitor(androidContext()) }
    single<NabiIhsanRepository> { NabiIhsanRepositoryImpl() }
    viewModel { NabiIhsanViewModel(get(), get()) }
}
