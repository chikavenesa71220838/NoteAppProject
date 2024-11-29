package com.example.lammoire

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.findNavController

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
}