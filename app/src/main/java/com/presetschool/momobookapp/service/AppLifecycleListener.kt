package com.presetschool.momobookapp.service

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class AppLifecycleListener(private val service: ForegroundService) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onBackground() {
        Log.d("OverlayDebug", "App moved to background, showing overlay")
        service.showOverlay()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeground() {
        Log.d("OverlayDebug", "App moved to foreground, hiding overlay")
        service.hideOverlay()
    }
}
