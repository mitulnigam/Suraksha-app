package com.suraksha.app.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class FallInferenceManager(private val context: Context) {

    companion object {
        private const val TAG = "FallInferenceManager"

        const val SEQ_LEN = 800
        const val CHANNELS = 3
    }

    private var interpreter: Interpreter = loadInterpreter()
    private val labels: List<String> = loadLabels()

    private fun loadInterpreter(): Interpreter {
        val buffer = loadModelFile("model.tflite")
        return Interpreter(buffer, Interpreter.Options().apply { setNumThreads(4) })
    }

    private fun loadModelFile(fileName: String): MappedByteBuffer {
        val fd = context.assets.openFd(fileName)
        val inputStream = FileInputStream(fd.fileDescriptor)
        val channel = inputStream.channel
        return channel.map(
            FileChannel.MapMode.READ_ONLY,
            fd.startOffset,
            fd.declaredLength
        )
    }

    private fun loadLabels(): List<String> {
        return context.assets.open("labels.txt")
            .bufferedReader().useLines { it.toList() }
    }

    fun runInference(accWindow: List<Triple<Float, Float, Float>>): Pair<String, Float> {
        try {
            val input = Array(1) { Array(SEQ_LEN) { FloatArray(CHANNELS) } }

            val start = if (accWindow.size >= SEQ_LEN) accWindow.size - SEQ_LEN else 0
            val seq = accWindow.subList(start, accWindow.size)

            val G = 9.81f

            for (i in seq.indices) {
                val (x, y, z) = seq[i]
                input[0][i][0] = x / G
                input[0][i][1] = y / G
                input[0][i][2] = z / G
            }

            val outputTensor = interpreter.getOutputTensor(0)
            val outputShape = outputTensor.shape()
            Log.d(TAG, "Model output shape: ${outputShape.joinToString("x")}")

            val output: FloatArray = when {

                outputShape.size == 2 && outputShape[1] == 1 -> {
                    val rawOutput = Array(outputShape[0]) { FloatArray(1) }
                    interpreter.run(input, rawOutput)

                    val avgPrediction = rawOutput.map { it[0] }.average().toFloat()

                    val isFall = avgPrediction > 0.5f

                    val probs = FloatArray(labels.size) { 0f }
                    if (isFall) {

                        val fallIdx = labels.indexOfFirst { it.contains("fall", ignoreCase = true) }
                        if (fallIdx >= 0) probs[fallIdx] = avgPrediction
                    } else {

                        val noFallIdx = labels.indexOfFirst { it == "no_fall" || it == "drop_left" }
                        if (noFallIdx >= 0) probs[noFallIdx] = 1f - avgPrediction
                    }
                    probs
                }

                outputShape.size == 2 && outputShape[1] == labels.size -> {
                    val output = Array(1) { FloatArray(labels.size) }
                    interpreter.run(input, output)
                    output[0]
                }

                outputShape.size == 1 && outputShape[0] == labels.size -> {
                    val output = FloatArray(labels.size)
                    interpreter.run(input, output)
                    output
                }

                else -> {
                    Log.e(TAG, "âŒ Unsupported output shape: ${outputShape.joinToString("x")}")
                    Log.e(TAG, "Expected either [342, 1] or [1, ${labels.size}] or [${labels.size}]")

                    val probs = FloatArray(labels.size) { 0f }
                    val dropIdx = labels.indexOf("drop_left")
                    if (dropIdx >= 0) probs[dropIdx] = 0.3f
                    probs
                }
            }

            val maxIdx = output.indices.maxByOrNull { output[it] } ?: 0
            val topLabel = if (maxIdx < labels.size) labels[maxIdx] else "unknown"
            val topConf = output[maxIdx]

            val sortedPredictions = output.indices
                .sortedByDescending { output[it] }
                .take(3)
                .filter { it < labels.size }
                .map { "${labels[it]}: ${(output[it] * 100).toInt()}%" }
                .joinToString(", ")

            Log.i(TAG, "ML Inference complete:")
            Log.i(TAG, "  Top prediction: $topLabel (${(topConf*100).toInt()}%)")
            Log.i(TAG, "  All top 3: $sortedPredictions")
            Log.i(TAG, "  Sequence length: ${seq.size}/$SEQ_LEN samples")

            return Pair(topLabel, topConf)

        } catch (e: Exception) {
            Log.e(TAG, "âŒ Inference failed: ${e.message}", e)

            return Pair("drop_left", 0.3f)
        }
    }

    fun close() {
        interpreter.close()
    }
}
