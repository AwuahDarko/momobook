package com.presetschool.momobookapp.service

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class AppLifecycleListener(private val service: ForegroundService) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onBackground() {
//        service.showOverlay()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeground() {
//        service.hideOverlay()
    }
}
