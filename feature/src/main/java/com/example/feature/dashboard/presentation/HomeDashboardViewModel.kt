package com.example.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.dashboard.domain.model.HomeAsmaSummary
import com.example.feature.dashboard.domain.model.HomeCharitySummary
import com.example.feature.dashboard.domain.model.HomeDailyActivitySummary
import com.example.feature.dashboard.domain.model.HomeDhikrSummary
import com.example.feature.dashboard.domain.model.HomePrayerSummary
import com.example.feature.dashboard.domain.model.HomeProfileSummary
import com.example.feature.dashboard.domain.model.HomeQuranSummary
import com.example.feature.dashboard.domain.model.HomeSectionState
import com.example.feature.dashboard.domain.repository.DailyActivityRepository
import com.example.feature.dashboard.domain.usecase.ObserveHomeAsmaSummaryUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomeCharitySummaryUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomeDailyActivitiesUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomeDhikrSummaryUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomePrayerSummaryUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomeProfileSummaryUseCase
import com.example.feature.dashboard.domain.usecase.ObserveHomeQuranSummaryUseCase
import com.example.feature.dashboard.domain.usecase.RefreshHomeDashboardUseCase
import com.example.feature.dashboard.domain.usecase.currentHijriDate
import com.example.feature.prayer.domain.model.PrayerCalculationMethod
import com.example.feature.prayer.domain.model.PrayerMadhhab
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.example.feature.prayer.domain.usecase.UpdatePrayerSettingsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

