package com.example.lammoire

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import android.view.Window
import android.view.WindowManager

class startafterlogin : Fragment(R.layout.fragment_startafterlogin) {
    var backButtonTime: Long = 0
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_startafterlogin, container, false)
        changeStatusBarColor("#B68730")

        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    findNavController().navigate(R.id.action_startafterlogin_to_mainMenu)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("La Memoire")
            .setNegativeButtonText("Use account password")
            .build()

        val btnStart = view.findViewById<Button>(R.id.btnGatau)
        btnStart.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            // This function is called automatically when the inbuilt back button is pressed
            override fun handleOnBackPressed() {
                //
                // Checks whether the time elapsed between two consecutive back button presses is less than 3 seconds.
                if (backButtonTime + 3000 > System.currentTimeMillis()) {
                    requireActivity().finish()
                } else {
                    Toast.makeText(context, "Press back again to leave the app.", Toast.LENGTH_LONG).show()
                }
                backButtonTime = System.currentTimeMillis()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

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