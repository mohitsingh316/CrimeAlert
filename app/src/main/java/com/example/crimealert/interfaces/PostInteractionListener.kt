package com.example.crimealert.interfaces

import com.example.crimealert.model.Post

interface PostInteractionListener {
    fun onLikeClick(post: Post)
    fun onCommentClick(post: Post)
    fun onShareClick(post: Post)
}
