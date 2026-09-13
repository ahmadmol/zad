package com.example.feature.prayer

import com.example.feature.prayer.data.local.dao.PrayerLogDao
import com.example.feature.prayer.data.local.entity.PrayerLogEntity
import com.example.feature.prayer.data.repository.ManualPrayerLogRepositoryImpl
import com.example.feature.prayer.domain.model.ManualPrayerDaySummary
import com.example.feature.prayer.domain.model.ManualPrayerStatus
import com.example.feature.prayer.domain.model.PrayerName
import com.example.feature.prayer.domain.model.TrackablePrayers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Phase 6 — Manual Prayer Tracker behaviour, over an in-memory DAO. */
class ManualPrayerLogRepositoryTest {

    /** Mirrors the Room DAO contract, including REPLACE-on-conflict semantics. */
    private class FakePrayerLogDao : PrayerLogDao {
        val rows = MutableStateFlow<List<PrayerLogEntity>>(emptyList())

        override suspend fun upsert(entry: PrayerLogEntity) {
            rows.value = rows.value.filterNot {
                it.dateEpochDay == entry.dateEpochDay && it.prayerName == entry.prayerName
            } + entry
        }

        override suspend fun delete(dateEpochDay: Long, prayerName: String) {
            rows.value = rows.value.filterNot {
                it.dateEpochDay == dateEpochDay && it.prayerName == prayerName
            }
        }

        override fun observeDay(dateEpochDay: Long): Flow<List<PrayerLogEntity>> =
            rows.map { all -> all.filter { it.dateEpochDay == dateEpochDay } }

        override fun observeRange(
            fromEpochDay: Long,
            toEpochDay: Long
        ): Flow<List<PrayerLogEntity>> = rows.map { all ->
            all.filter { it.dateEpochDay in fromEpochDay..toEpochDay }
                .sortedBy { it.dateEpochDay }
        }

        override suspend fun getRange(
            fromEpochDay: Long,
            toEpochDay: Long
        ): List<PrayerLogEntity> =
            rows.value.filter { it.dateEpochDay in fromEpochDay..toEpochDay }

        override suspend fun clearAll() {
            rows.value = emptyList()
        }
    }

    private val dao = FakePrayerLogDao()
    private val repository = ManualPrayerLogRepositoryImpl(dao)
    private val today = LocalDate.of(2026, 3, 10)

    @Test
    fun `logging a prayer records it for that day`() = runTest {
        repository.log(today, PrayerName.FAJR, ManualPrayerStatus.PERFORMED)

        val summary = repository.observeDay(today).first()
        assertEquals(ManualPrayerStatus.PERFORMED, summary.logs[PrayerName.FAJR])
        assertEquals(1, summary.loggedCount)
    }

    @Test
    fun `logging the same prayer twice is idempotent`() = runTest {
        repository.log(today, PrayerName.ASR, ManualPrayerStatus.PERFORMED)
        repository.log(today, PrayerName.ASR, ManualPrayerStatus.LATE)

        assertEquals(1, dao.rows.value.size)
        val summary = repository.observeDay(today).first()
        assertEquals(ManualPrayerStatus.LATE, summary.logs[PrayerName.ASR])
    }

    @Test
    fun `clearing returns a prayer to not recorded`() = runTest {
        repository.log(today, PrayerName.ISHA, ManualPrayerStatus.PERFORMED)
        repository.clear(today, PrayerName.ISHA)

        val summary = repository.observeDay(today).first()
        assertTrue(summary.logs.isEmpty())
        assertEquals(TrackablePrayers.all.size, summary.unloggedCount)
    }

    @Test
    fun `sunrise can never be logged`() = runTest {
        repository.log(today, PrayerName.SUNRISE, ManualPrayerStatus.PERFORMED)

        assertTrue(dao.rows.value.isEmpty())
        assertFalse(TrackablePrayers.isTrackable(PrayerName.SUNRISE))
        assertEquals(5, TrackablePrayers.all.size)
    }

    @Test
    fun `days are isolated from each other`() = runTest {
        repository.log(today, PrayerName.FAJR, ManualPrayerStatus.PERFORMED)
        repository.log(today.plusDays(1), PrayerName.FAJR, ManualPrayerStatus.PERFORMED)

        assertEquals(1, repository.observeDay(today).first().loggedCount)
        assertEquals(1, repository.observeDay(today.plusDays(1)).first().loggedCount)
    }

    @Test
    fun `range query groups by day in order`() = runTest {
        repository.log(today, PrayerName.FAJR, ManualPrayerStatus.PERFORMED)
        repository.log(today, PrayerName.ASR, ManualPrayerStatus.PERFORMED)
        repository.log(today.plusDays(2), PrayerName.ISHA, ManualPrayerStatus.LATE)

        val week: List<ManualPrayerDaySummary> =
            repository.observeRange(today, today.plusDays(6)).first()

        assertEquals(2, week.size)
        assertEquals(today, week[0].date)
        assertEquals(2, week[0].performedCount)
        assertEquals(today.plusDays(2), week[1].date)
        assertEquals(1, week[1].performedCount)
    }

    @Test
    fun `late counts as performed but not performed does not`() = runTest {
        repository.log(today, PrayerName.FAJR, ManualPrayerStatus.LATE)
        repository.log(today, PrayerName.DHUHR, ManualPrayerStatus.NOT_PERFORMED)

        val summary = repository.observeDay(today).first()
        assertEquals(2, summary.loggedCount)
        assertEquals(1, summary.performedCount)
    }

    @Test
    fun `unlogged prayers are never reported as missed`() = runTest {
        repository.log(today, PrayerName.FAJR, ManualPrayerStatus.PERFORMED)

        val summary = repository.observeDay(today).first()
        // The remaining four are simply absent — the app makes no claim about them.
        assertEquals(4, summary.unloggedCount)
        assertFalse(summary.logs.containsKey(PrayerName.MAGHRIB))
    }

    @Test
    fun `unknown stored status degrades instead of dropping the row`() = runTest {
        dao.upsert(
            PrayerLogEntity(
                dateEpochDay = today.toEpochDay(),
                prayerName = PrayerName.MAGHRIB.name,
                status = "SOMETHING_OLD",
                loggedAtEpochMillis = 0L
            )
        )

        val summary = repository.observeDay(today).first()
        assertEquals(ManualPrayerStatus.PERFORMED, summary.logs[PrayerName.MAGHRIB])
    }

    @Test
    fun `corrupt prayer name rows are ignored rather than crashing`() = runTest {
        dao.upsert(
            PrayerLogEntity(
                dateEpochDay = today.toEpochDay(),
                prayerName = "NOT_A_PRAYER",
                status = ManualPrayerStatus.PERFORMED.name,
                loggedAtEpochMillis = 0L
            )
        )

        assertTrue(repository.observeDay(today).first().logs.isEmpty())
    }

    @Test
    fun `clearAll removes every entry`() = runTest {
        repository.log(today, PrayerName.FAJR, ManualPrayerStatus.PERFORMED)
        repository.log(today.plusDays(1), PrayerName.ISHA, ManualPrayerStatus.PERFORMED)

        repository.clearAll()

        assertTrue(dao.rows.value.isEmpty())
    }
}
