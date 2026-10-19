package com.example.mol.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.feature.azkar.data.local.database.AzkarSeedManager
import com.example.feature.core.data.local.database.IhsanDatabase
import com.example.feature.quran.data.local.QuranAssetLoader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

/** Resolves the actual application graph, unlike isolated Room repository tests. */
@RunWith(AndroidJUnit4::class)
class ProductionStartupTest {
    @Test fun actualApplicationGraphSeedsAndReopensWithoutLosingContent() = runBlocking {
        val graph = GlobalContext.get()
        val seed = graph.get<AzkarSeedManager>()
        seed.ensureSeeded()
        graph.get<QuranAssetLoader>().loadIfNeeded()
        val db = graph.get<IhsanDatabase>()
        assertTrue(db.azkarDao().countZikr() >= 13)
        assertEquals(114, db.quranDao().countSurahs())
        assertEquals(6236, db.quranDao().countAyahs())
        assertEquals(0, seed.ensureSeeded())
        assertEquals(6236, db.quranDao().countAyahs())
    }
}
