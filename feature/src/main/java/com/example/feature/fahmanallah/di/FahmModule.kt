package com.example.feature.fahmanallah.di

import com.example.feature.core.network.AndroidConnectivityMonitor
import com.example.feature.core.network.ConnectivityMonitor
import com.example.feature.fahmanallah.data.repository.FahmRepositoryImpl
import com.example.feature.fahmanallah.domain.repository.FahmRepository
import com.example.feature.fahmanallah.presentation.FahmViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val fahmModule = module {
    single<ConnectivityMonitor> { AndroidConnectivityMonitor(androidContext()) }
    single<FahmRepository> { FahmRepositoryImpl() }
    viewModel { FahmViewModel(get(), get()) }
}
