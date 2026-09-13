package com.example.feature.prayer.data.repository

import com.example.feature.prayer.data.local.dao.PrayerLogDao
import com.example.feature.prayer.data.local.entity.PrayerLogEntity
import com.example.feature.prayer.domain.model.ManualPrayerDaySummary
import com.example.feature.prayer.domain.model.ManualPrayerStatus
import com.example.feature.prayer.domain.model.PrayerName
import com.example.feature.prayer.domain.model.TrackablePrayers
import com.example.feature.prayer.domain.repository.ManualPrayerLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ManualPrayerLogRepositoryImpl(
    private val dao: PrayerLogDao
) : ManualPrayerLogRepository {

    override suspend fun log(date: LocalDate, prayer: PrayerName, status: ManualPrayerStatus) {
        // Sunrise is a time marker, not an obligatory prayer — never loggable.
        if (!TrackablePrayers.isTrackable(prayer)) return
        dao.upsert(
            PrayerLogEntity(
                dateEpochDay = date.toEpochDay(),
                prayerName = prayer.name,
                status = status.name,
                loggedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun clear(date: LocalDate, prayer: PrayerName) {
        dao.delete(date.toEpochDay(), prayer.name)
    }

    override fun observeDay(date: LocalDate): Flow<ManualPrayerDaySummary> =
        dao.observeDay(date.toEpochDay()).map { rows -> rows.toSummary(date) }

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<ManualPrayerDaySummary>> =
        dao.observeRange(from.toEpochDay(), to.toEpochDay()).map { rows ->
            rows.groupBy { it.dateEpochDay }
                .toSortedMap()
                .map { (epochDay, dayRows) ->
                    dayRows.toSummary(LocalDate.ofEpochDay(epochDay))
                }
        }

    override suspend fun clearAll() = dao.clearAll()

    private fun List<PrayerLogEntity>.toSummary(date: LocalDate): ManualPrayerDaySummary =
        ManualPrayerDaySummary(
            date = date,
            logs = mapNotNull { row ->
                val prayer = runCatching { PrayerName.valueOf(row.prayerName) }.getOrNull()
                    ?: return@mapNotNull null
                if (!TrackablePrayers.isTrackable(prayer)) return@mapNotNull null
                prayer to ManualPrayerStatus.fromStorage(row.status)
            }.toMap()
        )
}
