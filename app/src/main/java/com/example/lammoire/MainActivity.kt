package com.example.lammoire

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
//    private lateinit var auth: FirebaseAuth

    // In your MainActivity.kt
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        val logged = sharedPreferences.getBoolean("is_logged_in", false)

        if (logged) {
            setContentView(R.layout.fragment_startafterlogin)
        } else {
            setContentView(R.layout.activity_main)
        }
    }
}