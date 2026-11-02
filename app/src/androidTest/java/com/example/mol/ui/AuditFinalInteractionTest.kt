package com.example.mol.ui

import android.app.Instrumentation
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.asma.presentation.AsmaViewModel
import com.example.feature.dashboard.GlobalSearchScreen
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.*
import com.example.feature.live.*
import com.example.feature.profile.ManageListingsScreen
import com.example.feature.quran.presentation.QuranViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.GlobalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mol.navigation.AppNavHost
import com.example.mol.navigation.Screen
import java.io.File
import java.util.concurrent.atomic.AtomicReference

class AuditFinalInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val inst get() = InstrumentationRegistry.getInstrumentation()
    private val output get() = File(inst.targetContext.getExternalFilesDir(null), "audit-final-interactions").apply { mkdirs() }
    private fun shell(command: String) = android.os.ParcelFileDescriptor.AutoCloseInputStream(inst.uiAutomation.executeShellCommand(command))
        .bufferedReader().use { it.readText() }
    private fun focus() = shell("dumpsys window").lineSequence().firstOrNull { it.contains("mCurrentFocus=") }.orEmpty()
    private fun shot(name: String, external: Boolean = false) {
        if (!external) {
            compose.waitForIdle()
            assertTrue("A foreign window obscures the app", compose.activity.hasWindowFocus() || focus().contains(inst.targetContext.packageName))
        }
        android.os.SystemClock.sleep(500)
        val bitmap = checkNotNull(inst.uiAutomation.takeScreenshot())
        File(output, "$name.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    private fun monitor(actions: Set<String>): Pair<Instrumentation.ActivityMonitor, AtomicReference<Intent?>> {
        val seen = AtomicReference<Intent?>(null)
        val monitor = object : Instrumentation.ActivityMonitor() {
            override fun onStartActivity(intent: Intent?): Instrumentation.ActivityResult? {
                if (intent != null && intent.action in actions) seen.set(Intent(intent))
                return null // Observe the actual launch; do not intercept or fabricate a result.
            }
        }
        inst.addMonitor(monitor)
        return monitor to seen
    }
    private fun returnFromExternal() {
        repeat(4) {
            if (focus().contains(inst.targetContext.packageName)) return
            shell("input keyevent 4")
            android.os.SystemClock.sleep(500)
        }
    }
    @Test fun storedContactPreferenceDispatchesTheCorrespondingIntentWithoutCallingOrSending() {
        val users = GlobalContext.get().get<UserRepository>()
        val repo = GlobalContext.get().get<EhsanRepository>()
        val prior = runBlocking { users.getUser().first() }
        val ids = mutableMapOf<String, Long>()
        val seen = AtomicReference<Intent?>(null)
        val gate = object : Instrumentation.ActivityMonitor() {
            override fun onStartActivity(intent: Intent?): Instrumentation.ActivityResult? {
                if (intent != null && intent.dataString in setOf("tel:+12025550123", "https://wa.me/12025550123")) {
                    seen.set(Intent(intent))
                    // Verify real production dispatch while preventing any contact with a recipient.
                    return Instrumentation.ActivityResult(android.app.Activity.RESULT_CANCELED, null)
                }
                return null
            }
        }
        try {
            runBlocking {
                users.saveUser(UserEntity(firstName = "اختبار", lastName = "تواصل", phoneNumber = "+12025550123"))
                for (method in listOf("PHONE", "WHATSAPP")) {
                    val title = "اختبار تواصل $method ${System.nanoTime()}"
                    repo.addDonation(Donation(title = title, description = "سجل آلي محلي، لا تواصل فعلي", category = "كتب", location = "حلب",
                        donorName = "اختبار", phoneNumber = "+12025550123", contactMethod = method))
                    ids[method] = repo.getAllDonations().first().single { it.title == title }.id
                }
            }
            val selected = mutableStateOf(ids.getValue("PHONE"))
            compose.setContent { IhsanTheme { key(selected.value) {
                com.example.feature.ehsan.presentation.IhsanDetailsScreen(selected.value, {})
            } } }
            inst.addMonitor(gate)
            for (method in listOf("PHONE", "WHATSAPP")) {
                seen.set(null)
                val row = runBlocking { repo.getDonationById(ids.getValue(method))!! }
                assertEquals(method, row.contactMethod)
                compose.runOnIdle { selected.value = row.id }
                compose.waitUntil(10_000) { compose.onAllNodesWithText(row.title).fetchSemanticsNodes().isNotEmpty() }
                compose.onNodeWithText("تواصل لتنسيق التبرع").performScrollTo().assertIsEnabled().performClick()
                compose.waitUntil(5_000) { seen.get() != null }
                assertEquals(if (method == "PHONE") Intent.ACTION_DIAL else Intent.ACTION_VIEW, seen.get()!!.action)
                assertEquals(if (method == "PHONE") "tel:+12025550123" else "https://wa.me/12025550123", seen.get()!!.dataString)
                shot("contact_dispatch_$method")
            }
            File(output, "contact-dispatch.txt").writeText("PASS persisted PHONE/WHATSAPP -> actual details-button ACTION_DIAL/tel or ACTION_VIEW/official wa.me. ActivityMonitor intercepted only the reserved test destinations; no call/message or external WhatsApp service tested.\n")
        } finally {
            inst.removeMonitor(gate)
            runBlocking {
                ids.values.forEach { id -> repo.getDonationById(id)?.let { repo.deleteDonation(it) } }
                if (prior == null) users.logout() else users.saveUser(prior)
            }
        }
    }
    @Test fun verifiedDuaCopiesActualTextAndOpensActualShareSheetWithSource() {
        val repo = GlobalContext.get().get<com.example.feature.duas.domain.repository.DuaRepository>()
        val dua = runBlocking { repo.loadDuasIfNeeded(); repo.getAllDuas().first().first() }
        compose.setContent { IhsanTheme { com.example.feature.duas.DuaDetailScreen(dua, {}, { _, _ -> }) } }
        compose.onNodeWithText("نسخ").performScrollTo().performClick()
        compose.runOnIdle {
            val clipboard = compose.activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            assertEquals(dua.text, clipboard.primaryClip!!.getItemAt(0).coerceToText(compose.activity).toString())
        }
        val (monitor, seen) = monitor(setOf(Intent.ACTION_CHOOSER))
        try {
            compose.onNodeWithContentDescription("مشاركة").performClick()
            compose.waitUntil(10_000) { seen.get() != null }
            @Suppress("DEPRECATION") val payload = seen.get()!!.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
            assertEquals(Intent.ACTION_SEND, payload.action)
            assertEquals("text/plain", payload.type)
            assertEquals("${dua.title}\n\n${dua.text}\n\nالمصدر: ${dua.source}", payload.getStringExtra(Intent.EXTRA_TEXT))
            compose.waitUntil(10_000) { focus().contains("ChooserActivity") || focus().contains("ResolverActivity") }
            shot("dua_actual_share_sheet", external = true)
            File(output, "dua-copy-share.txt").writeText("PASS actual Android clipboard equals source text; ACTION_CHOOSER / ACTION_SEND text/plain contains full text and source. No recipient selected.\n")
        } finally { inst.removeMonitor(monitor); returnFromExternal() }
    }
    @Test fun legacyDetailRouteRedirectsToCurrentMissingStateInBothThemes() {
        lateinit var nav: NavHostController
        val dark = mutableStateOf(false)
        compose.setContent { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            IhsanTheme(darkTheme = dark.value) { nav = rememberNavController(); AppNavHost(nav) }
        } }
        compose.waitUntil(30_000) { nav.currentDestination?.route != Screen.Splash.route }
        for (isDark in listOf(false, true)) {
            compose.runOnIdle { dark.value = isDark; nav.navigate("donation_detail_screen/999999") }
            compose.waitUntil(20_000) { nav.currentDestination?.route == Screen.IhsanDetails.route }
            compose.waitUntil(20_000) { compose.onAllNodesWithText("لم يتم العثور على الحالة").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("لم يتم العثور على الحالة").assertExists()
            assertEquals(999999L, nav.currentBackStackEntry!!.arguments!!.getLong("id"))
            shot("legacy_redirect_${if (isDark) "dark" else "light"}")
            compose.runOnIdle { nav.popBackStack() }
        }
    }
    @Test fun marksOnlyQueryDoesNotExposeAllAsmaAsMatchingSearchResults() {
        var asma: AsmaViewModel? = null
        var quran: QuranViewModel? = null
        compose.setContent { IhsanTheme {
            val names = koinViewModel<AsmaViewModel>()
            val verses = koinViewModel<QuranViewModel>()
            SideEffect { asma = names; quran = verses }
            GlobalSearchScreen({}, { _, _ -> }, {}, {}, { _, _ -> }, asmaViewModel = names, quranViewModel = verses)
        } }
        compose.waitUntil(30_000) { asma?.uiState?.value?.let { it.asmaList.size == 99 && !it.isLoading } == true }
        val query = "َُِ"
        compose.onAllNodes(hasSetTextAction())[0].performTextInput(query)
        compose.waitUntil(30_000) {
            asma?.uiState?.value?.searchQuery == query && quran?.uiState?.value?.let { it.searchQuery == query && !it.isSearching } == true
        }
        compose.onNodeWithText("(0)").assertExists()
        assertTrue(asma!!.uiState.value.searchResults.isEmpty())
        shot("marks_only_zero_results")
    }
    @Test fun officialSourceButtonActuallyLaunchesAnExternalHandlerWithTheOfficialUri() {
        compose.setContent { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { IhsanTheme {
            LiveStreamScreen(source = LiveSourceType.HARAM, onBack = {})
        } } }
        val (monitor, seen) = monitor(setOf(Intent.ACTION_VIEW))
        try {
            compose.onNodeWithText("تطبيق يوتيوب").performScrollTo().performClick()
            compose.waitUntil(10_000) { seen.get() != null }
            assertEquals(LiveStreamSources.youtubeChannelLiveUrl(LiveStreamSources.HARAM_YOUTUBE_CHANNEL_ID), seen.get()!!.dataString)
            compose.waitUntil(10_000) {
                val window = focus()
                window.contains("com.android.chrome") || window.contains("com.google.android.youtube") || window.contains("ResolverActivity")
            }
            File(output, "official-source-intent.txt").writeText("URI=${seen.get()!!.dataString}\nFOCUS=${focus()}\n")
            shot("official_source_external_handler", external = true)
        } finally { inst.removeMonitor(monitor); returnFromExternal() }
    }
    @Test fun imagePickerCancelAndImageRemovalUseActualEditDialogAndPersistence() {
        val users = GlobalContext.get().get<UserRepository>()
        val repo = GlobalContext.get().get<EhsanRepository>()
        val store = EhsanImageStore(inst.targetContext)
        val prior = runBlocking { users.getUser().first() }
        val source = File(inst.targetContext.cacheDir, "audit-edit-${System.nanoTime()}.png")
        val bitmap = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.GREEN)
        source.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
        var image: String? = null
        var id: Long? = null
        val title = "اختبار صور الواجهة ${System.nanoTime()}"
        val dark = mutableStateOf(true)
        try {
            runBlocking {
                users.saveUser(UserEntity(firstName = "اختبار", lastName = "صور", phoneNumber = "+12025550124"))
                image = store.persist(android.net.Uri.fromFile(source))!!
                repo.addDonation(Donation(title = title, description = "اختبار آلي محلي؛ ليس عرضًا حقيقيًا", category = "تعليم",
                    location = "حلب", type = "OFFER", donorName = "اختبار", phoneNumber = "", imageUrl = image))
                id = repo.getAllDonations().first().single { it.title == title }.id
            }
            compose.setContent { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme(darkTheme = dark.value) { ManageListingsScreen("OFFER", {}, {}) }
            } }
            compose.waitUntil(20_000) { compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("تعديل").performClick()
            compose.onNodeWithText("تغيير الصور").performScrollTo().assertIsEnabled()
            compose.onNodeWithText("حذف الصور").assertIsEnabled()
            shot("dark_edit_images")
            compose.runOnIdle { dark.value = false }; shot("light_edit_images")
            val (monitor, seen) = monitor(setOf("android.provider.action.PICK_IMAGES", Intent.ACTION_OPEN_DOCUMENT, Intent.ACTION_GET_CONTENT))
            try {
                compose.onNodeWithText("تغيير الصور").performClick()
                compose.waitUntil(10_000) { seen.get() != null }
                compose.waitUntil(10_000) { focus().contains("providers.media") || focus().contains("PhotoPicker", ignoreCase = true) || focus().contains("documentsui") }
                File(output, "image-picker-intent.txt").writeText("ACTION=${seen.get()!!.action}\nFOCUS=${focus()}\n")
                shot("actual_image_picker", external = true)
            } finally { inst.removeMonitor(monitor); returnFromExternal() }
            compose.onNodeWithText("حذف الصور").performScrollTo().performClick()
            compose.onNodeWithText("حذف الصور").assertIsNotEnabled()
            compose.onNodeWithText("إلغاء").performClick()
            assertEquals(image, runBlocking { repo.getDonationById(id!!)!!.imageUrl })
            compose.onNodeWithText("تعديل").performClick()
            compose.onNodeWithText("حذف الصور").performScrollTo().performClick()
            compose.onNodeWithText("حفظ").performClick()
            compose.waitUntil(10_000) { runBlocking { repo.getDonationById(id!!)!!.imageUrl == null } && store.resolve(image) == null }
            assertNull(store.resolve(image))
        } finally { runBlocking {
            id?.let { repo.getDonationById(it)?.let { row -> repo.deleteDonation(row) } }
            image?.let { store.delete(it) }; source.delete()
            if (prior == null) users.logout() else users.saveUser(prior)
        } }
    }
}
