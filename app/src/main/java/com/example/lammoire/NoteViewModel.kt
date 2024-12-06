package com.example.lammoire

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NoteViewModel : ViewModel() {
    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> get() = _notes

    private val db = FirebaseFirestore.getInstance()

    fun fetchNotes(userId: String) {
        db.collection("users").document(userId).collection("notes")
            .get()
            .addOnSuccessListener { result ->
                val noteList = result.map { document ->
                    Note(
                        id = document.id,
                        text = document.getString("text") ?: ""
                    )
                }
                _notes.value = noteList
            }
            .addOnFailureListener { exception ->
                // Handle any errors here
                Toast.makeText(null, "Error fetching notes", Toast.LENGTH_SHORT).show()
            }
    }
}