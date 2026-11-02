package com.example.mol.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.ehsan.domain.repository.*
import com.example.feature.ehsan.presentation.AddEhsanScreen
import com.example.feature.ehsan.presentation.RequestHelpScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext

class AuditCommunityCreateJourneyTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun offerFormActuallyCreatesOwnedLocalRecord() = create(false)
    @Test fun requestFormActuallyCreatesOwnedLocalRecord() = create(true)
    private fun create(request: Boolean) {
        val users = GlobalContext.get().get<UserRepository>()
        val repo = GlobalContext.get().get<EhsanRepository>()
        val previous = runBlocking { users.getUser().first() }
        val title = "اختبار نموذج محلي ${System.nanoTime()}"
        var returned = false
        try {
            // Reserved fictional NANP number, never dialed or sent to an external service.
            runBlocking { users.saveUser(UserEntity(firstName = "اختبار", lastName = "نموذج", phoneNumber = "+12025550123")) }
            compose.setContent { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { IhsanTheme {
                if (request) RequestHelpScreen(onNavigateBack = { returned = true })
                else AddEhsanScreen(onNavigateBack = { returned = true })
            } } }
            compose.onAllNodes(hasSetTextAction())[0].performTextInput(title)
            compose.onAllNodes(hasSetTextAction())[1].performTextInput("بيانات اختبار محلية؛ لا طلب حقيقي ولا تواصل")
            if (request) compose.onNodeWithText("اتصال هاتفي").performScrollTo().performClick()
            compose.onNodeWithText("التالي").performScrollTo().performClick()
            compose.waitForIdle()
            compose.onNodeWithText("التالي").performScrollTo().performClick()
            compose.waitForIdle()
            compose.onNodeWithText(if (request) "إرسال طلب المساعدة" else "نشر العرض").performScrollTo().performClick()
            compose.waitUntil(20_000) { runBlocking { repo.getAllDonations().first().any { it.title == title } } }
            val row = runBlocking { repo.getAllDonations().first().single { it.title == title } }
            assertEquals(if (request) "REQUEST" else "OFFER", row.type)
            assertEquals("USER", row.origin)
            assertEquals(if (request) "PHONE" else "WHATSAPP", row.contactMethod)
            assertTrue(row.ownerId!!.startsWith("local-profile:"))
            compose.waitUntil(10_000) { returned }
        } finally { runBlocking {
            repo.getAllDonations().first().filter { it.title == title }.forEach { repo.deleteDonation(it) }
            if (previous == null) users.logout() else users.saveUser(previous)
        } }
    }
}
