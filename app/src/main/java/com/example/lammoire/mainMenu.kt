package com.example.lammoire

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.activity.OnBackPressedCallback
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.Window
import android.view.WindowManager
import androidx.navigation.navArgs

class mainMenu : Fragment(R.layout.fragment_main_menu) {
//    private lateinit var sharedpreferences: SharedPreferences

    var backButtonTime: Long = 0
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)

        val toolLog = view.findViewById<Toolbar>(R.id.toolMain)
        toolLog.setNavigationIcon(R.drawable.baseline_account_circle_24)
        toolLog.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_profile)
        }

        changeStatusBarColor("#B68730")
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            // This function is called automatically when the inbuilt back button is pressed
            override fun handleOnBackPressed() {
                //
                // Checks whether the time elapsed between two consecutive back button presses is less than 3 seconds.
                if (backButtonTime + 3000 > System.currentTimeMillis()) {
                    requireActivity().finish()
                } else {
                    Toast.makeText(context, "tekan sekali lagi untuk kembali", Toast.LENGTH_LONG).show()
                }
                backButtonTime = System.currentTimeMillis()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        val floatButt = view.findViewById<FloatingActionButton>(R.id.floatButt)
        floatButt.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_main_note)
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