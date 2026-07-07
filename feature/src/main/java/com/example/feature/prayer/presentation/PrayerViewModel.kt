package com.example.feature.prayer.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Madhab
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.prayer.util.PrayerCalculator
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class PrayerViewModel(
    private val settingsManager: SettingsManager,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    private var lastLat: Double = 36.2021
    private var lastLng: Double = 37.1343
    private var currentMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE
    private var currentMadhab = Madhab.SHAFI
    private var useAutoLocation = true

    init {
        observeSettings()
        startCountdownTimer()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsManager.calculationMethodFlow,
                settingsManager.madhabFlow,
                settingsManager.useAutoLocationFlow,
                settingsManager.manualLocationLatFlow,
                settingsManager.manualLocationLngFlow
            ) { method, madhab, auto, lat, lng ->
                currentMethod = PrayerCalculator.getMethodFromString(method)
                currentMadhab = PrayerCalculator.getMadhabFromString(madhab)
                useAutoLocation = auto
                if (!auto) {
                    lastLat = lat
                    lastLng = lng
                }
                loadPrayerTimes()
            }.collect()
        }
    }

    @SuppressLint("MissingPermission")
    fun loadPrayerTimes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                if (useAutoLocation) {
                    val lastLocation = fusedLocationClient.lastLocation.await()
                    lastLocation?.let { 
                        lastLat = it.latitude
                        lastLng = it.longitude
                    }

                    val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                    location?.let { 
                        lastLat = it.latitude
                        lastLng = it.longitude
                    }
                }
                
                updateTimes(lastLat, lastLng)
            } catch (e: Exception) {
                updateTimes(lastLat, lastLng)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (true) {
                updateCountdown()
                delay(1000)
            }
        }
    }

    private fun updateCountdown() {
        val times = PrayerCalculator.calculate(lastLat, lastLng, method = currentMethod, madhab = currentMadhab)
        val now = System.currentTimeMillis()
        
        var next = times.firstOrNull { it.timestamp > now }
        var nextTimeMillis: Long
        
        if (next != null) {
            nextTimeMillis = next.timestamp
        } else {
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
            val tomorrowTimes = PrayerCalculator.calculate(lastLat, lastLng, tomorrow, method = currentMethod, madhab = currentMadhab)
            next = tomorrowTimes.first()
            nextTimeMillis = next.timestamp
        }

        val diff = nextTimeMillis - now
        val h = (diff / (1000 * 60 * 60)) % 24
        val m = (diff / (1000 * 60)) % 60
        val s = (diff / 1000) % 60
        
        _uiState.update { it.copy(
            nextPrayerName = next!!.nameAr,
            nextPrayerCountdown = String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
        ) }
    }

    private fun updateTimes(lat: Double, lng: Double) {
        val times = PrayerCalculator.calculate(lat, lng, method = currentMethod, madhab = currentMadhab)
        val dateFormat = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
        
        // Reverse geocoding for city name
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            try {
                val geocoder = Geocoder(context, Locale("ar"))
                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    val cityName = addresses.firstOrNull()?.let { 
                        it.locality ?: it.subAdminArea ?: it.adminArea ?: "موقعك الحالي"
                    } ?: "موقعك الحالي"
                    _uiState.update { it.copy(locationName = cityName) }
                }
            } catch (e: Exception) { }
        } else {
            val cityName = try {
                val geocoder = Geocoder(context, Locale("ar"))
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                addresses?.firstOrNull()?.let { 
                    it.locality ?: it.subAdminArea ?: it.adminArea ?: "موقعك الحالي"
                } ?: "موقعك الحالي"
            } catch (e: Exception) {
                if (lat == 36.2021) "حلب، سوريا" else "موقعك الحالي"
            }
            _uiState.update { it.copy(locationName = cityName) }
        }
        
        _uiState.update { 
            it.copy(
                prayerTimes = times,
                currentDate = dateFormat.format(Date()),
                hijriDate = HijriDateFormatter.nowFormatted()
            )
        }
    }

    fun onAction(action: PrayerAction) {
        when (action) {
            PrayerAction.OnRefresh -> loadPrayerTimes()
            is PrayerAction.OnToggleNotification -> {}
        }
    }
}
