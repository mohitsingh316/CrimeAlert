package com.example.crimealert.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.crimealert.R
import com.example.crimealert.databinding.ActivityMainBinding
import com.example.crimealert.bottom_fragments.community
import com.example.crimealert.bottom_fragments.home
import com.example.crimealert.bottom_fragments.profile
import com.example.crimealert.bottom_fragments.updates
import com.example.crimealert.nav_fragments.AboutUsFragment
import com.example.crimealert.nav_fragments.ContactsFragment
import com.example.crimealert.nav_fragments.SettingFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navView: NavigationView = findViewById(R.id.navView)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        mAuth = FirebaseAuth.getInstance()

        setupBottomNavigationView()
        setupDrawerToggle()
        setupFabClickListener()
        setupNavigationView(navView,drawerLayout)

        // Load the default fragment
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
            showBottomSheetDialog()
        }
    }

    private fun setupNavigationView(navView: NavigationView,drawerLayout: DrawerLayout) {
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_home -> {
                    loadFragment(home())
                }
                R.id.contacts -> {
                    loadFragment(ContactsFragment())
                }
                R.id.drawer_settings -> {
                    loadFragment(SettingFragment())
                }
                R.id.drawer_about -> {
                    loadFragment(AboutUsFragment())
                }
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
            R.id.nav_profile -> profile()
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


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_layout, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetView.findViewById<TextView>(R.id.local_police).setOnClickListener {
            Toast.makeText(this, "Local Police clicked", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<TextView>(R.id.close_contact).setOnClickListener {
            Toast.makeText(this, "Close Contact clicked", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<TextView>(R.id.local_community).setOnClickListener {
            Toast.makeText(this, "Local community_model clicked", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}
