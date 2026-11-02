package com.example.mol.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.nabiihsan.presentation.NabiIhsanMainScreen
import com.example.feature.learning.data.*
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LearningInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun searchDoesNotNavigateBeforeExplicitSubmission() {
        var submissions = 0
        var query = ""
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme { NabiIhsanMainScreen(onNavigateToSearch = { submissions++; query = it }) }
            }
        }
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("الهجرة")
        compose.runOnIdle { assertEquals(0, submissions) }
        compose.onAllNodes(hasSetTextAction())[0].performImeAction()
        compose.runOnIdle { assertEquals(1, submissions); assertEquals("الهجرة", query) }
    }
    @Test fun actualDataStoreBookmarksAndBioSurviveNewStoreInstances() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = UserPreferences(context)
        val savedBio = preferences.userBio.first()
        val first = PreferenceLearningBookmarkStore(preferences)
        val id = "qa-${System.nanoTime()}"
        try {
            first.toggle(LearningCollection.SANHYA_FAVORITES, id)
            first.toggle(LearningCollection.NABI_LATER, id)
            preferences.setUserBio("نبذة محلية محفوظة")
            val reopened = PreferenceLearningBookmarkStore(UserPreferences(context))
            assertTrue(id in reopened.observe(LearningCollection.SANHYA_FAVORITES).first())
            assertTrue(id in reopened.observe(LearningCollection.NABI_LATER).first())
            assertFalse(id in reopened.observe(LearningCollection.NABI_FAVORITES).first())
            assertEquals("نبذة محلية محفوظة", UserPreferences(context).userBio.first())
            preferences.clearUserPresentation() // the same reset invoked by local-profile logout
            assertEquals("", preferences.userBio.first())
            assertTrue(id in reopened.observe(LearningCollection.SANHYA_FAVORITES).first())
            assertTrue(id in reopened.observe(LearningCollection.NABI_LATER).first())
        } finally {
            first.toggle(LearningCollection.SANHYA_FAVORITES, id)
            first.toggle(LearningCollection.NABI_LATER, id)
            preferences.setUserBio(savedBio)
        }
    }
}
