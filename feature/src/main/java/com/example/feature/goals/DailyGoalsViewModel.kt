package com.example.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate

data class DailyGoalsState(val ledger: DailyGoalLedger? = null, val date: LocalDate = LocalDate.now(),
    val loading: Boolean = true, val readError: String? = null, val actionError: String? = null,
    val busy: Boolean = false, val saved: Int = 0) {
    val todayGoals: List<DailyGoal> get() = ledger?.goals?.filter { it.scheduledOn(date) }.orEmpty()
}

class DailyGoalsViewModel(private val repository: DailyGoalRepository) : ViewModel() {
    private val reload = MutableStateFlow(0)
    private val operations = MutableStateFlow(DailyGoalsState(loading = false))
    // Subscription owns the timer. No daily rollover writes, inferred completion or background polling.
    private val dates = flow {
        while (currentCoroutineContext().isActive) { emit(repository.today()); delay(30_000) }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = reload.flatMapLatest {
        combine(repository.ledger, dates) { ledger, date -> DailyGoalsState(ledger, date, loading = false) }
            .catch { error ->
                if (error is CancellationException) throw error
                emit(DailyGoalsState(date = repository.today(), loading = false,
                    readError = "تعذر قراءة الأهداف. أعد المحاولة دون مسح البيانات."))
            }
    }.combine(operations) { data, op -> data.copy(busy = op.busy, actionError = op.actionError, saved = op.saved) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), DailyGoalsState(date = repository.today()))

    fun retry() { reload.value++ }
    fun save(id: String?, title: String, target: Int, weekdays: Set<Int>) = mutate(saved = true) { repository.save(id, title, target, weekdays) }
    fun archive(id: String) = mutate { repository.archive(id) }
    fun increment(id: String, date: LocalDate, delta: Int) = mutate { repository.increment(id, date, delta) }
    fun complete(id: String, date: LocalDate, completed: Boolean) = mutate { repository.complete(id, date, completed) }
    private fun mutate(saved: Boolean = false, action: suspend () -> Unit) {
        if (operations.value.busy || state.value.ledger == null) return
        operations.update { it.copy(busy = true, actionError = null) }
        viewModelScope.launch {
            try {
                action()
                if (saved) operations.update { it.copy(saved = it.saved + 1) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { operations.update { it.copy(actionError = "تعذر حفظ التغيير؛ بقيت بياناتك. راجع اليوم والمدخلات ثم أعد المحاولة.") } }
            finally { operations.update { it.copy(busy = false) } }
        }
    }
}
