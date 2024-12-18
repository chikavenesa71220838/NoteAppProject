package com.example.lammoire

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class mainMenu : Fragment(R.layout.fragment_main_menu) {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var textUname: TextView
    private val noteViewModel: NoteViewModel by viewModels()
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    var backButtonTime: Long = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawerLayout)
        val toolbar: Toolbar = view.findViewById(R.id.toolMain)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        actionBarDrawerToggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            R.string.nav_open,
            R.string.nav_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = ""

        actionBarDrawerToggle.setToolbarNavigationClickListener {

        }

        toolbar.setNavigationIcon(R.drawable.baseline_account_circle_24)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        textUname = view.findViewById(R.id.text_uname)
        fetchUserData()

        changeStatusBarColor("#B68730")
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backButtonTime + 3000 > System.currentTimeMillis()) {
                    requireActivity().finish()
                } else {
                    Toast.makeText(context, "tekan sekali lagi untuk kembali", Toast.LENGTH_LONG).show()
                }
                backButtonTime = System.currentTimeMillis()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        val floatButt: FloatingActionButton = view.findViewById(R.id.floatButt)
        floatButt.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_main_note)
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)

        noteAdapter = NoteAdapter(mutableListOf())
        recyclerView.adapter = noteAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        val navigationView: NavigationView = view.findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.profile_nav_menu -> {
                    findNavController().navigate(R.id.action_mainMenu_to_profile)
                    true
                }
                R.id.settings_nav_menu -> {
                    findNavController().navigate(R.id.action_mainMenu_to_reset)
                    true
                }
                R.id.logout_nav_menu -> {
                    (activity as MainActivity).logout()
                    true
                }
                else -> false
            }
        }

        return view
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username")
                        textUname.text = username
                    }
                }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        val userId = auth.currentUser?.uid
        if (!userId.isNullOrEmpty()) {
            noteViewModel.fetchNotes(userId)
        } else {
            // Handle the case where userId is null or empty
            Toast.makeText(context, "User ID is invalid", Toast.LENGTH_SHORT).show()
        }

        noteViewModel.notes.observe(viewLifecycleOwner, Observer { notes ->
            noteAdapter.updateData(notes.toMutableList())
        })
    }

    override fun onResume() {
        super.onResume()
        val userId = auth.currentUser?.uid ?: ""
        noteViewModel.fetchNotes(userId)

        val drawerLayout: DrawerLayout = view?.findViewById(R.id.drawerLayout) ?: return
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun changeStatusBarColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = requireActivity().window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = android.graphics.Color.parseColor(color)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}