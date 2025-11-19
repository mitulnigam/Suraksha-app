package com.suraksha.app.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.suraksha.app.MainActivity
import com.suraksha.app.R
import com.suraksha.app.utils.AlertManager
import com.suraksha.app.utils.FallDetectionHelper
import com.suraksha.app.utils.HotwordDetector
import com.suraksha.app.utils.LocationManager
import com.suraksha.app.utils.ShakeDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SurakshaService : Service() {

    private var lastAlertTime: Long = 0

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var locationManager: LocationManager
    private lateinit var fallHelper: FallDetectionHelper
    private lateinit var shakeDetector: ShakeDetector
    private var hotwordDetector: HotwordDetector? = null

    private var isShakeListenerActive = false
    private var isVoiceListenerActive = false
    private var isFallListenerActive = false
    private var isAppInForeground = false

    private val microphoneHandler = Handler(Looper.getMainLooper())
    private var microphoneTimeoutRunnable: Runnable? = null

    companion object {
        private const val TAG = "SurakshaService"
        private const val COOLDOWN_PERIOD_MS = 10000
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "SurakshaServiceChannel"
        private const val MIC_TIMEOUT_MS = 180000L // 3 minutes

        const val ACTION_START_SHAKE_LISTENER = "com.suraksha.app.START_SHAKE"
        const val ACTION_STOP_SHAKE_LISTENER = "com.suraksha.app.STOP_SHAKE"
        const val ACTION_START_VOICE_LISTENER = "com.suraksha.app.START_VOICE"
        const val ACTION_STOP_VOICE_LISTENER = "com.suraksha.app.STOP_VOICE"
        const val ACTION_START_FALL_LISTENER = "com.suraksha.app.START_FALL"
        const val ACTION_STOP_FALL_LISTENER = "com.suraksha.app.STOP_FALL"
        const val ACTION_APP_FOREGROUND = "com.suraksha.app.APP_FOREGROUND"
        const val ACTION_APP_BACKGROUND = "com.suraksha.app.APP_BACKGROUND"
        const val ACTION_UPDATE_HOTWORD = "com.suraksha.app.UPDATE_HOTWORD"
        const val ACTION_SYNC_LISTENERS = "com.suraksha.app.SYNC_LISTENERS"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")

        locationManager = LocationManager()

        fallHelper = FallDetectionHelper(
            context = this,
            onFallDetected = {
                Log.i(TAG, "Fall detected! Triggering alert.")
                onMotionDetected() // Start 3-min mic window
                triggerAlert("Fall Detection")
            }
        )

        shakeDetector = ShakeDetector(
            context = this,
            onTripleShake = {
                Log.i(TAG, "Triple shake detected! Triggering alert.")
                onMotionDetected() // Start 3-min mic window
                triggerAlert("Shake Gesture")
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand, action: ${intent?.action}")

        startForegroundWithNotification()

        when (intent?.action) {
            ACTION_START_SHAKE_LISTENER -> startShakeListener()
            ACTION_STOP_SHAKE_LISTENER -> stopShakeListener()
            ACTION_START_VOICE_LISTENER -> startHotword()
            ACTION_STOP_VOICE_LISTENER -> stopHotword()
            ACTION_START_FALL_LISTENER -> startFallListener()
            ACTION_STOP_FALL_LISTENER -> stopFallListener()
            ACTION_APP_FOREGROUND -> { onAppForeground(); syncListenersFromPrefs() }
            ACTION_APP_BACKGROUND -> onAppBackground()
            ACTION_UPDATE_HOTWORD -> updateHotword()
            ACTION_SYNC_LISTENERS, null -> syncListenersFromPrefs()
        }

        return START_STICKY
    }

    private fun syncListenersFromPrefs() {
        val prefs = getSharedPreferences("SurakshaSettings", MODE_PRIVATE)
        val shakeEnabled = prefs.getBoolean("SHAKE_ENABLED", true)
        val voiceEnabled = prefs.getBoolean("VOICE_ENABLED", true)
        val fallEnabled = prefs.getBoolean("FALL_ENABLED", false)

        // Shake
        if (shakeEnabled) startShakeListener() else stopShakeListener()

        // Fall
        if (fallEnabled) startFallListener() else stopFallListener()

        // Voice / Hotword
        if (voiceEnabled) {
            if (!isVoiceListenerActive) startHotword() else updateHotword()
        } else {
            if (isVoiceListenerActive) stopHotword()
        }

        Log.d(TAG, "Sync complete. shake=$shakeEnabled voice=$voiceEnabled fall=$fallEnabled")
    }

    private fun startShakeListener() {
        if (isShakeListenerActive) return
        shakeDetector.startListening()
        isShakeListenerActive = true
        Log.d(TAG, "Shake listener STARTED.")
    }

    private fun stopShakeListener() {
        if (!isShakeListenerActive) return
        shakeDetector.stopListening()
        isShakeListenerActive = false
        Log.d(TAG, "Shake listener STOPPED.")
    }

    private fun startHotword() {
        if (isVoiceListenerActive) return

        val sharedPrefs = getSharedPreferences("SurakshaSettings", MODE_PRIVATE)
        val hotword = sharedPrefs.getString("HOTWORD", "help me") ?: "help me"

        hotwordDetector = HotwordDetector(
            context = this,
            hotword = hotword,
            onHotword = {
                Log.i(TAG, "Hotword detected! Triggering alert.")
                triggerAlert("Hotword")
            }
        )

        // Only start if app is in foreground OR in 3-min window after motion
        if (isAppInForeground || isMicrophoneWindowActive()) {
            hotwordDetector?.start()
        }

        isVoiceListenerActive = true
        Log.d(TAG, "Hotword detector initialized with word: '$hotword'")
    }

    private fun stopHotword() {
        if (!isVoiceListenerActive) return
        hotwordDetector?.stop()
        hotwordDetector = null
        isVoiceListenerActive = false
        cancelMicrophoneTimeout()
        Log.d(TAG, "Hotword detector STOPPED.")
    }

    private fun updateHotword() {
        if (!isVoiceListenerActive) return

        val sharedPrefs = getSharedPreferences("SurakshaSettings", MODE_PRIVATE)
        val newHotword = sharedPrefs.getString("HOTWORD", "help me") ?: "help me"

        hotwordDetector?.updateHotword(newHotword)

        // Restart detector with new hotword
        hotwordDetector?.stop()
        hotwordDetector = HotwordDetector(
            context = this,
            hotword = newHotword,
            onHotword = {
                Log.i(TAG, "Hotword detected! Triggering alert.")
                triggerAlert("Hotword")
            }
        )

        if (isAppInForeground || isMicrophoneWindowActive()) {
            hotwordDetector?.start()
        }

        Log.d(TAG, "Hotword updated to: '$newHotword'")
    }

    private fun startFallListener() {
        if (isFallListenerActive) return
        fallHelper.startListening()
        isFallListenerActive = true
        Log.d(TAG, "Fall listener STARTED.")
    }

    private fun stopFallListener() {
        if (!isFallListenerActive) return
        fallHelper.stopListening()
        isFallListenerActive = false
        Log.d(TAG, "Fall listener STOPPED.")
    }

    private fun onAppForeground() {
        isAppInForeground = true
        Log.d(TAG, "App in FOREGROUND - microphone always on")

        // Start hotword detector if voice is enabled
        if (isVoiceListenerActive && hotwordDetector != null) {
            hotwordDetector?.start()
        }

        cancelMicrophoneTimeout()
    }

    private fun onAppBackground() {
        isAppInForeground = false
        Log.d(TAG, "App in BACKGROUND - microphone off until motion detected")

        // Stop hotword detector when app goes to background
        hotwordDetector?.stop()
    }

    private fun onMotionDetected() {
        if (!isVoiceListenerActive || isAppInForeground) return

        Log.d(TAG, "Motion detected - starting 3-minute microphone window")

        // Cancel any existing timeout
        cancelMicrophoneTimeout()

        // Start hotword detector
        hotwordDetector?.start()

        // Schedule automatic stop after 3 minutes
        microphoneTimeoutRunnable = Runnable {
            Log.d(TAG, "3-minute microphone window expired")
            if (!isAppInForeground) {
                hotwordDetector?.stop()
            }
        }
        microphoneHandler.postDelayed(microphoneTimeoutRunnable!!, MIC_TIMEOUT_MS)
    }

    private fun isMicrophoneWindowActive(): Boolean {
        return microphoneTimeoutRunnable != null
    }

    private fun cancelMicrophoneTimeout() {
        microphoneTimeoutRunnable?.let {
            microphoneHandler.removeCallbacks(it)
            microphoneTimeoutRunnable = null
        }
    }

    private fun showTriggerPopup(triggerType: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, "SOS triggered ($triggerType)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerAlert(triggerType: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAlertTime > COOLDOWN_PERIOD_MS) {
            Log.i(TAG, "Alert triggered by: $triggerType. Cooldown over. Sending alert.")
            lastAlertTime = currentTime

            showTriggerPopup(triggerType)

            serviceScope.launch {
                locationManager.getCurrentLocation(this@SurakshaService) { location ->
                    serviceScope.launch {
                        Log.d(TAG, "Got location: $location. Sending alert...")
                        AlertManager.sendAlert(this@SurakshaService, location)
                    }
                }
            }
        } else {
            Log.w(TAG, "Alert triggered by $triggerType, but still in cooldown.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        stopShakeListener()
        stopHotword()
        stopFallListener()
        cancelMicrophoneTimeout()
        serviceJob.cancel()
    }

    private fun startForegroundWithNotification() {
        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "POST_NOTIFICATIONS permission not granted. Stopping service.")
                stopSelf()
                return
            }
        }

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Suraksha is Active")
            .setContentText("You are protected.")
            .setSmallIcon(R.drawable.ic_shield)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID, "Suraksha Service Channel", NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }


    override fun onBind(intent: Intent?): IBinder? = null
}