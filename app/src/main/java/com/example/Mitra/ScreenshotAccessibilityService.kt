package com.example.Mitra

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class ScreenshotAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ScreenshotAccessService"
        private const val REQUEST_CODE_SYSTEM_ALERT_WINDOW = 1

        var instance: ScreenshotAccessibilityService? = null

        fun isServiceRunning(context: Context): Boolean {
            val service = instance
            return service != null && service.packageName == context.packageName
        }
    }

    private lateinit var overlayButtonManager: OverlayButtonManager
    private lateinit var screenshotManager: ScreenshotManager

    override fun onServiceConnected() {
        Log.d(TAG, "Service connected")
        instance = this
        overlayButtonManager = OverlayButtonManager(this) {
            screenshotManager.takeScreenshot()
        }
        screenshotManager = ScreenshotManager(this)
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        } else {
            overlayButtonManager.showOverlayButton()
        }
        Toast.makeText(this, "Accessibility Service Connected", Toast.LENGTH_SHORT).show()
    }

    private fun requestOverlayPermission() {
        Log.d(TAG, "Requesting overlay permission")
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // No op
    }

    override fun onInterrupt() {
        // No op
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        instance = null
        super.onDestroy()
    }

    fun showOverlayButton() {
        overlayButtonManager.showOverlayButton()
    }
}
