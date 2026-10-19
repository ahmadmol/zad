package com.example.mol.ui

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import androidx.test.platform.app.InstrumentationRegistry
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.asma.presentation.AsmaScreen
import com.example.feature.dashboard.GlobalSearchScreen
import com.example.feature.ehsan.EhsanScreen
import com.example.feature.ehsan.presentation.AddEhsanScreen
import com.example.feature.ehsan.presentation.RequestHelpScreen
import com.example.feature.live.LiveSourceType
import com.example.feature.live.LiveStreamScreen
import com.example.feature.onboarding.OnboardingScreen
import com.example.feature.profile.DonationHistoryScreen
import com.example.feature.quran.domain.model.Surah
import com.example.feature.quran.domain.model.Verse
import com.example.feature.quran.presentation.QuranReaderScreen
import com.example.feature.quran.presentation.QuranUiState
import com.example.feature.reminders.RemindersScreen
import com.example.feature.settings.SettingsScreen
import com.example.feature.settings.presentation.SettingsUiState
import com.example.feature.splashScreen.SplashScreenContent
import com.example.feature.tasbih.presentation.TasbihScreen
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale

/** Production components with real local content; no reference screenshot is rendered by the app. */
class ReferenceAcceptanceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private data class Sample(val width: Int = 360, val height: Int = 800, val dark: Boolean = false, val screen: Int = 0)

    @Test fun referenceScreensFitPhoneAndTabletInBothThemes() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val output = File(instrumentation.targetContext.getExternalFilesDir(null), "reference-acceptance").apply { mkdirs() }
        val phase = InstrumentationRegistry.getArguments().getString("referencePhase") ?: "3"
        val screens = if (phase == "2") 0..2 else if (phase == "asma") 3..3 else 0..12
        val sizes = if (phase == "darkPalette") listOf(393 to 852) else if (phase == "2") listOf(360 to 800) else listOf(360 to 800, 393 to 852, 412 to 915, 600 to 960)
        val failures = mutableListOf<String>()
        val lightGeometry = mutableMapOf<String, Map<String, String>>()
        val sample = mutableStateOf(Sample())
        val local = JSONArray(instrumentation.targetContext.assets.open("quran_full.json").bufferedReader().use { it.readText() })
            .getJSONObject(0).getJSONArray("verses")
        val fatihah = (0 until local.length()).map { i -> Verse(i + 1, 1, i + 1, local.getJSONObject(i).getString("text")) }
        compose.setContent {
            val value = sample.value
            val original = LocalConfiguration.current
            val context = LocalContext.current
            val configuration = Configuration(original).apply {
                screenWidthDp = value.width; screenHeightDp = value.height
                setLocale(Locale.forLanguageTag("ar"))
            }
            val density = context.resources.displayMetrics.widthPixels.toFloat() / value.width
            CompositionLocalProvider(LocalConfiguration provides configuration, LocalDensity provides Density(density, 1f),
                LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme(darkTheme = value.dark) {
                    Box(Modifier.fillMaxWidth().height(value.height.dp)) {
                        key(value.screen, value.width) {
                            when (value.screen) {
                                0 -> AddEhsanScreen(onNavigateBack = {})
                                1 -> RequestHelpScreen(onNavigateBack = {})
                                2 -> QuranReaderScreen(QuranUiState(selectedSurah = Surah(1,"الفاتحة","مكية",7,1), ayahs = fatihah),
                                    onBack = {}, onRetry = {}, onSaveLastRead = { _, _ -> }, onToggleBookmark = { _, _ -> },
                                    onTogglePlay = {}, onPlayNext = {}, onPlayPrevious = {}, onDownloadSurah = {}, onSeekTo = {}, onUpdateFontSize = {})
                                3 -> AsmaScreen(onNavigateBack = {})
                                4 -> TasbihScreen(onNavigateBack = {})
                                5 -> SettingsScreen(SettingsUiState(isLoading = false), onAction = {}, onBack = {})
                                6 -> RemindersScreen(onBack = {})
                                7 -> LiveStreamScreen(source = LiveSourceType.HARAM, onBack = {})
                                8 -> DonationHistoryScreen(onBack = {}, onItemClick = {})
                                9 -> GlobalSearchScreen(onNavigateToDuaLibrary = {}, onOpenResult = { _, _ -> }, onBack = {}, onNavigateToQuran = { _, _ -> }, onNavigateToDua = {})
                                10 -> OnboardingScreen(onFinish = {})
                                11 -> SplashScreenContent(isDark = value.dark)
                                12 -> EhsanScreen()
                            }
                        }
                    }
                }
            }
        }
        for ((width, height) in sizes) for (dark in listOf(false, true)) for (screen in screens) {
            compose.runOnIdle { sample.value = Sample(width, height, dark, screen) }
            compose.waitForIdle()
            SystemClock.sleep(650)
            if (phase == "darkPalette" && screen in listOf(3, 9, 12)) {
                compose.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().indices.forEach { index ->
                    compose.onAllNodes(hasSetTextAction())[index].performTextClearance()
                }
                compose.waitForIdle()
                SystemClock.sleep(350)
            }
            if (screen == 2) compose.waitUntil(30_000) {
                compose.onAllNodesWithText("سُورَةُ الفاتحة").fetchSemanticsNodes().isNotEmpty()
            }
            val name = "${width}_${if (dark) "dark" else "light"}_$screen"
            val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
            File(output, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            if (screen == 9) {
                compose.onAllNodes(hasSetTextAction())[0].performTextInput("الله")
                compose.waitUntil(15_000) { compose.onAllNodesWithText("نتائج البحث").fetchSemanticsNodes().isNotEmpty() }
                SystemClock.sleep(1_200)
                compose.waitForIdle()
                val results = compose.onRoot().captureToImage().asAndroidBitmap()
                File(output, "${name}_results.png").outputStream().use { results.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
            val texts = compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult), useUnmergedTree = true)
            texts.fetchSemanticsNodes().indices.forEach { index ->
                val results = mutableListOf<TextLayoutResult>()
                texts[index].performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                results.forEach { result ->
                    val ellipsized = (0 until result.lineCount).any { result.isLineEllipsized(it) }
                    val clippedWidth = (0 until result.lineCount).any { result.getLineRight(it) - result.getLineLeft(it) > result.size.width + 1f }
                    if ((result.didOverflowHeight || clippedWidth) && !ellipsized) failures += "$name: clipped text ${result.layoutInput.text.take(50)}"
                }
            }
            val geometry = texts.fetchSemanticsNodes().mapIndexed { index, node ->
                val label = node.config.getOrNull(SemanticsProperties.Text)?.joinToString("|")?.toString().orEmpty()
                "$index:$label" to node.boundsInRoot.toString()
            }.toMap()
            val geometryKey = "$width/$screen"
            if (!dark) lightGeometry[geometryKey] = geometry
            else if (screen != 7) {
                val light = lightGeometry[geometryKey].orEmpty()
                geometry.forEach { (label, bounds) ->
                    if (label.substringAfter(':').isNotBlank() && label in light && light[label] != bounds) failures += "$name: Light/Dark geometry changed: $label"
                }
            }
            if (screen == 2) {
                compose.onNodeWithText("الصفحة 1 من 604").assertExists()
                compose.onNodeWithText("الصفحة 1 من 604").assertIsDisplayed()
            }
        }
        File(output, "failures.txt").writeText(failures.joinToString("\n"))
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }
}
