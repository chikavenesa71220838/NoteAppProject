package com.example.lammoire

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class startMenu : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start_menu, container, false)

//        val auth = FirebaseAuth.getInstance()
//        val logged = auth.currentUser
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)

//
//        if (logged != null) {
//            val intent = Intent(requireContext(), mainMenu::class.java)
//            startActivity(intent)
//        }


        btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_startMenu_to_registerpage)
        }

        btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_startMenu_to_loginpage)
        }

        return view
    }
}