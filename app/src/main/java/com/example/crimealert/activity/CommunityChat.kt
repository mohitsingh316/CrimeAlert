package com.example.crimealert.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crimealert.R
import com.example.crimealert.adapter.MessageAdapter
import com.example.crimealert.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CommunityChat : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private val PICK_MEDIA_REQUEST_CODE = 1
    private val TAG = "CommunityChat"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recyclerView = findViewById(R.id.chatRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(messages)
        recyclerView.adapter = adapter

        val communityName = intent.getStringExtra("COMMUNITY_NAME") ?: ""
        val toolbar: Toolbar = findViewById(R.id.community_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = communityName

        loadMessages(communityName)

        // Handle sending messages
        findViewById<ImageButton>(R.id.sendButton).setOnClickListener {
            val messageText = findViewById<EditText>(R.id.messageEditText).text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(communityName, messageText)
                findViewById<EditText>(R.id.messageEditText).text.clear()
            }
        }

        // Handle sending media (images/videos)
        findViewById<ImageButton>(R.id.mediaButton).setOnClickListener {
            openMediaPicker()
        }
    }

    private fun openMediaPicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/* video/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        startActivityForResult(intent, PICK_MEDIA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_MEDIA_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                Log.d(TAG, "Selected media URI: $uri")
                uploadMedia(uri)
            }
        }
    }

    private fun uploadMedia(uri: Uri) {
        val mediaType = when (contentResolver.getType(uri)) {
            "image/jpeg", "image/png" -> "image"
            "video/mp4", "video/avi" -> "video"
            else -> {
                Log.e(TAG, "Unsupported media type: ${contentResolver.getType(uri)}")
                return // If it's neither, do nothing
            }
        }

        val storageReference = FirebaseStorage.getInstance().reference.child("media/${UUID.randomUUID()}")
        val uploadTask = storageReference.putFile(uri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { e ->
                    Log.e(TAG, "Upload failed", e)
                    throw e
                }
            }
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { downloadUri ->
                    sendMessageWithMedia(downloadUri.toString(), mediaType)
                }
            } else {
                Log.e(TAG, "Failed to get download URL", task.exception)
            }
        }
    }

    private fun sendMessageWithMedia(mediaUrl: String, mediaType: String) {
        val communityName = intent.getStringExtra("COMMUNITY_NAME") ?: ""
        val database = FirebaseDatabase.getInstance()
        val communityRef = database.getReference("communities").child(communityName)
        val userUUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users").document(userUUID).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name") ?: ""
                val message = Message(
                    sender = FirebaseAuth.getInstance().currentUser?.email ?: "",
                    name = userName,
                    userUUID = userUUID,
                    text = "", // Empty text for media messages
                    mediaUrl = mediaUrl, // Media URL
                    mediaType = mediaType, // Media type
                    timestamp = System.currentTimeMillis()
                )

                communityRef.child("messages").push().setValue(message)
                    .addOnSuccessListener {
                        // Handle success
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Failed to send message with media", it)
                    }
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to retrieve user name", it)
            }
    }

    private fun sendMessage(communityName: String, messageText: String) {
        val database = FirebaseDatabase.getInstance()
        val communityRef = database.getReference("communities").child(communityName)
        val userUUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val firestore = FirebaseFirestore.getInstance()

        // Fetch the user's name from Firestore
        firestore.collection("users").document(userUUID).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name") ?: ""
                val message = Message(
                    sender = FirebaseAuth.getInstance().currentUser?.email ?: "",
                    name = userName,
                    userUUID = userUUID,
                    text = messageText,
                    timestamp = System.currentTimeMillis()
                )

                // Send the message to Firebase Realtime Database
                communityRef.child("messages").push().setValue(message)
                    .addOnSuccessListener {
                        // Handle success (no need to update UI here)
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Failed to send message", it)
                    }
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to retrieve user name", it)
            }
    }

    private fun loadMessages(communityName: String) {
        val database = FirebaseDatabase.getInstance()
        val messagesRef = database.getReference("communities").child(communityName).child("messages")

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMessages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        newMessages.add(message)
                    }
                }
                updateRecyclerView(newMessages)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load messages", error.toException())
            }
        })
    }

    private fun updateRecyclerView(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        adapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(messages.size - 1)
    }

    // Handle back navigation
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Go back to the previous activity
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
