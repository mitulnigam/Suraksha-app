package com.suraksha.app.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log


object IconManager {

    private const val TAG = "IconManager"

    private const val MAIN_ACTIVITY = "com.suraksha.app.MainActivity"
    private const val CALCULATOR_ALIAS = "com.suraksha.app.CalculatorAlias"

    
    fun disguiseIcon(context: Context) {
        try {
            val packageManager = context.packageManager

            packageManager.setComponentEnabledSetting(
                ComponentName(context, MAIN_ACTIVITY),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )

            packageManager.setComponentEnabledSetting(
                ComponentName(context, CALCULATOR_ALIAS),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            PinManager.setAppDisguised(context, true)
            Log.i(TAG, "ðŸŽ­ App icon disguised as Calculator")

        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to disguise icon: ${e.message}", e)
        }
    }

    
    fun revealIcon(context: Context) {
        try {
            val packageManager = context.packageManager

            packageManager.setComponentEnabledSetting(
                ComponentName(context, MAIN_ACTIVITY),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            packageManager.setComponentEnabledSetting(
                ComponentName(context, CALCULATOR_ALIAS),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )

            PinManager.setAppDisguised(context, false)
            Log.i(TAG, "âœ… App icon revealed (back to original)")

        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to reveal icon: ${e.message}", e)
        }
    }

    
    fun isIconDisguised(context: Context): Boolean {
        return PinManager.isAppDisguised(context)
    }
}

