package com.example.mol.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.dashboard.GlobalSearchScreen
import com.example.feature.fahmanallah.data.repository.FahmRepositoryImpl
import com.example.feature.fahmanallah.domain.model.FahmEpisodeProgressState
import com.example.feature.quran.domain.repository.QuranRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext
import java.io.File

class VideoAuditRegressionTest {
    @get:Rule(order = 0) val preservePreferences = PreserveDevicePreferences()
    @get:Rule(order = 1) val compose = createAndroidComposeRule<ComponentActivity>()
    private fun capture(name: String) {
        compose.waitForIdle()
        android.os.SystemClock.sleep(700) // Wait for the native framebuffer after recomposition.
        assertTrue("Activity must own the screenshot", compose.activity.hasWindowFocus())
        val inst = InstrumentationRegistry.getInstrumentation()
        val dir = File(inst.targetContext.getExternalFilesDir(null), "video-audit-20261015").apply { mkdirs() }
        val bitmap = checkNotNull(inst.uiAutomation.takeScreenshot())
        File(dir, "$name.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    @Test fun kahfSearchFindsChapterWithoutUnrelatedDuasAndQuranFilterStillFindsIt() {
        val dark = mutableStateOf(false)
        var selected: Pair<Int, Int?>? = null
        compose.setContent { IhsanTheme(darkTheme = dark.value) {
            GlobalSearchScreen({}, { s, a -> selected = s to a }, {}, {}, { _, _ -> })
        } }
        compose.onNodeWithText("سورة الكهف").performClick()
        val result = hasText("سورة الكهف", substring = true) and hasClickAction()
        compose.waitUntil(30_000) { compose.onAllNodes(result).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("الأدعية المأثورة").assertDoesNotExist()
        compose.onNodeWithText("(3640)").assertDoesNotExist()
        capture("kahf_all_light")
        compose.onNodeWithText("القرآن", useUnmergedTree = true).performScrollTo().performClick()
        compose.onAllNodes(result).onFirst().assertExists()
        compose.runOnIdle { dark.value = true }
        capture("kahf_quran_dark")
        compose.onAllNodes(result).onFirst().performClick()
        compose.runOnIdle { assertEquals(18 to 1, selected) }
    }
    @Test fun realQuranRepositoryFindsBothChapterQueriesAndVerseText() = runBlocking {
        val repo = GlobalContext.get().get<QuranRepository>()
        assertTrue(repo.searchAyahs("سورة الكهف").any { it.surahId == 18 && it.verseNumber == 1 })
        assertTrue(repo.searchAyahs("الفاتحة").any { it.surahId == 1 && it.verseNumber == 1 })
        assertTrue(repo.searchAyahs("الله لا اله الا هو الحي القيوم").any { it.surahId == 2 && it.verseNumber == 255 })
        assertTrue(repo.searchAyahs("الحمد لله رب العالمين").any { it.surahId == 1 && it.verseNumber == 2 })
        assertTrue(repo.searchAyahs("مالك يوم الدين").any { it.surahId == 1 && it.verseNumber == 4 })
        assertTrue(repo.searchAyahs("َُِ").isEmpty())
    }
    @Test fun actualDuaAndHadithQueriesStayRelevantAndOpenTypedDetails() {
        var duaId: Long? = null
        var openedKind: com.example.feature.dashboard.presentation.SearchResultKind? = null
        compose.setContent { IhsanTheme {
            GlobalSearchScreen({}, { _, _ -> }, { duaId = it }, {}, { kind, _ -> openedKind = kind })
        } }
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("دعاء السفر")
        compose.waitUntil(30_000) { compose.onAllNodesWithText("الأدعية المأثورة").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("الأدعية", useUnmergedTree = true).performScrollTo().performClick()
        compose.onNodeWithText("(2)").assertExists()
        capture("travel_dua_results")
        compose.onAllNodes(hasClickAction() and hasText("دعاء")).onFirst().performClick()
        compose.runOnIdle { assertNotNull(duaId) }
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("إنما الأعمال بالنيات")
        compose.onNodeWithText("الأحاديث", useUnmergedTree = true).performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("الأحاديث النبوية").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("الأدعية المأثورة").assertDoesNotExist()
        capture("hadith_query_results")
        compose.onAllNodes(hasClickAction() and hasText("حديث نبوي")).onFirst().performClick()
        compose.runOnIdle { assertEquals(com.example.feature.dashboard.presentation.SearchResultKind.HADITH, openedKind) }
    }
    @Test fun bothLiveChannelsReachPlayingOrArabicFallbackAndRetryRecreatesPlayer() {
        val source = mutableStateOf(com.example.feature.live.LiveSourceType.HARAM)
        compose.setContent { IhsanTheme { key(source.value) {
            com.example.feature.live.LiveStreamScreen(source = source.value, onBack = {})
        } } }
        val inst = InstrumentationRegistry.getInstrumentation()
        val result = File(inst.targetContext.getExternalFilesDir(null), "audit-live-results.txt")
        result.writeText("")
        com.example.feature.live.LiveSourceType.entries.forEach { channel ->
            compose.runOnIdle { source.value = channel }
            val playing = hasStateDescription("تشغيل البث مؤكد")
            val failed = hasText("تعذر تشغيل البث داخل التطبيق", substring = true)
            compose.waitUntil(40_000) {
                compose.onAllNodes(playing).fetchSemanticsNodes().isNotEmpty() || compose.onAllNodes(failed).fetchSemanticsNodes().isNotEmpty()
            }
            val didPlay = compose.onAllNodes(playing).fetchSemanticsNodes().isNotEmpty()
            result.appendText("${channel.id}=" + (if (didPlay) "ANDROID_PLAYING_CONFIRMED" else "ANDROID_EMBED_UNAVAILABLE_EXTERNAL_FALLBACK") + "\n")
            capture("live_${channel.id}")
            if (!didPlay) {
                compose.onNodeWithText("فتح المصدر الرسمي").assertExists()
                compose.onNodeWithText("إعادة المحاولة").performClick()
                compose.waitUntil(5_000) { compose.onAllNodes(failed).fetchSemanticsNodes().isEmpty() }
                compose.waitUntil(40_000) {
                    compose.onAllNodes(playing).fetchSemanticsNodes().isNotEmpty() || compose.onAllNodes(failed).fetchSemanticsNodes().isNotEmpty()
                }
            }
        }
    }
    @Test fun fahmCompletionFavoritesAndWatchLaterSurviveNewRepositoryInstances() = runBlocking {
        val prefs = GlobalContext.get().get<UserPreferences>()
        val old = prefs.learningIds(FahmRepositoryImpl.STATE_KEY).first()
        try {
            prefs.updateLearningIds(FahmRepositoryImpl.STATE_KEY) { emptySet() }
            val repo = FahmRepositoryImpl(prefs)
            repo.toggleFavorite("ep_01"); repo.toggleWatchLater("ep_02"); repo.markCompleted("ep_01")
            repo.updateEpisodeProgress("ep_02", FahmEpisodeProgressState.IN_PROGRESS, 73)
            val reopened = FahmRepositoryImpl(UserPreferences(InstrumentationRegistry.getInstrumentation().targetContext))
            assertTrue(reopened.getEpisodeById("ep_01").first()!!.isFavorite)
            assertEquals(FahmEpisodeProgressState.COMPLETED, reopened.getEpisodeById("ep_01").first()!!.progressState)
            assertTrue(reopened.getEpisodeById("ep_02").first()!!.isWatchLater)
            assertEquals(73, reopened.getEpisodeById("ep_02").first()!!.lastPlayedSeconds)
            assertEquals(1, reopened.getJourneyProgress().first().completedCount)
            assertEquals(3, reopened.getJourneyProgress().first().percentage)
            reopened.resetFeatureData()
            assertEquals(0, repo.getJourneyProgress().first().completedCount)
            assertTrue(repo.getFavoriteEpisodes().first().isEmpty())
        } finally { prefs.updateLearningIds(FahmRepositoryImpl.STATE_KEY) { old } }
    }
}
