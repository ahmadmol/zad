package com.example.feature.azkar.data.local.database

import com.example.feature.azkar.data.local.dao.AzkarDao
import com.example.feature.azkar.data.local.entity.ZikrEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 2B — AzkarSeedManager unit tests.
 *
 * These tests use an in-memory [AzkarSeedVersionStore] and an in-memory
 * [AzkarDao] fake. The contract under test is the manager's behavior
 * against a database that may be empty, partial, or already-seeded; user
 * state (favorites / counts / progress) is never read or mutated.
 */
class AzkarSeedManagerTest {

    @Test
    fun `empty azkar_table is fully populated`() = runTest {
        val dao = FakeAzkarDao(initial = emptyList())
        val settings = FakeSeedStore(initialVersion = 0)
        val manager = AzkarSeedManager(dao, settings)

        val inserted = manager.ensureSeeded()

        assertEquals(AzkarSeedManager.SEED_ROWS.size, inserted)
        assertEquals(AzkarSeedManager.SEED_ROWS.size, dao.rows().size)
        assertEquals(AzkarSeedManager.CURRENT_SEED_VERSION, settings.currentVersion())
    }

    @Test
    fun `partial seed repairs only missing rows`() = runTest {
        val existing = AzkarSeedManager.SEED_ROWS.take(2).mapIndexed { idx, seed ->
            seed.copy(
                id = (idx + 1).toLong(),
                currentCount = 99,
                isFavorite = idx == 0,
                dailyProgress = 12,
                lastUpdatedDate = "2026-08-23"
            )
        }
        val dao = FakeAzkarDao(initial = existing)
        val settings = FakeSeedStore(initialVersion = 0)
        val manager = AzkarSeedManager(dao, settings)

        val inserted = manager.ensureSeeded()

        assertEquals(AzkarSeedManager.SEED_ROWS.size - 2, inserted)
        assertEquals(AzkarSeedManager.SEED_ROWS.size, dao.rows().size)
        // Pre-existing rows must remain untouched.
        val firstTwo = dao.rows().sortedBy { it.id }.take(2)
        assertEquals(1L, firstTwo[0].id)
        assertEquals(99, firstTwo[0].currentCount)
        assertTrue(firstTwo[0].isFavorite)
        assertEquals(2L, firstTwo[1].id)
        assertEquals(99, firstTwo[1].currentCount)
    }

    @Test
    fun `seed run twice is idempotent`() = runTest {
        val dao = FakeAzkarDao(initial = emptyList())
        val settings = FakeSeedStore(initialVersion = 0)
        val manager = AzkarSeedManager(dao, settings)

        val first = manager.ensureSeeded()
        val second = manager.ensureSeeded()

        assertEquals(AzkarSeedManager.SEED_ROWS.size, first)
        assertEquals(0, second)
        assertEquals(AzkarSeedManager.SEED_ROWS.size, dao.rows().size)
        assertEquals(AzkarSeedManager.CURRENT_SEED_VERSION, settings.currentVersion())
    }

    @Test
    fun `stored version matching current skips re-insert unless a row is missing`() = runTest {
        val dao = FakeAzkarDao(initial = emptyList())
        val settings = FakeSeedStore(initialVersion = AzkarSeedManager.CURRENT_SEED_VERSION)
        val manager = AzkarSeedManager(dao, settings)

        // Manually pre-populate the table so the partial-recovery path is the
        // only one that could insert anything.
        val preInserted = AzkarSeedManager.SEED_ROWS.mapIndexed { idx, seed ->
            seed.copy(id = (idx + 1).toLong())
        }
        preInserted.forEach { dao.insertZikr(it) }

        val inserted = manager.ensureSeeded()

        assertEquals(0, inserted)
        assertEquals(preInserted.size, dao.rows().size)
    }

    @Test
    fun `user-added row with same content key as seed is not duplicated`() = runTest {
        // A user-added row that happens to share the seed's natural key
        // (title+category+text) must not be overwritten. The manager must
        // leave the existing row alone and only insert rows that are
        // genuinely missing.
        val userRow = AzkarSeedManager.SEED_ROWS.first().copy(
            id = 42L,
            currentCount = 7,
            isFavorite = true,
            dailyProgress = 3,
            lastUpdatedDate = "2026-08-22"
        )
        val dao = FakeAzkarDao(initial = listOf(userRow))
        val settings = FakeSeedStore(initialVersion = 0)
        val manager = AzkarSeedManager(dao, settings)

        val inserted = manager.ensureSeeded()

        // The seeded natural key already exists; the manager must not insert a duplicate.
        assertEquals(AzkarSeedManager.SEED_ROWS.size - 1, inserted)
        val sameKey = dao.rows().filter { it.id == 42L }
        assertEquals(1, sameKey.size)
        assertEquals(7, sameKey.first().currentCount)
        assertTrue(sameKey.first().isFavorite)
    }

    @Test
    fun `concurrent callers are serialized`() = runTest {
        val dao = FakeAzkarDao(initial = emptyList())
        val settings = FakeSeedStore(initialVersion = 0)
        val manager = AzkarSeedManager(dao, settings)

        // Fire two concurrent repair jobs. Even if both reach the read
        // step, the mutex inside the manager must serialize them, and the
        // second call must observe the bumped version and no-op.
        val totalInserted = coroutineScope {
            val a = async { manager.ensureSeeded() }
            val b = async { manager.ensureSeeded() }
            a.await() + b.await()
        }

        assertEquals(AzkarSeedManager.SEED_ROWS.size, totalInserted)
        assertEquals(AzkarSeedManager.SEED_ROWS.size, dao.rows().size)
    }

