package com.example.crimealert.repository

import com.example.crimealert.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Method to get the current user's ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Method to get the user's name from Firestore using the user ID
    suspend fun getUserName(userId: String): String {
        return try {
            val documentSnapshot = firestore.collection("users").document(userId).get().await()
            val user = documentSnapshot.toObject(User::class.java)
            user?.name ?: "Unknown"
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }
    }

    // Method to get the user's profile image URL from Firestore using the user ID
    suspend fun getUserImage(userId: String): String? {
        return try {
            val documentSnapshot = firestore.collection("users").document(userId).get().await()
            val user = documentSnapshot.toObject(User::class.java)
            user?.imageUrl
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
