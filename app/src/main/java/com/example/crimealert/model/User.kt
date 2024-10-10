package com.example.crimealert.model

data class User(
    val email: String? = null,
    val imageUrl: String? = null,
    val joined_community: List<String>? = null,
    val mobileNo: String? = null,
    val name: String? = null,
    val userId: String = ""  // Providing a default value to handle cases where userId might be missing
)
