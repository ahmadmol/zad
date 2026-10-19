package com.example.mol.ui

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import androidx.test.platform.app.InstrumentationRegistry
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.quran.domain.model.Surah
import com.example.feature.quran.domain.model.Verse
import com.example.feature.quran.presentation.QuranReaderScreen
import com.example.feature.quran.presentation.QuranUiState
import org.json.JSONArray
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale

/** Render the production reader with real local verses, including dense and multi-surah pages. */
class QuranPageBackgroundTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private data class Sample(val width: Int = 360, val height: Int = 800,
        val dark: Boolean = false, val page: Int = 1)

    @Test fun originalFrameAndTextFitBothThemes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val output = File(context.getExternalFilesDir(null), "quran-background").apply { mkdirs() }
        val full = JSONArray(context.assets.open("quran_full.json").bufferedReader().use { it.readText() })
        val starts = JSONArray(context.assets.open("mushaf_page_starts.json").bufferedReader().use { it.readText() })
        val failures = mutableListOf<String>()
        val sample = mutableStateOf(Sample())
        compose.setContent {
            val value = sample.value
            val start = starts.getJSONArray(value.page - 1)
            val surahId = start.getInt(0)
            val data = full.getJSONObject(surahId - 1)
            val verses = data.getJSONArray("verses")
            val ayahs = (0 until verses.length()).map { i ->
                val verse = verses.getJSONObject(i)
                Verse(i + 1, surahId, verse.getInt("id"), verse.getString("text"))
            }
            val configuration = Configuration(LocalConfiguration.current).apply {
                screenWidthDp = value.width; screenHeightDp = value.height
                setLocale(Locale.forLanguageTag("ar"))
            }
            val density = context.resources.displayMetrics.widthPixels.toFloat() / value.width
            CompositionLocalProvider(LocalConfiguration provides configuration,
                LocalDensity provides Density(density, 1f), LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme(darkTheme = value.dark) {
                    Box(Modifier.fillMaxWidth().height(value.height.dp)) {
                        key(value.page, value.width) {
                            QuranReaderScreen(QuranUiState(
                                selectedSurah = Surah(surahId, data.getString("name"), "مكية", ayahs.size, 1),
                                ayahs = ayahs, initialAyahScroll = start.getInt(1)),
                                onBack = {}, onRetry = {}, onSaveLastRead = { _, _ -> },
                                onToggleBookmark = { _, _ -> }, onTogglePlay = {},
                                onPlayNext = {}, onPlayPrevious = {}, onDownloadSurah = {},
                                onSeekTo = {}, onUpdateFontSize = {})
                        }
                    }
                }
            }
        }
        for ((width, height) in listOf(360 to 800, 600 to 960)) {
            for (page in listOf(1, 2, 50, 604)) for (dark in listOf(false, true)) {
                compose.runOnIdle { sample.value = Sample(width, height, dark, page) }
                compose.waitUntil(30_000) {
                    compose.onAllNodesWithText("الصفحة $page من 604").fetchSemanticsNodes().isNotEmpty() &&
                        compose.onAllNodesWithText("سُورَةُ ", substring = true).fetchSemanticsNodes().isNotEmpty()
                }
                compose.waitForIdle()
                val name = "${width}_${if (dark) "dark" else "light"}_page$page"
                val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
                File(output, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                val texts = compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult), useUnmergedTree = true)
                texts.fetchSemanticsNodes().indices.forEach { i ->
                    val results = mutableListOf<TextLayoutResult>()
                    texts[i].performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                    results.forEach { result ->
                        // Compose wraps the paragraph at the page width. The critical
                        // fit condition is that every line remains within its height.
                        if (result.didOverflowHeight)
                            failures += "$name: vertical text overflow ${result.layoutInput.text.take(40)}"
                    }
                }
            }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }
}