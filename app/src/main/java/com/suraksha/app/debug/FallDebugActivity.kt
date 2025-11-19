package com.suraksha.app.debug

import android.app.Activity
import android.content.Context
import android.hardware.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.widget.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.suraksha.app.R
import kotlin.math.sqrt

class FallDebugActivity : Activity(), SensorEventListener {

    private lateinit var chart: LineChart
    private lateinit var freeFallInput: EditText
    private lateinit var impactInput: EditText
    private lateinit var pickupInput: EditText
    private lateinit var saveBtn: Button

    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null
    private var gyro: Sensor? = null

    private val dataPoints = ArrayList<Entry>()
    private var index = 0f

    private val sensorThread = HandlerThread("DebugSensorThread")
    private lateinit var sensorHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fall_debug)

        chart = findViewById(R.id.lineChart)
        freeFallInput = findViewById(R.id.freeFallET)
        impactInput = findViewById(R.id.impactET)
        pickupInput = findViewById(R.id.pickupET)
        saveBtn = findViewById(R.id.saveBtn)

        val prefs = getSharedPreferences("fall_tuning", Context.MODE_PRIVATE)

        freeFallInput.setText(prefs.getFloat("freefall", 1.6f).toString())
        impactInput.setText(prefs.getFloat("impact", 16.0f).toString())
        pickupInput.setText(prefs.getFloat("pickup", 15.0f).toString())

        saveBtn.setOnClickListener {
            prefs.edit()
                .putFloat("freefall", freeFallInput.text.toString().toFloat())
                .putFloat("impact", impactInput.text.toString().toFloat())
                .putFloat("pickup", pickupInput.text.toString().toFloat())
                .apply()
            Toast.makeText(this, "Saved thresholds!", Toast.LENGTH_SHORT).show()
        }

        chart.description = Description().apply { text = "VM (Accelerometer)" }
        chart.data = LineData()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        sensorThread.start()
        sensorHandler = Handler(sensorThread.looper)

        accel?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME, sensorHandler) }
        gyro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME, sensorHandler) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val vm = sqrt(
                (event.values[0] * event.values[0] +
                        event.values[1] * event.values[1] +
                        event.values[2] * event.values[2]).toDouble()
            ).toFloat()

            runOnUiThread {
                val data = chart.data
                var set = data.getDataSetByIndex(0)
                if (set == null) {
                    set = LineDataSet(null, "VM")
                    set.lineWidth = 2f
                    set.setDrawCircles(false)
                    data.addDataSet(set)
                }

                data.addEntry(Entry(index, vm), 0)
                data.notifyDataChanged()
                chart.notifyDataSetChanged()
                chart.setVisibleXRangeMaximum(150f)
                chart.moveViewToX(index)

                index += 1f
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        sensorThread.quitSafely()
    }
}
