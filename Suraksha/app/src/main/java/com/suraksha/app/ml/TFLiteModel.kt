package com.suraksha.app.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * TensorFlow Lite model wrapper for fall detection inference.
 *
 * Expected model input shape: [1, SEQ_LEN, 3]
 * - SEQ_LEN: Number of timesteps (e.g., 800 samples)
 * - 3: X, Y, Z accelerometer channels
 *
 * Expected model output shape: [1, 10]
 * - 10 classes corresponding to labels.txt
 *
 * Usage example:
 * ```
 * val model = TFLiteModel(context)
 *
 * // Prepare input data [1, 800, 3]
 * val input = Array(1) { Array(800) { FloatArray(3) } }
 *
 * // Fill with sensor data (normalized)
 * for (i in 0 until 800) {
 *     input[0][i][0] = accelX[i] / 9.81f  // Normalize to g-units
 *     input[0][i][1] = accelY[i] / 9.81f
 *     input[0][i][2] = accelZ[i] / 9.81f
 * }
 *
 * // Run inference
 * val probabilities = model.predict(input)
 *
 * // Get top prediction
 * val maxIdx = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
 * val confidence = probabilities[maxIdx]
 * Log.d("ML", "Prediction: class=$maxIdx confidence=${confidence * 100}%")
 *
 * // Clean up
 * model.close()
 * ```
 */
class TFLiteModel(private val context: Context) {

    private var interpreter: Interpreter? = null

    init {
        interpreter = Interpreter(loadModel("model.tflite"))
    }

    private fun loadModel(modelName: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.declaredLength
        )
    }

    /**
     * Run inference on input sensor data.
     *
     * @param input Array of shape [1, SEQ_LEN, 3] containing normalized accelerometer data
     * @return FloatArray of size 10 containing class probabilities
     *
     * Example:
     * ```
     * val result = model.predict(input)
     * val maxIdx = result.indices.maxByOrNull { result[it] } ?: 0
     * val confidence = result[maxIdx]
     * Log.d("ML", "Predicted class: $maxIdx with confidence: ${confidence * 100}%")
     * ```
     */
    fun predict(input: Array<Array<FloatArray>>): FloatArray {
        val output = Array(1) { FloatArray(10) }   // 10 classes (matches labels.txt)
        interpreter?.run(input, output)
        return output[0]
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

