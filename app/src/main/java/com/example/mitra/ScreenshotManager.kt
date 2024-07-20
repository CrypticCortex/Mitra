package com.example.mitra

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.ScreenshotResult
import android.accessibilityservice.AccessibilityService.TakeScreenshotCallback
import android.graphics.Bitmap
import android.hardware.HardwareBuffer
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor

class ScreenshotManager(private val service: AccessibilityService) {

    private val overlayManager = OverlayManager(service)

    fun takeScreenshot() {
        Log.d("ScreenshotManager", "Taking screenshot")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val executor = Executor { command -> Handler(Looper.getMainLooper()).post(command) }
            service.takeScreenshot(Display.DEFAULT_DISPLAY, executor, object : TakeScreenshotCallback {
                override fun onSuccess(screenshot: ScreenshotResult) {
                    Log.d("ScreenshotManager", "Screenshot taken successfully")
                    val hardwareBuffer: HardwareBuffer? = screenshot.hardwareBuffer
                    val colorSpace = screenshot.colorSpace
                    if (hardwareBuffer != null && colorSpace != null) {
                        val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)
                        if (bitmap != null) {
                            Log.d("ScreenshotManager", "Saving bitmap")
                            saveBitmap(bitmap)
                            hardwareBuffer.close()
                        } else {
                            Log.e("ScreenshotManager", "Failed to wrap hardware buffer into bitmap")
                        }
                    } else {
                        Log.e("ScreenshotManager", "Hardware buffer or color space is null")
                    }
                }

                override fun onFailure(errorCode: Int) {
                    Log.e("ScreenshotManager", "Screenshot failed with error code: $errorCode")
                }
            })
        } else {
            Toast.makeText(service, "Screenshot requires API level 30 or higher", Toast.LENGTH_SHORT).show()
            Log.e("ScreenshotManager", "API level is lower than 30")
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        Log.d("ScreenshotManager", "Saving bitmap to file")
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Mitra")
        if (!directory.exists()) {
            Log.d("ScreenshotManager", "Creating directory: ${directory.absolutePath}")
            directory.mkdirs()
        }
        val filePath = "${directory.absolutePath}/screenshot_${System.currentTimeMillis()}.png"
        val imageFile = File(filePath)
        try {
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Toast.makeText(service, "Screenshot saved: $filePath", Toast.LENGTH_SHORT).show()
            Log.d("ScreenshotManager", "Screenshot saved: $filePath")

            Handler(Looper.getMainLooper()).post {
                overlayManager.showGradientOverlay()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(service, "Failed to save screenshot", Toast.LENGTH_SHORT).show()
            Log.e("ScreenshotManager", "Failed to save screenshot: ${e.message}")
        }
    }
}
