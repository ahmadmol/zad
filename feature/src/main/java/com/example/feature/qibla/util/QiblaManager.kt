package com.example.feature.qibla.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.*

class QiblaManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val hasCompassSensors: Boolean get() = accelerometer != null && magnetometer != null

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)

    // smoothing / state
    private var smoothedAzimuth: Float? = null

    // declination correction (magnetic -> true)
    private var declination: Float = 0f

    private var onRotationChanged: ((Float) -> Unit)? = null

    // sensor accuracy flow
    private val accuracyFlow = MutableSharedFlow<Int>(replay = 1)

    fun getRotationFlow(): Flow<Float> = callbackFlow {
        if (!hasCompassSensors) { close(); return@callbackFlow }
        smoothedAzimuth = null
        onRotationChanged = { rotation ->
            trySend(rotation)
        }
        
        sensorManager.registerListener(this@QiblaManager, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this@QiblaManager, magnetometer, SensorManager.SENSOR_DELAY_UI)
        
        awaitClose {
            sensorManager.unregisterListener(this@QiblaManager)
            onRotationChanged = null
        }
    }

    fun getAccuracyFlow(): Flow<Int> = accuracyFlow.asSharedFlow()

    fun updateDeclinationFromLocation(lat: Double, lng: Double, alt: Double = 0.0) {
        declination = android.hardware.GeomagneticField(
            lat.toFloat(), lng.toFloat(), alt.toFloat(), System.currentTimeMillis()
        ).declination
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.copyOf()
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.copyOf()
        }

        if (gravity.all { it == 0f } || geomagnetic.all { it == 0f }) return
        
        val r = FloatArray(9)
        val i = FloatArray(9)
        
        if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(r, orientation)
            val azimuthInRadians = orientation[0]
            val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
            // Standard sensor azimuth in degrees [0, 360) clockwise from magnetic north
            val magneticAz = ((azimuthInDegrees % 360f) + 360f) % 360f

            // apply declination to convert to true north
            var corrected = magneticAz + declination
            // normalize to 0..360
            corrected = ((corrected % 360f) + 360f) % 360f

            // smoothing on circular values
            val prev = smoothedAzimuth
            val smoothed = if (prev == null) {
                corrected
            } else {
                val delta = ((corrected - prev + 540f) % 360f) - 180f
                var next = prev + 0.25f * delta
                next = ((next % 360f) + 360f) % 360f
                next
            }
            smoothedAzimuth = smoothed
            onRotationChanged?.invoke(smoothed)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Accelerometer accuracy does not establish magnetic heading accuracy.
        // Do not let its callbacks dismiss the compass calibration warning.
        if (sensor?.type != Sensor.TYPE_MAGNETIC_FIELD) return
        try {
            accuracyFlow.tryEmit(accuracy)
        } catch (_: Exception) {
        }
    }

    companion object {
        private const val KAABA_LAT = 21.4225
        private const val KAABA_LNG = 39.8262

        fun calculateQiblaDirection(userLat: Double, userLng: Double): Float {
            val phi1 = Math.toRadians(userLat)
            val phi2 = Math.toRadians(KAABA_LAT)
            val deltaL = Math.toRadians(KAABA_LNG - userLng)

            val y = sin(deltaL) * cos(phi2)
            val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaL)
            
            var qibla = Math.toDegrees(atan2(y, x)).toFloat()
            if (qibla < 0) qibla += 360f
            return qibla
        }
    }
}
