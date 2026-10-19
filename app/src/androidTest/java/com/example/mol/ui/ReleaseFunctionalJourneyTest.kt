package com.example.mol.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ehsan.domain.repository.EhsanRepository
import com.example.feature.ehsan.domain.repository.UserRepository
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.profile.ManageListingsScreen
import com.example.feature.learning.presentation.components.IhsanYouTubePlayer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext
import java.io.File

class ReleaseFunctionalJourneyTest {
    @get:Rule(order = 0) val preservePreferences = PreserveDevicePreferences()
    @get:Rule(order = 1) val compose=createAndroidComposeRule<ComponentActivity>()
    private val inst get()=InstrumentationRegistry.getInstrumentation()
    private fun screenshot(name:String) {
        compose.waitForIdle()
        android.os.SystemClock.sleep(500)
        val windows=android.os.ParcelFileDescriptor.AutoCloseInputStream(inst.uiAutomation.executeShellCommand("dumpsys window")).bufferedReader().use { it.readText() }
        val focused=windows.lineSequence().firstOrNull { it.contains("mCurrentFocus=") }.orEmpty()
        assertTrue("A non-app window obscures the capture: $focused",compose.activity.hasWindowFocus() || focused.contains(inst.targetContext.packageName))
        val dir=File(inst.targetContext.getExternalFilesDir(null),"release-journeys").apply { mkdirs() }
        val bitmap=checkNotNull(inst.uiAutomation.takeScreenshot())
        File(dir,"$name.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }
        bitmap.recycle()
    }
    @Test fun quranReaderZoomCollapseAndReciterSelectionPersistWhileRecordingsAreDisabled() {
        val prefs=GlobalContext.get().get<com.example.feature.core.preferences.UserPreferences>()
        val oldFont=runBlocking { prefs.quranFontSize.first() }
        val oldReader=runBlocking { prefs.quranReaderId.first() }
        try {
            compose.setContent { IhsanTheme {
                val nav=androidx.navigation.compose.rememberNavController()
                com.example.mol.navigation.AppNavHost(nav)
                LaunchedEffect(Unit) { nav.navigate(com.example.mol.navigation.Screen.QuranReader.createRoute(2,282)) }
            } }
            compose.waitUntil(30_000) { compose.onAllNodesWithText("التلاوات تنتظر الترخيص").fetchSemanticsNodes().isNotEmpty() }
            // Page48 contains the longest Quran verse; validate base layout before intentional zoom.
            val textLayouts=compose.onAllNodes(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult),useUnmergedTree=true)
            textLayouts.fetchSemanticsNodes().indices.forEach { index ->
                val results=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
                textLayouts[index].performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(results) }
                results.forEach { layout -> assertFalse("Base reader text overflows: ${layout.layoutInput.text}",layout.didOverflowHeight) }
            }
            screenshot("quran_longest_verse_base_page48")
            compose.onNodeWithContentDescription("تشغيل").assertIsNotEnabled()
            compose.onNodeWithContentDescription("طي المشغل").performClick()
            compose.onNodeWithText("مشغل التلاوة مطوي").assertExists()
            compose.onNodeWithText("إظهار المشغل").performClick()
            compose.onNodeWithText("Aa").performClick()
            val editableProgress = SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsActions.SetProgress) and isEnabled()
            compose.waitUntil(10_000) { compose.onAllNodes(editableProgress).fetchSemanticsNodes().isNotEmpty() }
            val slider=compose.onAllNodes(editableProgress).onFirst()
            slider.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress) { it(40f) }
            compose.waitUntil(10_000) { runBlocking { prefs.quranFontSize.first()==40f } }
            screenshot("quran_zoom_142_percent")
            compose.onNodeWithText("Aa").performClick()
            compose.onNodeWithContentDescription("اختر القارئ").performClick()
            compose.onNodeWithText("ماهر المعيقلي").performClick()
            compose.waitUntil(10_000) { runBlocking { prefs.quranReaderId.first()=="MaherAlMuaiqly128kbps" } }
            compose.onNodeWithContentDescription("تشغيل").assertIsNotEnabled()
        } finally { runBlocking { prefs.setQuranFontSize(oldFont); prefs.setQuranReaderId(oldReader ?: "Alafasy_128kbps") } }
    }
    @Test fun globalBrowseDuasOpensVerifiedLibraryInsteadOfObsoleteSampleId() {
        compose.setContent { IhsanTheme {
            val nav=androidx.navigation.compose.rememberNavController()
            com.example.mol.navigation.AppNavHost(nav)
            LaunchedEffect(Unit) { nav.navigate(com.example.mol.navigation.Screen.GlobalSearch.route) }
        } }
        compose.waitUntil(30_000) { compose.onAllNodesWithText("تصفّح المحتوى").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("الأدعية").performClick()
        compose.waitUntil(20_000) { compose.onAllNodesWithText("أدعية الصباح").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("أدعية من القرآن الكريم").assertExists()
    }
    @Test fun localListingEditCancelStatusAndConfirmedDeleteUseActualRepository() {
        val users=GlobalContext.get().get<UserRepository>()
        val repo=GlobalContext.get().get<EhsanRepository>()
        val prior=runBlocking { users.getUser().first() }
        val title="سجل اختبار واجهة ${System.nanoTime()}"
        var rowId:Long?=null
        try {
            runBlocking {
                users.saveUser(UserEntity(firstName="اختبار آلي",lastName="محلي",phoneNumber="qa-ui-no-contact"))
                repo.addDonation(Donation(title=title,description="بيانات اختبار آلي محلية تحذف بعد الاختبار",category="تعليم",location="حلب",type="OFFER",donorName="اختبار",phoneNumber=""))
                rowId=repo.getAllDonations().first().first { it.title==title }.id
            }
            val dark=mutableStateOf(false)
            compose.setContent { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme(darkTheme=dark.value) { ManageListingsScreen("OFFER",{}, {}) }
            } }
            compose.waitUntil(20_000) { compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
            screenshot("light_owned_listing")
            compose.runOnIdle { dark.value=true };screenshot("dark_owned_listing")
            compose.onNodeWithText("تعديل").performClick()
            compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("عنوان محفوظ من الواجهة")
            compose.onNodeWithText("حفظ",useUnmergedTree=true).performClick()
            compose.waitUntil(10_000) { runBlocking { repo.getDonationById(rowId!!)!!.title=="عنوان محفوظ من الواجهة" } }
            compose.onNodeWithText("إنهاء محلي").performClick()
            compose.waitUntil(10_000) { runBlocking { repo.getDonationById(rowId!!)!!.status=="COMPLETED" } }
            compose.onNodeWithText("حذف").performClick()
            screenshot("dark_delete_confirmation")
            compose.onNodeWithText("إلغاء").performClick()
            assertNotNull(runBlocking { repo.getDonationById(rowId!!) })
            compose.onNodeWithText("حذف").performClick()
            compose.onAllNodesWithText("حذف").onLast().performClick()
            compose.waitUntil(10_000) { runBlocking { repo.getDonationById(rowId!!)==null } }
        } finally {
            runBlocking {
                rowId?.let { repo.getDonationById(it)?.let { row -> repo.deleteDonation(row) } }
                if(prior!=null) users.saveUser(prior) else users.logout()
            }
        }
    }

    @Test fun officialVideoReportsActualPlayingOrExplicitExternalFallback() {
        val ids=InstrumentationRegistry.getArguments().getString("videoIds")?.split(",") ?: listOf("Kx9Wj4vP69A")
        val current=mutableStateOf(ids.first())
        var externalClicks=0
        compose.setContent { IhsanTheme { key(current.value) {
            IhsanYouTubePlayer(current.value,null,true,true,onOpenExternal={externalClicks++})
        } } }
        val dir=File(inst.targetContext.getExternalFilesDir(null),"release-journeys").apply { mkdirs() }
        val result=File(dir,"video-results.txt");result.writeText("")
        val unknown=mutableListOf<String>()
        ids.forEach { id ->
            compose.runOnIdle { current.value=id }
            compose.waitForIdle()
            compose.onNodeWithContentDescription("تشغيل الفيديو").performClick()
            val playing=hasStateDescription("تشغيل الفيديو مؤكد")
            val fallback=hasText("تعذر تشغيل الفيديو داخل التطبيق")
            var observedPlaying = false
            var observedFallback = false
            val confirmed=runCatching { compose.waitUntil(40_000) {
                observedPlaying = compose.onAllNodes(playing).fetchSemanticsNodes().isNotEmpty()
                observedFallback = compose.onAllNodes(fallback).fetchSemanticsNodes().isNotEmpty()
                observedPlaying || observedFallback
            } }.isSuccess
            // Preserve the observed PLAYING signal. It may legitimately change to buffering
            // before the next semantics read; that is not evidence that fallback appeared.
            val didPlay=observedPlaying
            if (confirmed && !didPlay) assertTrue("Fallback must actually have been observed", observedFallback)
            val state=when { didPlay -> "ANDROID_PLAYING_CONFIRMED"; confirmed -> "ANDROID_EMBED_UNAVAILABLE_EXTERNAL_FALLBACK"; else -> "ANDROID_PLAYBACK_UNVERIFIED" }
            result.appendText("$id=$state\n")
            screenshot("video_${id}_$state")
            if(!confirmed) unknown+=id
            else if(!didPlay) {
                val before=externalClicks
                compose.onNodeWithText("المشاهدة على YouTube").performClick()
                assertEquals(before+1,externalClicks)
            }
        }
        assertTrue("No playback or explicit fallback observed: $unknown",unknown.isEmpty())
    }
}
