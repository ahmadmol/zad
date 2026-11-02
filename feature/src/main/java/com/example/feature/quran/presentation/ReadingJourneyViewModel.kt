package com.example.feature.quran.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.quran.domain.model.*
import com.example.feature.quran.domain.repository.ReadingJourneyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ReadingJourneyState(val plans: List<ReadingPlan> = emptyList(),
    val completed: Map<String, Set<Int>> = emptyMap(), val notes: List<PrivateAyahNote> = emptyList(),
    val loading: Boolean = true, val error: String? = null, val busy: Boolean = false,
    val message: String? = null, val dataReady: Boolean = false,
    val deletedNoteId: String? = null) {
    val activePlan: ReadingPlan? get() = plans.firstOrNull { it.active }
}

class ReadingJourneyViewModel(private val repository: ReadingJourneyRepository) : ViewModel() {
    private val mutation = Mutex()
    private val _state = MutableStateFlow(ReadingJourneyState())
    val state = _state.asStateFlow()
    var pendingExport: String? = null
        private set

    private var observation: kotlinx.coroutines.Job? = null
    init { reload() }
    fun reload() {
        observation?.cancel()
        _state.update { it.copy(loading = true, error = null, dataReady = false) }
        observation = viewModelScope.launch {
            combine(repository.plans, repository.completedPages, repository.notes) { plans, completed, notes ->
                Triple(plans, completed, notes)
            }.catch { e ->
                if (e is CancellationException) throw e
                _state.update { it.copy(loading = false, dataReady = false, error = "تعذر قراءة بياناتك المحلية. حاول مجددًا دون حذفها.") }
            }.collect { (plans, completed, notes) ->
                _state.update { it.copy(plans = plans, completed = completed, notes = notes, loading = false, dataReady = true) }
            }
        }
    }
    private fun write(deletedId: String? = null, block: suspend () -> Unit) {
        // Ignore duplicate submits while a previous operation is pending; retain drafts on failure.
        if (_state.value.busy || !_state.value.dataReady) return
        _state.update { it.copy(busy = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                mutation.withLock { block() }
                _state.update { it.copy(message = "حُفظ التغيير على هذا الجهاز", deletedNoteId = deletedId) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update { it.copy(error = if (e is IllegalArgumentException) e.message else "تعذر الحفظ؛ بقيت البيانات السابقة محفوظة") }
            } finally { _state.update { it.copy(busy = false) } }
        }
    }
    fun create(first: Int, last: Int, days: Int) = write { repository.createPlan(first, last, days) }
    fun extend(id: String, days: Int) = write { repository.extendPlan(id, days) }
    fun confirm(id: String, pages: Set<Int>) = write { repository.confirmPages(id, pages) }
    fun clear(id: String, page: Int) = write { repository.clearPage(id, page) }
    fun saveNote(id: String?, surah: Int, ayah: Int, text: String) = write { repository.saveNote(id, surah, ayah, text) }
    fun deleteNote(id: String) = write(deletedId = id) { repository.deleteNote(id) }
    fun consumeDeletedNote() { _state.update { it.copy(deletedNoteId = null) } }
    fun clearMessage() { _state.update { it.copy(message = null, error = null) } }
    fun prepareExport(selected: Set<String>): Boolean = try {
        pendingExport = null
        check(_state.value.dataReady && !_state.value.loading) { "أعد قراءة الملاحظات قبل التصدير" }
        pendingExport = PrivateNotesExport.encode(_state.value.notes, selected)
        true
    } catch (e: IllegalStateException) {
        _state.update { it.copy(error = e.message) }; false
    } catch (e: IllegalArgumentException) {
        _state.update { it.copy(error = e.message) }; false
    }
    fun finishExport(success: Boolean) {
        pendingExport = null
        _state.update { it.copy(message = if (success) "صُدّرت الملاحظات المحددة إلى الملف الذي اخترته" else null,
            error = if (success) null else "تعذر التصدير؛ لم تتغير ملاحظاتك المحلية") }
    }
    fun cancelExport() { pendingExport = null }
}
