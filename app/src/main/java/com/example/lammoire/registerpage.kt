package com.example.lammoire

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class registerpage : Fragment(R.layout.fragment_registerpage) {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registerpage, container, false)

        val toolLog = view.findViewById<Toolbar>(R.id.toolbarRegis)
        toolLog.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        toolLog.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val registerButton = view.findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            register(view)
        }

        return view
    }

    private fun register(view: View) {
        val email = view.findViewById<EditText>(R.id.emailRegis).text.toString()
        val pass = view.findViewById<EditText>(R.id.passwordRegis).text.toString()
        val confirmPass = view.findViewById<EditText>(R.id.confirmPasswordRegis).text.toString()

        if (email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(context, "tolong isi dengan lengkap", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != confirmPass) {
            Toast.makeText(context, "password tidak cocok", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(requireContext(), loginpage::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "registrasi gagal, tolong coba lagi", Toast.LENGTH_SHORT).show()
                }
            }
    }
}