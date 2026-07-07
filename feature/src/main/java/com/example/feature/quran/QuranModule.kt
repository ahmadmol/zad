package com.example.feature.quran

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.feature.quran.data.local.QuranAssetLoader
import com.example.feature.quran.data.repository.QuranRepositoryImpl
import com.example.feature.quran.domain.repository.QuranRepository
import com.example.feature.quran.domain.usecase.GetAyahsUseCase
import com.example.feature.quran.domain.usecase.GetSurahUseCase
import com.example.feature.quran.domain.usecase.GetSurahsUseCase
import com.example.feature.quran.domain.usecase.SearchAyahsUseCase
import com.example.feature.quran.presentation.QuranViewModel
import com.example.feature.quran.util.AudioPlayerHandler
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val quranModule = module {
    single { QuranAssetLoader(androidContext(), get()) }
    single<QuranRepository> { QuranRepositoryImpl(get(), get(), get(), get()) }
    single { AudioPlayerHandler(androidContext()) }
    single { GetSurahsUseCase(get()) }
    single { GetSurahUseCase(get()) }
    single { GetAyahsUseCase(get()) }
    single { SearchAyahsUseCase(get()) }
    viewModel { QuranViewModel(get(), get(), androidContext()) }
}
