package com.example.lammoire

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import android.view.Window
import android.view.WindowManager

class startMenu : Fragment(R.layout.fragment_start_menu) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start_menu, container, false)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        changeStatusBarColor("#B68730")

        btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_startMenu_to_registerpage)
        }

        btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_startMenu_to_loginpage)
        }

        return view
    }

    private fun changeStatusBarColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = requireActivity().window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = android.graphics.Color.parseColor(color)
        }
    }
}