package com.example.mitra

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton

class OverlayManager(private val context: Context) {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    fun showGradientOverlay() {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try {
            overlayView = LayoutInflater.from(context).inflate(R.layout.gradient_overlay_with_close, null)
            Log.d("OverlayManager", "Overlay inflated successfully")
        } catch (e: Exception) {
            Log.e("OverlayManager", "Error inflating overlay", e)
        }
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(context).inflate(R.layout.gradient_overlay_with_close, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
        }

        val closeButton = overlayView.findViewById<ImageButton>(R.id.button_close)
        closeButton.setOnClickListener {
            removeGradientOverlay()
        }

        windowManager.addView(overlayView, params)
    }

    fun removeGradientOverlay() {
        if (::overlayView.isInitialized && overlayView.isAttachedToWindow) {
            windowManager.removeView(overlayView)
        }
    }
}
