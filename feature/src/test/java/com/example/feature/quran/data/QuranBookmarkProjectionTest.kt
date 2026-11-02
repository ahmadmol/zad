package com.example.feature.quran.data
import com.example.feature.quran.data.local.dao.*
import com.example.feature.quran.data.local.QuranAssetLoader
import com.example.feature.quran.data.repository.QuranRepositoryImpl
import com.example.feature.core.preferences.UserPreferences
import io.mockk.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
class QuranBookmarkProjectionTest {
 @Test fun `all bookmark text comes from projection without per-row queries and toggle delegates atomic boundary`()=runTest {
  val dao=mockk<QuranDao>();every { dao.observeBookmarkDetails() } returns flowOf(listOf(BookmarkProjection(3,1,2,"الفاتحة","نص")))
  coEvery { dao.toggleBookmarkAtomically(1,2) } just Runs
  val repo=QuranRepositoryImpl(dao,mockk<DownloadDao>(),mockk<QuranAssetLoader>(),mockk<UserPreferences>())
  assertEquals("نص",repo.observeAllBookmarks().first().single().text)
  repo.toggleBookmark(1,2);coVerify(exactly=1) { dao.toggleBookmarkAtomically(1,2) }
  coVerify(exactly=0) { dao.getSurahById(any()) };coVerify(exactly=0) { dao.getAyahBySurahAndVerse(any(),any()) }
 }
}
