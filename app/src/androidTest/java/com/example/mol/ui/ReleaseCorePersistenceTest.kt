package com.example.mol.ui

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.feature.core.data.local.database.IhsanDatabase
import com.example.feature.quran.domain.repository.QuranRepository
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.azkar.data.local.entity.ZikrEntity
import com.example.feature.azkar.data.repository.AzkarRepositoryImpl
import com.example.feature.azkar.util.DateProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

@RunWith(AndroidJUnit4::class)
class ReleaseCorePersistenceTest {
    @Test fun canonicalEditionUpdateRetainsVerseIdAndBookmark() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, IhsanDatabase::class.java).build()
        try {
            val dao = db.quranDao()
            dao.insertAyahs(listOf(com.example.feature.quran.data.local.entity.AyahEntity(
                id = 901, surahId = 2, verseNumber = 255, text = "old edition")))
            dao.insertBookmark(com.example.feature.quran.data.local.entity.BookmarkEntity(surahId=2, verseNumber=255))
            val loader = com.example.feature.quran.data.local.QuranAssetLoader(context, dao)
            loader.loadIfNeeded(); loader.loadIfNeeded()
            assertEquals(6236, dao.countAyahs())
            assertEquals(901L, dao.getAyahBySurahAndVerse(2,255)!!.id)
            assertTrue(dao.observeIsBookmarked(2,255).first())
            assertTrue(dao.getAyahBySurahAndVerse(2,255)!!.text.startsWith("ٱللَّهُ"))
        } finally { db.close() }
    }
    @Test fun quranRealRoomSearchBookmarksAndPreferencesSurviveNewRepositoryReads() = runBlocking {
        val koin = GlobalContext.get()
        val repo = koin.get<QuranRepository>()
        assertEquals(114, repo.getAllSurahs().size)
        assertEquals(286, repo.getAyahsBySurah(2).size)
        val found = repo.searchAyahs("الله لا اله الا هو الحي القيوم")
        assertTrue("Unvocalized Arabic must find 2:255", found.any { it.surahId == 2 && it.verseNumber == 255 })
        val oldBookmark = repo.observeIsBookmarked(2, 255).first()
        repo.toggleBookmark(2,255)
        assertEquals(!oldBookmark, repo.observeIsBookmarked(2,255).first())
        repo.toggleBookmark(2,255)
        assertEquals(oldBookmark, repo.observeIsBookmarked(2,255).first())
        val preferences = koin.get<UserPreferences>()
        val previousFont = preferences.quranFontSize.first()
        preferences.setQuranFontSize(34f)
        val independentReader = UserPreferences(InstrumentationRegistry.getInstrumentation().targetContext)
        assertEquals(34f, independentReader.quranFontSize.first())
        preferences.setQuranFontSize(previousFont)
    }

    @Test fun realSqlDailyRolloverRetainsFavoritesAndTasbihTotal() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, IhsanDatabase::class.java).build()
        try {
            val dao = db.azkarDao()
            dao.insertZikr(ZikrEntity(id=1, text="ذكر الاختبار", category="أذكار الصباح", targetCount=3, currentCount=3,
                isFavorite=true, lastUpdatedDate="2026-10-13"))
            dao.insertZikr(ZikrEntity(id=2, text="عدّاد الاختبار", category="تسبيح", targetCount=0, currentCount=10, lastUpdatedDate="2026-10-13"))
            val repo = AzkarRepositoryImpl(dao, object : DateProvider { override fun getCurrentDate()="2026-10-14" })
            repo.incrementCounter(1); repo.incrementCounter(2)
            assertEquals(1, dao.getZikrById(1)!!.currentCount)
            assertTrue(dao.getZikrById(1)!!.isFavorite)
            assertEquals(11, dao.getZikrById(2)!!.currentCount)
            assertEquals(2, dao.getStatByDate("2026-10-14")!!.totalCount)
        } finally { db.close() }
    }
}
