package com.example.mitra

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.mitra.R

class LandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Hide the ActionBar

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isPermissionsGranted = sharedPreferences.getBoolean("permissions_granted", false)

        if (isPermissionsGranted) {
            // Show the landing screen for 3 seconds and then navigate to HomeActivity
            setContentView(R.layout.activity_landing)
            Handler(Looper.getMainLooper()).postDelayed({
                navigateToHome()
            }, 3000) // 3000 milliseconds = 3 seconds
        } else {
            setContentView(R.layout.activity_landing)
            val iconUp: ImageView = findViewById(R.id.icon_up)
            iconUp.setOnClickListener {
                val intent = Intent(this, NextActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
