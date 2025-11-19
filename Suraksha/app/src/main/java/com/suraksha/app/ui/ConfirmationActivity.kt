package com.suraksha.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.suraksha.app.R

class ConfirmationActivity : Activity() {

    companion object {
        private const val TAG = "ConfirmationActivity"
    }

    private lateinit var countdown: TextView
    private lateinit var cancelBtn: Button

    private var timer: CountDownTimer? = null
    private val totalMs = 15000L // 15 sec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ConfirmationActivity created")
        setContentView(R.layout.activity_confirmation)

        val label = intent.getStringExtra("label") ?: "unknown"
        val conf = intent.getFloatExtra("confidence", 0f)
        val detectionType = intent.getStringExtra("detectionType") ?: "REAL_FALL"
        val pickupTime = intent.getIntExtra("pickupTime", -1)

        Log.w(TAG, "Detection: type=$detectionType, label=$label, confidence=${(conf*100).toInt()}%, pickupTime=${pickupTime}s")

        // Set title and message based on detection type
        val titleText: String
        val detailText: String

        when (detectionType) {
            "REAL_FALL" -> {
                titleText = "🚨 REAL FALL DETECTED!"
                detailText = "ML Model confirmed: $label (${(conf*100).toInt()}%)"
                Log.w(TAG, "🚨 ML CONFIRMED REAL FALL")
            }
            "PICKUP_CHECK" -> {
                titleText = "⚠️ ARE YOU OKAY?"
                detailText = "Phone picked up ${pickupTime}s after impact. Checking on you..."
                Log.w(TAG, "⚠️ PICKUP CHECK: User might have fallen and picked up phone")
            }
            else -> {
                titleText = "⚠️ FALL DETECTED"
                detailText = "$label (${(conf*100).toInt()}%)"
            }
        }

        findViewById<TextView>(R.id.titleTv).text = titleText
        findViewById<TextView>(R.id.detailTv).text = detailText

        countdown = findViewById(R.id.countdownTv)
        cancelBtn = findViewById(R.id.cancelBtn)

        cancelBtn.setOnClickListener {
            Log.i(TAG, "✅ User tapped CANCEL - they're okay, no SOS sent")
            timer?.cancel()
            finish()
        }

        timer = object : CountDownTimer(totalMs, 1000) {
            override fun onTick(ms: Long) {
                val secondsLeft = ms / 1000
                countdown.text = "${secondsLeft}s — Tap CANCEL if you're OK"
                if (secondsLeft <= 5) {
                    Log.w(TAG, "⚠️ SOS countdown: ${secondsLeft}s remaining...")
                }
            }

            override fun onFinish() {
                Log.w(TAG, "⏰ COUNTDOWN FINISHED - User did not cancel, sending SOS NOW!")
                sendSOS()
                finish()
            }
        }
        timer?.start()
        Log.w(TAG, "⏱️ 15-second countdown started - SOS will send unless user taps CANCEL")
    }

    private fun sendSOS() {
        Log.w(TAG, "📡 Broadcasting ACTION_TRIGGER_SOS intent...")
        val i = Intent("com.suraksha.app.ACTION_TRIGGER_SOS")
        sendBroadcast(i)
        Log.w(TAG, "✅ Broadcast sent successfully")
    }

    override fun onDestroy() {
        Log.d(TAG, "ConfirmationActivity destroyed")
        timer?.cancel()
        super.onDestroy()
    }
}
