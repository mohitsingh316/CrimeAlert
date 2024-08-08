package com.example.crimealert.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crimealert.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var mAuth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser: FirebaseUser? = mAuth.currentUser
        if (currentUser != null) {
            updateUI(currentUser)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        // Initialize EditText and Button
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)

        // Set OnClickListener for loginButton
        loginButton.setOnClickListener {
            // Get entered username and password
            val enteredUsername: String = username.text.toString()
            val enteredPassword: String = password.text.toString()

            // Check if entered username and password are not empty
            if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sign in with Firebase Authentication
            mAuth.signInWithEmailAndPassword(enteredUsername, enteredPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = mAuth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(
                                this,
                                "Invalid credentials. Please check your username and password.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                        updateUI(null)
                    }
                }
        }

        // Set OnClickListener for signupText Button
        val signupText: Button = findViewById(R.id.signupText)
        signupText.setOnClickListener {
            val intent = Intent(this, Create_Account_Activity::class.java)
            startActivity(intent)
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            // Start MainActivity if the user is successfully signed in
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optional: close LoginActivity so the user cannot go back
        } else {
            // Handle the UI update for when the user is null (e.g., show login screen)
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
