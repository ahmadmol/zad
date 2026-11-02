package com.example.feature.core.data.local.database
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.quran.data.local.entity.BookmarkEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class BookmarkTransactionTest {
 @Test fun concurrentToggleAndProjectionRetainOrphansWithoutDuplicateNewRows() = runBlocking {
  val context=ApplicationProvider.getApplicationContext<Context>()
  val db=Room.inMemoryDatabaseBuilder(context,IhsanDatabase::class.java).build()
  try {
   val dao=db.quranDao()
   dao.insertBookmark(BookmarkEntity(id=5,surahId=999,verseNumber=1,timestamp=1))
   coroutineScope { repeat(40) { launch(Dispatchers.IO) { dao.toggleBookmarkAtomically(1,1) } } }
   assertFalse(dao.isBookmarkedNow(1,1))
   val orphan=dao.observeBookmarkDetails().first().single()
   assertEquals(5L,orphan.id);assertEquals("",orphan.verseText)
   dao.toggleBookmarkAtomically(1,1);assertTrue(dao.isBookmarkedNow(1,1))
  } finally { db.close() }
 }
}
