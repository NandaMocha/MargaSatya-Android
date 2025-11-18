package com.margasatya

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MargaSatyaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
