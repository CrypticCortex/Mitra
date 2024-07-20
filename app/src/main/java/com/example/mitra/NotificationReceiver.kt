package com.example.mitra

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val service = ScreenshotAccessibilityService.instance
            service?.showOverlayButton()
        }
    }
}
