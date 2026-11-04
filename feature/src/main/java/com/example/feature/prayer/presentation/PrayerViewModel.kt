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
import kotlinx.coroutines.flow.catch
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
    private var observation: kotlinx.coroutines.Job? = null

    init {
        observePrayerState()
        perform {
            facade.refreshLocation()
            facade.reconcileSchedule(PrayerReconciliationReason.ApplicationStart)
        }
    }

    private fun observePrayerState() {
        observation?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }
        observation = viewModelScope.launch {
            val settingsFlow = settingsRepository?.observeSettings()
                ?: MutableStateFlow(PrayerCalculationSettings())

            combine(
                facade.prayerDay,
                facade.nextPrayer,
                facade.locationState,
                settingsFlow,
                facade.systemStatus
            ) { day, next, location, settings, system ->
                UiBundle(day, next, location, settings, system)
            }.catch { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, error = "تعذر قراءة إعدادات الصلاة؛ أعد المحاولة دون مسح البيانات") }
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
                        time = com.example.feature.prayer.presentation.PrayerTimeFormatter.format(
                            instant.epochMillis, java.time.ZoneId.of(day.timeZoneId), Locale("ar")
                        ),
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
                } ?: "—"

                dateFormat.timeZone = java.util.TimeZone.getTimeZone(
                    day?.timeZoneId ?: java.time.ZoneId.systemDefault().id
                )
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
                        madhhabName = madhhabLabel(settings.madhhab),
                        prePrayerReminderMinutes = settings.prePrayerNotificationMinutes,
                        adhanEnabled = settings.notificationsEnabled,
                        scheduleWarning = bundle.system.lastFailureSummary
                    )
                }
            }
        }

    }

    private fun perform(action: suspend () -> Unit) {
        if (_uiState.value.isUpdating) return
        _uiState.update { it.copy(isUpdating = true, error = null) }
        viewModelScope.launch {
            try { action() }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "تعذر تحديث الصلاة أو حفظ إعدادها؛ أعد المحاولة") }
            } finally { _uiState.update { it.copy(isUpdating = false) } }
        }
    }

    fun onAction(action: PrayerAction) {
        when (action) {
            PrayerAction.OnRefresh -> {
                if (_uiState.value.isUpdating) return
                observePrayerState()
                perform {
                    facade.refreshLocation()
                    facade.reconcileSchedule(PrayerReconciliationReason.ManualRetry)
                }
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
                perform {
                    val methodEnum = AdhanPrayerCalculator.parseMethod(action.methodStr)
                    settingsRepository?.updateCalculationMethod(methodEnum)
                    facade.reconcileSchedule(PrayerReconciliationReason.SettingsChanged)
                }
            }
            is PrayerAction.OnUpdateMadhhab -> perform {
                val madhhab = runCatching { PrayerMadhhab.valueOf(action.madhhab) }
                    .getOrDefault(PrayerMadhhab.SHAFI)
                settingsRepository?.updateMadhhab(madhhab)
                facade.reconcileSchedule(PrayerReconciliationReason.SettingsChanged)
            }
            is PrayerAction.OnUpdateLocationMode -> perform {
                settingsRepository?.updateUseAutoLocation(action.enabled)
                facade.refreshLocation()
                facade.reconcileSchedule(PrayerReconciliationReason.LocationChanged)
            }
            is PrayerAction.OnUpdateManualLocation -> perform {
                settingsRepository?.updateManualLocation(action.city, action.latitude, action.longitude)
                facade.refreshLocation()
                facade.reconcileSchedule(PrayerReconciliationReason.LocationChanged)
            }
            is PrayerAction.OnUpdateSound -> perform {
                settingsRepository?.updateNotificationSoundType(action.soundType)
                facade.reconcileSchedule(PrayerReconciliationReason.SettingsChanged)
            }
            is PrayerAction.OnUpdatePrePrayerMinutes -> {
                perform {
                    settingsRepository?.updatePrePrayerMinutes(action.minutes)
                    facade.reconcileSchedule(PrayerReconciliationReason.SettingsChanged)
                }
            }
            is PrayerAction.OnUpdateIqamahMinutes -> perform {
                settingsRepository?.updateIqamahMinutes(action.minutes)
                facade.reconcileSchedule(PrayerReconciliationReason.SettingsChanged)
            }
            is PrayerAction.OnToggleAdhan -> {
                perform {
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

    private fun madhhabLabel(madhhab: com.example.feature.prayer.domain.model.PrayerMadhhab): String = when (madhhab) {
        com.example.feature.prayer.domain.model.PrayerMadhhab.SHAFI -> "شافعي، مالكي، حنبلي"
        com.example.feature.prayer.domain.model.PrayerMadhhab.HANAFI -> "حنفي"
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
        val settings: PrayerCalculationSettings,
        val system: com.example.feature.prayer.domain.model.PrayerSystemStatus
    )
}

