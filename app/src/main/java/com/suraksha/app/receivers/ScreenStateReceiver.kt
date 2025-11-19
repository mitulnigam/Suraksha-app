package com.suraksha.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.suraksha.app.screens.GestureCaptureActivity

class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                val prefs = context.getSharedPreferences("SurakshaSettings", Context.MODE_PRIVATE)
                prefs.edit().putLong("LAST_SCREEN_OFF", System.currentTimeMillis()).apply()
            }
            Intent.ACTION_USER_PRESENT -> {
                val prefs = context.getSharedPreferences("SurakshaSettings", Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("SGESTURE_ENABLED", false)
                val lastOff = prefs.getLong("LAST_SCREEN_OFF", 0L)
                val justUnlocked = System.currentTimeMillis() - lastOff < 20000L
                if (enabled && justUnlocked) {
                    Log.d("ScreenStateReceiver", "Launching GestureCaptureActivity after unlock (S gesture window)")
                    val activityIntent = Intent(context, GestureCaptureActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(activityIntent)
                }
            }
        }
    }
}
