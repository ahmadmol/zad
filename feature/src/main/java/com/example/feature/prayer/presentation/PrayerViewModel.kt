package com.example.feature.prayer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.prayer.PrayerTime
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.model.PrayerLocationSource
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerLocationUnavailableReason
import com.example.feature.prayer.domain.model.PrayerReconciliationReason
import com.example.feature.prayer.domain.model.PrayerAlarmPermissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerViewModel(
    private val facade: PrayerTimesFacade
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))

    init {
        viewModelScope.launch {
            combine(
                facade.prayerDay,
                facade.nextPrayer,
                facade.locationState,
                facade.systemStatus
            ) { day, next, location, status ->
                UiBundle(day, next, location, status)
            }.collect { bundle ->
                val day = bundle.day
                val next = bundle.next
                val location = bundle.location
                val status = bundle.status
                val times = day?.instants?.map { instant ->
                    val now = System.currentTimeMillis()
                    val activeWindowEnd = instant.epochMillis + 45 * 60 * 1000
                    val isActive = now >= instant.epochMillis && now < activeWindowEnd
                    PrayerTime(
                        nameAr = instant.name.arabic,
                        nameEn = instant.name.english,
                        time = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(Date(instant.epochMillis)),
                        timestamp = instant.epochMillis,
                        isPast = now > instant.epochMillis && !isActive,
                        isActive = isActive
                    )
                }.orEmpty()

                val countdown = next?.let { n ->
                    val diff = n.remainingMillis
                    val h = (diff / (1000 * 60 * 60)) % 24
                    val m = (diff / (1000 * 60)) % 60
                    val s = (diff / 1000) % 60
                    String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
                } ?: "00:00:00"

                _uiState.update {
                    it.copy(
                        prayerTimes = times,
                        nextPrayerName = next?.name?.arabic.orEmpty(),
                        nextPrayerCountdown = countdown,
                        locationName = locationLabel(location),
                        locationSourceLabel = locationSourceLabel(location),
                        currentDate = dateFormat.format(Date()),
                        hijriDate = HijriDateFormatter.nowFormatted(),
                        isLoading = location is PrayerLocationState.Loading,
                        locationUnavailable = location is PrayerLocationState.Unavailable,
                        locationUnavailableMessage = (location as? PrayerLocationState.Unavailable)
                            ?.let { unavailableMessage(it.reason) },
                        systemStatusExpanded = it.systemStatusExpanded,
                        notificationPermissionLabel = permissionLabel(status.alarmPermission),
                        lastScheduleSummary = status.lastFailureSummary
                            ?: status.lastReconciliationSuccess?.let { ok ->
                                if (ok) "تمت جدولة ${status.scheduledAlarmCount} تنبيهاً"
                                else "تعذر إكمال الجدولة"
                            },
                        canRetryLocation = location is PrayerLocationState.Unavailable ||
                            location is PrayerLocationState.Available
                    )
                }
            }
        }

        viewModelScope.launch {
            facade.refreshLocation()
            facade.reconcileSchedule(PrayerReconciliationReason.ApplicationStart)
        }
    }

    fun onAction(action: PrayerAction) {
        when (action) {
            PrayerAction.OnRefresh -> viewModelScope.launch {
                facade.refreshLocation()
                facade.reconcileSchedule(PrayerReconciliationReason.ManualRetry)
            }
            is PrayerAction.OnToggleNotification -> {}
            PrayerAction.OnToggleSystemStatus -> {
                _uiState.update { it.copy(systemStatusExpanded = !it.systemStatusExpanded) }
            }
            PrayerAction.OnRetrySchedule -> viewModelScope.launch {
                facade.reconcileSchedule(PrayerReconciliationReason.ManualRetry)
            }
        }
    }

    private fun locationLabel(state: PrayerLocationState): String = when (state) {
        is PrayerLocationState.Available -> state.location.displayName
        is PrayerLocationState.Unavailable -> "الموقع غير متاح"
        PrayerLocationState.Loading -> "جاري تحديد الموقع..."
    }

    private fun locationSourceLabel(state: PrayerLocationState): String? = when (state) {
        is PrayerLocationState.Available -> when (state.source) {
            PrayerLocationSource.Device -> "المصدر: الجهاز"
            PrayerLocationSource.Saved -> "المصدر: موقع محفوظ"
            PrayerLocationSource.Manual -> "المصدر: يدوي"
        }
        else -> null
    }

    private fun unavailableMessage(reason: PrayerLocationUnavailableReason): String = when (reason) {
        PrayerLocationUnavailableReason.PERMISSION_DENIED -> "يلزم إذن الموقع لحساب المواقيت"
        PrayerLocationUnavailableReason.PROVIDER_DISABLED -> "خدمة الموقع متوقفة"
        PrayerLocationUnavailableReason.TIMEOUT -> "تعذر الحصول على الموقع، حاول مرة أخرى"
        PrayerLocationUnavailableReason.NO_SAVED_LOCATION -> "لا يوجد موقع محفوظ. حدّث الموقع أو اختر مدينة يدوياً"
        PrayerLocationUnavailableReason.UNKNOWN -> "تعذر تحديد الموقع"
    }

    private fun permissionLabel(state: PrayerAlarmPermissionState): String = when (state) {
        PrayerAlarmPermissionState.GrantedExact -> "تنبيهات دقيقة متاحة"
        PrayerAlarmPermissionState.InexactOnly -> "تنبيهات غير دقيقة فقط"
        PrayerAlarmPermissionState.NotificationsDenied -> "إذن الإشعارات غير ممنوح"
        PrayerAlarmPermissionState.Unknown -> "حالة التنبيهات غير معروفة"
    }

    private data class UiBundle(
        val day: com.example.feature.prayer.domain.model.PrayerDay?,
        val next: com.example.feature.prayer.domain.model.NextPrayer?,
        val location: PrayerLocationState,
        val status: com.example.feature.prayer.domain.model.PrayerSystemStatus
    )
}
