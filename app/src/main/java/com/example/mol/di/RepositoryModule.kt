package com.example.mol.di

import com.example.feature.asma.data.local.AsmaLocalDataSource
import com.example.feature.asma.data.repository.AsmaRepositoryImpl
import com.example.feature.asma.domain.repository.AsmaRepository
import com.example.feature.azkar.data.repository.AzkarRepositoryImpl
import com.example.feature.azkar.domain.repository.AzkarRepository
import com.example.feature.duas.data.repository.DuaRepositoryImpl
import com.example.feature.duas.domain.repository.DuaRepository
import com.example.feature.ehsan.data.repository.EhsanRepositoryImpl
import com.example.feature.ehsan.data.repository.UserRepositoryImpl
import com.example.feature.ehsan.domain.repository.EhsanRepository
import com.example.feature.ehsan.domain.repository.UserRepository
import com.example.feature.hadith.data.repository.HadithRepositoryImpl
import com.example.feature.hadith.domain.repository.HadithRepository
import com.example.feature.qibla.util.QiblaManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { AsmaLocalDataSource(androidContext()) }
    single<AsmaRepository> { AsmaRepositoryImpl(get()) }

    single<AzkarRepository> { AzkarRepositoryImpl(get(), get()) }
    single<DuaRepository> { DuaRepositoryImpl(androidContext(), get()) }

    single<EhsanRepository> { EhsanRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<HadithRepository> { HadithRepositoryImpl(get()) }

    single { QiblaManager(androidContext()) }
}
