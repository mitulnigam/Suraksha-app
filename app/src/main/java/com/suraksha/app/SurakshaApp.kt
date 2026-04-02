package com.suraksha.app

import android.app.Application
import android.util.Log

class SurakshaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("SurakshaApp", "Application started")
    }
}