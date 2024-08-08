package com.example.crimealert.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
        val communityPageTextView = findViewById<TextView>(R.id.community_page_txtview)
        communityPageTextView.text = communityName
        loadMessages(communityName)

        // Handle sending messages
        findViewById<ImageButton>(R.id.sendButton).setOnClickListener {
            val messageText = findViewById<EditText>(R.id.messageEditText).text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(communityName, messageText)
                findViewById<EditText>(R.id.messageEditText).text.clear()
            }
        }
        findViewById<ImageButton>(R.id.mediaButton).setOnClickListener {
            openMediaPicker()
        }
    }
    private fun uploadMedia(uri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference.child("media/${UUID.randomUUID()}")
        val uploadTask = storageReference.putFile(uri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { downloadUri ->
                    sendMessageWithMedia(downloadUri.toString())
                }
            } else {
                // Handle failures
            }
        }
    }
    private fun sendMessageWithMedia(mediaUrl: String) {
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
                    timestamp = System.currentTimeMillis()
                )

                communityRef.child("messages").push().setValue(message)
                    .addOnSuccessListener {
                        // Handle success
                    }
                    .addOnFailureListener {
                        // Handle failure
                    }
            }
            .addOnFailureListener {
                // Handle errors retrieving the user's name
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
                        // Handle failure
                    }
            }
            .addOnFailureListener {
                // Handle errors retrieving the user's name
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
                // Handle possible errors
            }
        })
    }

    private fun updateRecyclerView(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        adapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun openMediaPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/* video/*"
        startActivityForResult(intent, PICK_MEDIA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_MEDIA_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                uploadMedia(uri)
            }
        }
    }
}
