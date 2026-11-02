package com.example.feature.prayer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.prayer.domain.model.ManualPrayerStatus
import com.example.feature.prayer.domain.model.PrayerName
import com.example.feature.prayer.domain.model.TrackablePrayers
import com.example.feature.prayer.domain.repository.ManualPrayerLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Clock
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.feature.statistics.domain.PrayerTrackerStatsCalculator
import com.example.feature.statistics.presentation.PrayerTrackerWeekStats

data class ManualPrayerTrackerUiState(
    val date: LocalDate = LocalDate.now(),
    val trackable: List<PrayerName> = TrackablePrayers.all,
    val logs: Map<PrayerName, ManualPrayerStatus> = emptyMap(),
    val loading: Boolean = true,
    val busy: Boolean = false,
    val dataReady: Boolean = false,
    val error: String? = null,
    val month: com.example.feature.statistics.domain.PrayerMonthStats = com.example.feature.statistics.domain.PrayerMonthStats(emptyList(),0,0,0,0),
    val stats: PrayerTrackerWeekStats = PrayerTrackerWeekStats(0, 0, 0, 0)
) {
    val performedCount: Int
        get() = logs.values.count {
            it == ManualPrayerStatus.PERFORMED || it == ManualPrayerStatus.LATE
        }
}

/**
 * Phase 6 — Manual Prayer Tracker.
 *
 * Records only what the user taps. Nothing is auto-logged and nothing is ever
 * marked missed by the app.
 */
class ManualPrayerTrackerViewModel(
    private val repository: ManualPrayerLogRepository,
    private val clock: Clock? = null,
    private val savedState: SavedStateHandle = SavedStateHandle()
) : ViewModel() {

    private fun today() = clock?.let { LocalDate.now(it) } ?: LocalDate.now()
    private val _uiState = MutableStateFlow(ManualPrayerTrackerUiState(date =
        savedState.get<Long>("prayer_journal_day")?.let { LocalDate.ofEpochDay(it) }?.takeIf { it <= today() }
            ?: today()))
    val uiState: StateFlow<ManualPrayerTrackerUiState> = _uiState.asStateFlow()
    private var observer: Job? = null
    private val writes = Mutex()

    init {
        observeDay(_uiState.value.date)
    }

    private fun observeDay(date: LocalDate) {
        observer?.cancel()
        _uiState.update { it.copy(date = date, logs = emptyMap(), stats = PrayerTrackerWeekStats(0, 0, 0, 0), loading = true, error = null, dataReady = false) }
        observer = combine(repository.observeDay(date), repository.observeRange(date.minusDays(13), date),
            repository.observeRange(date.withDayOfMonth(1), minOf(date.withDayOfMonth(date.lengthOfMonth()), today()))) { day, range, month ->
            Triple(day, PrayerTrackerStatsCalculator.calculate(range, date), PrayerTrackerStatsCalculator.month(month, date, today()))
        }.catch { e ->
            if (e is CancellationException) throw e
            _uiState.update { it.copy(loading = false, dataReady = false, error = "تعذر قراءة سجل الصلاة. لم تُحذف بياناتك.") }
        }.onEach { (summary, stats, month) -> _uiState.update { it.copy(date = date, logs = summary.logs, stats = stats, month = month, loading = false, dataReady = true) } }
            .launchIn(viewModelScope)
    }

    fun selectDate(date: LocalDate) {
        if (date <= today() && !_uiState.value.busy) {
            savedState["prayer_journal_day"] = date.toEpochDay()
            observeDay(date)
        }
    }
    fun retry() = observeDay(_uiState.value.date)

    fun setStatus(prayer: PrayerName, status: ManualPrayerStatus?) {
        if (!TrackablePrayers.isTrackable(prayer) || _uiState.value.busy || !_uiState.value.dataReady) return
        val date = _uiState.value.date
        _uiState.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try { writes.withLock {
                if (status == null) repository.clear(date, prayer) else repository.log(date, prayer, status)
            } } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(error = "تعذر حفظ التغيير. حاول مجددًا.") }
            } finally { _uiState.update { it.copy(busy = false) } }
        }
    }

    /**
     * Cycles a prayer through: not recorded → performed → late → not performed →
     * not recorded. Tapping again always gets the user back to "not recorded", so a
     * mistaken tap is never sticky.
     */
    fun onPrayerTapped(prayer: PrayerName) {
        if (!TrackablePrayers.isTrackable(prayer)) return
        val next = when (_uiState.value.logs[prayer]) {
            null -> ManualPrayerStatus.PERFORMED
            ManualPrayerStatus.PERFORMED -> ManualPrayerStatus.LATE
            ManualPrayerStatus.LATE -> ManualPrayerStatus.NOT_PERFORMED
            ManualPrayerStatus.NOT_PERFORMED -> null
        }
        setStatus(prayer, next)
    }
}
