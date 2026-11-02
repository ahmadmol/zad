package com.example.feature.core

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class HijriPreferencePersistenceTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun `real DataStore reopens offset and leaves existing preferences intact`() = runBlocking {
        val file=temporary.root.resolve("isolated.preferences_pb")
        val job=SupervisorJob()
        val store=PreferenceDataStoreFactory.create(scope=CoroutineScope(Dispatchers.IO+job),produceFile={file})
        val preferences=UserPreferences(store)
        assertEquals(0,preferences.hijriDayOffset.first())
        store.edit { it[stringPreferencesKey("user_bio")]="private existing data" }
        for(offset in -2..2) { preferences.setHijriDayOffset(offset);assertEquals(offset,preferences.hijriDayOffset.first()) }
        for(offset in listOf(-3,3)) {
            try { preferences.setHijriDayOffset(offset);fail() } catch (_: IllegalArgumentException) {}
            assertEquals(2,preferences.hijriDayOffset.first())
        }
        job.cancelAndJoin()
        val reopenedJob=SupervisorJob()
        try {
            val reopened=PreferenceDataStoreFactory.create(scope=CoroutineScope(Dispatchers.IO+reopenedJob),produceFile={file})
            assertEquals(2,UserPreferences(reopened).hijriDayOffset.first())
            assertEquals("private existing data",reopened.data.first()[stringPreferencesKey("user_bio")])
        } finally { reopenedJob.cancelAndJoin() }
    }
}
