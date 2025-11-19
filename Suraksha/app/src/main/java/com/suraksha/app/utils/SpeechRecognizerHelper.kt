package com.suraksha.app.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * A helper class to manage the Android SpeechRecognizer for continuous
 * background keyword detection.
 */
class SpeechRecognizerHelper(
    private val context: Context,
    private val onKeywordDetected: () -> Unit
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var keyword = "HELP ME" // Default keyword
    private var lastKeywordDetectionTime: Long = 0
    private val KEYWORD_COOLDOWN_MS = 2000L // 2 seconds cooldown to prevent duplicate triggers

    companion object {
        private const val TAG = "SpeechRecognizerHelper"
    }

    // Creates and configures the SpeechRecognizer
    private fun getSpeechRecognizer(): SpeechRecognizer? {
        // Stop and destroy the old one if it exists
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()

        // Check if SpeechRecognizer is available
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "SpeechRecognizer is not available on this device.")
            return null
        }

        // Create a new one
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(this)
        return recognizer
    }
    
    // Creates the intent for speech recognition
    private fun createSpeechIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // Get results as they speak
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            // Keep listening continuously
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, Long.MAX_VALUE)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, Long.MAX_VALUE)
        }
    }

    // Call this from the service to start the whole process
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

    // Call this from the service to stop the whole process
    fun stopListening() {
        if (!isListening) return
        Log.d(TAG, "Stopping listener.")
        isListening = false
        speechRecognizer?.stopListening()
    }

    // Call this when the service is destroyed
    fun destroy() {
        isListening = false
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    // --- RecognitionListener Callbacks ---

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

            // Check if the partial result contains our keyword
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
        // Also check final results for keyword detection
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (matches != null && matches.isNotEmpty()) {
            val spokenText = matches[0].uppercase()
            Log.d(TAG, "Final result: $spokenText")

            // Check if the final result contains our keyword
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
        
        // Restart listening if we are still supposed to be active
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

    // This is the most important part:
    // When the listener errors or finishes, we restart it to listen continuously.
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

        // Don't restart on certain critical errors
        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            Log.e(TAG, "Microphone permission denied. Stopping listener.")
            isListening = false
            return
        }

        // If we are still supposed to be listening, restart the recognizer
        if (isListening) {
            Log.d(TAG, "Restarting listener after error...")
            // Add a small delay before restarting to avoid rapid restart loops
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
            }, 500) // 500ms delay
        }
    }

    override fun onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech")
        // Restart listening if we are still supposed to be active
        // Note: onResults will be called after this, so we'll restart there
        // But we can also restart here as a backup
        if (isListening) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isListening) {
                    speechRecognizer = getSpeechRecognizer()
                    if (speechRecognizer != null) {
                        speechRecognizer?.startListening(createSpeechIntent())
                        Log.d(TAG, "Listener restarted after end of speech")
                    }
                }
            }, 300) // Small delay before restart
        }
    }

    // --- Other unused callbacks ---
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}