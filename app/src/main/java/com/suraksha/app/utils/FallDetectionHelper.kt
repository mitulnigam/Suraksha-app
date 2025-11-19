package com.suraksha.app.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt


class FallDetectionHelper(
    context: Context,
    private val onFallDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var isListenerRegistered = false
    private var isFalling = false
    private var fallStartTime: Long = 0

    companion object {
        private const val TAG = "FallDetectionHelper"

        private const val FREE_FALL_THRESHOLD = 2.5f
        private const val IMPACT_THRESHOLD = 20.0f
        private const val FALL_TIME_WINDOW_MS = 500
    }

    fun startListening() {
        if (accelerometer == null) {
            Log.e(TAG, "No accelerometer sensor found. Fall detection cannot start.")
            return
        }
        if (isListenerRegistered) return

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        isListenerRegistered = true
        isFalling = false
        Log.d(TAG, "Fall detection listener STARTED.")
    }

    fun stopListening() {
        if (!isListenerRegistered) return
        sensorManager.unregisterListener(this)
        isListenerRegistered = false
        Log.d(TAG, "Fall detection listener STOPPED.")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt(x * x + y * y + z * z)

        val currentTime = System.currentTimeMillis()


        if (magnitude < FREE_FALL_THRESHOLD) {
            if (!isFalling) {

                isFalling = true
                fallStartTime = currentTime
                Log.d(TAG, "Potential fall detected: FREE FALL state started.")
            }
        }

        else if (isFalling) {
            if (magnitude > IMPACT_THRESHOLD) {

                Log.i(TAG, "!!! FALL DETECTED !!! Free fall followed by impact.")
                onFallDetected()
            }


            if (currentTime - fallStartTime > FALL_TIME_WINDOW_MS) {
                isFalling = false
                Log.d(TAG, "Fall state timed out. Resetting.")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}