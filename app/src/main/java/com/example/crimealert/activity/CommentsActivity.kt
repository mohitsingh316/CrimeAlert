package com.example.crimealert.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crimealert.adapter.CommentsAdapter
import com.example.crimealert.databinding.ActivityCommentsBinding
import com.example.crimealert.model.Comment
import com.example.crimealert.model.User
import com.google.firebase.database.*
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import com.example.crimealert.R

class CommentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentsBinding
    private lateinit var commentsAdapter: CommentsAdapter
    private val commentsList = mutableListOf<Comment>()
    private val usersMap = mutableMapOf<String, String>()  // Maps userId to userName
    private lateinit var database: DatabaseReference
    private lateinit var postId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Comments"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Enable back button

        postId = intent.getStringExtra("postId") ?: return

        // Initialize the database reference to the comments of the specific post
        database = FirebaseDatabase.getInstance().reference.child("posts").child(postId).child("comments")

        commentsAdapter = CommentsAdapter(commentsList, usersMap, this)
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentsRecyclerView.adapter = commentsAdapter

        fetchComments()
        fetchUsers()

        binding.commentButton.setOnClickListener {
            postComment()
        }
    }

    // Handle the toolbar back button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()  // Close the activity and return to the previous one
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fetchComments() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentsList.clear()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    if (comment != null) {
                        commentsList.add(comment)
                    }
                }
                commentsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CommentsActivity, "Failed to load comments: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUsers() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && !user.userId.isNullOrEmpty()) {
                        usersMap[user.userId!!] = user.name ?: "Unknown"
                    }
                }
                commentsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CommentsActivity, "Failed to load users: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun postComment() {
        val commentText = binding.commentEditText.text.toString()
        val userId = "YourUserId"  // Replace this with a real user ID from authentication
        if (commentText.isNotBlank()) {
            val commentId = database.push().key ?: return
            val comment = Comment(id = commentId, text = commentText, userId = userId, timestamp = System.currentTimeMillis())
            database.child(commentId).setValue(comment).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.commentEditText.text.clear()
                    Toast.makeText(this, "Comment posted!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to post comment.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Comment cannot be empty.", Toast.LENGTH_SHORT).show()
        }
    }
}
