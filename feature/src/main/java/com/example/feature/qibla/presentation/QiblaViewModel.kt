package com.example.feature.qibla.presentation

import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerLocationSource
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerLocationUnavailableReason
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.example.feature.qibla.util.QiblaManager
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class QiblaUiState(
    val compassRotation: Float = 0f,
    val qiblaAngle: Float = 0f,
    val hasCalculatedBearing: Boolean = false,
    val locationName: String = "",
    val cityAndCountry: String = "",
    val isLoading: Boolean = true,
    val hasLocationPermission: Boolean = false,
    val error: String? = null,
    val calibrationMessage: String? = null,
    val sensorAccuracy: Int? = null,
    val hasCompassSensors: Boolean = true,
    val isAligned: Boolean = false
)

/** Qibla consumes Prayer's canonical saved/manual/device location source. */
class QiblaViewModel(
    private val qiblaManager: QiblaManager,
    private val locationRepository: PrayerLocationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(QiblaUiState(hasCompassSensors = qiblaManager.hasCompassSensors))
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()
    private var alignedState = false
    private var compassJob: kotlinx.coroutines.Job? = null
    private var accuracyJob: kotlinx.coroutines.Job? = null
    private var locationJob: kotlinx.coroutines.Job? = null
    private var refreshJob: kotlinx.coroutines.Job? = null

    init {
        observeLocation()
    }

    fun setCompassActive(active: Boolean) {
        if (active) {
            if (compassJob?.isActive != true) compassJob = observeCompass()
            if (accuracyJob?.isActive != true) accuracyJob = observeSensorAccuracy()
        } else {
            compassJob?.cancel(); accuracyJob?.cancel()
            alignedState = false
            _uiState.update { it.copy(isAligned = false, sensorAccuracy = null) }
        }
    }

    private fun observeCompass() = viewModelScope.launch {
        qiblaManager.getRotationFlow().catch { error ->
            if (error is kotlinx.coroutines.CancellationException) throw error
            alignedState = false
            _uiState.update { it.copy(isAligned=false,hasCompassSensors=false,error="تعذر قراءة مستشعر الاتجاه؛ أعد فتح الشاشة للمحاولة") }
        }.collect { rotation ->
            val qibla = _uiState.value.qiblaAngle
            val difference = abs((rotation - qibla + 540f) % 360f - 180f)
            alignedState = when {
                !_uiState.value.hasCalculatedBearing -> false
                !alignedState && difference < 3f -> true
                alignedState && difference > 5f -> false
                else -> alignedState
            }
            _uiState.update { it.copy(compassRotation = rotation, isAligned = alignedState) }
        }
    }

    private fun observeSensorAccuracy() = viewModelScope.launch {
        qiblaManager.getAccuracyFlow().catch { error ->
            if (error is kotlinx.coroutines.CancellationException) throw error
            _uiState.update { it.copy(sensorAccuracy=null,calibrationMessage="تعذر قراءة دقة المستشعر") }
        }.collect { accuracy ->
            val message = when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE,
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "حرّك الهاتف على شكل رقم 8 لمعايرة البوصلة"
                else -> null
            }
            _uiState.update { it.copy(calibrationMessage = message, sensorAccuracy = accuracy) }
        }
    }

    private fun observeLocation() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
        locationRepository.observeLocation().catch { error ->
            if (error is kotlinx.coroutines.CancellationException) throw error
            _uiState.update { it.copy(isLoading=false,error="تعذر قراءة الموقع المحفوظ؛ أعد المحاولة دون مسح البيانات") }
        }.collect { state ->
            when (state) {
                PrayerLocationState.Loading -> _uiState.update { it.copy(isLoading = true) }
                is PrayerLocationState.Available -> applyLocation(state.location, state.source)
                is PrayerLocationState.Unavailable -> if (!_uiState.value.hasCalculatedBearing) {
                    _uiState.update {
                        it.copy(isLoading = false, error = unavailableMessage(state.reason))
                    }
                }
            }
        }
        }
    }

    fun updateLocationAndCalculateQibla() {
        if (refreshJob?.isActive == true) return
        if (locationJob?.isActive != true) observeLocation()
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !it.hasCalculatedBearing, error = null) }
            try {
            locationRepository.refreshLocation()
                .onSuccess { location ->
                    // Refresh may return the selected manual city without requesting GPS.
                    // Keep the canonical source instead of presenting that city as a device fix.
                    val canonical = locationRepository.observeLocation().first()
                    val source = (canonical as? PrayerLocationState.Available)
                        ?.takeIf { it.location == location }?.source ?: PrayerLocationSource.Device
                    applyLocation(location, source)
                }
                .onFailure { error ->
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    if (!_uiState.value.hasCalculatedBearing) {
                        _uiState.update {
                            it.copy(isLoading = false, error = "تعذر تحديث الموقع. تحقق من الإذن وخدمة الموقع أو استخدم موقعًا محفوظًا.")
                        }
                    }
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) {
                _uiState.update { it.copy(isLoading=false,error="تعذر تحديث الموقع؛ أعد المحاولة. يبقى الاتجاه السابق إن توفر.") }
            }
        }
    }

    private fun applyLocation(location: PrayerLocation, source: PrayerLocationSource) {
        qiblaManager.updateDeclinationFromLocation(location.latitude, location.longitude)
        val angle = QiblaManager.calculateQiblaDirection(location.latitude, location.longitude)
        val sourceLabel = when (source) {
            PrayerLocationSource.Device -> "الموقع الحالي"
            PrayerLocationSource.Saved -> "موقع محفوظ"
            PrayerLocationSource.Manual -> "موقع يدوي"
        }
        _uiState.update {
            it.copy(
                qiblaAngle = angle,
                hasCalculatedBearing = true,
                locationName = location.displayName,
                cityAndCountry = sourceLabel,
                isLoading = false,
                error = null
            )
        }
    }

    private fun unavailableMessage(reason: PrayerLocationUnavailableReason) = when (reason) {
        PrayerLocationUnavailableReason.PERMISSION_DENIED -> "يلزم إذن الموقع عند عدم وجود موقع محفوظ أو يدوي"
        PrayerLocationUnavailableReason.PROVIDER_DISABLED -> "خدمة الموقع متوقفة"
        PrayerLocationUnavailableReason.TIMEOUT -> "تعذر الحصول على موقع جديد"
        PrayerLocationUnavailableReason.NO_SAVED_LOCATION -> "لا يوجد موقع محفوظ. اسمح بالموقع أو اختر مدينة من إعدادات الصلاة."
        PrayerLocationUnavailableReason.UNKNOWN -> "تعذر تحديد الموقع"
    }
}
