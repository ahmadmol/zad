package com.example.feature.ihsanplus.integration.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.ihsanplus.integration.contract.IhsanPlusPrayerSource
import com.example.feature.ihsanplus.integration.model.IhsanPlusPrayerSourceSnapshot
import com.example.feature.prayer.domain.model.PrayerAlarmPermissionState
import com.example.feature.prayer.domain.model.PrayerLocationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ControlledPrayerAssistUiModel(
    val nextPrayerArabic: String?,
    val locationAvailable: Boolean,
    val locationLabel: String,
    val notificationsOk: Boolean,
    val alarmPermissionLabel: String,
    val lastReconcileSuccess: Boolean?,
    val scheduledAlarmCount: Int
)

sealed interface ControlledPrayerAssistUiState {
    data object Loading : ControlledPrayerAssistUiState
    data class Content(val model: ControlledPrayerAssistUiModel) : ControlledPrayerAssistUiState
    data class Error(val canRetry: Boolean = true) : ControlledPrayerAssistUiState
}

/**
 * Read-only Prayer Assist status from Part 1 facade via production adapter.
 * Does not schedule alarms or recalculate times.
 */
class ControlledPrayerAssistViewModel(
    private val prayerSource: IhsanPlusPrayerSource
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ControlledPrayerAssistUiState>(ControlledPrayerAssistUiState.Loading)
    val uiState: StateFlow<ControlledPrayerAssistUiState> = _uiState.asStateFlow()

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch {
            prayerSource.observeSnapshot()
                .catch { _uiState.value = ControlledPrayerAssistUiState.Error() }
                .collect { snapshot ->
                    _uiState.value = ControlledPrayerAssistUiState.Content(map(snapshot))
                }
        }
    }

    companion object {
        fun map(snapshot: IhsanPlusPrayerSourceSnapshot): ControlledPrayerAssistUiModel {
            val locationAvailable = snapshot.locationState is PrayerLocationState.Available
            val locationLabel = when (val loc = snapshot.locationState) {
                is PrayerLocationState.Available -> loc.location.displayName
                is PrayerLocationState.Unavailable -> "الموقع غير متاح"
                PrayerLocationState.Loading -> "جاري تحديد الموقع..."
            }
            val permission = snapshot.systemStatus.alarmPermission
            val notificationsOk = permission != PrayerAlarmPermissionState.NotificationsDenied
            val alarmLabel = when (permission) {
                PrayerAlarmPermissionState.GrantedExact -> "تنبيهات دقيقة متاحة"
                PrayerAlarmPermissionState.InexactOnly -> "تنبيهات تقريبية فقط"
                PrayerAlarmPermissionState.NotificationsDenied -> "إذن الإشعارات غير متاح"
                PrayerAlarmPermissionState.Unknown -> "حالة الإذن غير معروفة"
            }
            return ControlledPrayerAssistUiModel(
                nextPrayerArabic = snapshot.nextPrayer?.name?.arabic,
                locationAvailable = locationAvailable,
                locationLabel = locationLabel,
                notificationsOk = notificationsOk,
                alarmPermissionLabel = alarmLabel,
                lastReconcileSuccess = snapshot.systemStatus.lastReconciliationSuccess,
                scheduledAlarmCount = snapshot.systemStatus.scheduledAlarmCount
            )
        }
    }
}