    @Test
    fun `current seed version is at least one`() {
        // Bumping CURRENT_SEED_VERSION is a deliberate act; the unit test
        // asserts the constant is non-zero and the SEED_ROWS list is
        // non-empty so the repair always has something to do.
        assertTrue(AzkarSeedManager.CURRENT_SEED_VERSION >= 1)
        assertTrue(AzkarSeedManager.SEED_ROWS.isNotEmpty())
    }

    @Test
    fun `ensureSeededIfTableEmpty is a no-op when the table is not empty`() = runTest {
        // Pre-seed the table with a single user row (no canonical match).
        val existing = AzkarSeedManager.SEED_ROWS.first().copy(
            id = 99L,
            currentCount = 5,
            isFavorite = true
        )
        val dao = FakeAzkarDao(initial = listOf(existing))
        val settings = FakeSeedStore(initialVersion = 0)
        val manager = AzkarSeedManager(dao, settings)

        val ran = manager.ensureSeededIfTableEmpty()

        assertEquals(false, ran)
        // No data was inserted and no version was bumped.
        assertEquals(1, dao.rows().size)
        assertEquals(0, settings.currentVersion())
    }

    @Test
    fun `ensureSeededIfTableEmpty repairs when the table is empty`() = runTest {
        val dao = FakeAzkarDao(initial = emptyList())
        val settings = FakeSeedStore(initialVersion = 0)
        val manager = AzkarSeedManager(dao, settings)

        val ran = manager.ensureSeededIfTableEmpty()

        assertEquals(true, ran)
        assertEquals(AzkarSeedManager.SEED_ROWS.size, dao.rows().size)
        assertEquals(AzkarSeedManager.CURRENT_SEED_VERSION, settings.currentVersion())
    }
}

/**
 * In-memory [AzkarSeedVersionStore] used by the unit tests. The production
 * adapter is [SettingsManagerBackedStore]; this fake holds the version
 * in a private field and survives within a single test process.
 */
private class FakeSeedStore(initialVersion: Int) : AzkarSeedVersionStore {
    private var version: Int = initialVersion
    fun currentVersion(): Int = version
    override suspend fun readSeedVersion(): Int = version
    override suspend fun writeSeedVersion(value: Int) { version = value }
}

/**
 * In-memory [AzkarDao] that satisfies the surface used by the seed manager.
 * Daily-stat methods are no-ops because the manager does not touch them.
 */
private class FakeAzkarDao(initial: List<ZikrEntity>) : AzkarDao {
    private val rows = MutableStateFlow<List<ZikrEntity>>(initial)
    private val nextId = MutableStateFlow(
        (initial.maxOfOrNull { it.id } ?: 0L) + 1L
    )

    fun rows(): List<ZikrEntity> = rows.value

    override fun getAllAzkar(): Flow<List<ZikrEntity>> = rows

    override fun getAzkarByCategory(category: String): Flow<List<ZikrEntity>> =
        rows.map { it.filter { r -> r.category == category } }

    override fun getFavoriteAzkar(): Flow<List<ZikrEntity>> =
        rows.map { it.filter { r -> r.isFavorite } }

    override suspend fun insertZikr(zikr: ZikrEntity) {
        val assigned = if (zikr.id == 0L) {
            zikr.copy(id = nextId.value).also { nextId.value += 1L }
        } else zikr
        rows.value = rows.value.filterNot { it.id == assigned.id } + assigned
    }

    override suspend fun updateZikrCount(zikrId: Long, newCount: Int, date: String) {
        rows.value = rows.value.map {
            if (it.id == zikrId) it.copy(currentCount = newCount, lastUpdatedDate = date) else it
        }
    }

    override suspend fun incrementZikrCount(zikrId: Long, date: String) {
        rows.value = rows.value.map {
            if (it.id == zikrId) {
                it.copy(
                    currentCount = it.currentCount + 1,
                    dailyProgress = if (it.lastUpdatedDate == date) it.dailyProgress + 1 else 1,
                    lastUpdatedDate = date
                )
            } else it
        }
    }

    override suspend fun resetZikrCount(zikrId: Long) {
        rows.value = rows.value.map { if (it.id == zikrId) it.copy(currentCount = 0) else it }
    }

    override suspend fun resetCategoryCount(category: String) {
        rows.value = rows.value.map { if (it.category == category) it.copy(currentCount = 0) else it }
    }

    override suspend fun toggleFavorite(zikrId: Long) {
        rows.value = rows.value.map {
            if (it.id == zikrId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    override suspend fun deleteAllAzkar() {
        rows.value = emptyList()
    }

    override suspend fun deleteZikrById(zikrId: Long) {
        rows.value = rows.value.filterNot { it.id == zikrId }
    }

    override suspend fun getZikrById(zikrId: Long): ZikrEntity? =
        rows.value.firstOrNull { it.id == zikrId }

    override suspend fun countZikr(): Int = rows.value.size

    override suspend fun countByContentKey(title: String, category: String, text: String): Int =
        rows.value.count { it.title == title && it.category == category && it.text == text }

    // Daily stats — not used by the seed manager but the interface requires them.
    override suspend fun insertOrUpdateDailyStat(stat: com.example.feature.azkar.data.local.entity.DailyStatEntity) {
        // no-op for these tests
    }

    override suspend fun getStatByDate(date: String): com.example.feature.azkar.data.local.entity.DailyStatEntity? =
        null

    override fun getLast7DaysStats(): Flow<List<com.example.feature.azkar.data.local.entity.DailyStatEntity>> =
        MutableStateFlow(emptyList())

    override suspend fun incrementDailyTotal(date: String) {
        // no-op
    }
}
