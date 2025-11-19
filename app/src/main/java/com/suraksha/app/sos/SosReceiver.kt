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
            Log.w("SosReceiver", "âš ï¸ SOS TRIGGER RECEIVED! Starting emergency alert...")
            Toast.makeText(context, "ðŸš¨ SENDING EMERGENCY SOS...", Toast.LENGTH_LONG).show()

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.i("SosReceiver", "Attempting to get location...")
                    val lm = LocationManager()

                    val locationJob = async {
                        var capturedLocation: android.location.Location? = null
                        lm.getCurrentLocation(context) { location ->
                            Log.i("SosReceiver", "Location received: lat=${location?.latitude}, lon=${location?.longitude}")
                            capturedLocation = location
                        }

                        delay(3000)
                        capturedLocation
                    }

                    val location = withTimeoutOrNull(5000) {
                        locationJob.await()
                    }

                    if (location == null) {
                        Log.w("SosReceiver", "Location timeout or null - sending SOS without location")
                    }

                    Log.w("SosReceiver", "âš ï¸ SENDING SOS TO CONTACTS NOW...")
                    AlertManager.sendAlert(context, location)
                    Log.w("SosReceiver", "âœ… SOS SENT SUCCESSFULLY!")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "âœ… SOS sent to emergency contacts", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("SosReceiver", "âŒ FAILED TO SEND SOS: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "âŒ Failed to send SOS: ${e.message}", Toast.LENGTH_LONG).show()
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
