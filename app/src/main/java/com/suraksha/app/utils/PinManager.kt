package com.suraksha.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


object PinManager {

    private const val TAG = "PinManager"
    private const val PREFS_NAME = "pin_prefs"
    private const val KEY_PIN = "user_pin"
    private const val KEY_PIN_SET = "pin_is_set"
    private const val KEY_APP_DISGUISED = "app_disguised"

    private fun getPrefs(context: Context): SharedPreferences {
        return try {

            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create encrypted prefs, using regular: ${e.message}")
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    
    fun isPinSet(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PIN_SET, false)
    }

    
    fun setupPin(context: Context, pin: String): Boolean {
        return try {
            if (pin.length != 4 || !pin.all { it.isDigit() }) {
                Log.e(TAG, "Invalid PIN format")
                return false
            }

            getPrefs(context).edit()
                .putString(KEY_PIN, pin)
                .putBoolean(KEY_PIN_SET, true)
                .apply()

            Log.i(TAG, "âœ… PIN setup successful")
            true
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to setup PIN: ${e.message}")
            false
        }
    }

    
    fun verifyPin(context: Context, enteredPin: String): Boolean {
        val storedPin = getPrefs(context).getString(KEY_PIN, null)
        return storedPin != null && storedPin == enteredPin
    }

    
    fun changePin(context: Context, oldPin: String, newPin: String): Boolean {
        return try {
            if (!verifyPin(context, oldPin)) {
                Log.e(TAG, "Old PIN incorrect")
                return false
            }

            if (newPin.length != 4 || !newPin.all { it.isDigit() }) {
                Log.e(TAG, "Invalid new PIN format")
                return false
            }

            getPrefs(context).edit()
                .putString(KEY_PIN, newPin)
                .apply()

            Log.i(TAG, "âœ… PIN changed successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to change PIN: ${e.message}")
            false
        }
    }

    
    fun isAppDisguised(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_APP_DISGUISED, false)
    }

    
    fun setAppDisguised(context: Context, disguised: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_APP_DISGUISED, disguised)
            .apply()
        Log.i(TAG, "App disguised state: $disguised")
    }

    
    fun resetPin(context: Context) {
        getPrefs(context).edit().clear().apply()
        Log.w(TAG, "âš ï¸ PIN reset - all data cleared")
    }
}

