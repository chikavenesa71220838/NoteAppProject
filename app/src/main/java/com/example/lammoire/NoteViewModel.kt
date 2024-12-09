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
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(null, "Error fetching notes", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val noteList = snapshots.map { document ->
                        Note(
                            id = document.id,
                            text = document.getString("text") ?: "",
                            timestamp = document.getLong("timestamp") ?: 0L,
                            location = document.getString("location") ?: "",
                            creationDate = document.getLong("creationDate") ?: 0L
                        )
                    }
                    _notes.value = noteList
                }
            }
    }

}