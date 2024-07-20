package com.example.mitra

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
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

    private lateinit var screenshotManager: ScreenshotManager

    override fun onServiceConnected() {
        Log.d(TAG, "Service connected")
        instance = this
        screenshotManager = ScreenshotManager(this)
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        } else {
            showOverlayButton()
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

    fun showOverlayButton() {
        Log.d(TAG, "Trying to show overlay button")
        val overlayButton = LayoutInflater.from(this).inflate(R.layout.overlay_button, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        }
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try {
            windowManager.addView(overlayButton, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying overlay button", e)
        }

        val buttonScreenshot = overlayButton.findViewById<ImageView>(R.id.buttonScreenshot)
        buttonScreenshot.setOnClickListener {
            Log.d(TAG, "Overlay button clicked")
            screenshotManager.takeScreenshot()
        }
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
}
