package com.example.mol.di

import com.example.feature.asma.domain.usecase.GetAsmaUseCase
import com.example.feature.asma.domain.usecase.LoadAsmaUseCase
import com.example.feature.azkar.domain.usecase.*
import com.example.feature.ehsan.domain.usecase.*
import com.example.feature.hadith.domain.usecase.GetHadithsUseCase
import com.example.feature.qibla.domain.usecase.GetQiblaDirectionUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { GetAsmaUseCase(get()) }
    single { LoadAsmaUseCase(get()) }

    single { GetAzkarUseCase(get()) }
    single { IncrementCounterUseCase(get()) }
    single { ResetCounterUseCase(get()) }
    single { ToggleFavoriteUseCase(get()) }
    single { AddCustomZikrUseCase(get()) }
    single { GetLast7DaysStatsUseCase(get()) }

    single { GetHadithsUseCase(get()) }

    single { GetQiblaDirectionUseCase() }

    single { AddDonationUseCase(get()) }
    single { GetDonationByIdUseCase(get()) }
    single { GetDonationsUseCase(get()) }
    single { GetMyDonationsUseCase(get()) }
    single { UpdateDonationStatusUseCase(get()) }
    single { DeleteDonationUseCase(get()) }
}
