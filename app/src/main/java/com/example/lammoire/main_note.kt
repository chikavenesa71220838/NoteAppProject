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
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController

class main_note : Fragment(R.layout.fragment_main_note) {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
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
        val view = inflater.inflate(R.layout.fragment_main_note, container, false)
        val editText = view.findViewById<EditText>(R.id.editText)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        val noteId = arguments?.getString("NOTE_ID")
        val noteText = arguments?.getString("NOTE_TEXT")
        editText.setText(noteText)

        val toolLog = view.findViewById<Toolbar>(R.id.toolbarMain)
        toolLog.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        toolLog.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        saveButton.setOnClickListener {
            val text = editText.text.toString()
            val userId = auth.currentUser?.uid
            if (text.isNotEmpty() && userId != null) {
                if (noteId != null && noteId.isNotEmpty()) {
                    val noteRef = firestore.collection("users").document(userId).collection("notes").document(noteId)
                    noteRef.update("text", text)
                } else {
                    val note = hashMapOf("text" to text)
                    firestore.collection("users").document(userId).collection("notes").add(note)
                }
                Toast.makeText(context, "note berhasil disimpan", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_main_note_to_mainMenu)
            } else {
                Toast.makeText(context, "tidak bisa membuat note kosong", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}