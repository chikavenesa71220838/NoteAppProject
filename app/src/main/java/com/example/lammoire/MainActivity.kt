package com.example.lammoire

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.findNavController
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val permissions = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        val logged = sharedPreferences.getBoolean("is_logged_in", false)


        val navController = findNavController(R.id.nav_host_fragment)
        if (logged) {
            navController.navigate(R.id.startafterlogin)
        } else {
            navController.navigate(R.id.startMenu)
        }

        if (!hasPermissions()) {
            requestPermissions()
        }

    }

    private fun hasPermissions(): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    // Permission denied, show a message to the user
                    Toast.makeText(this, "Permission denied. Some features may not work.", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            proceedWithAppFunctionality()
        }
    }
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    private fun proceedWithAppFunctionality() {
        // Your code to proceed with the app functionality after all permissions are granted
    }

    fun logout() {
        val sharedPreferences = getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        FirebaseAuth.getInstance().signOut()
        findNavController(R.id.nav_host_fragment).navigate(R.id.startMenu)
    }
}