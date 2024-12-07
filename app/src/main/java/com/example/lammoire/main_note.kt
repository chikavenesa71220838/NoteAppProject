package com.example.lammoire

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class main_note : Fragment(R.layout.fragment_main_note) {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var pickFileLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        changeStatusBarColor("#B68730")

        pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                if (uri != null) {
                    Toast.makeText(context, "File selected: $uri", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun changeStatusBarColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = requireActivity().window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = android.graphics.Color.parseColor(color)
        }
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_note, container, false)
        val editText = view.findViewById<EditText>(R.id.editText)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val attachmentButton = view.findViewById<FloatingActionButton>(R.id.attachmentIssues)

        val noteTimestamp = arguments?.getLong("timestamp") ?: System.currentTimeMillis()
        val noteId = arguments?.getString("NOTE_ID")
        val noteText = arguments?.getString("NOTE_TEXT")

        editText.setText(noteText)

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val timestampView = view.findViewById<TextView>(R.id.tanggal)
        val timestampText = if (noteTimestamp > 0L) {
            dateFormat.format(Date(noteTimestamp))
        } else {
            ""
        }
        timestampView.text = timestampText

        val toolLog = view.findViewById<Toolbar>(R.id.toolbarMain)
        toolLog.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        toolLog.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        saveButton.setOnClickListener {
            val text = editText.text.toString()
            val userId = auth.currentUser?.uid

            if (text.isNotEmpty() && userId != null) {
                val noteRef = firestore.collection("users").document(userId).collection("notes")

                if (!noteId.isNullOrEmpty()) {
                    noteRef.document(noteId).get().addOnSuccessListener { document ->
                        if (document != null) {
                            val updatedNote = mapOf(
                                "text" to text,
                                "timestamp" to System.currentTimeMillis()
                            )
                            noteRef.document(noteId).set(updatedNote)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Catatan diperbarui", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Gagal memperbarui catatan", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                } else {
                    val newNote = mapOf(
                        "text" to text,
                        "timestamp" to System.currentTimeMillis()
                    )
                    noteRef.add(newNote)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Catatan disimpan", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Gagal menyimpan catatan", Toast.LENGTH_SHORT).show()
                        }
                }

                findNavController().navigate(R.id.action_main_note_to_mainMenu)
            } else {
                Toast.makeText(context, "Tidak bisa membuat catatan kosong", Toast.LENGTH_SHORT).show()
            }
        }
        attachmentButton.setOnClickListener {
            showAttachmentOptions()
        }
        return view
    }

    private fun showAttachmentOptions() {
        val options = arrayOf("pilih dari folder", "pilih dari galeri", "kamera")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("pilih opsi")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "*/*"
                    pickFileLauncher.launch(intent)
                }
                1 -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    pickFileLauncher.launch(intent)
                }
                2 -> {
                    val intent = Intent("android.media.action.IMAGE_CAPTURE")
                    pickFileLauncher.launch(intent)
                }
            }
        }
        builder.show()
    }
}