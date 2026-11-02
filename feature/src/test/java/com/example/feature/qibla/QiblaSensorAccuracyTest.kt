package com.example.feature.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.example.feature.qibla.util.QiblaManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class QiblaSensorAccuracyTest {
    @Test
    fun `accelerometer accuracy cannot hide an unreliable compass`() = runTest {
        val accelerometer = mockk<Sensor> { every { type } returns Sensor.TYPE_ACCELEROMETER }
        val magnetometer = mockk<Sensor> { every { type } returns Sensor.TYPE_MAGNETIC_FIELD }
        val sensors = mockk<SensorManager> {
            every { getDefaultSensor(Sensor.TYPE_ACCELEROMETER) } returns accelerometer
            every { getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) } returns magnetometer
        }
        val context = mockk<Context> {
            every { getSystemService(Context.SENSOR_SERVICE) } returns sensors
        }
        val manager = QiblaManager(context)
        manager.onAccuracyChanged(magnetometer, SensorManager.SENSOR_STATUS_UNRELIABLE)
        assertEquals(SensorManager.SENSOR_STATUS_UNRELIABLE, manager.getAccuracyFlow().first())

        manager.onAccuracyChanged(accelerometer, SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
        assertEquals(
            "A reliable accelerometer must not dismiss magnetic calibration warnings",
            SensorManager.SENSOR_STATUS_UNRELIABLE,
            manager.getAccuracyFlow().first()
        )

        manager.onAccuracyChanged(magnetometer, SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
        assertEquals(SensorManager.SENSOR_STATUS_ACCURACY_HIGH, manager.getAccuracyFlow().first())
    }
}
