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
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import java.util.concurrent.Executor
import java.util.regex.Pattern

class loginpage : Fragment(R.layout.fragment_loginpage) {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    var backButtonTime: Long = 0
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

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

    @SuppressLint("MissingInflatedId")
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

        val lupaPAss = view.findViewById<TextView>(R.id.lupaPass)
        lupaPAss.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    lupaPass()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("La Memoire")
            .setNegativeButtonText("Use account password")
            .build()

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

    private fun lupaPass() {
        val input = view?.findViewById<EditText>(R.id.username)?.text.toString()
        if (input.isEmpty()) {
            Toast.makeText(context, "Tolong isi email atau username terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEmail(input)) {
            auth.sendPasswordResetEmail(input)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Email reset password telah dikirim", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Gagal mengirim email reset password", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            db.collection("users").whereEqualTo("username", input).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(context, "Username tidak ditemukan", Toast.LENGTH_SHORT).show()
                    } else {
                        val email = documents.documents[0].getString("email")
                        if (email != null) {
                            auth.sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Email reset password telah dikirim", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Gagal mengirim email reset password", Toast.LENGTH_SHORT).show()
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