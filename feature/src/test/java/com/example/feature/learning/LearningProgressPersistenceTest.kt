package com.example.feature.learning
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.learning.data.LearningProgressStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import java.time.*
class LearningProgressPersistenceTest {
 @Test fun `manual progress reopens preserves favorites unknown ids and survives presentation signout`() = runTest {
  val file = Files.createTempDirectory("learning-progress").resolve("prefs.preferences_pb").toFile()
  var scope = CoroutineScope(SupervisorJob()+Dispatchers.IO)
  var prefs = UserPreferences(PreferenceDataStoreFactory.create(scope=scope,produceFile={file}))
  prefs.toggleLearningId("nabi_favorites","official-1")
  prefs.updateLearningIds("nabi_progress_v1") { it + "future-format" }
  val clock=Clock.fixed(Instant.parse("2026-10-21T08:00:00Z"),ZoneOffset.UTC)
  var store=LearningProgressStore(prefs,clock)
  assertTrue(store.observe("nabi").first().isEmpty())
  store.save("nabi","official-1","official-1",150,true)
  store.save("sanhya","episode-2","story-1",33,false)
  prefs.clearUserPresentation()
  scope.cancel(); scope.coroutineContext[Job]!!.join()
  scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
  try {
   prefs=UserPreferences(PreferenceDataStoreFactory.create(scope=scope,produceFile={file}));store=LearningProgressStore(prefs,clock)
   assertEquals(150,store.observe("nabi").first().getValue("official-1").seconds)
   assertTrue(store.observe("nabi").first().getValue("official-1").completed)
   assertEquals("story-1",store.observe("sanhya").first().getValue("episode-2").parentId)
   assertEquals(setOf("official-1"),prefs.learningIds("nabi_favorites").first())
   store.save("nabi","official-1","official-1",150,false)
   assertFalse(store.observe("nabi").first().getValue("official-1").completed)
   assertTrue("future-format" in prefs.learningIds("nabi_progress_v1").first())
  } finally { scope.cancel();scope.coroutineContext[Job]!!.join() }
 }
 @Test fun `limits and version parser do not invent completion`() = runTest {
  assertNull(LearningProgressStore.decode("2|x|x|20|true|0"))
  assertNull(LearningProgressStore.decode("1|x|x|90000|true|0"))
  assertNull(LearningProgressStore.decode("1|x|x|20|invalid|0"))
  assertFalse(LearningProgressStore.decode("1|x|x|20|false|0")!!.completed)
 }
}
