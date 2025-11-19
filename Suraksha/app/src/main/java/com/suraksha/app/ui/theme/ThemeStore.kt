package com.suraksha.app.ui.theme

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object ThemeStore {
    private const val PREFS = "SurakshaSettings"
    private const val KEY_DARK = "DARK_MODE"

    val isDark = mutableStateOf(true)

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        isDark.value = prefs.getBoolean(KEY_DARK, true)
    }

    fun setDark(context: Context, dark: Boolean) {
        isDark.value = dark
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK, dark)
            .apply()
    }
}
