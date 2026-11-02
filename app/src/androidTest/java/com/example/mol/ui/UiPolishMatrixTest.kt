package com.example.mol.ui

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.core.view.WindowCompat
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.dashboard.presentation.components.*
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.presentation.components.*
import com.example.feature.profile.presentation.components.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale

/** Render the production UI components with deterministic data; no repositories are replaced. */
class UiPolishMatrixTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private data class Case(val width: Int = 360, val dark: Boolean = false, val scale: Float = 1f, val page: Int = 0)
    private val case = mutableStateOf(Case())
    private val lightGeometry = mutableMapOf<String, List<String>>()
    private var selectedRoute = ""

    @Test fun arabicScreensRemainReadableAcrossWidthThemeAndFontScale() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val output = File(context.getExternalFilesDir(null), "ui-polish-matrix").apply { mkdirs() }
        compose.setContent {
            val sample = case.value
            val configuration = Configuration(LocalConfiguration.current).apply {
                screenWidthDp = sample.width
                screenHeightDp = when (sample.width) { 390 -> 844; 412 -> 915; else -> 800 }
                setLocale(Locale.forLanguageTag("ar"))
            }
            val density = context.resources.displayMetrics.widthPixels.toFloat() / sample.width
            CompositionLocalProvider(LocalConfiguration provides configuration,
                LocalDensity provides Density(density, sample.scale), LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme(darkTheme = sample.dark) {
                    Box(Modifier.fillMaxWidth().height(configuration.screenHeightDp.dp)) {
                        key(sample) { MatrixPage(sample.page, sample.scale) }
                    }
                }
            }
        }
        for (width in listOf(360, 390, 412)) for (dark in listOf(false, true)) for (scale in listOf(1f, 1.3f)) {
            for (page in 0..4) {
                compose.runOnIdle { case.value = Case(width, dark, scale, page) }
                compose.waitForIdle()
                compose.runOnIdle {
                    val controller = WindowCompat.getInsetsController(compose.activity.window, compose.activity.window.decorView)
                    assertEquals("Status icons must match the reference skyline header", !dark,
                        controller.isAppearanceLightStatusBars)
                }
                val name = "${width}_${if (dark) "dark" else "light"}_${scale}_$page"
                if (page == 1) {
                    // Assert the rendered row placement, including fractional pixel rounding.
                    val quranTop = compose.onNodeWithText("القرآن").fetchSemanticsNode().boundsInRoot.top
                    val qiblaTop = compose.onNodeWithText("القبلة").fetchSemanticsNode().boundsInRoot.top
                    assertEquals("Reference phone services must use three columns", quranTop, qiblaTop, 1f)
                }
                capture(output, "${name}_top")
                // Text layout, rather than source-code shape, detects actual Arabic clipping.
                val texts = compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult), useUnmergedTree = true)
                texts.fetchSemanticsNodes().indices.forEach { index ->
                    val results = mutableListOf<TextLayoutResult>()
                    texts[index].performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                    results.forEach { result ->
                        // RTL paragraphs can retain the available width after the Text node
                        // shrinks to its intrinsic width; measure the painted lines instead.
                        val clipped = (0 until result.lineCount).any { line ->
                            (result.layoutInput.constraints.hasBoundedWidth &&
                                result.getLineRight(line) - result.getLineLeft(line) > result.size.width + 1f) ||
                                result.getLineBottom(line) > result.size.height + 1f || result.isLineEllipsized(line)
                        } || result.multiParagraph.didExceedMaxLines
                        assertFalse("$width/$dark/$scale/$page: ${result.layoutInput.text}; size=${result.size}; paragraph=${result.multiParagraph.width}x${result.multiParagraph.height}", clipped)
                    }
                }
                val geometry = texts.fetchSemanticsNodes().map { node -> node.boundsInRoot.toString() }
                val geometryKey = "$width/$scale/$page"
                if (dark) assertEquals("Light/Dark must keep identical text geometry: $geometryKey", lightGeometry[geometryKey], geometry)
                else lightGeometry[geometryKey] = geometry
                compose.onNodeWithText("نهاية المحتوى").performScrollTo().assertIsDisplayed()
                val end = compose.onNodeWithText("نهاية المحتوى").fetchSemanticsNode().boundsInRoot
                val nav = compose.onNodeWithTag("ihsan_bottom_navigation").fetchSemanticsNode().boundsInRoot
                assertTrue("Content hidden by navigation: $name", end.bottom <= nav.top)
                capture(output, "${name}_bottom")
                if (width <= 412 && page != 1) {
                    // The window appearance updates after Android draws the scrolled layer.
                    compose.waitUntil(timeoutMillis = 5_000) {
                        WindowCompat.getInsetsController(compose.activity.window, compose.activity.window.decorView)
                            .isAppearanceLightStatusBars == !dark
                    }
                    compose.runOnIdle {
                        assertEquals("Status icons must follow the body: $name", !dark,
                            WindowCompat.getInsetsController(compose.activity.window, compose.activity.window.decorView)
                                .isAppearanceLightStatusBars)
                    }
                }
                if (page == 1) {
                    compose.onNodeWithText("عرض الكل").performScrollTo().performClick()
                    compose.onNodeWithText("الأسماء الحسنى").performScrollTo().performClick()
                    compose.runOnIdle { assertEquals("asma", selectedRoute) }
                }
            }
        }
    }

    private fun capture(dir: File, name: String) {
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Composable private fun MatrixPage(page: Int, scale: Float) {
        Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), bottomBar = {
            IhsanBottomNavigation(listOf(
                IhsanBottomNavDestination("الرئيسية", Icons.Outlined.Home, Icons.Default.Home),
                IhsanBottomNavDestination("إحسان", Icons.Outlined.FavoriteBorder, Icons.Default.Favorite),
                IhsanBottomNavDestination("حسابي", Icons.Outlined.Person, Icons.Default.Person)
            ), if (page >= 4) 2 else if (page >= 2) 1 else 0, {},
                Modifier.fillMaxWidth().heightIn(min = 60.dp * scale).navigationBarsPadding())
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                when (page) {
                    0 -> {
                        HomeContextualHero("حلب", "الأربعاء، ٢٦ ربيع الآخر ١٤٤٨", "الظهر", "02:01:21", "العصر", "03:36 م",
                            "إِنَّمَا الْمُؤْمِنُونَ إِخْوَةٌ فَأَصْلِحُوا بَيْنَ أَخَوَيْكُمْ", "سورة الحجرات: آية ١٠", {}, {}, {})
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HomeQuranContinueCard("ص", 38, 14, { _, _ -> }, {})
                            HomeDailyJourneyCard({})
                            HomeEhsanCommunityCard({})
                            HomeLiveStreamCard({})
                            HomeDiscoverServicesGrid({ selectedRoute = it })
                        }
                    }
                    1 -> HomeDiscoverServicesGrid({ selectedRoute = it }, Modifier.padding(16.dp))
                    2, 3 -> {
                        EhsanHeaderBanner("حلب", "الأربعاء، ٢٦ ربيع الآخر ١٤٤٨")
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            EhsanSegmentedTabs(if (page == 2) "OFFER" else "REQUEST", {})
                            EhsanSearchField("", {}, "حلب")
                            EhsanFilterRow("DEFAULT", {})
                            LocalBoardNoticeCard()
                            DonationCardItem(Donation(title = if (page == 2) "حقائب مدرسية لأطفال حلب" else "مساعدة لأسرة في سيف الدولة",
                                description = "توفير المستلزمات الأساسية للأطفال في مدينة حلب.", category = "أخرى", location = "حلب — سيف الدولة",
                                type = if (page == 2) "OFFER" else "REQUEST", donorName = "زائر", phoneNumber = "0999999999", createdAt = 1789776000000L), null, {})
                            AddEhsanFab(if (page == 2) "OFFER" else "REQUEST", {})
                        }
                    }
                    4 -> {
                        ProfileHeroHeader("حلب", "الأربعاء، ٢٦ ربيع الآخر ١٤٤٨", {}, {})
                        Column(Modifier.padding(horizontal = 16.dp).layout { measurable, constraints ->
                            val child = measurable.measure(constraints)
                            val overlap = 48.dp.roundToPx()
                            layout(child.width, child.height - overlap) { child.placeRelative(0, -overlap) }
                        }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ProfileAvatarOverlay("زائر", {}, modifier = Modifier.align(Alignment.CenterHorizontally), avatarModel = "file:///missing-avatar.jpg")
                            ProfileTitleHeader()
                            ProfileGroupedSection("الحساب", Icons.Default.Person) {
                                ProfileSettingRow("تحرير الملف الشخصي", "تحديث معلوماتك وصورتك", {}, icon = Icons.Default.Edit)
                                ProfileSettingRow("سجل المساهمات", "اطّلع على سجل تبرعاتك ومساهماتك", {}, icon = Icons.Default.Favorite)
                            }
                            ProfileGroupedSection("المظهر والتفضيلات", Icons.Default.Settings) {
                                ProfileSettingRow("جميع الإعدادات", "تخصيص المظهر والإشعارات والموقع والبيانات", {}, icon = Icons.Default.Settings)
                                ProfileSettingRow("الوضع الداكن", "تغيير مظهر التطبيق", {}, icon = Icons.Default.DarkMode,
                                    trailingContent = { Switch(IhsanTheme.isDark, {}) })
                            }
                        }
                    }
                }
                Text("نهاية المحتوى", Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}
