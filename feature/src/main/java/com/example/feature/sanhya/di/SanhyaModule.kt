package com.example.feature.sanhya.di

import com.example.feature.sanhya.data.repository.SanhyaRepositoryImpl
import com.example.feature.sanhya.domain.repository.SanhyaRepository
import com.example.feature.sanhya.presentation.SanhyaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val sanhyaModule = module {
    single<SanhyaRepository> { SanhyaRepositoryImpl() }
    viewModel { SanhyaViewModel(get(), get(), get()) }
}
