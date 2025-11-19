package com.suraksha.app

import android.app.Application
import android.util.Log
import com.google.android.libraries.places.api.Places

class SurakshaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            val apiKey = getString(R.string.google_maps_key).trim()

            Log.d("SurakshaApp", "Initializing Places SDK with API key: ${apiKey.take(10)}...")

            if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY_HERE") {
                Log.e("SurakshaApp", "⚠️ Invalid API key. Places SDK not initialized.")
                Log.e("SurakshaApp", "Please set a valid Google Maps API key in build.gradle.kts")
                return
            }

            if (!Places.isInitialized()) {
                Places.initialize(applicationContext, apiKey)
                Log.d("SurakshaApp", "✅ Places SDK initialized successfully!")
            } else {
                Log.d("SurakshaApp", "Places SDK already initialized.")
            }
        } catch (e: Exception) {
            Log.e("SurakshaApp", "❌ Failed to initialize Places SDK: ${e.message}", e)
        }
    }
}