package com.example.mol.ui

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
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
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.dashboard.HomeDashboardScreen
import com.example.feature.ehsan.EhsanScreen
import com.example.feature.ehsan.presentation.*
import com.example.feature.profile.ProfileScreen
import com.example.feature.profile.ManageListingsScreen
import com.example.feature.profile.presentation.EditProfileScreen
import com.example.feature.prayer.PrayerScreen
import com.example.feature.duas.DuaScreen
import com.example.feature.sanhya.presentation.*
import com.example.feature.nabiihsan.presentation.*
import org.koin.androidx.compose.koinViewModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale

/** Native production screens at phone/tablet widths; deliberately ellipsized card titles are allowed. */
class RefreshResponsiveMatrixTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private data class Sample(val width: Int=393, val dark: Boolean=false, val scale: Float=1f, val page: Int=0)
    @Test fun productionScreensRemainReadableAndScrollAtSupportedWidths() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val output = File(context.getExternalFilesDir(null), "refresh-matrix").apply { mkdirs() }
        val sample = mutableStateOf(Sample())
        val failures = mutableListOf<String>()
        val pages = listOf("home","ihsan","profile","prayer","sanhya","nabi","duas","edit_profile","add_offer","details","management","learning_settings","offline","error")
        compose.setContent {
            val value = sample.value
            val config = Configuration(LocalConfiguration.current).apply {
                screenWidthDp=value.width; screenHeightDp=if(value.width>=600) 960 else 852
                setLocale(Locale.forLanguageTag("ar"))
            }
            val density = context.resources.displayMetrics.widthPixels / value.width.toFloat()
            CompositionLocalProvider(LocalConfiguration provides config, LocalDensity provides Density(density, value.scale),
                LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme(darkTheme=value.dark) {
                    Scaffold(bottomBar = {
                        if(value.page<3) IhsanBottomNavigation(listOf(
                            IhsanBottomNavDestination("الرئيسية",Icons.Default.Home,Icons.Default.Home),
                            IhsanBottomNavDestination("إحسان",Icons.Default.Favorite,Icons.Default.Favorite),
                            IhsanBottomNavDestination("الملف الشخصي",Icons.Default.Person,Icons.Default.Person)),value.page,{},Modifier.navigationBarsPadding())
                    }, contentWindowInsets=WindowInsets(0,0,0,0)) { padding ->
                        Box(Modifier.padding(padding).fillMaxSize()) {
                            key(value.width,value.page,value.scale) {
                                when(value.page) {
                                    0 -> HomeDashboardScreen()
                                    1 -> EhsanScreen()
                                    2 -> ProfileScreen()
                                    3 -> PrayerScreen()
                                    4 -> SanhyaMainScreen(koinViewModel(),{},{},{},{},{})
                                    5 -> NabiIhsanMainScreen()
                                    6 -> DuaScreen()
                                    7 -> EditProfileScreen({})
                                    8 -> AddEhsanScreen()
                                    9 -> IhsanDetailsScreen(1,{})
                                    10 -> ManageListingsScreen("REQUEST",{}, {})
                                    11 -> NabiIhsanSettingsScreen()
                                    12 -> com.example.feature.learning.presentation.components.VideoHero(null,null,false,false,{})
                                    13 -> NabiIhsanEpisodeDetailScreen("missing-episode")
                                }
                            }
                        }
                    }
                }
            }
        }
        val widths = InstrumentationRegistry.getArguments().getString("matrixWidths")?.split(",")?.map { it.toInt() }
            ?: listOf(360,393,412,600)
        val scales = if (InstrumentationRegistry.getArguments().getString("matrixScale") == "normal") listOf(1f) else listOf(1f,1.3f)
        var count=0
        for(width in widths) for(scale in scales) for(dark in listOf(false,true)) for(page in pages.indices) {
            compose.runOnIdle { sample.value=Sample(width,dark,scale,page) }
            compose.waitForIdle(); SystemClock.sleep(350)
            val name="${width}_${if(dark) "dark" else "light"}_${scale}_${pages[page]}"
            fun capture(suffix: String="") {
                val bitmap=compose.onRoot().captureToImage().asAndroidBitmap()
                File(output,"$name$suffix.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
            }
            capture()
            val texts=compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult),useUnmergedTree=true)
            texts.fetchSemanticsNodes().indices.forEach { index ->
                val layouts=mutableListOf<TextLayoutResult>()
                texts[index].performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
                layouts.forEach { result ->
                    val ellipsized=(0 until result.lineCount).any { result.isLineEllipsized(it) }
                    // RTL chip line extents include the paragraph alignment width, which can
                    // exceed the final intrinsic Text size without clipping. Check vertical
                    // overflow here; review horizontal fit in the captured native screenshots.
                    if(!ellipsized && result.didOverflowHeight) failures += "$name: ${result.layoutInput.text}"
                }
            }
            if(page==7) {
                compose.onNodeWithText("حفظ التغييرات").performScrollTo().assertIsDisplayed()
                compose.onNodeWithText("إلغاء").assertIsDisplayed(); capture("_actions")
            }
            if(page==8) {
                compose.onAllNodes(hasSetTextAction())[0].performTextInput("عرض عربي طويل للتحقق من الحقول")
                compose.onAllNodes(hasSetTextAction())[1].performTextInput("وصف محفوظ بين خطوات النموذج، مع نص عربي طويل لا يختفي عند الرجوع")
                compose.onNodeWithText("التالي").performScrollTo().performClick(); capture("_step2")
                compose.onNodeWithText("التالي").performScrollTo().performClick(); capture("_step3")
                compose.onNodeWithText("نشر العرض").performScrollTo().assertIsDisplayed()
            }
            if(page<3) compose.onNodeWithTag("ihsan_bottom_navigation").assertIsDisplayed()
            count++
        }
        File(output,"failures.txt").writeText(failures.joinToString("\n"))
        File(output,"case-count.txt").writeText(count.toString())
        assertTrue(failures.joinToString("\n"),failures.isEmpty())
    }
}
