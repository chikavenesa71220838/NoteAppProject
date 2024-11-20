package com.example.lammoire

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            setContentView(R.layout.fragment_startafterlogin)
//        } else {
//            setContentView(R.layout.fragment_start_menu)
//        }

        setContentView(R.layout.activity_main)
    }
}