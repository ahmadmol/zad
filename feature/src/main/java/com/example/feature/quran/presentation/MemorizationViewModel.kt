package com.example.feature.quran.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.quran.data.repository.MemorizationRepository
import com.example.feature.quran.data.local.entity.*
import com.example.feature.quran.domain.model.ReviewResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class MemorizationState(val plans: List<MemorizationPlanEntity> = emptyList(),
    val reviews: List<MemorizationReviewEntity> = emptyList(), val surahs: List<SurahEntity> = emptyList(),
    val verses: List<AyahEntity> = emptyList(), val loading: Boolean = true,
    val busy: Boolean = false, val error: String? = null, val saved: Int = 0,
    val dataReady: Boolean = false)

class MemorizationViewModel(private val repository: MemorizationRepository) : ViewModel() {
    private val mutable = MutableStateFlow(MemorizationState())
    val state = mutable.asStateFlow()
    private var observer: Job? = null
    private var verseJob: Job? = null
    init { reload() }
    fun consumeSaved() { mutable.update { it.copy(saved=0) } }
    fun today() = repository.today()
    fun reload() {
        observer?.cancel()
        mutable.update { it.copy(loading = true, error = null, dataReady = false) }
        observer = viewModelScope.launch {
            combine(repository.plans, repository.reviews, repository.surahs) { p, r, s -> Triple(p,r,s) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    mutable.update { it.copy(loading = false, dataReady = false, error = "تعذر قراءة سجل الحفظ المحلي") }
                }.collect { (p,r,s) -> mutable.update { it.copy(plans=p, reviews=r, surahs=s, loading=false, dataReady=true) } }
        }
    }
    fun loadVerses(plan: MemorizationPlanEntity) {
        verseJob?.cancel()
        mutable.update { it.copy(verses = emptyList(), error = null) }
        verseJob = viewModelScope.launch {
            try { val rows = repository.verses(plan.surahId, plan.firstAyah, plan.lastAyah)
                mutable.update { it.copy(verses = rows) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                mutable.update { it.copy(error = "تعذر قراءة الآيات المعتمدة؛ أعد فتح الاختبار للمحاولة") }
            }
        }
    }
    private fun write(action: suspend () -> Unit) {
        if (mutable.value.busy || !mutable.value.dataReady) return
        mutable.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try { action(); mutable.update { it.copy(saved = it.saved + 1) } }
            catch (e: Exception) {
                if (e is CancellationException) throw e
                mutable.update { it.copy(error = if (e is IllegalArgumentException) e.message else "تعذر الحفظ؛ بياناتك السابقة محفوظة") }
            } finally { mutable.update { it.copy(busy = false) } }
        }
    }
    fun save(id: String?, surah: Int, first: Int, last: Int) = write { repository.save(id,surah,first,last) }
    fun confirm(id: String) = write { repository.confirm(id) }
    fun archive(id: String) = write { repository.archive(id) }
    fun reschedule(id: String, dueDay: Long) = write { repository.reschedule(id,dueDay) }
    fun review(id: String, result: ReviewResult) = write { repository.review(id,result) }
}
