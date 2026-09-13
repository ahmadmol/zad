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

data class ManualPrayerTrackerUiState(
    val date: LocalDate = LocalDate.now(),
    val trackable: List<PrayerName> = TrackablePrayers.all,
    val logs: Map<PrayerName, ManualPrayerStatus> = emptyMap()
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
    private val repository: ManualPrayerLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualPrayerTrackerUiState())
    val uiState: StateFlow<ManualPrayerTrackerUiState> = _uiState.asStateFlow()

    init {
        observeDay(_uiState.value.date)
    }

    private fun observeDay(date: LocalDate) {
        repository.observeDay(date)
            .onEach { summary -> _uiState.update { it.copy(date = date, logs = summary.logs) } }
            .launchIn(viewModelScope)
    }

    /**
     * Cycles a prayer through: not recorded → performed → late → not performed →
     * not recorded. Tapping again always gets the user back to "not recorded", so a
     * mistaken tap is never sticky.
     */
    fun onPrayerTapped(prayer: PrayerName) {
        if (!TrackablePrayers.isTrackable(prayer)) return
        viewModelScope.launch {
            when (_uiState.value.logs[prayer]) {
                null -> repository.log(_uiState.value.date, prayer, ManualPrayerStatus.PERFORMED)
                ManualPrayerStatus.PERFORMED ->
                    repository.log(_uiState.value.date, prayer, ManualPrayerStatus.LATE)
                ManualPrayerStatus.LATE ->
                    repository.log(_uiState.value.date, prayer, ManualPrayerStatus.NOT_PERFORMED)
                ManualPrayerStatus.NOT_PERFORMED ->
                    repository.clear(_uiState.value.date, prayer)
            }
        }
    }
}
