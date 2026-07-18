package com.example.feature.dashboard.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.designsystem.component.DailyActivityItemData
import com.example.feature.R
import com.example.feature.asma.domain.usecase.GetAsmaUseCase
import com.example.feature.asma.domain.util.AsmaTodayResolver
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.azkar.domain.usecase.GetAzkarUseCase
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.ehsan.domain.usecase.GetDonationsUseCase
import com.example.feature.prayer.PrayerTime
import com.example.feature.prayer.domain.calculator.NextPrayerSelector
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerReconciliationReason
import com.example.feature.quran.domain.repository.QuranRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

class HomeDashboardViewModel(
    private val getAzkarUseCase: GetAzkarUseCase,
    private val getAsmaUseCase: GetAsmaUseCase,
    private val getDonationsUseCase: GetDonationsUseCase,
    private val userPreferences: UserPreferences,
    private val settingsManager: SettingsManager,
    private val prayerFacade: PrayerTimesFacade,
    private val quranRepository: QuranRepository,
    context: Context
) : ViewModel() {

    private val applicationContext = context.applicationContext
    private val _uiState = MutableStateFlow(HomeDashboardUiState())
    val uiState: StateFlow<HomeDashboardUiState> = _uiState.asStateFlow()

    private var dataObserveJob: Job? = null
    private var dailyActivityJob: Job? = null
    private var countdownJob: Job? = null
    private var prayerObserveJob: Job? = null
    private var lastReadJob: Job? = null
    private val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale("ar"))
    private val activityDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val prayerTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    private val dailyActivityTemplates = listOf(
        DailyActivityTemplate(
            id = DailyActivityIds.QURAN_READING,
            title = "قراءة قرآن",
            targetCount = DailyActivityIds.targetFor(DailyActivityIds.QURAN_READING),
            unit = "مرة",
            route = "quran_list"
        ),
        DailyActivityTemplate(
            id = DailyActivityIds.MORNING_AZKAR,
            title = "أذكار الصباح",
            targetCount = DailyActivityIds.targetFor(DailyActivityIds.MORNING_AZKAR),
            unit = "مرة",
            route = "azkar_screen"
        ),
        DailyActivityTemplate(
            id = DailyActivityIds.EVENING_AZKAR,
            title = "أذكار المساء",
            targetCount = DailyActivityIds.targetFor(DailyActivityIds.EVENING_AZKAR),
            unit = "مرة",
            route = "azkar_screen"
        ),
        DailyActivityTemplate(
            id = DailyActivityIds.TASBEEH,
            title = "تسبيح",
            targetCount = DailyActivityIds.targetFor(DailyActivityIds.TASBEEH),
            unit = "حبة",
            route = "tasbih_screen"
        ),
        DailyActivityTemplate(
            id = DailyActivityIds.DAILY_DUA,
            title = "دعاء اليوم",
            targetCount = DailyActivityIds.targetFor(DailyActivityIds.DAILY_DUA),
            unit = "مرة",
            route = "dua_screen"
        ),
        DailyActivityTemplate(
            id = DailyActivityIds.DAILY_NAME,
            title = "اسم اليوم",
            targetCount = DailyActivityIds.targetFor(DailyActivityIds.DAILY_NAME),
            unit = "مرة",
            route = "asma_screen"
        )
    )

    init {
        observePrayerFacade()
        observeData()
        observeDailyActivities()
        observeLastRead()
        startClockTicker()
        viewModelScope.launch {
            prayerFacade.refreshLocation()
            prayerFacade.reconcileSchedule(PrayerReconciliationReason.ApplicationStart)
        }
    }

    private fun observePrayerFacade() {
        prayerObserveJob?.cancel()
        prayerObserveJob = viewModelScope.launch {
            combine(
                prayerFacade.nextPrayer,
                prayerFacade.prayerDay,
                prayerFacade.locationState
            ) { next, day, location ->
                Triple(next, day, location)
            }.collectLatest { (next, day, location) ->
                val now = System.currentTimeMillis()
                val prayers = day?.instants?.map { instant ->
                    val activeWindowEnd = instant.epochMillis + 45 * 60 * 1000
                    val isActive = now >= instant.epochMillis && now < activeWindowEnd
                    PrayerTime(
                        nameAr = instant.name.arabic,
                        nameEn = instant.name.english,
                        time = prayerTimeFormat.format(Date(instant.epochMillis)),
                        timestamp = instant.epochMillis,
                        isPast = now > instant.epochMillis && !isActive,
                        isActive = isActive
                    )
                }.orEmpty()

                val timeLeft = next?.let { n ->
                    val diff = n.remainingMillis
                    val hours = diff / (1000 * 60 * 60)
                    val minutes = (diff / (1000 * 60)) % 60
                    val seconds = (diff / 1000) % 60
                    String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                }.orEmpty()

                val progress = next?.let { NextPrayerSelector.progressFraction(it, now) } ?: 0f
                val locationLabel = when (location) {
                    is PrayerLocationState.Available -> location.location.displayName
                    is PrayerLocationState.Unavailable -> "الموقع غير متاح"
                    PrayerLocationState.Loading -> "جاري تحديد الموقع..."
                }

                _uiState.update { state ->
                    state.copy(
                        data = state.data.copy(
                            nextPrayerName = next?.name?.arabic.orEmpty(),
                            nextPrayerTimeLeft = timeLeft,
                            prayerProgress = progress,
                            allPrayers = prayers,
                            location = locationLabel
                        )
                    )
                }
            }
        }
    }

    private fun startClockTicker() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val currentTimeStr = timeFormat.format(Date())
                _uiState.update { state ->
                    state.copy(data = state.data.copy(currentTime = currentTimeStr))
                }
                delay(1.seconds)
            }
        }
    }

    private fun todayDate(): String = activityDateFormatter.format(Date())

    private fun observeLastRead() {
        lastReadJob?.cancel()
        lastReadJob = viewModelScope.launch {
            combine(
                userPreferences.lastReadSurahId,
                userPreferences.lastReadAyahNumber
            ) { surahId, ayahNumber ->
                if (surahId != null && ayahNumber != null) surahId to ayahNumber else null
            }.collectLatest { pair ->
                if (pair == null) {
                    _uiState.update { state ->
                        state.copy(
                            data = state.data.copy(
                                lastReadSurahId = null,
                                lastReadSurahName = null,
                                lastReadAyahNumber = null
                            )
                        )
                    }
                    return@collectLatest
                }

                val (surahId, ayahNumber) = pair
                val surah = runCatching { quranRepository.getSurahById(surahId) }.getOrNull()
                _uiState.update { state ->
                    state.copy(
                        data = state.data.copy(
                            lastReadSurahId = surahId,
                            lastReadSurahName = surah?.name ?: "سورة $surahId",
                            lastReadAyahNumber = ayahNumber
                        )
                    )
                }
            }
        }
    }

    private fun observeDailyActivities() {
        dailyActivityJob?.cancel()
        dailyActivityJob = viewModelScope.launch {
            combine(
                userPreferences.dailyActivityDate,
                userPreferences.dailyActivityCounts
            ) { date, counts -> date to counts }
                .collectLatest { (date, counts) ->
                    val today = todayDate()
                    if (date != today) {
                        userPreferences.resetDailyActivityCounts(today)
                        return@collectLatest
                    }

                    _uiState.update { state ->
                        state.copy(
                            data = state.data.copy(
                                dailyActivities = createDailyActivities(counts)
                            )
                        )
                    }
                }
        }
    }

    private fun createDailyActivities(counts: Map<String, Int>): List<DailyActivityItemData> {
        return dailyActivityTemplates.map { template ->
            val current = counts[template.id] ?: 0
            DailyActivityItemData(
                id = template.id,
                title = template.title,
                currentCount = current,
                targetCount = template.targetCount,
                unit = template.unit,
                isCompleted = current >= template.targetCount,
                route = template.route
            )
        }
    }

    private suspend fun incrementDailyActivityCount(id: String) {
        userPreferences.incrementDailyActivityCount(id)
    }

    private data class DailyActivityTemplate(
        val id: String,
        val title: String,
        val targetCount: Int,
        val unit: String,
        val route: String
    )

    private fun observeData() {
        dataObserveJob?.cancel()
        dataObserveJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                getAzkarUseCase(),
                getAsmaUseCase(),
                getDonationsUseCase(),
                userPreferences.userName
            ) { azkar, asma, donations, userName ->
                val dailyZikr = azkar.firstOrNull { it.category == "أذكار الصباح" } ?: azkar.firstOrNull()
                val totalProgress = if (azkar.isNotEmpty()) {
                    azkar.map {
                        if (it.targetCount > 0) it.currentCount.toFloat() / it.targetCount else 0f
                    }.average().toFloat()
                } else 0f

                val dailyAsma = AsmaTodayResolver.selectDailyName(asma)

                val offers = donations.count { it.type == "OFFER" }
                val requests = donations.count { it.type == "REQUEST" }

                _uiState.update { state ->
                    state.copy(
                        data = state.data.copy(
                            userName = userName,
                            hijriDate = HijriDateFormatter.nowFormatted(),
                            spotlightAllahName = dailyAsma?.name
                                ?: applicationContext.getString(R.string.default_allah_name),
                            spotlightTransliteration = dailyAsma?.transliteration
                                ?: applicationContext.getString(R.string.default_allah_transliteration),
                            spotlightMeaning = dailyAsma?.meaning
                                ?: applicationContext.getString(R.string.default_allah_meaning),
                            dailyZikrTitle = dailyZikr?.text
                                ?: applicationContext.getString(R.string.default_zikr_title),
                            dailyZikrProgress = totalProgress,
                            dailyZikrPercentage = "${(totalProgress * 100).toInt()}٪",
                            communityOffersCount = offers,
                            communityRequestsCount = requests,
                            activeVolunteersCount = 12,
                            dailyDuas = azkar.take(5)
                        ),
                        isLoading = false
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect()
        }
    }

    fun onAction(action: HomeDashboardAction) {
        when (action) {
            HomeDashboardAction.OnRefresh -> {
                observeData()
                viewModelScope.launch {
                    prayerFacade.refreshLocation()
                    prayerFacade.reconcileSchedule(PrayerReconciliationReason.ManualRetry)
                }
            }
            HomeDashboardAction.OnProfileClick -> { /* Handle profile navigation via event */ }
            is HomeDashboardAction.OnPrayerClick -> {
                _uiState.update { it.copy(data = it.data.copy(selectedPrayerIndex = action.index)) }
            }
            HomeDashboardAction.OnDismissPrayerDetails -> {
                _uiState.update { it.copy(data = it.data.copy(selectedPrayerIndex = null)) }
            }
            HomeDashboardAction.OnShowPrayerSettings -> {
                _uiState.update { it.copy(data = it.data.copy(isPrayerSettingsVisible = true)) }
            }
            HomeDashboardAction.OnDismissPrayerSettings -> {
                _uiState.update { it.copy(data = it.data.copy(isPrayerSettingsVisible = false)) }
            }
            HomeDashboardAction.OnShowCitySelection -> {
                _uiState.update { it.copy(data = it.data.copy(isCitySelectionVisible = true)) }
            }
            HomeDashboardAction.OnDismissCitySelection -> {
                _uiState.update { it.copy(data = it.data.copy(isCitySelectionVisible = false)) }
            }
            is HomeDashboardAction.OnUpdateCalculationMethod -> {
                viewModelScope.launch { settingsManager.setCalculationMethod(action.method) }
            }
            is HomeDashboardAction.OnUpdateMadhab -> {
                viewModelScope.launch { settingsManager.setMadhab(action.madhab) }
            }
            is HomeDashboardAction.OnUpdateLocationMode -> {
                viewModelScope.launch { settingsManager.setUseAutoLocation(action.isAuto) }
            }
            is HomeDashboardAction.OnSelectCity -> {
                viewModelScope.launch {
                    settingsManager.setManualLocation(action.name, action.lat, action.lng)
                }
            }
            is HomeDashboardAction.OnUpdatePrePrayerNotification -> {
                viewModelScope.launch {
                    settingsManager.setPrePrayerNotificationMinutes(action.minutes)
                }
            }
            is HomeDashboardAction.OnUpdateIqamahNotification -> {
                viewModelScope.launch {
                    settingsManager.setIqamahNotificationMinutes(action.minutes)
                }
            }
            is HomeDashboardAction.OnUpdateNotificationSound -> {
                viewModelScope.launch {
                    settingsManager.setNotificationSoundType(action.type)
                }
            }
            is HomeDashboardAction.OnDailyActivityClick -> {
                viewModelScope.launch { incrementDailyActivityCount(action.activityId) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dataObserveJob?.cancel()
        dailyActivityJob?.cancel()
        lastReadJob?.cancel()
        countdownJob?.cancel()
        prayerObserveJob?.cancel()
    }
}
