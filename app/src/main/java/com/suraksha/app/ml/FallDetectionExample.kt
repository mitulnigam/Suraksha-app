package com.suraksha.app.ml

import android.content.Context
import android.util.Log
import com.suraksha.app.services.FallDetectorService


class FallDetectionExample(private val context: Context) {

    companion object {
        private const val TAG = "FallDetectionExample"
        private const val SEQ_LEN = 800

        private val LABELS = listOf(
            "drop_pickup_1s",
            "drop_pickup_2s",
            "drop_pickup_3s",
            "drop_pickup_4s",
            "drop_pickup_5s",
            "drop_left",
            "sim_fall",
            "real_fall",
            "no_fall",
            "phone_drop"
        )
    }

    private val model = TFLiteModel(context)

    
    fun classifyFallEvent(sensorRows: List<FallDetectorService.SensorRow>): Pair<String, Float> {

        val accData = sensorRows.filter { it.type == "ACC" }

        if (accData.isEmpty()) {
            Log.w(TAG, "No accelerometer data available")
            return Pair("UNKNOWN", 0f)
        }

        val input = Array(1) { Array(SEQ_LEN) { FloatArray(3) } }

        val startIdx = if (accData.size > SEQ_LEN) accData.size - SEQ_LEN else 0
        val dataToUse = accData.subList(startIdx, accData.size)

        for (i in dataToUse.indices) {
            val row = dataToUse[i]

            input[0][i][0] = (row.x / 9.81).toFloat()
            input[0][i][1] = (row.y / 9.81).toFloat()
            input[0][i][2] = (row.z / 9.81).toFloat()
        }


        val probabilities = model.predict(input)

        val maxIdx = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        val confidence = probabilities[maxIdx]

        val label = if (maxIdx < LABELS.size) LABELS[maxIdx] else "UNKNOWN_$maxIdx"

        Log.d(TAG, "Prediction: $label (${(confidence * 100).toInt()}%)")

        val topPredictions = probabilities.indices
            .sortedByDescending { probabilities[it] }
            .take(3)
            .map { idx ->
                val lbl = if (idx < LABELS.size) LABELS[idx] else "UNKNOWN_$idx"
                "$lbl: ${(probabilities[idx] * 100).toInt()}%"
            }
        Log.d(TAG, "Top 3: ${topPredictions.joinToString(", ")}")

        return Pair(label, confidence)
    }

    
    fun runDummyInference() {
        Log.d(TAG, "Running dummy inference...")

        val input = Array(1) { Array(SEQ_LEN) { FloatArray(3) } }

        for (i in 0 until SEQ_LEN) {
            input[0][i][0] = 0.1f
            input[0][i][1] = 0.0f
            input[0][i][2] = 1.0f
        }

        val result = model.predict(input)

        val maxIdx = result.indices.maxByOrNull { result[it] } ?: 0
        val confidence = result[maxIdx]
        val label = if (maxIdx < LABELS.size) LABELS[maxIdx] else "UNKNOWN_$maxIdx"

        Log.d(TAG, "Dummy prediction: $label with confidence: ${(confidence * 100).toInt()}%")
    }

    
    fun close() {
        model.close()
    }
}

