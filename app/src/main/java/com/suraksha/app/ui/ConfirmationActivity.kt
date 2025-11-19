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
    private val totalMs = 15000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ConfirmationActivity created")
        setContentView(R.layout.activity_confirmation)

        val label = intent.getStringExtra("label") ?: "unknown"
        val conf = intent.getFloatExtra("confidence", 0f)
        val detectionType = intent.getStringExtra("detectionType") ?: "REAL_FALL"
        val pickupTime = intent.getIntExtra("pickupTime", -1)

        Log.w(TAG, "Detection: type=$detectionType, label=$label, confidence=${(conf*100).toInt()}%, pickupTime=${pickupTime}s")

        val iconTv = findViewById<TextView>(R.id.iconTv)
        val titleText: String
        val detailText: String

        when (detectionType) {
            "REAL_FALL" -> {
                iconTv.text = "ðŸš¨"
                titleText = "REAL FALL DETECTED!"
                detailText = "ML Model confirmed: $label (${(conf*100).toInt()}%)"
                Log.w(TAG, "ðŸš¨ ML CONFIRMED REAL FALL")
            }
            "PICKUP_CHECK" -> {
                iconTv.text = "âš ï¸"
                titleText = "ARE YOU OKAY?"
                detailText = "Phone picked up ${pickupTime}s after impact.\nChecking on you..."
                Log.w(TAG, "âš ï¸ PICKUP CHECK: User might have fallen and picked up phone")
            }
            else -> {
                iconTv.text = "âš ï¸"
                titleText = "FALL DETECTED"
                detailText = "$label (${(conf*100).toInt()}%)"
            }
        }

        findViewById<TextView>(R.id.titleTv).text = titleText
        findViewById<TextView>(R.id.detailTv).text = detailText

        countdown = findViewById(R.id.countdownTv)
        cancelBtn = findViewById(R.id.cancelBtn)

        cancelBtn.setOnClickListener {
            Log.i(TAG, "âœ… User tapped CANCEL - they're okay, no SOS sent")
            timer?.cancel()
            finish()
        }

        timer = object : CountDownTimer(totalMs, 1000) {
            override fun onTick(ms: Long) {
                val secondsLeft = ms / 1000
                countdown.text = "$secondsLeft"
                if (secondsLeft <= 5) {
                    Log.w(TAG, "âš ï¸ SOS countdown: ${secondsLeft}s remaining...")
                }
            }

            override fun onFinish() {
                Log.w(TAG, "â° COUNTDOWN FINISHED - User did not cancel, sending SOS NOW!")
                sendSOS()
                finish()
            }
        }
        timer?.start()
        Log.w(TAG, "â±ï¸ 15-second countdown started - SOS will send unless user taps CANCEL")
    }

    private fun sendSOS() {
        try {
            Log.w(TAG, "ðŸ“¡ Broadcasting ACTION_TRIGGER_SOS intent...")
            val i = Intent("com.suraksha.app.ACTION_TRIGGER_SOS")
            i.setPackage(packageName)
            sendBroadcast(i)
            Log.w(TAG, "âœ… Broadcast sent successfully to package: $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to send broadcast: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "ConfirmationActivity destroyed")
        timer?.cancel()
        super.onDestroy()
    }
}