class HomeDashboardViewModel(
    observeHomeProfile: ObserveHomeProfileSummaryUseCase,
    observeHomePrayer: ObserveHomePrayerSummaryUseCase,
    observeHomeQuran: ObserveHomeQuranSummaryUseCase,
    observeDailyActivities: ObserveHomeDailyActivitiesUseCase,
    observeHomeDhikr: ObserveHomeDhikrSummaryUseCase,
    observeHomeAsma: ObserveHomeAsmaSummaryUseCase,
    observeHomeCharity: ObserveHomeCharitySummaryUseCase,
    private val refreshHome: RefreshHomeDashboardUseCase,
    private val updatePrayerSettings: UpdatePrayerSettingsUseCase,
    private val prayerLocationRepository: PrayerLocationRepository,
    private val dailyActivityRepository: DailyActivityRepository
) : ViewModel() {

    private val ephemeral = MutableStateFlow(EphemeralUi())
    private var clockJob: Job? = null

    private val coreSections = combine(
        observeHomeProfile(),
        observeHomePrayer(),
        observeHomeQuran(),
        observeDailyActivities(),
        observeHomeDhikr()
    ) { profile, prayer, quran, daily, dhikr ->
        CoreSections(profile, prayer, quran, daily, dhikr)
    }

    private val sections = combine(
        coreSections,
        observeHomeAsma(),
        observeHomeCharity()
    ) { core, asma, charity ->
        SectionBundle(
            profile = core.profile,
            prayer = core.prayer,
            quran = core.quran,
            dailyActivities = core.daily,
            dhikr = core.dhikr,
            asma = asma,
            charity = charity
        )
    }

    private val combined = combine(sections, ephemeral) { bundle, epi ->
        HomeDashboardUiState(
            profile = bundle.profile,
            prayer = bundle.prayer,
            quran = bundle.quran,
            dailyActivities = bundle.dailyActivities,
            dhikr = bundle.dhikr,
            asma = bundle.asma,
            charity = bundle.charity,
            currentTime = epi.currentTime,
            hijriDate = epi.hijriDate,
            isRefreshing = epi.isRefreshing,
            refreshErrorMessage = epi.refreshErrorMessage,
            selectedPrayerIndex = epi.selectedPrayerIndex,
            isPrayerSettingsVisible = epi.isPrayerSettingsVisible,
            isCitySelectionVisible = epi.isCitySelectionVisible
        )
    }

    val uiState: StateFlow<HomeDashboardUiState> = combined.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeDashboardUiState()
    )

    init {
        startClock()
        viewModelScope.launch { refreshHome().onFailure { setRefreshError(it) } }
    }

    private fun startClock() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            var lastDateKey: String? = null
            while (true) {
                val now = Date()
                val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)
                ephemeral.update {
                    it.copy(currentTime = ArabicClockFormatter.format(now, includeSeconds = true))
                        .let { next ->
                            if (DashboardDatePolicy.shouldRefreshHijriDate(lastDateKey, dateKey)) {
                                lastDateKey = dateKey
                                next.copy(hijriDate = currentHijriDate())
                            } else next
                        }
                }
                delay(1.seconds)
            }
        }
    }

    fun onAction(action: HomeDashboardAction) {
        when (action) {
            HomeDashboardAction.OnRefresh -> viewModelScope.launch {
                ephemeral.update { it.copy(isRefreshing = true, refreshErrorMessage = null) }
                refreshHome().onFailure { setRefreshError(it) }
                ephemeral.update { it.copy(isRefreshing = false) }
            }
            HomeDashboardAction.OnRetryPrayer -> viewModelScope.launch {
                ephemeral.update { it.copy(refreshErrorMessage = null) }
                refreshHome().onFailure { setRefreshError(it) }
            }
            HomeDashboardAction.OnProfileClick -> Unit
            is HomeDashboardAction.OnPrayerClick -> {
                ephemeral.update { it.copy(selectedPrayerIndex = action.index) }
            }
            HomeDashboardAction.OnDismissPrayerDetails -> {
                ephemeral.update { it.copy(selectedPrayerIndex = null) }
            }
            HomeDashboardAction.OnShowPrayerSettings -> {
                ephemeral.update { it.copy(isPrayerSettingsVisible = true) }
            }
            HomeDashboardAction.OnDismissPrayerSettings -> {
                ephemeral.update { it.copy(isPrayerSettingsVisible = false) }
            }
            HomeDashboardAction.OnShowCitySelection -> {
                ephemeral.update { it.copy(isCitySelectionVisible = true) }
            }
            HomeDashboardAction.OnDismissCitySelection -> {
                ephemeral.update { it.copy(isCitySelectionVisible = false) }
            }
            is HomeDashboardAction.OnUpdateCalculationMethod -> viewModelScope.launch {
                updatePrayerSettings.updateMethod(parseMethod(action.method))
            }
            is HomeDashboardAction.OnUpdateMadhab -> viewModelScope.launch {
                updatePrayerSettings.updateMadhhab(parseMadhhab(action.madhab))
            }
            is HomeDashboardAction.OnUpdateLocationMode -> viewModelScope.launch {
                updatePrayerSettings.updateUseAutoLocation(action.isAuto)
            }
            is HomeDashboardAction.OnSelectCity -> viewModelScope.launch {
                prayerLocationRepository.saveManualLocation(action.lat, action.lng, action.name)
            }
            is HomeDashboardAction.OnUpdatePrePrayerNotification -> viewModelScope.launch {
                updatePrayerSettings.updatePrePrayer(action.minutes)
            }
            is HomeDashboardAction.OnUpdateIqamahNotification -> viewModelScope.launch {
                updatePrayerSettings.updateIqamah(action.minutes)
            }
            is HomeDashboardAction.OnUpdateNotificationSound -> viewModelScope.launch {
                updatePrayerSettings.updateNotificationSound(action.type)
            }
            is HomeDashboardAction.OnDailyActivityClick -> viewModelScope.launch {
                dailyActivityRepository.increment(action.activityId)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        clockJob?.cancel()
    }

    private fun setRefreshError(error: Throwable) {
        ephemeral.update {
            it.copy(
                refreshErrorMessage = error.message ?: "تعذر تحديث الموقع ومواقيت الصلاة."
            )
        }
    }

    private fun parseMethod(raw: String): PrayerCalculationMethod =
        runCatching { PrayerCalculationMethod.valueOf(raw) }
            .getOrDefault(PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE)

    private fun parseMadhhab(raw: String): PrayerMadhhab =
        runCatching { PrayerMadhhab.valueOf(raw) }
            .getOrDefault(PrayerMadhhab.SHAFI)

    private data class EphemeralUi(
        val currentTime: String = "",
        val hijriDate: String = "",
        val isRefreshing: Boolean = false,
        val refreshErrorMessage: String? = null,
        val selectedPrayerIndex: Int? = null,
        val isPrayerSettingsVisible: Boolean = false,
        val isCitySelectionVisible: Boolean = false
    )

    private data class CoreSections(
        val profile: HomeSectionState<HomeProfileSummary>,
        val prayer: HomeSectionState<HomePrayerSummary>,
        val quran: HomeSectionState<HomeQuranSummary>,
        val daily: HomeSectionState<HomeDailyActivitySummary>,
        val dhikr: HomeSectionState<HomeDhikrSummary>
    )

    private data class SectionBundle(
        val profile: HomeSectionState<HomeProfileSummary>,
        val prayer: HomeSectionState<HomePrayerSummary>,
        val quran: HomeSectionState<HomeQuranSummary>,
        val dailyActivities: HomeSectionState<HomeDailyActivitySummary>,
        val dhikr: HomeSectionState<HomeDhikrSummary>,
        val asma: HomeSectionState<HomeAsmaSummary>,
        val charity: HomeSectionState<HomeCharitySummary>
    )
}
