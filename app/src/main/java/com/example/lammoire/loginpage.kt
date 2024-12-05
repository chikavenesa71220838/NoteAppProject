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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.view.Window
import android.view.WindowManager
import java.util.regex.Pattern

class loginpage : Fragment(R.layout.fragment_loginpage) {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    var backButtonTime: Long = 0

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
        val view = inflater.inflate(R.layout.fragment_loginpage, container, false)

        val toolLog = view.findViewById<Toolbar>(R.id.toolbarLogin)
        toolLog.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        toolLog.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_loginpage_to_startMenu)
        }

        val loginButton = view.findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            login(view)
        }

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backButtonTime + 3000 > System.currentTimeMillis()) {
                    requireActivity().finish()
                } else {
                    Toast.makeText(context, "tekan sekali lagi untuk kembali", Toast.LENGTH_LONG).show()
                }
                backButtonTime = System.currentTimeMillis()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        return view
    }

    private fun simpanSession(email: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_email", email)
        editor.putBoolean("is_logged_in", true)
        editor.apply()
    }

    private fun isEmail(input: String): Boolean {
        val emailPattern = Pattern.compile(".+@.+")
        return emailPattern.matcher(input).matches()
    }

    private fun login(view: View) {
        val input = view.findViewById<EditText>(R.id.username).text.toString()
        val password = view.findViewById<EditText>(R.id.password).text.toString()

        if (input.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "tolong lengkapi data yang ada", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEmail(input)) {
            auth.signInWithEmailAndPassword(input, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        simpanSession(input)
                        findNavController().navigate(R.id.action_loginpage_to_mainMenu)
                    } else {
                        Toast.makeText(context, "login gagal, tolong cek kembali data anda", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            db.collection("users").whereEqualTo("username", input).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(context, "username tidak ditemukan", Toast.LENGTH_SHORT).show()
                    } else {
                        val email = documents.documents[0].getString("email")
                        if (email != null) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        simpanSession(email)
                                        findNavController().navigate(R.id.action_loginpage_to_mainMenu)
                                    } else {
                                        Toast.makeText(context, "login gagal, tolong cek kembali data anda", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}