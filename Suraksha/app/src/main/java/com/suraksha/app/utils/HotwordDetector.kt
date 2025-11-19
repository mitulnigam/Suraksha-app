package com.suraksha.app.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * Hotword detection using Android's built-in SpeechRecognizer (free, no API key needed).
 * Continuously listens for a configurable hotword and triggers callback when detected.
 */
class HotwordDetector(
    private val context: Context,
    private val hotword: String = "help me",
    private val onHotword: () -> Unit
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var lastDetectionTime: Long = 0
    private val detectionCooldownMs = 2000L // 2 seconds cooldown

    companion object {
        private const val TAG = "HotwordDetector"
    }

    fun start() {
        if (isListening) return
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition not available on this device")
            return
        }

        isListening = true
        startRecognition()
        Log.d(TAG, "Hotword detector STARTED. Listening for: '$hotword'")
    }

    fun stop() {
        isListening = false
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        Log.d(TAG, "Hotword detector STOPPED.")
    }

    fun updateHotword(newHotword: String) {
        Log.d(TAG, "Hotword updated to: '$newHotword'")
    }

    private fun startRecognition() {
        if (!isListening) return

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, Long.MAX_VALUE)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, Long.MAX_VALUE)
        }

        speechRecognizer?.startListening(intent)
    }

    private fun restartRecognition() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isListening) {
                startRecognition()
            }
        }, 500)
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.v(TAG, "Ready for speech")
    }

    override fun onBeginningOfSpeech() {
        Log.v(TAG, "Speech started")
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.forEach { result ->
            if (result.contains(hotword, ignoreCase = true)) {
                val now = System.currentTimeMillis()
                if (now - lastDetectionTime > detectionCooldownMs) {
                    lastDetectionTime = now
                    Log.i(TAG, "Hotword detected: '$result'")
                    onHotword()
                }
            }
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.forEach { result ->
            Log.d(TAG, "Final result: $result")
            if (result.contains(hotword, ignoreCase = true)) {
                val now = System.currentTimeMillis()
                if (now - lastDetectionTime > detectionCooldownMs) {
                    lastDetectionTime = now
                    Log.i(TAG, "Hotword detected in final results: '$result'")
                    onHotword()
                }
            }
        }
        restartRecognition()
    }

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_AUDIO -> "Audio error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            else -> "Unknown error ($error)"
        }
        Log.v(TAG, "Recognition error: $errorMessage")

        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            Log.e(TAG, "Microphone permission denied")
            isListening = false
            return
        }

        restartRecognition()
    }

    override fun onEndOfSpeech() {
        Log.v(TAG, "End of speech")
    }

    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
