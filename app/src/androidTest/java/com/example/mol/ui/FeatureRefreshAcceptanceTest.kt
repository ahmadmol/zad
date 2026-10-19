package com.example.mol.ui

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.platform.app.InstrumentationRegistry
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ehsan.presentation.AddEhsanScreen
import com.example.feature.ehsan.presentation.IhsanDetailsScreen
import com.example.feature.profile.ManageListingsScreen
import com.example.feature.profile.DonationHistoryScreen
import com.example.feature.profile.presentation.EditProfileScreen
import com.example.feature.duas.DuaScreen
import com.example.feature.sanhya.presentation.*
import com.example.feature.sanhya.data.datasource.SanhyaFixtureData
import com.example.feature.nabiihsan.presentation.*
import org.koin.androidx.compose.koinViewModel
import org.junit.Rule
import org.junit.Test
import java.io.File

class FeatureRefreshAcceptanceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun ihsanFormsDetailsAndOwnershipStates() {
        val output = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "feature-refresh").apply { mkdirs() }
        val page = mutableIntStateOf(0)
        val storyId = SanhyaFixtureData.defaultStories.getOrElse(1) { SanhyaFixtureData.defaultStories.first() }.id
        val night = mutableStateOf(false)
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme(darkTheme = night.value) {
                    Box(Modifier.fillMaxSize()) {
                        key(page.intValue, night.value) {
                            when (page.intValue) {
                                0 -> IhsanDetailsScreen(1, {})
                                1 -> AddEhsanScreen()
                                2 -> EditProfileScreen({})
                                3 -> ManageListingsScreen("OFFER", {}, {})
                                4 -> DonationHistoryScreen({}, {})
                                5 -> DuaScreen()
                                6 -> SanhyaMainScreen(koinViewModel(), {}, {}, {}, {}, {})
                                7 -> SanhyaStoryDetailScreen(storyId, koinViewModel(), {}, {}, {})
                                8 -> SanhyaEpisodesScreen(storyId, koinViewModel(), {})
                                9 -> SanhyaFavoritesScreen(koinViewModel(), {}, {})
                                10 -> SanhyaWatchLaterScreen(koinViewModel(), {}, {})
                                11 -> SanhyaSettingsScreen(koinViewModel(), {})
                                12 -> SanhyaQuranVersesScreen(storyId, koinViewModel(), {}, { _, _ -> })
                                13 -> NabiIhsanMainScreen()
                                14 -> NabiIhsanEpisodeDetailScreen("ep_01")
                                15 -> NabiIhsanEpisodesScreen()
                                16 -> NabiIhsanRecipesScreen()
                                17 -> NabiIhsanRecipeDetailScreen("missing-recipe")
                                18 -> NabiIhsanFavoritesScreen()
                                19 -> NabiIhsanWatchLaterScreen()
                                20 -> NabiIhsanSearchScreen(initialQuery = "غير موجود إطلاقًا")
                                21 -> NabiIhsanSettingsScreen()
                            }
                        }
                    }
                }
            }
        }
        fun shot(name: String) {
            compose.waitForIdle(); SystemClock.sleep(600)
            val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
            File(output, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        }
        val phase = InstrumentationRegistry.getArguments().getString("refreshPhase")
        val screens = if (phase == "3") listOf(5) else if (phase == "4") (6..12).toList()
            else if (phase == "5") (13..21).toList() else (0..4).toList()
        for (dark in listOf(false,true)) for (screen in screens) {
            compose.runOnIdle { night.value = dark; page.intValue = screen }
            compose.waitForIdle(); SystemClock.sleep(800)
            val prefix = "393_${if(dark) "dark" else "light"}"
            if (screen == 5) compose.onNodeWithText("الكل").performClick()
            shot("${prefix}_phase2_$screen")
            if (screen == 1) {
                compose.onAllNodes(hasSetTextAction())[0].performTextInput("عرض للتحقق من حفظ المسودة")
                compose.onAllNodes(hasSetTextAction())[1].performTextInput("وصف عربي طويل يوضح تفاصيل العرض ويبقى محفوظًا عند الرجوع بين الخطوات")
                compose.onNodeWithText("التالي").performScrollTo().performClick()
                shot("${prefix}_add_step2")
                compose.onNodeWithText("التالي").performScrollTo().performClick()
                shot("${prefix}_add_step3")
                compose.onNodeWithText("المعلومات").performScrollTo().performClick()
                compose.onNodeWithText("عرض للتحقق من حفظ المسودة").assertExists()
            }
            if (screen == 3 || screen == 4) {
                compose.onNodeWithText("تعذر تحديد سجلاتك").assertExists()
            }
            if (screen == 5) {
                compose.onNodeWithText("أدعية الصباح").performClick()
                compose.onNodeWithText("لا توجد نتائج").assertExists()
                shot("${prefix}_dua_missing_category")
                compose.onNodeWithContentDescription("رجوع").performClick()
                compose.onNodeWithText("أدعية نبوية").performScrollTo().performClick()
                compose.onNodeWithText("دعاء قضاء الحاجة").assertExists()
                compose.onNodeWithText("دعاء الاستخارة").assertExists()
                shot("${prefix}_dua_results")
            }
        }
    }
}
