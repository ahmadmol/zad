package com.example.mol.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.data.local.database.IhsanDatabase
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.ehsan.data.repository.EhsanRepositoryImpl
import com.example.feature.ehsan.data.repository.UserRepositoryImpl
import com.example.feature.ehsan.domain.usecase.*
import com.example.feature.ehsan.presentation.*
import com.example.feature.profile.ManageListingsScreen
import com.example.feature.profile.presentation.ProfileViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Real production forms/repositories against a dedicated in-memory Room database.
 * No active user, listing or production database is replaced for UI QA. */
class PhysicalCommunityJourneyTest {
    @get:Rule(order = 0) val preservePreferences = PreserveDevicePreferences()
    @get:Rule(order = 1) val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun offerCreateEditCancelCompleteDeleteInBothThemes() = journey(false)
    @Test fun requestCreateEditCancelCompleteDeleteInBothThemes() = journey(true)

    private fun journey(request: Boolean) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, IhsanDatabase::class.java).build()
        val users = UserRepositoryImpl(db.userDao())
        val repo = EhsanRepositoryImpl(db.donationDao(), users = db.userDao(), imageStore = EhsanImageStore(context))
        val store = ViewModelStore()
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
                AddEhsanViewModel::class.java -> AddEhsanViewModel(AddDonationUseCase(repo), users,
                    ValidatePhoneNumberUseCase(), EhsanImageStore(context))
                ProfileViewModel::class.java -> ProfileViewModel(GetMyDonationsUseCase(repo),
                    UpdateDonationStatusUseCase(repo), DeleteDonationUseCase(repo), users,
                    SettingsManager(context), UserPreferences(context), repo)
                else -> error("Unexpected fixture model")
            } as T
        }
        val provider = ViewModelProvider(store, factory)
        val management = mutableStateOf(false)
        val dark = mutableStateOf(false)
        val disposed = mutableStateOf(false)
        try {
            runBlocking { users.saveUser(UserEntity(firstName = "اختبار", lastName = "معزول", phoneNumber = "+12025550123")) }
            val form = provider[AddEhsanViewModel::class.java]
            val profile = provider[ProfileViewModel::class.java]
            compose.setContent { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                if (!disposed.value) IhsanTheme(darkTheme = dark.value) {
                    if (management.value) ManageListingsScreen(if (request) "REQUEST" else "OFFER", {}, {}, profile)
                    else if (request) RequestHelpScreen({ management.value = true }, form)
                    else AddEhsanScreen(onNavigateBack = { management.value = true }, viewModel = form)
                }
            } }
            val title = "اختبار محلي معزول"
            compose.onAllNodes(hasSetTextAction())[0].performTextInput(title)
            compose.onAllNodes(hasSetTextAction())[1].performTextInput("بيانات اختبار، لا حالة حقيقية ولا تواصل")
            if (request) compose.onNodeWithText("اتصال هاتفي").performScrollTo().performClick()
            compose.onNodeWithText("التالي").performScrollTo().performClick()
            compose.onNodeWithText("التالي").performScrollTo().performClick()
            compose.onNodeWithText(if (request) "إرسال طلب المساعدة" else "نشر العرض").performScrollTo().performClick()
            compose.waitUntil(20_000) { management.value && runBlocking { repo.getAllDonations().first().any { it.title == title } } }
            val row = runBlocking { repo.getAllDonations().first().single() }
            assertEquals(if (request) "REQUEST" else "OFFER", row.type)
            assertEquals(if (request) "PHONE" else "WHATSAPP", row.contactMethod)
            assertEquals("USER", row.origin)
            assertTrue(row.ownerId!!.startsWith("local-profile:"))
            for (night in listOf(false, true)) {
                compose.runOnIdle { dark.value = night }
                compose.waitUntil(10_000) { compose.onAllNodesWithText("تعديل").fetchSemanticsNodes().isNotEmpty() }
                compose.onNodeWithText("تعديل").performClick()
                compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("عنوان ${if (night) "داكن" else "فاتح"}")
                compose.onNodeWithText("حفظ").performClick()
                val expected = "عنوان ${if (night) "داكن" else "فاتح"}"
                compose.waitUntil(10_000) { runBlocking { repo.getDonationById(row.id)?.title == expected } }
                compose.onNodeWithText("حذف").performClick()
                compose.onNodeWithText("إلغاء").performClick()
                assertNotNull(runBlocking { repo.getDonationById(row.id) })
            }
            compose.onNodeWithText("إنهاء محلي").performClick()
            compose.waitUntil(10_000) { runBlocking { repo.getDonationById(row.id)?.status == "COMPLETED" } }
            compose.onNodeWithText("إعادة إتاحة").performClick()
            compose.waitUntil(10_000) { runBlocking { repo.getDonationById(row.id)?.status == "AVAILABLE" } }
            compose.onNodeWithText("حذف").performClick()
            compose.onAllNodesWithText("حذف").onLast().performClick()
            compose.waitUntil(10_000) { runBlocking { repo.getDonationById(row.id) == null } }
        } finally {
            compose.runOnIdle { disposed.value = true }
            compose.waitForIdle()
            store.clear()
            db.close()
        }
    }
}
