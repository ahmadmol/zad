package com.example.feature.fasting.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.fasting.data.FastingRepository
import com.example.feature.fasting.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate

data class FastingState(val records: List<FastingRecord> = emptyList(), val offset: Int = 0,
    val loading: Boolean = true, val busy: Boolean = false, val error: String? = null, val saved: Boolean = false,
    val dataReady: Boolean = false)

class FastingViewModel(private val repository: FastingRepository, private val preferences: UserPreferences): ViewModel() {
    private val mutable=MutableStateFlow(FastingState())
    val state=mutable.asStateFlow()
    private var observation: Job?=null
    init { reload() }
    fun reload() {
        observation?.cancel()
        mutable.update { it.copy(loading=true,error=null,dataReady=false) }
        observation=viewModelScope.launch {
            combine(repository.records,preferences.hijriDayOffset) { records,offset -> records to offset }
                .catch { e ->
                    if(e is CancellationException) throw e
                    mutable.update { it.copy(loading=false,dataReady=false,error="تعذر قراءة سجل الصيام أو تفضيل التقويم. بياناتك السابقة محفوظة.") }
                }.collect { (records,offset) -> mutable.update { it.copy(records=records,offset=offset,loading=false,dataReady=true) } }
        }
    }
    private fun write(action: suspend () -> Unit) {
        if(mutable.value.busy || !mutable.value.dataReady) return
        mutable.update { it.copy(busy=true,error=null,saved=false) }
        viewModelScope.launch {
            try { action();mutable.update { it.copy(saved=true) } }
            catch(e: Exception) {
                if(e is CancellationException) throw e
                mutable.update { it.copy(error=if(e is IllegalArgumentException) e.message else "تعذر حفظ التغيير. لم يُعلن نجاح العملية؛ أعد المحاولة.") }
            } finally { mutable.update { it.copy(busy=false) } }
        }
    }
    fun save(date: LocalDate,status: FastingStatus,typeLabel: String)=write { repository.save(date,status,typeLabel) }
    fun delete(date: LocalDate)=write { repository.delete(date) }
    fun clearFastingOnly()=write { repository.clearFastingOnly() }
    fun acknowledgeSaved() { mutable.update { it.copy(saved=false) } }
}
