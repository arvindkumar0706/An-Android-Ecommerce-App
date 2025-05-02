package com.akj.popcart

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class PopApplication : Application() {
    override fun onCreate() {
        super.onCreate()


    }
}