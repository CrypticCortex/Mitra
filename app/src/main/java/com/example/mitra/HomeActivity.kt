package com.example.mitra

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide() // Hide the ActionBar
    }

    override fun onBackPressed() {
        // Finish the activity and move the task to the back.
        moveTaskToBack(true)
    }
}
