package com.suraksha.app.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


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

    
    fun predict(input: Array<Array<FloatArray>>): FloatArray {
        val output = Array(1) { FloatArray(10) }
        interpreter?.run(input, output)
        return output[0]
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

