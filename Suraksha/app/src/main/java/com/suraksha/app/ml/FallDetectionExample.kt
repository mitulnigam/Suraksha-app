package com.suraksha.app.ml

import android.content.Context
import android.util.Log
import com.suraksha.app.services.FallDetectorService

/**
 * Example helper class demonstrating how to use TFLiteModel for fall detection.
 *
 * This shows the complete workflow from collecting sensor data to running inference.
 */
class FallDetectionExample(private val context: Context) {

    companion object {
        private const val TAG = "FallDetectionExample"
        private const val SEQ_LEN = 800  // Must match your model's expected input length

        // Labels corresponding to model output indices (from labels.txt)
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

    /**
     * Run inference on sensor data collected from FallDetectorService.
     *
     * @param sensorRows List of sensor readings from FallDetectorService
     * @return Pair of (predicted label, confidence)
     */
    fun classifyFallEvent(sensorRows: List<FallDetectorService.SensorRow>): Pair<String, Float> {
        // Step 1: Filter only accelerometer data
        val accData = sensorRows.filter { it.type == "ACC" }

        if (accData.isEmpty()) {
            Log.w(TAG, "No accelerometer data available")
            return Pair("UNKNOWN", 0f)
        }

        // Step 2: Prepare input array [1, SEQ_LEN, 3]
        val input = Array(1) { Array(SEQ_LEN) { FloatArray(3) } }

        // Step 3: Fill with sensor data (take last SEQ_LEN samples if longer, pad if shorter)
        val startIdx = if (accData.size > SEQ_LEN) accData.size - SEQ_LEN else 0
        val dataToUse = accData.subList(startIdx, accData.size)

        for (i in dataToUse.indices) {
            val row = dataToUse[i]
            // Normalize to g-units (divide by 9.81 m/s²)
            input[0][i][0] = (row.x / 9.81).toFloat()
            input[0][i][1] = (row.y / 9.81).toFloat()
            input[0][i][2] = (row.z / 9.81).toFloat()
        }
        // Remaining positions are left as zeros (padding)

        // Step 4: Run inference
        val probabilities = model.predict(input)

        // Step 5: Find top prediction
        val maxIdx = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        val confidence = probabilities[maxIdx]

        // Step 6: Get label name
        val label = if (maxIdx < LABELS.size) LABELS[maxIdx] else "UNKNOWN_$maxIdx"

        Log.d(TAG, "Prediction: $label (${(confidence * 100).toInt()}%)")

        // Log top 3 predictions for debugging
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

    /**
     * Example with dummy data (for testing).
     */
    fun runDummyInference() {
        Log.d(TAG, "Running dummy inference...")

        // Create dummy input [1, 800, 3]
        val input = Array(1) { Array(SEQ_LEN) { FloatArray(3) } }

        // Fill with sample data (simulating normalized accelerometer readings)
        for (i in 0 until SEQ_LEN) {
            input[0][i][0] = 0.1f  // x-axis (in g-units)
            input[0][i][1] = 0.0f  // y-axis
            input[0][i][2] = 1.0f  // z-axis (gravity)
        }

        // Run prediction
        val result = model.predict(input)

        // Find top prediction
        val maxIdx = result.indices.maxByOrNull { result[it] } ?: 0
        val confidence = result[maxIdx]
        val label = if (maxIdx < LABELS.size) LABELS[maxIdx] else "UNKNOWN_$maxIdx"

        Log.d(TAG, "Dummy prediction: $label with confidence: ${(confidence * 100).toInt()}%")
    }

    /**
     * Clean up resources when done.
     */
    fun close() {
        model.close()
    }
}

