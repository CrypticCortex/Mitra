package com.example.Mitra

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView

class OverlayButtonManager(private val context: Context, private val callback: () -> Unit) {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayButton: View
    private lateinit var params: WindowManager.LayoutParams
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    fun showOverlayButton() {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayButton = LayoutInflater.from(context).inflate(R.layout.overlay_button, null)
        params = WindowManager.LayoutParams(
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
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = context.resources.displayMetrics.heightPixels / 2
        }
        windowManager.addView(overlayButton, params)

        val buttonScreenshot = overlayButton.findViewById<ImageView>(R.id.buttonScreenshot)
        buttonScreenshot.setOnClickListener {
            callback()
        }

        overlayButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (event.rawY > context.resources.displayMetrics.heightPixels * 0.8) {
                        // Remove the overlay button and show notification
                        windowManager.removeView(overlayButton)
                        NotificationManager(context).showNotification()
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(overlayButton, params)
                    true
                }
                else -> false
            }
        }
    }

    fun removeOverlayButton() {
        windowManager.removeView(overlayButton)
    }
}
