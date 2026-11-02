package com.example.feature.goals

import androidx.datastore.preferences.core.*
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import java.time.*

class DailyGoalPersistenceTest {
    private class MutableClock(var instant: Instant) : Clock() {
        override fun instant() = instant
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = Clock.fixed(instant, zone)
    }

    @Test fun `explicit counts edits archiving and existing keys survive closing and reopening`() = runTest {
        val file = Files.createTempDirectory("ihsan-goals").resolve("preferences.preferences_pb").toFile()
        var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val clock = MutableClock(Instant.parse("2026-11-02T12:00:00Z"))
        var store = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
        var prefs = UserPreferences(store)
        var repo = DailyGoalRepository(prefs, clock)
        prefs.saveLastRead(2, 25)
        prefs.toggleLearningId("nabi_favorites", "ep_01")
        store.edit { it[stringPreferencesKey("future_preserved_key")] = "preserve" }
        repo.save(null, "Personal goal", 5, (1..7).toSet())
        val id = repo.ledger.first().goals.single().id
        assertEquals(0, repo.ledger.first().count(id, repo.today()))
        repo.increment(id, repo.today(), 1)
        repo.complete(id, repo.today(), true)
        assertEquals(5, repo.ledger.first().count(id, repo.today()))
        repo.complete(id, repo.today(), false)
        assertEquals(0, repo.ledger.first().count(id, repo.today()))
        repo.complete(id, repo.today(), true)
        clock.instant = Instant.parse("2026-11-03T01:00:00Z")
        assertEquals(0, repo.ledger.first().count(id, repo.today()))
        repo.save(id, "Edited goal", 10, setOf(1, 2, 3))
        assertEquals(5, repo.ledger.first().entries.single().count)
        assertEquals(5, repo.ledger.first().entries.single().target)
        prefs.clearUserPresentation()
        scope.cancel(); scope.coroutineContext[Job]!!.join()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            store = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
            prefs = UserPreferences(store); repo = DailyGoalRepository(prefs, clock)
            assertEquals("Edited goal", repo.ledger.first().goals.single().title)
            assertEquals(2 to 25, prefs.lastReadPosition.first())
            assertEquals(setOf("ep_01"), prefs.learningIds("nabi_favorites").first())
            assertEquals("preserve", store.data.first()[stringPreferencesKey("future_preserved_key")])
            repo.archive(id)
            assertTrue(repo.ledger.first().goals.single().archived)
            assertEquals(5, repo.ledger.first().entries.single().count)
        } finally { scope.cancel(); scope.coroutineContext[Job]!!.join() }
    }

    @Test fun `concurrent accepted taps are atomic bounded and independent of worship journals`() = runTest {
        val file = Files.createTempDirectory("ihsan-goal-taps").resolve("preferences.preferences_pb").toFile()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val prefs = UserPreferences(PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }))
            val repo = DailyGoalRepository(prefs, Clock.fixed(Instant.parse("2026-11-02T12:00:00Z"), ZoneOffset.UTC))
            repo.save(null, "Counted goal", 50, (1..7).toSet())
            val id = repo.ledger.first().goals.single().id
            coroutineScope { repeat(70) { launch(Dispatchers.Default) { repo.increment(id, repo.today(), 1) } } }
            assertEquals(50, repo.ledger.first().count(id, repo.today()))
            assertEquals(1, repo.ledger.first().entries.size)
            assertTrue(prefs.dailyActivityCounts.first().values.all { it == 0 })
            repo.increment(id, repo.today(), -10_000)
            assertEquals(0, repo.ledger.first().count(id, repo.today()))
        } finally { scope.cancel(); scope.coroutineContext[Job]!!.join() }
    }

    @Test fun `weekdays and midnight prevent stale-day actions without creating completion`() = runTest {
        val file = Files.createTempDirectory("ihsan-goal-midnight").resolve("preferences.preferences_pb").toFile()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val clock = MutableClock(Instant.parse("2026-11-02T23:59:59Z"))
            val repo = DailyGoalRepository(UserPreferences(PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })), clock)
            repo.save(null, "Monday", 1, setOf(1))
            val goal = repo.ledger.first().goals.single()
            val monday = repo.today()
            assertTrue(goal.scheduledOn(monday))
            assertFalse(goal.scheduledOn(monday.minusDays(7)))
            clock.instant = Instant.parse("2026-11-03T00:00:00Z")
            assertFalse(goal.scheduledOn(repo.today()))
            assertTrue(runCatching { repo.complete(goal.id, monday, true) }.isFailure)
            assertTrue(runCatching { repo.complete(goal.id, repo.today(), true) }.isFailure)
            assertTrue(repo.ledger.first().entries.isEmpty())
        } finally { scope.cancel(); scope.coroutineContext[Job]!!.join() }
    }

    @Test fun `corrupt and future-version records are never replaced with an empty ledger`() = runTest {
        val file = Files.createTempDirectory("ihsan-goal-corrupt").resolve("preferences.preferences_pb").toFile()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val store = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
            val repo = DailyGoalRepository(UserPreferences(store))
            val key = stringPreferencesKey("personal_daily_goals_v1")
            for (raw in listOf("{broken", """{"version":2,"goals":[],"entries":[]}""")) {
                store.edit { it[key] = raw }
                assertTrue(runCatching { repo.ledger.first() }.isFailure)
                assertTrue(runCatching { repo.save(null, "Test", 1, setOf(1)) }.isFailure)
                assertEquals(raw, store.data.first()[key])
            }
        } finally { scope.cancel(); scope.coroutineContext[Job]!!.join() }
    }

    @Test fun `invalid goals never reach persistence`() = runTest {
        val file = Files.createTempDirectory("ihsan-goal-validation").resolve("preferences.preferences_pb").toFile()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val repo = DailyGoalRepository(UserPreferences(PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })))
            for ((title, target, days) in listOf(Triple(" ", 1, setOf(1)), Triple("x", 0, setOf(1)), Triple("x", 10_001, setOf(1)), Triple("x", 1, emptySet()), Triple("x", 1, setOf(8)))) {
                assertTrue(runCatching { repo.save(null, title, target, days) }.isFailure)
            }
            assertTrue(repo.ledger.first().goals.isEmpty())
        } finally { scope.cancel(); scope.coroutineContext[Job]!!.join() }
    }
}
