package com.example.mol.ui

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.feature.core.data.local.database.IhsanDatabase
import com.example.feature.hadith.data.repository.HadithRepositoryImpl
import com.example.feature.quran.util.AudioPlayerHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import java.io.File

@RunWith(AndroidJUnit4::class)
class ReleaseContentAndPlayerTest {
    @Test fun verifiedDuaEditionPopulatesAllSectionsAndRetainsFavorites() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,IhsanDatabase::class.java).build()
        try {
            val repo=com.example.feature.duas.data.repository.DuaRepositoryImpl(context,db.duaDao())
            repo.loadDuasIfNeeded()
            val rows=repo.getAllDuas().first()
            assertEquals(39,rows.size)
            com.example.feature.duas.presentation.DuaSections.titles.forEach { section ->
                assertTrue(section,rows.any { com.example.feature.duas.presentation.DuaSections.matches(section,it.category) })
            }
            repo.toggleFavorite(22201,false)
            val reopened=com.example.feature.duas.data.repository.DuaRepositoryImpl(context,db.duaDao())
            reopened.loadDuasIfNeeded()
            assertTrue(reopened.getAllDuas().first().first { it.id==22201L }.isFavorite)
        } finally { db.close() }
    }
    @Test fun publisherCorpusLoadsAllRecordsAndFavoritesSurviveRepopulation() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,IhsanDatabase::class.java).build()
        try {
            val repo=HadithRepositoryImpl(db.hadithDao(),context)
            repo.initialPopulation()
            val rows=repo.getAllHadiths().first()
            assertEquals(3582,rows.size)
            assertTrue(rows.all { it.source.contains("HadeethEnc.com — v1.7.0") && it.source.contains("https://hadeethenc.com/ar/browse/hadith/") })
            val first=rows.first();repo.toggleFavorite(first.id)
            val reopened=HadithRepositoryImpl(db.hadithDao(),context);reopened.initialPopulation()
            assertTrue(reopened.getAllHadiths().first().first { it.id==first.id }.isFavorite)
            assertEquals(3582,db.hadithDao().getCount())
        } finally { db.close() }
    }

    @Test fun actualMediaSessionPlaysPausesAndSeeksOnlyNeutralSyntheticTestAudio() = runBlocking {
        val inst=InstrumentationRegistry.getInstrumentation()
        val file=File(inst.targetContext.cacheDir,"synthetic-player-test.wav")
        inst.context.assets.open("synthetic-player-test.wav").use { source -> file.outputStream().use(source::copyTo) }
        val player=withContext(Dispatchers.Main) { GlobalContext.get().get<AudioPlayerHandler>() }
        try {
            assertTrue(player.playAyah(android.net.Uri.fromFile(file).toString()))
            withTimeout(20_000) { player.isPlaying.first { it } }
            withTimeout(10_000) { player.duration.first { it > 0L } }
            assertTrue(player.duration.value in 5900L..6100L)
            withContext(Dispatchers.Main) { player.pause() }
            withTimeout(5_000) { player.isPlaying.first { !it } }
            withContext(Dispatchers.Main) { player.seekTo(2000);player.togglePlay() }
            withTimeout(5_000) { player.currentPosition.first { it >= 1900L } }
            assertTrue(player.currentPosition.value < 6000L)
        } finally { withContext(Dispatchers.Main) { player.pause() };file.delete() }
    }
}
