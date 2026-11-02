package com.example.feature.tasbih.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.azkar.domain.model.DailyStat
import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.azkar.domain.usecase.GetAzkarUseCase
import com.example.feature.azkar.domain.usecase.GetLast7DaysStatsUseCase
import com.example.feature.azkar.domain.usecase.IncrementCounterUseCase
import com.example.feature.azkar.domain.usecase.ResetCounterUseCase
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.core.util.HijriDateFormatter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasbihViewModel(
    private val getAzkarUseCase: GetAzkarUseCase,
    private val incrementCounterUseCase: IncrementCounterUseCase,
    private val resetCounterUseCase: ResetCounterUseCase,
    private val settingsManager: SettingsManager,
    private val userPreferences: UserPreferences,
    private val getLast7DaysStatsUseCase: GetLast7DaysStatsUseCase? = null
) : ViewModel() {

    private val _selectedTasbihIndex = MutableStateFlow(0)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val statsFlow: Flow<List<DailyStat>> = flow { emitAll(getLast7DaysStatsUseCase?.currentPeriodStats() ?: flowOf(emptyList())) }

    private val refresh = MutableStateFlow(0)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TasbihUiState> = refresh.flatMapLatest { combine(
        flow { emitAll(getAzkarUseCase()) },
        _selectedTasbihIndex,
        settingsManager.vibrationEnabledFlow,
        statsFlow, _error
    ) { allAzkar: List<Zikr>, selectedIndex: Int, vibration: Boolean, statsList: List<DailyStat>, error: String? ->
        val tasbihList = allAzkar.filter { it.category == "تسبيح" }
        val safeIndex = selectedIndex.coerceIn(0, maxOf(0, tasbihList.size - 1))
        val today = java.time.LocalDate.now()
        val weekStart = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val monthStart = today.withDayOfMonth(1)
        fun totalSince(start: java.time.LocalDate): Int = statsList.sumOf { row ->
            val date = runCatching { java.time.LocalDate.parse(row.date) }.getOrNull()
            if (date != null && date >= start && date <= today) row.totalCount else 0
        }
        val todayCount = totalSince(today)
        val weekCount = totalSince(weekStart)
        val monthCount = totalSince(monthStart)

        TasbihUiState(
            tasbihList = tasbihList,
            selectedTasbihIndex = safeIndex,
            isVibrationEnabled = vibration,
            isLoading = false,
            error = error,
            todayCount = todayCount,
            weekCount = weekCount,
            monthCount = monthCount,
            hijriDate = HijriDateFormatter.nowFormatted()
        )
    }.onStart { emit(TasbihUiState(isLoading = true)) }
      .catch { error -> if (error is kotlinx.coroutines.CancellationException) throw error
          emit(TasbihUiState(error = "تعذر قراءة التسبيح؛ أعد المحاولة")) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasbihUiState(isLoading = true)
    )

    fun onAction(action: TasbihAction) {
        when (action) {
            TasbihAction.Retry -> { _error.value = null; refresh.value++ }
            is TasbihAction.OnIncrement -> {
                viewModelScope.launch {
                    var counterSaved = false
                    try {
                        if (!incrementCounterUseCase(action.tasbihId)) return@launch
                        counterSaved = true
                        userPreferences.incrementDailyActivityCount(DailyActivityIds.TASBEEH)
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        _error.value = if (counterSaved) "حُفظ العدّ، وتعذر تحديث ملخص اليوم. لا تحتاج إلى إعادة الضغط لحفظ هذا العدّ."
                            else "تعذر حفظ التسبيح؛ أعد المحاولة"
                    }
                }
            }
            is TasbihAction.OnReset -> {
                viewModelScope.launch {
                    try {
                        resetCounterUseCase(action.tasbihId)
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        _error.value = "تعذر حفظ التسبيح؛ أعد المحاولة"
                    }
                }
            }
            is TasbihAction.OnSelectTasbih -> {
                _selectedTasbihIndex.value = action.index
            }
            TasbihAction.OnToggleVibration -> {
                viewModelScope.launch {
                    try { settingsManager.setVibrationEnabled(!uiState.value.isVibrationEnabled) }
                    catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                    catch (error: Exception) { _error.value = "تعذر حفظ إعداد الاهتزاز" }
                }
            }
        }
    }
}
