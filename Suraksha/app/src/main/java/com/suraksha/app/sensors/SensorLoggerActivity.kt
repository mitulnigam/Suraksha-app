package com.suraksha.app.sensors

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.suraksha.app.R
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ArrayBlockingQueue
import kotlin.math.abs
import kotlin.math.sqrt

class SensorLoggerActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null
    private var gyro: Sensor? = null

    // HandlerThread ensures sensor callbacks on background thread
    private val sensorThread = HandlerThread("SensorThread")
    private lateinit var sensorHandler: Handler

    // config
    private val SAMPLE_RATE_HZ = 50 // ~50 Hz per sensor
    private val SENSOR_DELAY_US = (1_000_000L / SAMPLE_RATE_HZ).toInt() // microseconds approx

    // Pre/Post window configuration
    private val PREBUFFER_SECONDS = 3
    private val POST_SECONDS = 10

    // detection thresholds (safe defaults, tune later)
    private val FREEFALL_LOW_MS2 = 2.0 // near 0 m/s^2 indicates free-fall (weightless)
    private val FREEFALL_MIN_MS = 150L // need at least 150 ms near zero
    private val IMPACT_HIGH_MS2 = 25.0 // impact spike threshold (~>2.5g measured magnitude)
    private val IMPACT_MAX_WAIT_MS = 1200L // max time between free-fall and impact (~1.2s)

    // simple ring buffer for prebuffer (store raw samples from both ACC and GYR)
    private val PREBUFFER_CAPACITY = SAMPLE_RATE_HZ * PREBUFFER_SECONDS * 2 // ~3s * (50Hz ACC + 50Hz GYR) ≈ 300
    private val prebuffer = ArrayBlockingQueue<SensorRow>(PREBUFFER_CAPACITY)

    // State machine
    private enum class State { IDLE, FREEFALL, POST_RECORDING }
    private var state: State = State.IDLE
    private var freefallStartNano: Long? = null
    private var freefallDetectedNano: Long? = null
    private var postEndNano: Long = 0L

    // Detection mode
    private var isDetecting = false

    // Hold captured rows for current candidate window
    private var eventRows = mutableListOf<SensorRow>()

    // UI
    private lateinit var statusTv: TextView
    private lateinit var startBtn: Button
    private lateinit var stopBtn: Button
    private lateinit var labelInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_logger)

        statusTv = findViewById(R.id.statusTv)
        startBtn = findViewById(R.id.startBtn)
        stopBtn = findViewById(R.id.stopBtn)
        labelInput = findViewById(R.id.labelInput)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        sensorThread.start()
        sensorHandler = Handler(sensorThread.looper)

        startBtn.setOnClickListener { startDetecting() }
        stopBtn.setOnClickListener { stopDetecting() }

        startBtn.isEnabled = true
        stopBtn.isEnabled = false

        updateStatus("Ready. Listening disabled")
    }

    private fun updateStatus(s: String) {
        runOnUiThread { statusTv.text = s }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isDetecting) return
        val ts = System.currentTimeMillis()
        val nano = SystemClock.elapsedRealtimeNanos()
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val ax = event.values[0].toDouble()
                val ay = event.values[1].toDouble()
                val az = event.values[2].toDouble()
                val vm = sqrt(ax * ax + ay * ay + az * az)
                val row = SensorRow(ts, nano, "ACC", ax, ay, az, vm)
                // prebuffer maintenance
                if (!prebuffer.offer(row)) {
                    prebuffer.poll()
                    prebuffer.offer(row)
                }
                // state machine on accel
                handleAccel(vm, row)
                // if capturing post window, keep rows
                if (state == State.POST_RECORDING) eventRows.add(row)
            }
            Sensor.TYPE_GYROSCOPE -> {
                val gx = event.values[0].toDouble()
                val gy = event.values[1].toDouble()
                val gz = event.values[2].toDouble()
                val row = SensorRow(ts, nano, "GYR", gx, gy, gz, 0.0)
                if (!prebuffer.offer(row)) {
                    prebuffer.poll()
                    prebuffer.offer(row)
                }
                if (state == State.POST_RECORDING) eventRows.add(row)
            }
        }
        // check post window timeout
        if (isDetecting && state == State.POST_RECORDING && nano >= postEndNano) {
            finalizeCandidateAndSave()
        }
    }

    private fun handleAccel(vm: Double, row: SensorRow) {
        val now = row.nano
        when (state) {
            State.IDLE -> {
                if (vm < FREEFALL_LOW_MS2) {
                    val start = freefallStartNano
                    if (start == null) {
                        freefallStartNano = now
                    } else if ((now - start) / 1_000_000L >= FREEFALL_MIN_MS) {
                        // free-fall confirmed
                        state = State.FREEFALL
                        freefallDetectedNano = now
                        updateStatus("Free-fall detected. Awaiting impact…")
                    }
                } else {
                    freefallStartNano = null
                }
            }
            State.FREEFALL -> {
                val sinceFreefallMs = ((now - (freefallDetectedNano ?: now)) / 1_000_000L)
                if (vm > IMPACT_HIGH_MS2) {
                    // impact detected -> start post capture
                    val preSnapshot = prebuffer.toList() // 3s pre
                    eventRows = mutableListOf<SensorRow>().apply {
                        addAll(preSnapshot)
                        add(row)
                    }
                    postEndNano = now + POST_SECONDS * 1_000_000_000L
                    state = State.POST_RECORDING
                    updateStatus("Impact detected. Capturing ${POST_SECONDS}s post window…")
                } else if (sinceFreefallMs > IMPACT_MAX_WAIT_MS) {
                    // timeout, reset
                    state = State.IDLE
                    freefallStartNano = null
                    freefallDetectedNano = null
                    updateStatus("Free-fall timed out. Reset.")
                }
            }
            State.POST_RECORDING -> {
                // handled in onSensorChanged and timeout
            }
        }
    }

    private fun finalizeCandidateAndSave() {
        val rowsToSave = eventRows.toList()
        // compute picked-up heuristic in post window rows
        val pickedUp = inferPickedUp(rowsToSave)
        saveCandidateCsv(rowsToSave, pickedUp)
        // reset state
        state = State.IDLE
        freefallStartNano = null
        freefallDetectedNano = null
        postEndNano = 0L
        eventRows.clear()
        updateStatus("Saved candidate (pickedUp=$pickedUp). Listening…")
        Toast.makeText(this, "Saved candidate window", Toast.LENGTH_SHORT).show()
    }

    private fun inferPickedUp(rows: List<SensorRow>): Boolean {
        // Look only at post rows (after the impact row). Heuristic:
        // if within the post window we see sustained motion: either
        //  - gyro magnitude > 1.0 rad/s, or
        //  - |accel magnitude - 9.81| > 4.0 m/s^2
        val GRAVITY = 9.81
        var overMotionMs = 0L
        var lastNano: Long? = null
        for (r in rows) {
            if (r.type == "ACC") {
                val dev = abs(r.vm - GRAVITY)
                val moving = dev > 4.0
                val ln = lastNano
                if (moving && ln != null) overMotionMs += (r.nano - ln) / 1_000_000L
                lastNano = r.nano
            } else if (r.type == "GYR") {
                val gm = sqrt(r.x * r.x + r.y * r.y + r.z * r.z)
                val moving = gm > 1.0
                val ln = lastNano
                if (moving && ln != null) overMotionMs += (r.nano - ln) / 1_000_000L
                lastNano = r.nano
            }
        }
        return overMotionMs >= 300 // at least ~300 ms of strong motion
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* ignore */ }

    private fun startDetecting() {
        if (isDetecting) return
        isDetecting = true
        state = State.IDLE
        freefallStartNano = null
        freefallDetectedNano = null
        eventRows.clear()
        prebuffer.clear()
        // register sensors
        accel?.let { sensorManager.registerListener(this, it, SENSOR_DELAY_US, sensorHandler) }
        gyro?.let { sensorManager.registerListener(this, it, SENSOR_DELAY_US, sensorHandler) }
        startBtn.isEnabled = false
        stopBtn.isEnabled = true
        updateStatus("Detecting… Free-fall < ${FREEFALL_LOW_MS2} m/s², Impact > ${IMPACT_HIGH_MS2} m/s², window ≤ ${IMPACT_MAX_WAIT_MS} ms. Saving 3s pre + 10s post candidates.")
        Toast.makeText(this, "Detection started", Toast.LENGTH_SHORT).show()
    }

    private fun stopDetecting() {
        if (!isDetecting) return
        isDetecting = false
        sensorManager.unregisterListener(this)
        startBtn.isEnabled = true
        stopBtn.isEnabled = false
        // if a capture was in progress, finalize it early
        if (state == State.POST_RECORDING && eventRows.isNotEmpty()) {
            finalizeCandidateAndSave()
        }
        state = State.IDLE
        freefallStartNano = null
        freefallDetectedNano = null
        postEndNano = 0L
        eventRows.clear()
        updateStatus("Stopped. Ready.")
    }

    private fun saveCandidateCsv(rows: List<SensorRow>, pickedUp: Boolean) {
        try {
            val fmt = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            val fileName = "candidate_${fmt.format(System.currentTimeMillis())}.csv"
            val outDir = getExternalFilesDir("sensor_logs") ?: filesDir
            outDir.mkdirs()
            val file = File(outDir, fileName)
            val fw = FileWriter(file)
            // metadata header
            fw.append("phone,model,sample_hz,label,rows,picked_up\n")
            fw.append("${android.os.Build.MANUFACTURER},${android.os.Build.MODEL},$SAMPLE_RATE_HZ,${labelInput.text},${rows.size},$pickedUp\n\n")
            // column header
            fw.append("ts_ms,ts_nano,type,x,y,z,vm\n")
            for (r in rows) {
                fw.append("${r.ts},${r.nano},${r.type},${r.x},${r.y},${r.z},${r.vm}\n")
            }
            fw.flush()
            fw.close()
            runOnUiThread { Toast.makeText(this, "Saved: ${file.absolutePath}", Toast.LENGTH_LONG).show() }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread { Toast.makeText(this, "Failed to save CSV: ${e.message}", Toast.LENGTH_LONG).show() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        sensorThread.quitSafely()
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
