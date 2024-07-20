package com.example.mitra

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.mitra.R

class NextActivity : AppCompatActivity() {

    private lateinit var requestPermissionIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next)
        supportActionBar?.hide() // Hide the ActionBar

        requestPermissionIcon = findViewById(R.id.request_permission_icon)
        requestPermissionIcon.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if the overlay permission has been granted
        if (Settings.canDrawOverlays(this) && ScreenshotAccessibilityService.isServiceRunning(this)) {
            ScreenshotAccessibilityService.instance?.showOverlayButton()
            setPermissionsGranted()
            navigateToHome()
        } else {
            requestPermissionIcon.alpha = 0.0f
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setPermissionsGranted() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit().putBoolean("permissions_granted", true).apply()
    }
}
