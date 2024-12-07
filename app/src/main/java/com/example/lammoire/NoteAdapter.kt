package com.example.lammoire

import android.annotation.SuppressLint
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
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("notes").document(noteId)
                .delete()
                .addOnSuccessListener {
                    notes.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(context, "Berhasil terhapus!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Gagal dihapus!", Toast.LENGTH_SHORT).show()
                }
        }
    }
}