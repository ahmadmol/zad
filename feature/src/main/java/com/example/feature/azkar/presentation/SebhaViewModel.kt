package com.example.feature.azkar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.azkar.domain.usecase.DeleteZikrUseCase
import com.example.feature.azkar.domain.usecase.EditZikrUseCase
import com.example.feature.azkar.domain.usecase.GetAzkarUseCase
import com.example.feature.azkar.domain.usecase.IncrementCounterUseCase
import com.example.feature.azkar.domain.usecase.ResetCounterUseCase
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SebhaUiEffect {
    data class ShowMessage(val message: String) : SebhaUiEffect
}

data class SebhaStats(
    val todayCount: Int,
    val thisWeekCount: Int,
    val thisMonthCount: Int,
    val totalCount: Int
)

class SebhaViewModel(
    private val getAzkarUseCase: GetAzkarUseCase,
    private val editZikrUseCase: EditZikrUseCase,
    private val deleteZikrUseCase: DeleteZikrUseCase,
    private val incrementCounterUseCase: IncrementCounterUseCase,
    private val resetCounterUseCase: ResetCounterUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val sebhahs: StateFlow<List<Zikr>> = getAzkarUseCase()
        .map { list -> list.filter { it.category.contains("سبح", ignoreCase = true) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<SebhaStats> = sebhahs
        .map { list ->
            val total = list.sumOf { it.currentCount }
            SebhaStats(
                todayCount = list.sumOf { it.dailyProgress },
                thisWeekCount = total,
                thisMonthCount = total,
                totalCount = total
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SebhaStats(0, 0, 0, 0))

    private val _effects = MutableSharedFlow<SebhaUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<SebhaUiEffect> = _effects.asSharedFlow()

    fun saveSebha(title: String, text: String, target: Int, existing: Zikr? = null) {
        val normalizedTitle = title.trim()
        val normalizedText = text.trim()
        val normalizedTarget = target.coerceAtLeast(1)

        if (normalizedTitle.isBlank()) {
            emitEffect(SebhaUiEffect.ShowMessage("يرجى كتابة اسم للمسبحة"))
            return
        }

        if (normalizedTarget <= 0) {
            emitEffect(SebhaUiEffect.ShowMessage("الهدف يجب أن يكون رقمًا صحيحًا أكبر من صفر"))
            return
        }

        viewModelScope.launch {
            if (existing != null) {
                editZikrUseCase(existing.copy(title = normalizedTitle, text = normalizedText, targetCount = normalizedTarget))
            } else {
                val zikr = Zikr(
                    id = 0,
                    title = normalizedTitle,
                    text = normalizedText,
                    currentCount = 0,
                    targetCount = normalizedTarget,
                    category = "سبحة",
                    isFavorite = false,
                    source = "user",
                    dailyProgress = 0
                )
                editZikrUseCase(zikr)
            }
        }
    }

    fun deleteSebha(id: Long) {
        viewModelScope.launch { deleteZikrUseCase(id) }
    }

    fun increment(id: Long) {
        viewModelScope.launch {
            val current = sebhahs.value.firstOrNull { it.id == id }
            val targetCount = current?.targetCount ?: 0
            val isFree = current?.category == "سبحة حرة"
            val shouldComplete = current != null && !isFree && targetCount > 0 && current.currentCount + 1 >= targetCount

            incrementCounterUseCase(id)

            if (shouldComplete) {
                userPreferences.incrementDailyActivityCount("tasbeeh", 1)
                emitEffect(SebhaUiEffect.ShowMessage("تم الوصول إلى الهدف 🎉"))
            }
        }
    }

    fun reset(id: Long) {
        viewModelScope.launch {
            resetCounterUseCase(id)
            emitEffect(SebhaUiEffect.ShowMessage("تمت إعادة تعيين العداد"))
        }
    }

    private fun emitEffect(effect: SebhaUiEffect) {
        _effects.tryEmit(effect)
    }
}
