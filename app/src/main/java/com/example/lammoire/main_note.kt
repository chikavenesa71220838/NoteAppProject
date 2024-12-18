package com.example.lammoire

import ImageAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
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
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class main_note : Fragment(R.layout.fragment_main_note) {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var pickFileLauncher: ActivityResultLauncher<Intent>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var imageAdapter: ImageAdapter
    private val imagePaths = mutableListOf<String>()
    private var noteId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        changeStatusBarColor("#B68730")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        pickFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null && data.data != null) {
                        val imageUri = data.data
                        val imagePath = imageUri?.let { saveImageToLocal(it) }
                        if (imagePath != null) {
                            imagePaths.add(imagePath)
                            imageAdapter.notifyItemInserted(imagePaths.size - 1)
                            saveImagePaths()
                        }
                    } else if (data != null && data.extras != null) {
                        val imageBitmap = data.extras!!.get("data") as Bitmap
                        val imagePath = saveBitmapToFile(imageBitmap)
                        if (imagePath != null) {
                            imagePaths.add(imagePath)
                            imageAdapter.notifyItemInserted(imagePaths.size - 1)
                            saveImagePaths()
                        }
                    }
                }
            }
    }

    private fun showDeleteDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Foto")
            .setMessage("Apakah Anda yakin ingin menghapus foto ini?")
            .setPositiveButton("Ya") { dialog, _ ->
                imagePaths.removeAt(position)
                imageAdapter.notifyItemRemoved(position)
                saveImagePaths()
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveImageToLocal(uri: Uri): String? {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().getExternalFilesDir(null), "selected_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file.absolutePath
    }

    private fun saveImagePaths() {
        val sharedPreferences = requireContext().getSharedPreferences("image_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("image_paths_$noteId", imagePaths.toSet())
        editor.apply()
    }

    private fun loadImagePaths() {
        val sharedPreferences = requireContext().getSharedPreferences("image_prefs", Context.MODE_PRIVATE)
        val savedPaths = sharedPreferences.getStringSet("image_paths_$noteId", emptySet())
        imagePaths.clear()
        imagePaths.addAll(savedPaths ?: emptySet())
        imageAdapter.notifyDataSetChanged()
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

        val imageRecyclerView = view.findViewById<RecyclerView>(R.id.imageRecyclerView)
        noteId = arguments?.getString("NOTE_ID")
        imageAdapter = ImageAdapter(imagePaths, noteId) { position ->
            showDeleteDialog(position)
        }
        imageRecyclerView.adapter = imageAdapter
        imageRecyclerView.layoutManager = LinearLayoutManager(context)

        loadImagePaths()
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
                getCurrentLocation { location ->
                    val noteRef = firestore.collection("users").document(userId).collection("notes")

                    if (!noteId.isNullOrEmpty()) {
                        val updatedNote = mapOf(
                            "text" to text,
                            "timestamp" to System.currentTimeMillis()
                        )
                        noteRef.document(noteId).update(updatedNote)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Catatan diperbarui", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Gagal memperbarui catatan", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        val newNote = mapOf(
                            "text" to text,
                            "timestamp" to System.currentTimeMillis(),
                            "creationDate" to System.currentTimeMillis(),
                            "location" to location
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
                }
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
        val options = arrayOf("Pilih dari folder", "Pilih dari galeri", "Kamera")
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

    private fun getPathFromUri(uri: Uri): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireContext().contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
        }
        return path
    }

    private fun getCurrentLocation(onLocationReceived: (String) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1001)
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val address = addresses?.get(0)?.getAddressLine(0)
                    if (address != null) {
                        onLocationReceived(address)
                    }
                } else {
                    onLocationReceived("Lokasi tidak tercakup dalam jangkauan")
                }
            }
            .addOnFailureListener {
                onLocationReceived("Gagal mendapatkan lokasi")
            }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String? {
        val filesDir = requireContext().filesDir
        val imageFile = File(filesDir, "captured_image_${System.currentTimeMillis()}.jpg")
        try {
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            return imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}