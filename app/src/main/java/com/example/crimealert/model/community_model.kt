package com.example.crimealert.model

import com.google.firebase.Timestamp

data class community_model(
    val id: String = "",
    val name: String = "",
    val owner_id: String = "",
    val members: List<String> = listOf(),
    val timestamp: Timestamp = Timestamp.now()
)
