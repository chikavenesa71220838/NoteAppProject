package com.example.lammoire

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(private var notes: MutableList<Note>) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteText: TextView = itemView.findViewById(R.id.noteText)
        val menuButton: ImageView = itemView.findViewById(R.id.menuButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat", "ResourceType")
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.noteText.text = note.text

        val timestamp = note.timestamp
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val dateText = dateFormat.format(Date(timestamp))

        holder.itemView.findViewById<TextView>(R.id.noteTimestamp).text = dateText

        holder.menuButton.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.menuButton)
            popupMenu.inflate(R.layout.note_menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_delete -> {
                        deleteNoteFromFirebase(holder.itemView.context, note.id, position)
                        true
                    }
                    R.id.action_detail -> {
                        fetchNoteDetails(holder.itemView.context, note.id)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        holder.itemView.setOnClickListener {
            val navController = androidx.navigation.Navigation.findNavController(holder.itemView)
            val bundle = Bundle().apply {
                putString("NOTE_ID", note.id)
                putString("NOTE_TEXT", note.text)
                putLong("timestamp", note.timestamp)
                putLong("creationDate", note.creationDate)
                putString("location", note.location)
            }
            navController.navigate(R.id.action_mainMenu_to_main_note, bundle)
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun updateData(newNotes: List<Note>) {
        this.notes = newNotes.sortedByDescending { it.timestamp }.map { note ->
            Note(
                id = note.id,
                text = note.text,
                timestamp = note.timestamp
            )
        }.toMutableList()
        notifyDataSetChanged()
    }

    private fun deleteNoteFromFirebase(context: Context, noteId: String, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Konfirmasi Penghapusan")
            .setMessage("Apakah Anda yakin ingin menghapus catatan ini?")
            .setPositiveButton("Ya") { dialog, _ ->
                val db = FirebaseFirestore.getInstance()
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    db.collection("users").document(userId).collection("notes").document(noteId)
                        .delete()
                        .addOnSuccessListener {
                            val index = notes.indexOfFirst { it.id == noteId }
                            if (index != -1) {
                                notes.removeAt(index)
                                notifyItemRemoved(index)
                            }
                            Toast.makeText(context, "Catatan berhasil dihapus!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Gagal menghapus catatan!", Toast.LENGTH_SHORT).show()
                        }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showNoteDetails(context: Context, note: Note) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val creationDateText = dateFormat.format(Date(note.creationDate))

        AlertDialog.Builder(context)
            .setTitle("Detail Catatan")
            .setMessage("Tanggal Pembuatan: $creationDateText\nLokasi: ${note.location}")
            .setPositiveButton("Tutup") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun fetchNoteDetails(context: Context, noteId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).collection("notes").document(noteId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val note = document.toObject(Note::class.java)
                    if (note != null) {
                        showNoteDetails(context, note)
                    }
                } else {
                    Toast.makeText(context, "Note not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error fetching note details", Toast.LENGTH_SHORT).show()
            }
    }

}