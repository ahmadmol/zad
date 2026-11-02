package com.example.feature.core.data.local.database
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.duas.data.local.entity.DuaEntity
import com.example.feature.hadith.data.local.entity.HadithEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class LibraryFavoriteTransactionTest {
 @Test fun concurrentTogglesPreserveRowsAndOtherFields()=runBlocking {
  val context=ApplicationProvider.getApplicationContext<Context>()
  val db=Room.inMemoryDatabaseBuilder(context,IhsanDatabase::class.java).build()
  try {
   val dua=DuaEntity(id=1,title="QA",text="synthetic",category="QA",source="QA",reference="QA",isFavorite=false)
   val hadith=HadithEntity(id=2,text="synthetic",narrator="QA",source="QA",category="QA",isFavorite=false)
   db.duaDao().insertDuas(listOf(dua));db.hadithDao().insertHadiths(listOf(hadith))
   coroutineScope { repeat(40) { launch(Dispatchers.IO) { db.duaDao().toggleFavorite(1);db.hadithDao().toggleFavorite(2) } } }
   assertEquals(dua,db.duaDao().getAllDuas().first().single());assertEquals(hadith,db.hadithDao().getAllHadiths().first().single())
  } finally { db.close() }
 }
}
