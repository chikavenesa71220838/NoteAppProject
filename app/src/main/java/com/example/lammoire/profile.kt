package com.example.lammoire

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class profile : Fragment(R.layout.fragment_profile) {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val toolLog = view.findViewById<Toolbar>(R.id.toolbarProf)
        toolLog.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        toolLog.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        usernameEditText = view.findViewById(R.id.usernameProfile)
        emailEditText = view.findViewById(R.id.emailProfile)
        passwordEditText = view.findViewById(R.id.passwordEditText)

        val saveButton = view.findViewById<Button>(R.id.saveButton)

        fetchUserData()

        saveButton.setOnClickListener {
            saveUserData()
        }

        return view
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username")
                        val email = document.getString("email")
                        usernameEditText.setText(username)
                        emailEditText.setText(email)
                    }
                }
        }
    }

    private fun saveUserData() {
        val userId = auth.currentUser?.uid
        val user = auth.currentUser
        if (userId != null && user != null) {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (password.isEmpty()) {
                Toast.makeText(context, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return
            }

            val userUpdates = hashMapOf<String, Any>(
                "username" to username,
                "email" to email
            )

            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        db.collection("users").document(userId).update(userUpdates)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    user.verifyBeforeUpdateEmail(email)
                                        .addOnCompleteListener { verifyTask ->
                                            if (verifyTask.isSuccessful) {
                                                Toast.makeText(context, "Data dan email berhasil diperbarui, tolong periksa email untuk verifikasi", Toast.LENGTH_SHORT).show()
                                                val sharedPreferences = requireActivity().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                                                val edt = sharedPreferences.edit()
                                                edt.clear()
                                                edt.apply()
                                                FirebaseAuth.getInstance().signOut()
                                                findNavController().navigate(R.id.loginpage)
                                            } else {
                                                Toast.makeText(context, "Gagal mengirim email verifikasi", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "Gagal menyimpan data di Firestore", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Gagal autentikasi ulang", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
}