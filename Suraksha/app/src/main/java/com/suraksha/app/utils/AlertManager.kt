package com.suraksha.app.utils

import android.content.Context
import android.location.Location
import android.telephony.SmsManager
import android.util.Log
import com.suraksha.app.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * A singleton object to handle the logic of sending the alert.
 * Using an 'object' means we can just call AlertManager.sendAlert()
 * from anywhere in the app, without needing to create an instance.
 */
object AlertManager {

    /**
     * Sends an SMS to all trusted contacts.
     * This is a "suspend" function, so it MUST be called from a Coroutine
     * (like in your HomeScreen's LaunchedEffect).
     */
    @Suppress("DEPRECATION") // Suppress lint warning for default SmsManager
    suspend fun sendAlert(context: Context, location: Location?) {

        // We use withContext(Dispatchers.IO) to run database and network
        // code on a safe background thread.
        withContext(Dispatchers.IO) {
            try {
                Log.d("AlertManager", "=== STARTING SOS ALERT PROCESS ===")

                // 1. Get the database
                val database = AppDatabase.getDatabase(context.applicationContext)
                Log.d("AlertManager", "Database instance obtained")

                // 2. Get all contacts from the database (one-time operation)
                // .first() gets the current list from the Flow
                val contacts = database.contactDao().getAllContacts().first()
                Log.i("AlertManager", "Found ${contacts.size} contacts in database")

                if (contacts.isEmpty()) {
                    Log.e("AlertManager", "❌ NO TRUSTED CONTACTS FOUND! Cannot send alert.")
                    Log.e("AlertManager", "Please add emergency contacts in the app first!")
                    return@withContext // Stop if there are no contacts
                }

                // Log all contacts
                contacts.forEachIndexed { index, contact ->
                    Log.d("AlertManager", "Contact ${index + 1}: ${contact.name} - ${contact.phoneNumber}")
                }

                // 3. Create the emergency message
                val message = if (location != null) {
                    "HELP! This is an emergency alert from Suraksha. My current location is: http://maps.google.com?q=${location.latitude},${location.longitude}"
                } else {
                    "HELP! This is an emergency alert from Suraksha. My location could not be determined, but I need help."
                }
                Log.d("AlertManager", "SOS Message: $message")

                // 4. Get the Android SMS service
                // Note: You must get the system service on the Main thread or from a Context
                // We are given 'context', so this is safe.
                val smsManager = context.getSystemService(SmsManager::class.java)

                if (smsManager == null) {
                    Log.e("AlertManager", "❌ Failed to get SmsManager - SMS service not available")
                    return@withContext
                }
                Log.d("AlertManager", "SmsManager obtained successfully")

                // 5. Loop through each contact and send the SMS
                var successCount = 0
                var failCount = 0

                contacts.forEach { contact ->
                    try {
                        Log.i("AlertManager", "Sending SMS to ${contact.name} at ${contact.phoneNumber}...")
                        smsManager.sendTextMessage(
                            contact.phoneNumber,
                            null, // scAddress (use default)
                            message,
                            null, // sentIntent (for checking if sent)
                            null  // deliveryIntent (for checking if delivered)
                        )
                        successCount++
                        Log.w("AlertManager", "✅ SMS SENT to ${contact.name} at ${contact.phoneNumber}")
                    } catch (e: Exception) {
                        failCount++
                        Log.e("AlertManager", "❌ FAILED to send SMS to ${contact.name}: ${e.message}", e)
                        // Continue to the next contact
                    }
                }

                Log.w("AlertManager", "=== SOS ALERT COMPLETE: $successCount sent, $failCount failed ===")
            } catch (e: Exception) {
                Log.e("AlertManager", "❌ CRITICAL ERROR in sendAlert: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
}