package com.example.feature.ramadan

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder

class RamadanPreferenceTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun `seasonal highlight reopens and signout leaves independent preferences intact`() = runBlocking {
        val file=temporary.root.resolve("ramadan.preferences_pb")
        val job=SupervisorJob()
        val prefs=UserPreferences(PreferenceDataStoreFactory.create(scope=CoroutineScope(Dispatchers.IO+job),produceFile={file}))
        assertFalse(prefs.ramadanHighlightEnabled.first())
        prefs.setHijriDayOffset(-2);prefs.setRamadanHighlightEnabled(true);prefs.setUserBio("profile")
        prefs.clearUserPresentation();job.cancelAndJoin()
        val reopenedJob=SupervisorJob()
        try {
            val reopened=UserPreferences(PreferenceDataStoreFactory.create(scope=CoroutineScope(Dispatchers.IO+reopenedJob),produceFile={file}))
            assertTrue(reopened.ramadanHighlightEnabled.first());assertEquals(-2,reopened.hijriDayOffset.first())
            assertEquals("",reopened.userBio.first())
        } finally {reopenedJob.cancelAndJoin()}
    }
}
