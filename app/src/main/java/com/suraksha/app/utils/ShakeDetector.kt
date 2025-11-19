package com.suraksha.app.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs
import kotlin.math.max


class ShakeDetector(
    context: Context,
    private val onTripleShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val linearAccel: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val accel: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var usingLinear = false
    private var registered = false

    private var gravityX = 0f
    private var gravityY = 0f
    private var gravityZ = 0f

    private var lastDirection: Int = 0
    private var alternationCount: Int = 0
    private var lastPeakTime: Long = 0L
    private var windowStartTime: Long = 0L

    companion object {
        private const val TAG = "ShakeDetector"

        private const val THRESHOLD_M_S2 = 12.0f
        private const val MIN_PEAK_GAP_MS = 150L
        private const val MAX_PEAK_GAP_MS = 450L
        private const val WINDOW_MS = 1800L
        private const val REQUIRED_ALTERNATIONS = 6
        private const val ALPHA = 0.8f
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
        Log.w(TAG, "âœ… Shake detector STARTED. UsingLinear=$usingLinear, Threshold=$THRESHOLD_M_S2 m/sÂ²")
        Log.w(TAG, "   SHAKE PHONE VIGOROUSLY 3 TIMES BACK AND FORTH TO TRIGGER")
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

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            gravityX = ALPHA * gravityX + (1 - ALPHA) * x
            gravityY = ALPHA * gravityY + (1 - ALPHA) * y
            gravityZ = ALPHA * gravityZ + (1 - ALPHA) * z
            Pair(x - gravityX, y - gravityY)
        }

        val absX = abs(lx)
        val absY = abs(ly)
        val value = if (absX >= absY) lx else ly
        val axis = if (absX >= absY) "X" else "Y"

        val now = System.currentTimeMillis()

        if (windowStartTime == 0L || now - windowStartTime > WINDOW_MS) {
            windowStartTime = now
            alternationCount = 0
            lastDirection = 0
            lastPeakTime = 0L
        }

        if (abs(value) >= THRESHOLD_M_S2) {
            val dir = if (value > 0) 1 else -1

            val dt = if (lastPeakTime == 0L) Long.MAX_VALUE else now - lastPeakTime
            if (dt >= MIN_PEAK_GAP_MS && dt <= MAX_PEAK_GAP_MS) {

                if (dir != lastDirection) {
                    alternationCount++
                    lastDirection = dir
                    lastPeakTime = now
                    Log.d(TAG, "ðŸ”µ Peak $alternationCount/$REQUIRED_ALTERNATIONS | dir=$dir | axis=$axis | accel=${String.format("%.1f", abs(value))} m/sÂ² | dt=${dt}ms")

                    if (alternationCount >= REQUIRED_ALTERNATIONS) {
                        Log.w(TAG, "âœ…âœ…âœ… TRIPLE SHAKE DETECTED! Triggering SOS! âœ…âœ…âœ…")
                        reset()
                        onTripleShake()

                        windowStartTime = now
                    }
                }
            } else if (dt > MAX_PEAK_GAP_MS) {

                alternationCount = 1
                lastDirection = dir
                lastPeakTime = now
                windowStartTime = max(windowStartTime, now - MIN_PEAK_GAP_MS)
                Log.v(TAG, "Restart count due to dt=$dt; dir=$dir axis=$axis")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}
