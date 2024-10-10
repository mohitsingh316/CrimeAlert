package com.example.crimealert.model

data class Post(
    var postId: String? = null,
    var userId: String? = null,
    var userName: String? = null,
    val userImage: String? = null,
    var postText: String? = null,
    var postImage: String? = null,
    var postVideo: String? = null,
    var postTime: Long? = null,
    var likeCount: Int = 0,
    var likesMap: HashMap<String, Boolean>? = HashMap() // Default to an empty HashMap
)

