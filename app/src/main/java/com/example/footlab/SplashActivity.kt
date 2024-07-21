package com.example.footlab

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start your main activity after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, LoginView::class.java)
            startActivity(intent)
            finish()
        }, 1000) // 1  seconds delay
    }
}
