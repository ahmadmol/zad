package com.example.mol.ui

import android.os.Process
import androidx.test.platform.app.InstrumentationRegistry
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.azkar.data.local.entity.ZikrEntity
import com.example.feature.core.data.local.database.IhsanDatabase
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.fahmanallah.data.repository.FahmRepositoryImpl
import com.example.feature.fahmanallah.domain.model.FahmEpisodeProgressState
import com.example.feature.quran.domain.repository.QuranRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.koin.core.context.GlobalContext
import java.io.File

/** Invoke prepare and verify in separate instrumentation processes with force-stop between. */
class AuditProcessRestartTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val backup get() = File(context.filesDir, "audit-process-backup.json")
    @Test fun prepareSavedState() = runBlocking {
        assertFalse("Finish restoring a previous run before preparing again", backup.exists())
        val koin = GlobalContext.get()
        val prefs = koin.get<UserPreferences>()
        val settings = koin.get<SettingsManager>()
        val quran = koin.get<QuranRepository>()
        quran.getAllSurahs()
        val saved = JSONObject().put("pid", Process.myPid())
            .put("mode", settings.themeModeFlow.first().name)
            .put("fahm", JSONArray(prefs.learningIds(FahmRepositoryImpl.STATE_KEY).first().toList()))
            .put("reader", prefs.quranReaderId.first() ?: "Alafasy_128kbps")
            .put("bookmark", quran.observeIsBookmarked(18, 10).first())
        backup.writeText(saved.toString())
        prefs.updateLearningIds(FahmRepositoryImpl.STATE_KEY) { emptySet() }
        FahmRepositoryImpl(prefs).apply { markCompleted("ep_29"); toggleFavorite("ep_29"); toggleWatchLater("ep_28") }
        settings.setThemeMode(SettingsManager.ThemeMode.DARK)
        prefs.setQuranReaderId("MaherAlMuaiqly128kbps")
        if (!saved.getBoolean("bookmark")) quran.toggleBookmark(18, 10)
        koin.get<IhsanDatabase>().azkarDao().insertZikr(ZikrEntity(id = -945901, title = "اختبار استدامة",
            text = "عداد اختبار آلي محلي", category = "تسبيح", targetCount = 0, currentCount = 12))
    }
    @Test fun verifySavedStateInNewProcessAndRestoreTestChanges() = runBlocking {
        val saved = JSONObject(backup.readText())
        val koin = GlobalContext.get()
        val prefs = koin.get<UserPreferences>()
        val quran = koin.get<QuranRepository>()
        try {
            assertNotEquals(saved.getInt("pid"), Process.myPid())
            val repo = koin.get<com.example.feature.fahmanallah.domain.repository.FahmRepository>()
            val ep = repo.getEpisodeById("ep_29").first()!!
            assertTrue(ep.isFavorite)
            assertEquals(FahmEpisodeProgressState.COMPLETED, ep.progressState)
            assertTrue(repo.getEpisodeById("ep_28").first()!!.isWatchLater)
            assertEquals(SettingsManager.ThemeMode.DARK, koin.get<SettingsManager>().themeModeFlow.first())
            assertEquals("MaherAlMuaiqly128kbps", prefs.quranReaderId.first())
            assertTrue(quran.observeIsBookmarked(18, 10).first())
            assertEquals(12, koin.get<IhsanDatabase>().azkarDao().getZikrById(-945901)!!.currentCount)
            File(context.getExternalFilesDir(null), "audit-process-restart.txt").writeText(
                "PASS: different PID after force-stop; Room counter/bookmark; DataStore reader/theme/Fahm completion/favorite/later\n")
        } finally {
            val values = saved.getJSONArray("fahm")
            prefs.updateLearningIds(FahmRepositoryImpl.STATE_KEY) { (0 until values.length()).map { values.getString(it) }.toSet() }
            koin.get<SettingsManager>().setThemeMode(SettingsManager.ThemeMode.valueOf(saved.getString("mode")))
            prefs.setQuranReaderId(saved.getString("reader"))
            if (quran.observeIsBookmarked(18, 10).first() != saved.getBoolean("bookmark")) quran.toggleBookmark(18, 10)
            koin.get<IhsanDatabase>().azkarDao().deleteZikrById(-945901)
            backup.delete()
        }
    }
}
