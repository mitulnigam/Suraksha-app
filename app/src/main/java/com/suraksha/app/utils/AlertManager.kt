package com.suraksha.app.utils

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import com.suraksha.app.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext


object AlertManager {

    @Volatile
    private var isAlertInProgress = false
    private var lastAlertTimestamp = 0L
    private const val ALERT_COOLDOWN_MS = 30000L

    
    @Suppress("DEPRECATION")
    suspend fun sendAlert(context: Context, location: Location?) {

        synchronized(this) {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastAlert = currentTime - lastAlertTimestamp

            if (isAlertInProgress) {
                Log.w("AlertManager", "âš ï¸ SOS TRIGGER IGNORED - Alert already in progress")
                return
            }

            if (timeSinceLastAlert < ALERT_COOLDOWN_MS) {
                val remainingCooldown = (ALERT_COOLDOWN_MS - timeSinceLastAlert) / 1000
                Log.w("AlertManager", "âš ï¸ SOS TRIGGER IGNORED - Cooldown active (${remainingCooldown}s remaining)")
                return
            }

            isAlertInProgress = true
            lastAlertTimestamp = currentTime
            Log.i("AlertManager", "ðŸ”’ SOS LOCK ACQUIRED - Starting alert process")
        }


        withContext(Dispatchers.IO) {
            try {
                Log.d("AlertManager", "=== STARTING SOS ALERT PROCESS ===")

                val database = AppDatabase.getDatabase(context.applicationContext)
                Log.d("AlertManager", "Database instance obtained")


                val contacts = database.contactDao().getAllContacts().first()
                Log.i("AlertManager", "Found ${contacts.size} contacts in database")

                if (contacts.isEmpty()) {
                    Log.e("AlertManager", "âŒ NO TRUSTED CONTACTS FOUND! Cannot send alert.")
                    Log.e("AlertManager", "Please add emergency contacts in the app first!")
                    return@withContext
                }

                contacts.forEachIndexed { index, contact ->
                    Log.d("AlertManager", "Contact ${index + 1}: ${contact.name} - ${contact.phoneNumber}")
                }

                val message = if (location != null) {
                    "HELP! This is an emergency alert from Suraksha. My current location is: https://maps.google.com/?q=${location.latitude},${location.longitude}"
                } else {
                    "HELP! This is an emergency alert from Suraksha. My location could not be determined, but I need help."
                }
                Log.d("AlertManager", "SOS Message: $message")



                val smsManager = context.getSystemService(SmsManager::class.java)

                if (smsManager == null) {
                    Log.e("AlertManager", "âŒ Failed to get SmsManager - SMS service not available")
                    return@withContext
                }
                Log.d("AlertManager", "SmsManager obtained successfully")

                var successCount = 0
                var failCount = 0

                contacts.forEach { contact ->
                    try {
                        Log.i("AlertManager", "Sending SMS to ${contact.name} at ${contact.phoneNumber}...")
                        smsManager.sendTextMessage(
                            contact.phoneNumber,
                            null,
                            message,
                            null,
                            null
                        )
                        successCount++
                        Log.w("AlertManager", "âœ… SMS SENT to ${contact.name} at ${contact.phoneNumber}")
                    } catch (e: Exception) {
                        failCount++
                        Log.e("AlertManager", "âŒ FAILED to send SMS to ${contact.name}: ${e.message}", e)

                    }
                }

                Log.w("AlertManager", "=== SOS ALERT COMPLETE: $successCount sent, $failCount failed ===")

                if (contacts.isNotEmpty()) {
                    val firstContact = contacts[0]
                    Log.i("AlertManager", "ðŸ“ž Making call to priority contact: ${firstContact.name} at ${firstContact.phoneNumber}")

                    try {
                        makeCall(context, firstContact.phoneNumber, firstContact.name)
                        Log.w("AlertManager", "âœ… CALL initiated to ${firstContact.name}")
                    } catch (e: Exception) {
                        Log.e("AlertManager", "âŒ FAILED to make call: ${e.message}", e)
                    }
                }

                try {
                    Log.d("AlertManager", "ðŸ” Checking disguise conditions:")
                    Log.d("AlertManager", "  - PIN set: ${PinManager.isPinSet(context)}")
                    Log.d("AlertManager", "  - Already disguised: ${PinManager.isAppDisguised(context)}")

                    if (PinManager.isPinSet(context) && !PinManager.isAppDisguised(context)) {
                        Log.w("AlertManager", "ðŸŽ­ Attempting to disguise icon...")
                        IconManager.disguiseIcon(context)
                        Log.w("AlertManager", "ðŸŽ­ App icon disguised as Calculator")
                    } else {
                        if (!PinManager.isPinSet(context)) {
                            Log.w("AlertManager", "âš ï¸ Icon disguise skipped: PIN not set. Please setup PIN in Settings first!")
                        } else {
                            Log.i("AlertManager", "â„¹ï¸ Icon disguise skipped: Already disguised")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AlertManager", "âŒ Failed to disguise icon: ${e.message}", e)
                    e.printStackTrace()
                }

                Log.w("AlertManager", "=== SOS ALERT PROCESS COMPLETE ===")
            } catch (e: Exception) {
                Log.e("AlertManager", "âŒ CRITICAL ERROR in sendAlert: ${e.message}", e)
                e.printStackTrace()
            } finally {

                synchronized(this@AlertManager) {
                    isAlertInProgress = false
                    Log.i("AlertManager", "ðŸ”“ SOS LOCK RELEASED - Ready for next trigger")
                }
            }
        }
    }

    
    private suspend fun makeCall(context: Context, phoneNumber: String, contactName: String) {
        withContext(Dispatchers.Main) {
            try {
                Log.d("AlertManager", "ðŸ“ž Initiating call to $phoneNumber...")

                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                context.startActivity(callIntent)
                Log.d("AlertManager", "ðŸ“ž Real call started to $contactName - will ring until answered")

            } catch (e: Exception) {
                Log.e("AlertManager", "âŒ Failed to make call: ${e.message}", e)
                throw e
            }
        }
    }
}