package com.example.crimealert.model

data class Message(
    val sender: String = "",       // Email or identifier of the sender
    val name: String = "",         // Display name of the sender
    val userUUID: String = "",     // UUID of the sender
    val text: String = "",         // Text content of the message (for text messages)
    val mediaUrl: String = "",     // URL of the media (for image/video messages)
    val mediaType: String = "text",// Type of message: "text", "image", or "video"
    val timestamp: Long = 0L       // Timestamp of when the message was sent
)
