package com.example.mitra

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

private const val REQUEST_OVERLAY_PERMISSIONS_CODE = 1

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonRequestAccessibility = findViewById<Button>(R.id.request_permission_button)
        buttonRequestAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if the overlay permission has been granted
        if (Settings.canDrawOverlays(this) && ScreenshotAccessibilityService.isServiceRunning(this)) {
            ScreenshotAccessibilityService.instance?.showOverlayButton()
        }
    }
}
