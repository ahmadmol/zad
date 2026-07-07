package com.example.feature.dashboard.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Madhab
import com.example.feature.R
import com.example.feature.asma.domain.usecase.GetAsmaUseCase
import com.example.feature.azkar.domain.usecase.GetAzkarUseCase
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.ehsan.domain.usecase.GetDonationsUseCase
import com.example.feature.prayer.util.PrayerCalculator
import com.example.feature.prayer.PrayerTime
import com.example.feature.prayer.util.PrayerNotificationScheduler
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.seconds

class HomeDashboardViewModel(
    private val getAzkarUseCase: GetAzkarUseCase,
    private val getAsmaUseCase: GetAsmaUseCase,
    private val getDonationsUseCase: GetDonationsUseCase,
    private val userPreferences: UserPreferences,
    private val settingsManager: SettingsManager,
    private val scheduler: PrayerNotificationScheduler,
    context: Context
) : ViewModel() {

    private val applicationContext = context.applicationContext
    private val _uiState = MutableStateFlow(HomeDashboardUiState())
    val uiState: StateFlow<HomeDashboardUiState> = _uiState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
    private var nextPrayer: PrayerTime? = null
    private var lastPrayerTimestamp: Long = 0

    private var currentMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE
    private var currentMadhab = Madhab.SHAFI
    private var useAutoLocation = true
    private var manualLat = 36.2021
    private var manualLng = 37.1343
    private var prePrayerMins = 0
    private var iqamahMins = 0

    private var dataObserveJob: Job? = null
    private var countdownJob: Job? = null
    private val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale("ar"))

    init {
        observeSettings()
        observeData()
        startPrayerCountdown()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsManager.calculationMethodFlow,
                settingsManager.madhabFlow,
                settingsManager.useAutoLocationFlow,
                settingsManager.manualLocationLatFlow,
                settingsManager.manualLocationLngFlow,
                settingsManager.prePrayerNotificationMinutesFlow,
                settingsManager.iqamahNotificationMinutesFlow
            ) { args ->
                currentMethod = PrayerCalculator.getMethodFromString(args[0] as String)
                currentMadhab = PrayerCalculator.getMadhabFromString(args[1] as String)
                useAutoLocation = args[2] as Boolean
                manualLat = args[3] as Double
                manualLng = args[4] as Double
                prePrayerMins = args[5] as Int
                iqamahMins = args[6] as Int
            }.collectLatest {
                refreshLocationAndPrayers()
            }
        }
    }

    private fun refreshLocationAndPrayers() {
        viewModelScope.launch {
            try {
                updateLocationAndPrayerData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun updateLocationAndPrayerData() {
        val lat: Double
        val lng: Double
        
        if (useAutoLocation) {
            val location = try {
                fusedLocationClient.lastLocation.await() ?: 
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
            } catch (e: Exception) { null }
            
            lat = location?.latitude ?: manualLat
            lng = location?.longitude ?: manualLng
            
            if (location != null) {
                updateLocationName(lat, lng)
            }
        } else {
            lat = manualLat
            lng = manualLng
            updateLocationName(lat, lng)
        }
        
        val today = Date()
        val prayers = PrayerCalculator.calculate(lat, lng, today, method = currentMethod, madhab = currentMadhab)
        
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
        val yesterdayPrayers = PrayerCalculator.calculate(lat, lng, yesterday, method = currentMethod, madhab = currentMadhab)

        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
        val tomorrowPrayers = PrayerCalculator.calculate(lat, lng, tomorrow, method = currentMethod, madhab = currentMadhab)
        
        // Schedule Notifications
        scheduler.schedulePrayerNotifications(prayers + tomorrowPrayers, prePrayerMins, iqamahMins)
        
        val now = System.currentTimeMillis()
        val next = prayers.firstOrNull { it.timestamp > now } ?: tomorrowPrayers.first()
        
        // Find the prayer that just passed to calculate progress
        val allPrayersSorted = (yesterdayPrayers + prayers + tomorrowPrayers).sortedBy { it.timestamp }
        val nextIndex = allPrayersSorted.indexOfFirst { it.timestamp > now }
        if (nextIndex > 0) {
            lastPrayerTimestamp = allPrayersSorted[nextIndex - 1].timestamp
        }

        nextPrayer = next
        
        _uiState.update { state -> 
            state.copy(
                data = state.data.copy(
                    nextPrayerName = next.nameAr,
                    allPrayers = prayers
                )
            )
        }
    }

    private fun startPrayerCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val currentTimeStr = timeFormat.format(Date(now))
                
                nextPrayer?.let { next ->
                    val diff = next.timestamp - now
                    
                    if (diff <= 0) {
                        updateLocationAndPrayerData()
                    } else {
                        val hours = (diff / (1000 * 60 * 60))
                        val minutes = (diff / (1000 * 60)) % 60
                        val seconds = (diff / 1000) % 60
                        val timeLeft = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                        
                        // Calculate progress
                        val totalDuration = next.timestamp - lastPrayerTimestamp
                        val elapsed = now - lastPrayerTimestamp
                        val progress = if (totalDuration > 0) elapsed.toFloat() / totalDuration else 0f

                        _uiState.update { state ->
                            state.copy(
                                data = state.data.copy(
                                    nextPrayerTimeLeft = timeLeft,
                                    currentTime = currentTimeStr,
                                    prayerProgress = progress.coerceIn(0f, 1f)
                                )
                            )
                        }
                    }
                } ?: run {
                    _uiState.update { state ->
                        state.copy(data = state.data.copy(currentTime = currentTimeStr))
                    }
                }
                delay(1.seconds)
            }
        }
    }

    private suspend fun updateLocationName(lat: Double, lng: Double) = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(applicationContext, Locale("ar"))
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    val address = addresses.firstOrNull()
                    if (address != null) {
                        val city = address.locality ?: address.adminArea ?: "حلب"
                        val country = address.countryName ?: "سوريا"
                        _uiState.update { state ->
                            state.copy(data = state.data.copy(location = "$city، $country"))
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val city = address.locality ?: address.adminArea ?: "حلب"
                    val country = address.countryName ?: "سوريا"
                    _uiState.update { state ->
                        state.copy(data = state.data.copy(location = "$city، $country"))
                    }
                }
            }
        } catch (e: Exception) {}
    }

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
                    azkar.map { if (it.targetCount > 0) it.currentCount.toFloat() / it.targetCount else 0f }.average().toFloat()
                } else 0f
                
                val randomAsma = asma.randomOrNull()
                
                val offers = donations.count { it.type == "OFFER" }
                val requests = donations.count { it.type == "REQUEST" }

                _uiState.update { state ->
                    state.copy(
                        data = state.data.copy(
                            userName = userName,
                            hijriDate = HijriDateFormatter.nowFormatted(),
                            spotlightAllahName = randomAsma?.name ?: applicationContext.getString(R.string.default_allah_name),
                            spotlightTransliteration = randomAsma?.transliteration ?: applicationContext.getString(R.string.default_allah_transliteration),
                            spotlightMeaning = randomAsma?.meaning ?: applicationContext.getString(R.string.default_allah_meaning),
                            dailyZikrTitle = dailyZikr?.text ?: applicationContext.getString(R.string.default_zikr_title),
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
                refreshLocationAndPrayers()
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
                viewModelScope.launch {
                    settingsManager.setCalculationMethod(action.method)
                }
            }
            is HomeDashboardAction.OnUpdateMadhab -> {
                viewModelScope.launch {
                    settingsManager.setMadhab(action.madhab)
                }
            }
            is HomeDashboardAction.OnUpdateLocationMode -> {
                viewModelScope.launch {
                    settingsManager.setUseAutoLocation(action.isAuto)
                }
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
        }
    }

    override fun onCleared() {
        super.onCleared()
        dataObserveJob?.cancel()
        countdownJob?.cancel()
    }
}
