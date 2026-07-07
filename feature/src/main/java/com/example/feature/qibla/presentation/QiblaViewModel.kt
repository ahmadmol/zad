package com.example.feature.qibla.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.qibla.util.QiblaManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class QiblaUiState(
    val compassRotation: Float = 0f,
    val qiblaAngle: Float = 0f,
    val locationName: String = "",
    val cityAndCountry: String = "",
    val isLoading: Boolean = true,
    val hasLocationPermission: Boolean = false,
    val error: String? = null,
    val calibrationMessage: String? = null,
    val sensorAccuracy: Int? = null,
    val isAligned: Boolean = false
)

class QiblaViewModel(
    private val qiblaManager: QiblaManager,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var alignedState = false

    init {
        observeCompass()
        observeSensorAccuracy()
    }

    private fun observeCompass() {
        viewModelScope.launch {
            qiblaManager.getRotationFlow()
                .collect { rotation ->
                    val qAngle = _uiState.value.qiblaAngle
                    val diff = Math.abs((rotation - qAngle + 180 + 360) % 360 - 180)
                    // hysteresis thresholds
                    val enterThreshold = 3.0f
                    val exitThreshold = 5.0f
                    if (qAngle == 0f) {
                        alignedState = false
                    } else {
                        if (!alignedState && diff < enterThreshold) alignedState = true
                        else if (alignedState && diff > exitThreshold) alignedState = false
                    }
                    _uiState.update { it.copy(compassRotation = rotation, isAligned = alignedState) }
                }
        }
    }

    private fun observeSensorAccuracy() {
        viewModelScope.launch {
            qiblaManager.getAccuracyFlow().collect { accuracy ->
                val message = when (accuracy) {
                    SensorManager.SENSOR_STATUS_UNRELIABLE, SensorManager.SENSOR_STATUS_ACCURACY_LOW ->
                        "حرّك الهاتف على شكل رقم 8 لمعايرة البوصلة"
                    else -> null
                }
                _uiState.update { it.copy(calibrationMessage = message, sensorAccuracy = accuracy) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun updateLocationAndCalculateQibla() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        // verify permission
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine) {
            _uiState.update { it.copy(isLoading = false, hasLocationPermission = false, error = "برجاء تفعيل صلاحية الموقع") }
            return
        } else {
            _uiState.update { it.copy(hasLocationPermission = true) }
        }
        viewModelScope.launch {
            try {
                // Try to get last known location first for immediate result
                val lastLocation = fusedLocationClient.lastLocation.await()
                if (lastLocation != null) {
                    // update declination for more accurate compass readings
                    qiblaManager.updateDeclinationFromLocation(lastLocation.latitude, lastLocation.longitude, lastLocation.altitude)
                    processLocation(lastLocation)
                }

                // Then try to get a fresh accurate location
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                if (location != null) {
                    qiblaManager.updateDeclinationFromLocation(location.latitude, location.longitude, location.altitude)
                    processLocation(location)
                } else if (_uiState.value.qiblaAngle == 0f) {
                    _uiState.update { it.copy(isLoading = false, error = "تعذر تحديد الموقع بدقة، يرجى تفعيل GPS") }
                }
            } catch (e: Exception) {
                if (_uiState.value.qiblaAngle == 0f) {
                    _uiState.update { it.copy(isLoading = false, error = "خطأ: ${e.localizedMessage}") }
                }
            }
        }
    }

    private suspend fun processLocation(location: Location) {
        val qiblaAngle = QiblaManager.calculateQiblaDirection(location.latitude, location.longitude)
        
        val (locationName, cityAndCountry) = getAddressFromLocation(location.latitude, location.longitude)
        
        _uiState.update { 
            it.copy(
                qiblaAngle = qiblaAngle, 
                locationName = locationName,
                cityAndCountry = cityAndCountry,
                isLoading = false 
            ) 
        }
    }

    private suspend fun getAddressFromLocation(lat: Double, lng: Double): Pair<String, String> = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale("ar"))
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                return@withContext kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        if (address != null) {
                            val street = address.thoroughfare ?: address.subLocality ?: "موقع غير معروف"
                            val city = address.locality ?: address.adminArea ?: ""
                            val country = address.countryName ?: ""
                            continuation.resume(Pair(street, "$city، $country"))
                        } else {
                            continuation.resume(Pair("موقع مجهول", "جاري التحديد..."))
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val street = address.thoroughfare ?: address.subLocality ?: "موقع غير معروف"
                    val city = address.locality ?: address.adminArea ?: ""
                    val country = address.countryName ?: ""
                    return@withContext Pair(street, "$city، $country")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext Pair("موقع مجهول", "جاري التحديد...")
    }
}
