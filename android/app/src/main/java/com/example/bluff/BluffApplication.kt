package com.example.bluff

import android.app.Application
import com.example.bluff.di.AppContainer

class BluffApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
