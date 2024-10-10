package com.example.crimealert.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.crimealert.R
import com.example.crimealert.databinding.ActivityMainBinding
import com.example.crimealert.bottom_fragments.community
import com.example.crimealert.bottom_fragments.home
import com.example.crimealert.bottom_fragments.CrimeRatePredict
import com.example.crimealert.bottom_fragments.updates
import com.example.crimealert.nav_fragments.AboutUsFragment
import com.example.crimealert.nav_fragments.ContactsFragment
import com.example.crimealert.nav_fragments.profileFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mAuth: FirebaseAuth
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        mAuth = FirebaseAuth.getInstance()

        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val smsPermissionGranted = permissions[Manifest.permission.SEND_SMS] ?: false
            val callPermissionGranted = permissions[Manifest.permission.CALL_PHONE] ?: false
            if (locationPermissionGranted && smsPermissionGranted && callPermissionGranted) {
                getContactsAndTriggerSOS()
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }

        setupBottomNavigationView()
        setupDrawerToggle()
        setupFabClickListener()
        setupNavigationView()

        loadFragment(home())
    }

    private fun setupBottomNavigationView() {
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            handleBottomNavigationItemSelected(item)
        }
    }

    private fun setupDrawerToggle() {
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupFabClickListener() {
        binding.fab.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
            ) {
                getContactsAndTriggerSOS()
            } else {
                requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.CALL_PHONE
                ))
            }
        }
    }

    private fun setupNavigationView() {
        val navView: NavigationView = findViewById(R.id.navView)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_home -> loadFragment(home())
                R.id.contacts -> loadFragment(ContactsFragment())
                R.id.drawer_settings -> loadFragment(profileFragment())
                R.id.drawer_about -> loadFragment(AboutUsFragment())
                R.id.drawer_logout -> {
                    mAuth.signOut()
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun handleBottomNavigationItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment = when (item.itemId) {
            R.id.nav_home -> home()
            R.id.nav_profile -> CrimeRatePredict()
            R.id.nav_community -> community()
            R.id.nav_updates -> updates()
            else -> home()
        }
        loadFragment(fragment)
        return true
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, fragment).commit()
    }

    private fun getContactsAndTriggerSOS() {
        val userId = mAuth.currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val contacts = document.get("contacts") as? List<Map<String, String>> ?: emptyList()
                    val contactNumbers = contacts.mapNotNull { it["phone"] }
                    if (contactNumbers.isNotEmpty()) {
                        Log.d("MainActivity", "Fetched Contacts: $contactNumbers")
                        val sosActivity = SOSActivity(this)
                        sosActivity.triggerSOS(contactNumbers)
                    } else {
                        Toast.makeText(this, "No contacts found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("MainActivity", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("MainActivity", "Failed to get contacts: ${exception.message}")
            }
    }
}
