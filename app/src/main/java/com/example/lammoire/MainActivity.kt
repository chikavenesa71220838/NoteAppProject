package com.example.lammoire

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.findNavController
import android.view.Window
import android.view.WindowManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Set the activity layout

        val sharedPreferences = getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        val logged = sharedPreferences.getBoolean("is_logged_in", false)


        val navController = findNavController(R.id.nav_host_fragment)
        if (logged) {
            navController.navigate(R.id.startafterlogin)
        } else {
            navController.navigate(R.id.startMenu)
        }

    }
    private fun changeStatusBarColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = android.graphics.Color.parseColor(color)
        }
    }
}