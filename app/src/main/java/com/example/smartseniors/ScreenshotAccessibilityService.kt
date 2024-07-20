package com.example.smartseniors

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.HardwareBuffer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor

class ScreenshotAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ScreenshotAccessService"

        var instance: ScreenshotAccessibilityService? = null

    }

    override fun onServiceConnected() {
        Log.d(TAG, "Service connected")
        instance = this
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

    private fun showOverlayButton() {
        Log.d(TAG, "Showing overlay button")
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
        )
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(overlayButton, params)

        val buttonScreenshot = overlayButton.findViewById<ImageView>(R.id.buttonScreenshot)
        buttonScreenshot.setOnClickListener {
            Log.d(TAG, "Overlay button clicked")
            takeScreenshot()
        }
    }

    private fun takeScreenshot() {
        Log.d(TAG, "Taking screenshot")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val executor = Executor { command -> Handler(Looper.getMainLooper()).post(command) }
            takeScreenshot(Display.DEFAULT_DISPLAY, executor, object : TakeScreenshotCallback {
                override fun onSuccess(screenshot: ScreenshotResult) {
                    Log.d(TAG, "Screenshot taken successfully")
                    val hardwareBuffer: HardwareBuffer? = screenshot.hardwareBuffer
                    val colorSpace = screenshot.colorSpace
                    if (hardwareBuffer != null && colorSpace != null) {
                        val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)
                        if (bitmap != null) {
                            Log.d(TAG, "Saving bitmap")
                            saveBitmap(bitmap)
                            hardwareBuffer.close()
                        } else {
                            Log.e(TAG, "Failed to wrap hardware buffer into bitmap")
                        }
                    } else {
                        Log.e(TAG, "Hardware buffer or color space is null")
                    }
                }

                override fun onFailure(errorCode: Int) {
                    Log.e(TAG, "Screenshot failed with error code: $errorCode")
                }
            })
        } else {
            Toast.makeText(this, "Screenshot requires API level 30 or higher", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "API level is lower than 30")
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        Log.d(TAG, "Saving bitmap to file")
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Mitra"
        val file = File(directory)
        if (!file.exists()) {
            Log.d(TAG, "Creating directory: $directory")
            file.mkdirs()
        }
        val filePath = "$directory/screenshot_${System.currentTimeMillis()}.png"
        val imageFile = File(filePath)
        try {
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Toast.makeText(this, "Screenshot saved: $filePath", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Screenshot saved: $filePath")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save screenshot", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Failed to save screenshot: ${e.message}")
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
