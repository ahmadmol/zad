package com.example.feature.prayer.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.prayer.domain.model.PrayerLocation
import com.example.feature.prayer.domain.model.PrayerLocationSource
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerLocationUnavailableReason
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PrayerLocationRepositoryImpl(
    private val context: Context,
    private val settingsManager: SettingsManager,
    private val userPreferences: UserPreferences
) : PrayerLocationRepository {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val lastDeviceFix = MutableStateFlow<PrayerLocation?>(null)

    override fun observeLocation(): Flow<PrayerLocationState> {
        val manualBase = combine(
            settingsManager.useAutoLocationFlow,
            settingsManager.manualLocationLatFlow,
            settingsManager.manualLocationLngFlow,
            settingsManager.manualLocationCityFlow,
            userPreferences.userLatitude
        ) { useAuto, manualLat, manualLng, manualCity, savedLat ->
            ManualPartial(useAuto, manualLat, manualLng, manualCity, savedLat)
        }
        val manualOrSaved = combine(
            manualBase,
            userPreferences.userLongitude
        ) { partial, savedLng ->
            ManualSavedSnapshot(
                useAuto = partial.useAuto,
                manualLat = partial.manualLat,
                manualLng = partial.manualLng,
                manualCity = partial.manualCity,
                savedLat = partial.savedLat,
                savedLng = savedLng
            )
        }
        return combine(manualOrSaved, lastDeviceFix) { snapshot, device ->
            if (!snapshot.useAuto) {
                PrayerLocationState.Available(
                    location = PrayerLocation(
                        snapshot.manualLat,
                        snapshot.manualLng,
                        snapshot.manualCity.ifBlank { "موقع يدوي" }
                    ),
                    source = PrayerLocationSource.Manual
                )
            } else if (device != null) {
                PrayerLocationState.Available(location = device, source = PrayerLocationSource.Device)
            } else if (snapshot.savedLat != null && snapshot.savedLng != null) {
                PrayerLocationState.Available(
                    location = PrayerLocation(snapshot.savedLat, snapshot.savedLng, "موقع محفوظ"),
                    source = PrayerLocationSource.Saved
                )
            } else {
                PrayerLocationState.Unavailable(PrayerLocationUnavailableReason.NO_SAVED_LOCATION)
            }
        }.distinctUntilChanged()
    }

    private data class ManualPartial(
        val useAuto: Boolean,
        val manualLat: Double,
        val manualLng: Double,
        val manualCity: String,
        val savedLat: Double?
    )

    private data class ManualSavedSnapshot(
        val useAuto: Boolean,
        val manualLat: Double,
        val manualLng: Double,
        val manualCity: String,
        val savedLat: Double?,
        val savedLng: Double?
    )

    override suspend fun refreshLocation(): Result<PrayerLocation> {
        val useAuto = settingsManager.useAutoLocationFlow.first()
        if (!useAuto) {
            val lat = settingsManager.manualLocationLatFlow.first()
            val lng = settingsManager.manualLocationLngFlow.first()
            val city = settingsManager.manualLocationCityFlow.first()
            return Result.success(PrayerLocation(lat, lng, city.ifBlank { "موقع يدوي" }))
        }

        if (!hasLocationPermission()) {
            return Result.failure(LocationUnavailableException(PrayerLocationUnavailableReason.PERMISSION_DENIED))
        }

        return try {
            @SuppressLint("MissingPermission")
            val location = fusedLocationClient.lastLocation.await()
                ?: fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()

            if (location == null) {
                Result.failure(LocationUnavailableException(PrayerLocationUnavailableReason.TIMEOUT))
            } else {
                val displayName = resolveDisplayName(location.latitude, location.longitude)
                val prayerLocation = PrayerLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    displayName = displayName
                )
                userPreferences.saveLocation(location.latitude, location.longitude)
                lastDeviceFix.value = prayerLocation
                Result.success(prayerLocation)
            }
        } catch (e: SecurityException) {
            Result.failure(LocationUnavailableException(PrayerLocationUnavailableReason.PERMISSION_DENIED))
        } catch (e: Exception) {
            Result.failure(LocationUnavailableException(PrayerLocationUnavailableReason.UNKNOWN))
        }
    }

    override suspend fun saveManualLocation(
        latitude: Double,
        longitude: Double,
        displayName: String
    ): Result<Unit> = runCatching {
        settingsManager.setUseAutoLocation(false)
        settingsManager.setManualLocation(displayName, latitude, longitude)
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun resolveDisplayName(lat: Double, lng: Double): String {
        return try {
            if (!Geocoder.isPresent()) return "موقعك الحالي"
            val geocoder = Geocoder(context, Locale("ar"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCoroutine { cont ->
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        val name = addresses.firstOrNull()?.let {
                            it.locality ?: it.subAdminArea ?: it.adminArea
                        } ?: "موقعك الحالي"
                        cont.resume(name)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                addresses?.firstOrNull()?.let {
                    it.locality ?: it.subAdminArea ?: it.adminArea
                } ?: "موقعك الحالي"
            }
        } catch (_: Exception) {
            "موقعك الحالي"
        }
    }
}

class LocationUnavailableException(
    val reason: PrayerLocationUnavailableReason
) : Exception(reason.name)
