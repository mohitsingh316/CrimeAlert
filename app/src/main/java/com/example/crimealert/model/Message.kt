package com.example.crimealert.model

data class Message(
    val sender: String = "",
    val name: String = "",
    val userUUID: String = "",
    val text: String = "",
    val mediaUrl: String = "",
    val timestamp: Long = 0L
)
