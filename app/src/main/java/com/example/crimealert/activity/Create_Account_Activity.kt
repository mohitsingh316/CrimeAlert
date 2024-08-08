package com.example.crimealert.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.crimealert.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class Create_Account_Activity : AppCompatActivity() {
    private lateinit var signupName: EditText
    private lateinit var signupUsername: EditText
    private lateinit var signupMobileNo: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupConfirmPassword: EditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var signupButton: Button
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize EditText fields
        signupName = findViewById(R.id.signup_name)
        signupUsername = findViewById(R.id.signup_username)
        signupMobileNo = findViewById(R.id.signup_mobile_no)
        signupPassword = findViewById(R.id.signup_password)
        signupConfirmPassword = findViewById(R.id.signup_confirm_password)

        // Initialize the signup button
        signupButton = findViewById(R.id.signupButton)

        // Set OnClickListener for signupButton
        signupButton.setOnClickListener { createAccount() }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            updateUI(currentUser)
        }
    }

    // Method to create the account
    private fun createAccount() {
        val name = signupName.text.toString()
        val email = signupUsername.text.toString()
        val mobileNo = signupMobileNo.text.toString()
        val password = signupPassword.text.toString()
        val confirmPassword = signupConfirmPassword.text.toString()

        // Check if any field is empty
        if (name.isEmpty() || email.isEmpty() || mobileNo.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if password matches confirm password
        if (password != confirmPassword) {
            Toast.makeText(this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = mAuth.currentUser
                    user?.let {
                        saveUserToFirestore(user.uid, name, email, mobileNo, password)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    // Method to save user data to Firestore
    private fun saveUserToFirestore(userId: String, name: String, email: String, mobileNo: String, password: String) {
        val defaultImageUrl = "https://firebasestorage.googleapis.com/v0/b/crimealert-20d52.appspot.com/o/profile-circle-svgrepo-com.png?alt=media&token=e49c2622-9565-4dd9-8228-95c4b206c1ac"

        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "mobileNo" to mobileNo,
            "password" to password, // Store password (Note: Not recommended in plaintext, use hashing)
            "imageUrl" to defaultImageUrl// Use default image URL
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "user_model data successfully written!")
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                updateUI(mAuth.currentUser)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing user data", e)
                Toast.makeText(this, "Failed to create account: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Start MainActivity if the user is successfully signed in
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optional: close CreateAccountActivity so the user cannot go back
        } else {
            // Handle the UI update for when the user is null (e.g., show login screen)
        }
    }

    companion object {
        private const val TAG = "CreateAccountActivity"
    }
}
