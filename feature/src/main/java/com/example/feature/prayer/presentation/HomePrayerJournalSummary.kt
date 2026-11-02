package com.example.feature.prayer.presentation

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.feature.prayer.domain.repository.ManualPrayerLogRepository
import com.example.feature.prayer.domain.model.ManualPrayerDaySummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.koin.compose.koinInject
import java.time.LocalDate

private data class HomePrayerSummary(val day: ManualPrayerDaySummary?=null,val error: Boolean=false)

@Composable
fun HomePrayerJournalSummary(onOpen: () -> Unit) {
    val repository: ManualPrayerLogRepository = koinInject()
    val today by produceState(initialValue=LocalDate.now()) { while(true){value=LocalDate.now();delay(1000)} }
    val flow = remember(today,repository) {
        repository.observeDay(today).map { HomePrayerSummary(day=it) }.catch { e ->
            if(e is CancellationException) throw e
            emit(HomePrayerSummary(error=true))
        }
    }
    val state by flow.collectAsStateWithLifecycle(initialValue=HomePrayerSummary())
    IhsanContentCard {
        Text("سجل الصلاة اليوم",style=MaterialTheme.typography.titleMedium)
        when {
            state.error -> Text("تعذر قراءة السجل المحلي. افتح السجل لإعادة المحاولة.")
            state.day==null || state.day?.date!=today -> Text("جارٍ قراءة التسجيلات…")
            else -> Text("${state.day?.performedCount} أديتها بتسجيلك • ${state.day?.unloggedCount} غير مسجلة")
        }
        IhsanSecondaryButton(onClick=onOpen) { Text("فتح سجل الصلاة وملخص الشهر") }
    }
}
