package com.suraksha.app.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs
import kotlin.math.max

/**
 * Detects a precise "triple back-and-forth" shake gesture using the phone's motion sensors.
 *
 * Implementation notes:
 * - Prefers TYPE_LINEAR_ACCELERATION (gravity removed). Falls back to TYPE_ACCELEROMETER with a
 *   simple high-pass filter to remove gravity.
 * - Counts alternating peaks along the dominant axis (X/Y) that exceed a threshold, with timing
 *   constraints to avoid noise. We require 6 alternating half-cycles within a window to represent
 *   3 full back-and-forth shakes.
 */
class ShakeDetector(
    context: Context,
    private val onTripleShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val linearAccel: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val accel: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var usingLinear = false
    private var registered = false

    // High-pass filter state for accelerometer fallback
    private var gravityX = 0f
    private var gravityY = 0f
    private var gravityZ = 0f

    // Gesture detection state
    private var lastDirection: Int = 0 // -1, 0, +1
    private var alternationCount: Int = 0 // counts half-cycles
    private var lastPeakTime: Long = 0L
    private var windowStartTime: Long = 0L

    companion object {
        private const val TAG = "ShakeDetector"

        // Tunables (conservative to reduce false positives)
        private const val THRESHOLD_M_S2 = 8.5f        // minimum linear acceleration on dominant axis
        private const val MIN_PEAK_GAP_MS = 120L       // avoid double-counting the same swing
        private const val MAX_PEAK_GAP_MS = 600L       // swings should be reasonably fast
        private const val WINDOW_MS = 2200L            // all 6 alternations must fit inside this window
        private const val REQUIRED_ALTERNATIONS = 6    // 3 full back-and-forth cycles
        private const val ALPHA = 0.8f                 // high-pass filter constant for accel fallback
    }

    fun startListening() {
        if (registered) return

        usingLinear = linearAccel != null
        val sensor = linearAccel ?: accel
        if (sensor == null) {
            Log.e(TAG, "No suitable accelerometer sensor found. Shake detection cannot start.")
            return
        }
        reset()
        sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        registered = true
        Log.d(TAG, "Shake detector STARTED. UsingLinear=$usingLinear")
    }

    fun stopListening() {
        if (!registered) return
        sensorManager.unregisterListener(this)
        registered = false
        Log.d(TAG, "Shake detector STOPPED.")
    }

    private fun reset() {
        alternationCount = 0
        lastDirection = 0
        lastPeakTime = 0L
        windowStartTime = 0L
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (usingLinear && event.sensor.type != Sensor.TYPE_LINEAR_ACCELERATION) return
        if (!usingLinear && event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val (lx, ly) = if (usingLinear) {
            Pair(event.values[0], event.values[1])
        } else {
            // High-pass filter to approximate linear acceleration (remove gravity)
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            gravityX = ALPHA * gravityX + (1 - ALPHA) * x
            gravityY = ALPHA * gravityY + (1 - ALPHA) * y
            gravityZ = ALPHA * gravityZ + (1 - ALPHA) * z
            Pair(x - gravityX, y - gravityY)
        }

        // Choose dominant horizontal axis (ignore Z to reduce false positives)
        val absX = abs(lx)
        val absY = abs(ly)
        val value = if (absX >= absY) lx else ly
        val axis = if (absX >= absY) "X" else "Y"

        val now = System.currentTimeMillis()

        // Reset the window if it expired
        if (windowStartTime == 0L || now - windowStartTime > WINDOW_MS) {
            windowStartTime = now
            alternationCount = 0
            lastDirection = 0
            lastPeakTime = 0L
        }

        // Detect peak crossing threshold
        if (abs(value) >= THRESHOLD_M_S2) {
            val dir = if (value > 0) 1 else -1

            // Enforce time gap between peaks
            val dt = if (lastPeakTime == 0L) Long.MAX_VALUE else now - lastPeakTime
            if (dt >= MIN_PEAK_GAP_MS && dt <= MAX_PEAK_GAP_MS) {
                // Count alternations only when direction flips
                if (dir != lastDirection) {
                    alternationCount++
                    lastDirection = dir
                    lastPeakTime = now
                    Log.v(TAG, "Peak $alternationCount dir=$dir axis=$axis val=${abs(value)} dt=$dt")

                    if (alternationCount >= REQUIRED_ALTERNATIONS) {
                        Log.i(TAG, "Triple back-and-forth shake detected!")
                        reset()
                        onTripleShake()
                        // Start a fresh window after triggering to avoid immediate re-trigger
                        windowStartTime = now
                    }
                }
            } else if (dt > MAX_PEAK_GAP_MS) {
                // Too slow or first peak; restart counting from this direction
                alternationCount = 1
                lastDirection = dir
                lastPeakTime = now
                windowStartTime = max(windowStartTime, now - MIN_PEAK_GAP_MS)
                Log.v(TAG, "Restart count due to dt=$dt; dir=$dir axis=$axis")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no-op
    }
}
