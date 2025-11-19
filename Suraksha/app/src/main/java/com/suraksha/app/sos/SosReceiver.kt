package com.suraksha.app.sos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.suraksha.app.utils.AlertManager
import com.suraksha.app.utils.LocationManager
import kotlinx.coroutines.*

class SosReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("SosReceiver", "onReceive called, action=${intent?.action}")

        if (intent?.action == "com.suraksha.app.ACTION_TRIGGER_SOS") {
            Log.w("SosReceiver", "⚠️ SOS TRIGGER RECEIVED! Starting emergency alert...")
            Toast.makeText(context, "🚨 SENDING EMERGENCY SOS...", Toast.LENGTH_LONG).show()

            // Use goAsync to ensure broadcast receiver stays alive
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.i("SosReceiver", "Attempting to get location...")
                    val lm = LocationManager()

                    // Use withTimeoutOrNull to prevent hanging forever on location
                    val locationJob = async {
                        var capturedLocation: android.location.Location? = null
                        lm.getCurrentLocation(context) { location ->
                            Log.i("SosReceiver", "Location received: lat=${location?.latitude}, lon=${location?.longitude}")
                            capturedLocation = location
                        }
                        // Give it a moment to complete
                        delay(3000)
                        capturedLocation
                    }

                    val location = withTimeoutOrNull(5000) {
                        locationJob.await()
                    }

                    if (location == null) {
                        Log.w("SosReceiver", "Location timeout or null - sending SOS without location")
                    }

                    Log.w("SosReceiver", "⚠️ SENDING SOS TO CONTACTS NOW...")
                    AlertManager.sendAlert(context, location)
                    Log.w("SosReceiver", "✅ SOS SENT SUCCESSFULLY!")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ SOS sent to emergency contacts", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("SosReceiver", "❌ FAILED TO SEND SOS: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "❌ Failed to send SOS: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        } else {
            Log.d("SosReceiver", "Received intent with unexpected action: ${intent?.action}")
        }
    }
}
