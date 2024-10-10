package com.example.crimealert.model

data class Comment(
    val id: String = "",         // Unique identifier for the comment
    val text: String = "",       // Text of the comment
    val userId: String = "",     // User ID of the person who made the comment
    val timestamp: Long = 0      // Timestamp for when the comment was made
)
