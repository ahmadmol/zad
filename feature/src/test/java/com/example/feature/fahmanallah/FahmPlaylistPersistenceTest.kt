package com.example.feature.fahmanallah

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.fahmanallah.data.repository.FahmRepositoryImpl
import com.example.feature.fahmanallah.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder

class FahmPlaylistPersistenceTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun `real existing learning keys survive reorder progress signout and file reopen`() = runBlocking {
        val file=temporary.root.resolve("learning.preferences_pb")
        val job=SupervisorJob()
        val prefs=UserPreferences(PreferenceDataStoreFactory.create(scope=CoroutineScope(Dispatchers.IO+job),produceFile={file}))
        prefs.updateLearningIds(FahmRepositoryImpl.STATE_KEY) {setOf("ep_01|IN_PROGRESS|123|true|true","future_episode|COMPLETED|777|true|false")}
        val repo=FahmRepositoryImpl(prefs)
        repo.moveEpisode("ep_02",-1)
        repo.toggleFavorite("ep_03")
        repo.markCompleted("ep_01")
        prefs.clearUserPresentation()
        assertTrue(prefs.learningIds(FahmRepositoryImpl.STATE_KEY).first().contains("future_episode|COMPLETED|777|true|false"))
        job.cancelAndJoin()
        val reopenedJob=SupervisorJob()
        try {
            val restoredPrefs=UserPreferences(PreferenceDataStoreFactory.create(scope=CoroutineScope(Dispatchers.IO+reopenedJob),produceFile={file}))
            val restored=FahmRepositoryImpl(restoredPrefs)
            val rows=restored.getEpisodes().first()
            assertEquals("ep_02",rows.first().id)
            val first=rows.first {it.id=="ep_01"}
            assertEquals(FahmEpisodeProgressState.COMPLETED,first.progressState)
            assertEquals(123,first.lastPlayedSeconds);assertTrue(first.isFavorite);assertTrue(first.isWatchLater)
            assertTrue(rows.first {it.id=="ep_03"}.isFavorite)
            assertEquals(FahmEpisodeProgressState.NOT_STARTED,rows.first {it.id=="ep_02"}.progressState)
            assertTrue(restoredPrefs.learningIds(FahmRepositoryImpl.STATE_KEY).first().contains("future_episode|COMPLETED|777|true|false"))
        } finally {reopenedJob.cancelAndJoin()}
    }
    @Test fun `order handles missing duplicated and newly available catalog IDs without losing unknown keys`() {
        val stored=listOf("b","missing","b")
        assertEquals(listOf("b","a","c"),FahmPlaylistOrder.orderedIds(listOf("a","b","c"),stored))
        assertEquals(listOf("a","b","c","missing"),FahmPlaylistOrder.move(listOf("a","b","c"),stored,"a",-1))
        assertEquals(listOf("b","missing","a"),FahmPlaylistOrder.orderedIds(listOf("a","b","missing"),stored))
    }
    @Test fun `edge moves are stable and missing lesson cannot invent a playlist entry`() {
        assertEquals(listOf("a","b"),FahmPlaylistOrder.move(listOf("a","b"),emptyList(),"a",-1))
        try {FahmPlaylistOrder.move(listOf("a"),emptyList(),"missing",1);fail()} catch (_:IllegalArgumentException) {}
    }
}
