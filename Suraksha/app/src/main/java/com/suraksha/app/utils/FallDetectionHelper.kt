package com.suraksha.app.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

/**
 * A helper class to detect a fall event by analyzing accelerometer data.
 *
 * This simulates an AI model by detecting a "free fall" event
 * (low g-force) followed by a "high impact" event (high g-force).
 */
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
        // Thresholds for fall detection (these may need tuning)
        private const val FREE_FALL_THRESHOLD = 2.5f // g-force near zero
        private const val IMPACT_THRESHOLD = 20.0f  // High g-force spike
        private const val FALL_TIME_WINDOW_MS = 500 // Max time (0.5s) for a fall
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

        // Calculate the total magnitude of acceleration
        val magnitude = sqrt(x * x + y * y + z * z)

        val currentTime = System.currentTimeMillis()

        // --- Fall Detection Logic ---

        // 1. DETECT FREE FALL
        if (magnitude < FREE_FALL_THRESHOLD) {
            if (!isFalling) {
                // This is the start of a potential fall
                isFalling = true
                fallStartTime = currentTime
                Log.d(TAG, "Potential fall detected: FREE FALL state started.")
            }
        }
        // 2. DETECT IMPACT (if we are in a "falling" state)
        else if (isFalling) {
            if (magnitude > IMPACT_THRESHOLD) {
                // We were in free fall, and now we've hit a high impact.
                Log.i(TAG, "!!! FALL DETECTED !!! Free fall followed by impact.")
                onFallDetected() // This is a fall!
            }

            // 3. CHECK FOR TIMEOUT
            // If it's been too long since free fall started, reset.
            if (currentTime - fallStartTime > FALL_TIME_WINDOW_MS) {
                isFalling = false
                Log.d(TAG, "Fall state timed out. Resetting.")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}