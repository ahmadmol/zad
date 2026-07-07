package com.example.feature.quran

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.quran.data.local.QuranAssetLoader
import com.example.feature.quran.data.local.dao.QuranDao
import com.example.feature.quran.data.local.database.QuranDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuranDataIngestionTest {

    private lateinit var db: QuranDatabase
    private lateinit var dao: QuranDao
    private lateinit var assetLoader: QuranAssetLoader
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        // Use in-memory database for testing to ensure a clean slate
        db = Room.inMemoryDatabaseBuilder(context, QuranDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.quranDao()
        assetLoader = QuranAssetLoader(context, dao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun verifyFullDataIngestion() = runBlocking {
        // Act: Trigger the load
        assetLoader.loadIfNeeded()

        // Assert: Verify counts
        val surahCount = dao.countSurahs()
        val ayahCount = dao.countAyahs()

        assertEquals("Should have exactly 114 Surahs", 114, surahCount)
        assertEquals("Should have exactly 6236 Ayahs", 6236, ayahCount)
    }

    @Test
    fun verifyIdempotency() = runBlocking {
        // Load once
        assetLoader.loadIfNeeded()
        val firstAyahCount = dao.countAyahs()

        // Load again
        assetLoader.loadIfNeeded()
        val secondAyahCount = dao.countAyahs()

        assertEquals("Data count should remain 6236 after second load", 6236, secondAyahCount)
        assertEquals("Count should not change between loads", firstAyahCount, secondAyahCount)
    }
}
