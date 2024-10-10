package com.example.crimealert.repository

import com.example.crimealert.model.Post
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PostRepository {

    private val database = FirebaseDatabase.getInstance().reference.child("posts")

    // Method to toggle like/unlike for a post
    suspend fun toggleLike(postId: String, userId: String): Post? {
        val postRef = database.child(postId)

        return suspendCancellableCoroutine { continuation ->
            postRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val post = mutableData.getValue(Post::class.java) ?: return Transaction.success(mutableData)

                    // Check if the user already liked the post
                    if (post.likesMap?.containsKey(userId) == true) {
                        post.likesMap?.remove(userId)
                        post.likeCount = (post.likeCount - 1).coerceAtLeast(0)
                    } else {
                        post.likesMap?.put(userId, true)
                        post.likeCount += 1
                    }

                    mutableData.value = post
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                    if (committed) {
                        val updatedPost = dataSnapshot?.getValue(Post::class.java)
                        continuation.resume(updatedPost)
                    } else {
                        continuation.resume(null)
                    }

                    // Log error if there is one
                    databaseError?.toException()?.printStackTrace()
                }
            })
        }
    }

    // Method to get a single post by ID
    suspend fun getPost(postId: String): Post? {
        return try {
            val snapshot = database.child(postId).get().await()
            snapshot.getValue(Post::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Method to get all posts sorted by postTime in descending order
    suspend fun getAllPosts(): List<Post> {
        return try {
            val snapshot = database.orderByChild("postTime").get().await()
            snapshot.children
                .mapNotNull { it.getValue(Post::class.java) }
                .sortedByDescending { it.postTime ?: 0 } // Ensure that postTime is not null
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}