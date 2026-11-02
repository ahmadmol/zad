package com.example.feature.ramadan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.fasting.data.FastingRepository
import com.example.feature.fasting.domain.FastingRecord
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.model.*
import com.example.feature.quran.domain.model.ReadingPlan
import com.example.feature.quran.domain.repository.ReadingJourneyRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class RamadanState(val day: PrayerDay?=null, val location: PrayerLocationState=PrayerLocationState.Loading,
    val plan: ReadingPlan?=null, val pages: Set<Int> = emptySet(), val readingLoading: Boolean=true,
    val readingError: String?=null, val fasting: List<FastingRecord> = emptyList(), val fastingLoading: Boolean=true,
    val fastingError: String?=null, val offset: Int=0, val highlight: Boolean=false, val preferencesLoading: Boolean=true,
    val preferencesError: String?=null, val preferencesBusy: Boolean=false, val prayerBusy: Boolean=false, val prayerError: String?=null)

class RamadanViewModel(private val facade: PrayerTimesFacade, private val reading: ReadingJourneyRepository,
    private val fasting: FastingRepository, private val preferences: UserPreferences): ViewModel() {
    private val mutable=MutableStateFlow(RamadanState())
    val state=mutable.asStateFlow()
    private val dataJobs=mutableListOf<Job>()
    init {
        viewModelScope.launch {
            combine(facade.prayerDay,facade.locationState) { day,location -> day to location }
                .collect { (day,location) -> mutable.update { it.copy(day=day,location=location) } }
        }
        reloadData()
    }
    fun reloadData() {
        dataJobs.forEach { it.cancel() };dataJobs.clear()
        mutable.update { it.copy(readingLoading=true,fastingLoading=true,preferencesLoading=true,
            readingError=null,fastingError=null,preferencesError=null) }
        dataJobs+=viewModelScope.launch {
            combine(reading.plans,reading.completedPages) { plans,pages ->
                val plan=plans.firstOrNull { it.active };plan to pages[plan?.id].orEmpty()
            }.catch { e ->
                if(e is CancellationException) throw e
                mutable.update { it.copy(readingLoading=false,readingError="تعذر قراءة خطة الختمة. تقدمك السابق محفوظ.") }
            }.collect { (plan,pages) -> mutable.update { it.copy(plan=plan,pages=pages,readingLoading=false) } }
        }
        dataJobs+=viewModelScope.launch {
            fasting.records.catch { e ->
                if(e is CancellationException) throw e
                mutable.update { it.copy(fastingLoading=false,fastingError="تعذر قراءة سجل الصيام؛ لا يُعرض الصفر بدل بياناتك.") }
            }.collect { rows -> mutable.update { it.copy(fasting=rows,fastingLoading=false) } }
        }
        dataJobs+=viewModelScope.launch {
            combine(preferences.hijriDayOffset,preferences.ramadanHighlightEnabled) { offset,enabled -> offset to enabled }
                .catch { e ->
                    if(e is CancellationException) throw e
                    mutable.update { it.copy(preferencesLoading=false,preferencesError="تعذر قراءة إعداد العرض الموسمي والتقويم.") }
                }.collect { (offset,enabled) -> mutable.update { it.copy(offset=offset,highlight=enabled,preferencesLoading=false) } }
        }
    }
    fun refreshPrayer() {
        if(mutable.value.prayerBusy) return
        mutable.update { it.copy(prayerBusy=true,prayerError=null) }
        viewModelScope.launch {
            try { facade.refreshLocation() }
            catch(e: Exception) {
                if(e is CancellationException) throw e
                mutable.update { it.copy(prayerError="تعذر تحديث الموقع. يمكنك اختيار مدينة من شاشة المواقيت.") }
            } finally { mutable.update { it.copy(prayerBusy=false) } }
        }
    }
    fun highlight(enabled: Boolean) {
        if(mutable.value.preferencesBusy || mutable.value.preferencesLoading) return
        mutable.update { it.copy(preferencesBusy=true,preferencesError=null) }
        viewModelScope.launch {
            try { preferences.setRamadanHighlightEnabled(enabled) }
            catch(e: Exception) {
                if(e is CancellationException) throw e
                mutable.update { it.copy(preferencesError="تعذر حفظ تفضيل العرض الموسمي. بقي الإعداد السابق.") }
            } finally { mutable.update { it.copy(preferencesBusy=false) } }
        }
    }
}
