package com.suraksha.app.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log


class SpeechRecognizerHelper(
    private val context: Context,
    private val onKeywordDetected: () -> Unit
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var keyword = "HELP ME"
    private var lastKeywordDetectionTime: Long = 0
    private val KEYWORD_COOLDOWN_MS = 2000L

    companion object {
        private const val TAG = "SpeechRecognizerHelper"
    }

    private fun getSpeechRecognizer(): SpeechRecognizer? {

        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "SpeechRecognizer is not available on this device.")
            return null
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(this)
        return recognizer
    }

    private fun createSpeechIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)

            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, Long.MAX_VALUE)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, Long.MAX_VALUE)
        }
    }

    fun startListening(keyword: String) {
        if (isListening) {
            Log.d(TAG, "Already listening, updating keyword to: '${keyword.uppercase()}'")
            this.keyword = keyword.uppercase()
            return
        }
        
        this.keyword = keyword.uppercase()
        Log.d(TAG, "Starting to listen for keyword: '${this.keyword}'")
        
        speechRecognizer = getSpeechRecognizer()
        if (speechRecognizer == null) {
            Log.e(TAG, "Failed to create SpeechRecognizer. Cannot start listening.")
            isListening = false
            return
        }
        
        isListening = true
        val intent = createSpeechIntent()
        speechRecognizer?.startListening(intent)
        Log.d(TAG, "SpeechRecognizer started listening")
    }

    fun stopListening() {
        if (!isListening) return
        Log.d(TAG, "Stopping listener.")
        isListening = false
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        isListening = false
        speechRecognizer?.destroy()
        speechRecognizer = null
    }


    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "onReadyForSpeech")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech")
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (matches != null && matches.isNotEmpty()) {
            val spokenText = matches[0].uppercase()
            Log.d(TAG, "Partial result: $spokenText")

            if (spokenText.contains(keyword)) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastKeywordDetectionTime > KEYWORD_COOLDOWN_MS) {
                    lastKeywordDetectionTime = currentTime
                    Log.i(TAG, "!!! KEYWORD DETECTED IN PARTIAL RESULTS !!!")
                    onKeywordDetected()
                } else {
                    Log.d(TAG, "Keyword detected but still in cooldown, ignoring")
                }
            }
        }
    }

    override fun onResults(results: Bundle?) {

        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (matches != null && matches.isNotEmpty()) {
            val spokenText = matches[0].uppercase()
            Log.d(TAG, "Final result: $spokenText")

            if (spokenText.contains(keyword)) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastKeywordDetectionTime > KEYWORD_COOLDOWN_MS) {
                    lastKeywordDetectionTime = currentTime
                    Log.i(TAG, "!!! KEYWORD DETECTED IN FINAL RESULTS !!!")
                    onKeywordDetected()
                } else {
                    Log.d(TAG, "Keyword detected in final results but still in cooldown, ignoring")
                }
            }
        }

        if (isListening) {
            Log.d(TAG, "Restarting listener after final results...")
            speechRecognizer = getSpeechRecognizer()
            if (speechRecognizer != null) {
                speechRecognizer?.startListening(createSpeechIntent())
            } else {
                Log.e(TAG, "Failed to recreate SpeechRecognizer after final results")
                isListening = false
            }
        }
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
        Log.w(TAG, "onError: $errorMessage (code: $error)")

        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            Log.e(TAG, "Microphone permission denied. Stopping listener.")
            isListening = false
            return
        }

        if (isListening) {
            Log.d(TAG, "Restarting listener after error...")

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isListening) {
                    speechRecognizer = getSpeechRecognizer()
                    if (speechRecognizer != null) {
                        speechRecognizer?.startListening(createSpeechIntent())
                        Log.d(TAG, "Listener restarted after error")
                    } else {
                        Log.e(TAG, "Failed to recreate SpeechRecognizer after error")
                        isListening = false
                    }
                }
            }, 500)
        }
    }

    override fun onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech")



        if (isListening) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isListening) {
                    speechRecognizer = getSpeechRecognizer()
                    if (speechRecognizer != null) {
                        speechRecognizer?.startListening(createSpeechIntent())
                        Log.d(TAG, "Listener restarted after end of speech")
                    }
                }
            }, 300)
        }
    }

    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}