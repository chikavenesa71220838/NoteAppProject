package com.example.lammoire

import android.annotation.SuppressLint
import androidx.activity.OnBackPressedCallback
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class mainMenu : Fragment(R.layout.fragment_main_menu) {
    var backButtonTime: Long = 0
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)

        val toolLog = view.findViewById<Toolbar>(R.id.toolMain)
        toolLog.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        toolLog.setNavigationOnClickListener {
            requireActivity().finish()
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
}