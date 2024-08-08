package com.example.crimealert.model


data class user_model(
    val id: String = "",
    val name: String = "",
    val joined_communities: List<String> = emptyList()
)
