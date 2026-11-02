package com.example.feature.fasting.data

import com.example.feature.fasting.domain.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.LocalDate

/** D-03 Batch 03: civil-day keys, explicit status, optional personal label, isolated deletion. */
class FastingRepository(private val dao: FastingLogDao, private val clock: Clock? = null) {
    private val writes = Mutex()
    val records = dao.observeAll().map { rows -> rows.map { row -> FastingRecord(
        LocalDate.ofEpochDay(row.dateEpochDay), FastingStatus.entries.firstOrNull { it.name==row.status }, row.typeLabel) } }
    fun today(): LocalDate = clock?.let { LocalDate.now(it) } ?: LocalDate.now()
    suspend fun save(date: LocalDate, status: FastingStatus, typeLabel: String) = writes.withLock {
        val today = today()
        require(date <= today) { "لا يمكن تسجيل يوم مستقبلي" }
        require(typeLabel.length <= 80) { "الوصف الاختياري حتى 80 حرفًا" }
        dao.upsert(FastingLogEntity(date.toEpochDay(), status.name, typeLabel.trim(), clock?.millis() ?: System.currentTimeMillis()))
    }
    suspend fun delete(date: LocalDate) = writes.withLock { dao.deleteDay(date.toEpochDay()) }
    suspend fun clearFastingOnly() = writes.withLock { dao.clearFastingOnly() }
}
