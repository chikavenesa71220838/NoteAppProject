package com.example.lammoire

import android.annotation.SuppressLint
import android.os.Build
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.view.Window
import android.view.WindowManager

class registerpage : Fragment(R.layout.fragment_registerpage) {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        changeStatusBarColor("#B68730")
    }

    private fun changeStatusBarColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = requireActivity().window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = android.graphics.Color.parseColor(color)
        }
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

    @SuppressLint("CutPasteId")
    private fun register(view: View) {
        val uname = view.findViewById<EditText>(R.id.usernameRegis).text.toString()
        val email = view.findViewById<EditText>(R.id.emailRegis).text.toString()
        val pass = view.findViewById<EditText>(R.id.passwordRegis).text.toString()
        val confirmPass = view.findViewById<EditText>(R.id.confirmPasswordRegis).text.toString()

        if (uname.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(context, "Tolong isi dengan lengkap", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != confirmPass) {
            Toast.makeText(context, "Password tidak cocok", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val username = view.findViewById<EditText>(R.id.usernameRegis).text.toString()
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserToFirestore(userId, email, username)
                    } else {
                        Toast.makeText(context, "Gagal mendapatkan user ID", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Registrasi gagal, tolong coba lagi", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFirestore(userId: String, email: String, uname: String) {
        val user = hashMapOf(
            "username" to uname,
            "userId" to userId,
            "email" to email
        )

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(context, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_registerpage_to_loginpage)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
