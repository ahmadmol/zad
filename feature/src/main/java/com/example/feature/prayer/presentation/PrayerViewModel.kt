package com.example.feature.prayer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.prayer.PrayerTime
import com.example.feature.prayer.data.calculator.AdhanPrayerCalculator
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.model.PrayerCalculationMethod
import com.example.feature.prayer.domain.model.PrayerCalculationSettings
import com.example.feature.prayer.domain.model.PrayerLocationSource
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerLocationUnavailableReason
import com.example.feature.prayer.domain.model.PrayerMadhhab
import com.example.feature.prayer.domain.model.PrayerReconciliationReason
import com.example.feature.prayer.domain.repository.PrayerSettingsRepository
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
    private val facade: PrayerTimesFacade,
    private val settingsRepository: PrayerSettingsRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))

    init {
        viewModelScope.launch {
            val settingsFlow = settingsRepository?.observeSettings()
                ?: MutableStateFlow(PrayerCalculationSettings())

            combine(
                facade.prayerDay,
                facade.nextPrayer,
                facade.locationState,
                settingsFlow
            ) { day, next, location, settings ->
                UiBundle(day, next, location, settings)
            }.collect { bundle ->
                val day = bundle.day
                val next = bundle.next
                val location = bundle.location
                val settings = bundle.settings

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
                        calculationMethodName = methodLabel(settings.method),
                        prePrayerReminderMinutes = settings.prePrayerNotificationMinutes,
                        adhanEnabled = settings.notificationsEnabled
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
            is PrayerAction.OnSelectPrayer -> {
                _uiState.update { it.copy(selectedPrayer = action.prayer) }
            }
            is PrayerAction.OnToggleSettingsSheet -> {
                _uiState.update { it.copy(showSettingsBottomSheet = action.show) }
            }
            is PrayerAction.OnToggleCalendarSheet -> {
                _uiState.update { it.copy(showCalendarBottomSheet = action.show) }
            }
            is PrayerAction.OnUpdateCalculationMethod -> {
                viewModelScope.launch {
                    val methodEnum = AdhanPrayerCalculator.parseMethod(action.methodStr)
                    settingsRepository?.updateCalculationMethod(methodEnum)
                    facade.reconcileSchedule(PrayerReconciliationReason.SettingsChanged)
                }
            }
            is PrayerAction.OnUpdateMadhhab -> viewModelScope.launch {
                val madhhab = runCatching { PrayerMadhhab.valueOf(action.madhhab) }
                    .getOrDefault(PrayerMadhhab.SHAFI)
                settingsRepository?.updateMadhhab(madhhab)
                facade.reconcileSchedule(PrayerReconciliationReason.SettingsChanged)
            }
            is PrayerAction.OnUpdateLocationMode -> viewModelScope.launch {
                settingsRepository?.updateUseAutoLocation(action.enabled)
                facade.refreshLocation()
                facade.reconcileSchedule(PrayerReconciliationReason.LocationChanged)
            }
            is PrayerAction.OnUpdateManualLocation -> viewModelScope.launch {
                settingsRepository?.updateManualLocation(action.city, action.latitude, action.longitude)
                facade.refreshLocation()
                facade.reconcileSchedule(PrayerReconciliationReason.LocationChanged)
            }
            is PrayerAction.OnUpdateSound -> viewModelScope.launch {
                settingsRepository?.updateNotificationSoundType(action.soundType)
                facade.reconcileSchedule(PrayerReconciliationReason.SettingsChanged)
            }
            is PrayerAction.OnUpdatePrePrayerMinutes -> {
                viewModelScope.launch {
                    settingsRepository?.updatePrePrayerMinutes(action.minutes)
                    facade.reconcileSchedule(PrayerReconciliationReason.SettingsChanged)
                }
            }
            is PrayerAction.OnToggleAdhan -> {
                viewModelScope.launch {
                    settingsRepository?.updateNotificationsEnabled(action.enabled)
                    facade.reconcileSchedule(PrayerReconciliationReason.SettingsChanged)
                }
            }
        }
    }

    private fun methodLabel(method: PrayerCalculationMethod): String = when (method) {
        PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE -> "رابطة العالم الإسلامي"
        PrayerCalculationMethod.EGYPTIAN -> "الهيئة المصرية العامة للمساحة"
        PrayerCalculationMethod.KARACHI -> "جامعة العلوم الإسلامية بكراتشي"
        PrayerCalculationMethod.UMM_AL_QURA -> "أم القرى (مكة)"
        PrayerCalculationMethod.DUBAI -> "دبي"
        PrayerCalculationMethod.MOON_SIGHTING_COMMITTEE -> "لجنة رؤية الهلال"
        PrayerCalculationMethod.NORTH_AMERICA -> "ISNA (أمريكا الشمالية)"
        PrayerCalculationMethod.KUWAIT -> "الكويت"
        PrayerCalculationMethod.QATAR -> "قطر"
        PrayerCalculationMethod.SINGAPORE -> "سنغافورة"
        PrayerCalculationMethod.OTHER -> "طريقة أخرى"
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

    private data class UiBundle(
        val day: com.example.feature.prayer.domain.model.PrayerDay?,
        val next: com.example.feature.prayer.domain.model.NextPrayer?,
        val location: PrayerLocationState,
        val settings: PrayerCalculationSettings
    )
}

