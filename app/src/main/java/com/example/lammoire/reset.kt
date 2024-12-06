package com.example.lammoire

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class reset : Fragment(R.layout.fragment_reset) {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reset, container, false)

        val saveButton = view.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            changePassword(view)
        }

        return view
    }

    private fun changePassword(view: View) {
        val oldPassword = view.findViewById<EditText>(R.id.oldPass).text.toString()
        val newPassword = view.findViewById<EditText>(R.id.newPasse).text.toString()
        val confirmNewPassword = view.findViewById<EditText>(R.id.confNewPasse).text.toString()

        val sharedPreferences = requireActivity().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(context, "tolong isi semua dengan benar", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword == oldPassword) {
            Toast.makeText(context, "password baru tidak boleh sama dengan password lama", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmNewPassword) {
            Toast.makeText(context, "pastikan password baru sama", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null && user.email != null) {
            val email = user.email!!
            auth.signInWithEmailAndPassword(email, oldPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(context, "password berhasil diupdate", Toast.LENGTH_SHORT).show()
                                    val edt = sharedPreferences.edit()
                                    edt.clear()
                                    edt.apply()
                                    findNavController().navigate(R.id.action_reset_to_loginpage)
                                } else {
                                    Toast.makeText(context, "password gagal diupdate", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "password lama salah", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}