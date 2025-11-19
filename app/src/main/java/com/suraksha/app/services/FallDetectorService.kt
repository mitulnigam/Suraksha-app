package com.suraksha.app.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.suraksha.app.ml.FallInferenceManager
import com.suraksha.app.ui.ConfirmationActivity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class FallDetectorService : Service(), SensorEventListener {

    companion object {
        const val TAG = "FallDetectorService"
        private const val CHANNEL = "FALL_CHANNEL"
        private const val NOTIF_ID = 99
    }

    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null
    private var gyro: Sensor? = null

    private val SAMPLE_RATE = 50
    private val SENSOR_DELAY_US = (1_000_000 / SAMPLE_RATE)

    private val PREBUFFER_SEC = 3
    private val POSTBUFFER_SEC = 12
    private val freeFallThreshold = 3.5
    private val impactThreshold = 20.0
    private val pickupThreshold = 15.0
    private val FREEFALL_WINDOW_MS = 1200L

    private val preBuffer = ArrayDeque<SensorRow>()
    private val postBuffer = ArrayList<SensorRow>()
    private val recentVms = ArrayDeque<Pair<Long, Double>>()
    private val PEAK_WINDOW_MS = 350L

    private var freeFallTs: Long? = null
    private var impactTs: Long = 0L
    private var pickupDetected = false
    private var pickupTimeSec = -1
    private var collectingPost = false
    private var postStart = 0L

    private val seqLen = FallInferenceManager.SEQ_LEN
    private var inferenceManager: FallInferenceManager? = null

    override fun onCreate() {
        super.onCreate()

        try {
            inferenceManager = FallInferenceManager(applicationContext)
            Log.i(TAG, "ML inference initialized")
        } catch (e: Exception) {
            inferenceManager = null
            Log.w(TAG, "ML init failed, continuing without inference: ${e.message}")
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Fall detection active"))

        accel?.let { sensorManager.registerListener(this, it, SENSOR_DELAY_US) }
        gyro?.let { sensorManager.registerListener(this, it, SENSOR_DELAY_US) }

        Log.i(TAG, "Service started")
    }

    override fun onSensorChanged(event: SensorEvent) {
        val ts = System.currentTimeMillis()
        val nano = SystemClock.elapsedRealtimeNanos()

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0].toDouble()
            val ay = event.values[1].toDouble()
            val az = event.values[2].toDouble()
            val vm = sqrt(ax*ax + ay*ay + az*az)
            val row = SensorRow(ts, nano, "ACC", ax, ay, az, vm)
            handleRow(row)
        }

        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val row = SensorRow(ts, nano, "GYR",
                event.values[0].toDouble(),
                event.values[1].toDouble(),
                event.values[2].toDouble(),
                0.0
            )
            handleRow(row)
        }
    }

    private fun handleRow(row: SensorRow) {

        if (preBuffer.size >= SAMPLE_RATE * PREBUFFER_SEC)
            preBuffer.removeFirst()
        preBuffer.addLast(row)

        if (row.type == "ACC") {
            val now = row.ts
            recentVms.addLast(now to row.vm)
            while (recentVms.isNotEmpty() && now - recentVms.first().first > PEAK_WINDOW_MS)
                recentVms.removeFirst()
        }

        detectFreeFall(row)
        detectImpact(row)

        if (collectingPost) {
            postBuffer.add(row)
            detectPickup(row)

            if (row.ts - postStart > POSTBUFFER_SEC * 1000) {
                finishWindow()
            }
        }
    }

    private fun detectFreeFall(row: SensorRow) {
        if (row.type == "ACC" && row.vm < freeFallThreshold) {
            freeFallTs = row.ts
            Log.d(TAG, "FREE FALL detected @${row.ts}")
        }
    }

    private fun detectImpact(row: SensorRow) {
        val ff = freeFallTs ?: return
        val now = row.ts

        if (now - ff > FREEFALL_WINDOW_MS) {
            freeFallTs = null
            return
        }

        val peak = recentVms.maxOfOrNull { it.second } ?: 0.0

        if (peak > impactThreshold) {
            Log.w(TAG, "IMPACT detected peak=$peak at $now")

            impactTs = now
            pickupDetected = false
            pickupTimeSec = -1

            collectingPost = true
            postStart = now
            postBuffer.clear()

            freeFallTs = null
        }
    }

    private fun detectPickup(row: SensorRow) {
        if (pickupDetected || row.type != "ACC") return

        val dt = (row.ts - impactTs) / 1000.0
        if (dt < 0.4) return

        if (row.vm > pickupThreshold) {
            pickupDetected = true
            pickupTimeSec = dt.toInt()
            Log.i(TAG, "PICKUP @${pickupTimeSec}s")
        }
    }

    private fun finishWindow() {
        collectingPost = false

        val window = ArrayList<SensorRow>()
        window.addAll(preBuffer)
        window.addAll(postBuffer)

        runInference(window)
    }

    private fun runInference(window: List<SensorRow>) {
        val accSeq = window.filter { it.type == "ACC" }
            .map { Triple(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }

        val mgr = inferenceManager
        if (mgr == null) {
            Log.w(TAG, "âš ï¸ ML not available - cannot determine if real fall. Saving CSV only.")
            saveCsv(window, label = if (pickupDetected) "drop_pickup_${pickupTimeSec}s" else "drop_left")
            return
        }

        Thread {
            try {
                val (label, conf) = mgr.runInference(accSeq)

                Log.i(TAG, "ðŸ¤– ML PREDICTION: label='$label', confidence=${(conf*100).toInt()}%, pickup=$pickupDetected, pickupTime=${pickupTimeSec}s")

                val isRealFall = (label == "sim_fall" || label == "real_fall")
                val confidenceThreshold = 0.50f


                val wasPickedUpQuickly = pickupDetected && pickupTimeSec <= 15

                updateNotification("Last: $label (${(conf*100).toInt()}%)")

                when {

                    isRealFall && conf >= confidenceThreshold -> {
                        Log.w(TAG, "ðŸš¨ REAL FALL DETECTED by ML! label=$label, conf=${(conf*100).toInt()}%")
                        Log.w(TAG, "ðŸš¨ SENDING SOS IMMEDIATELY - No confirmation needed")
                        saveCsv(window, label)
                        sendSOSImmediately()
                    }


                    wasPickedUpQuickly && label.contains("drop", ignoreCase = true) -> {
                        Log.w(TAG, "âš ï¸ Phone picked up ${pickupTimeSec}s after impact (label=$label)")
                        Log.w(TAG, "âš ï¸ Could be fall + recovery - SENDING SOS IMMEDIATELY")
                        saveCsv(window, label)
                        sendSOSImmediately()
                    }

                    label.contains("drop", ignoreCase = true) || label.contains("pickup", ignoreCase = true) -> {
                        Log.d(TAG, "ðŸ“± Phone drop (pickup after ${pickupTimeSec}s) - not a person fall. No SOS.")
                        saveCsv(window, label)
                    }

                    else -> {
                        Log.d(TAG, "âšª Not a real fall (label=$label, conf=${(conf*100).toInt()}%). Saving CSV only.")
                        saveCsv(window, label)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "âŒ ML Inference error: ${e.message}", e)
                saveCsv(window, label = if (pickupDetected) "drop_pickup_${pickupTimeSec}s" else "drop_left")
            }
        }.start()
    }

    private fun updateNotification(text: String) {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIF_ID, buildNotification(text))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification: ${e.message}")
        }
    }

    private fun sendSOSImmediately() {
        try {
            Log.w(TAG, "ðŸ“¡ Broadcasting ACTION_TRIGGER_SOS intent immediately...")
            val i = Intent("com.suraksha.app.ACTION_TRIGGER_SOS")
            i.setPackage(packageName)
            sendBroadcast(i)
            Log.w(TAG, "âœ… SOS Broadcast sent successfully - Emergency SMS will be sent")

            updateNotification("ðŸš¨ FALL DETECTED - SOS SENT!")
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to send SOS broadcast: ${e.message}", e)
        }
    }

    private fun saveCsv(rows: List<SensorRow>, label: String) {
        try {
            val dir = getExternalFilesDir("candidates")!!
            dir.mkdirs()

            val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(dir, "candidate_$ts.csv")

            val fw = FileWriter(file)

            fw.write("label,$label\n")
            fw.write("impact_ts,$impactTs\n")
            fw.write("pickup_detected,$pickupDetected\n")
            fw.write("pickup_time_sec,$pickupTimeSec\n")
            fw.write("sample_hz,$SAMPLE_RATE\n")
            fw.write("\n")
            fw.write("ts_ms,ts_nano,type,x,y,z,vm\n")

            rows.forEach {
                fw.write("${it.ts},${it.nano},${it.type},${it.x},${it.y},${it.z},${it.vm}\n")
            }

            fw.flush()
            fw.close()

            Log.i(TAG, "Saved CSV: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "CSV save error: $e")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle("Suraksha Fall Detection")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL,
                "Fall Detection",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(ch)
        }
    }

    data class SensorRow(
        val ts: Long,
        val nano: Long,
        val type: String,
        val x: Double,
        val y: Double,
        val z: Double,
        val vm: Double
    )
}
