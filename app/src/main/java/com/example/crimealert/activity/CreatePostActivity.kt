package com.example.crimealert.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.crimealert.R
import com.example.crimealert.databinding.ActivityCreatePostBinding
import com.example.crimealert.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class CreatePostActivity : AppCompatActivity() {
    private var selectedUri: Uri? = null
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityCreatePostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Posting...")
        progressDialog.setCancelable(false)

        // Load current user's profile image
        val currentUser = FirebaseAuth.getInstance().currentUser
        Glide.with(this).load(currentUser?.photoUrl).into(binding.userImageView)
        binding.userNameTextView.text = currentUser?.displayName

        binding.selectImageButton.setOnClickListener {
            // Handle image and video selection
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            }
            startActivityForResult(Intent.createChooser(intent, "Select Media"), 100)
        }

        binding.postButton.setOnClickListener {
            val postText = binding.postEditText.text.toString().trim()

            if (postText.isEmpty() && selectedUri == null) {
                Toast.makeText(this, "Please add some content", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressDialog.show()

            if (selectedUri != null) {
                uploadMediaAndCreatePost(postText)
            } else {
                createPost(null, null, postText)
            }
        }
    }

    private fun uploadMediaAndCreatePost(postText: String) {
        val fileType = determineFileType(selectedUri!!)
        val storageReference = FirebaseStorage.getInstance().reference.child("posts")
            .child("${UUID.randomUUID()}.$fileType")

        storageReference.putFile(selectedUri!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    if (fileType == "image") {
                        createPost(uri.toString(), null, postText)
                    } else if (fileType == "video") {
                        createPost(null, uri.toString(), postText)
                    }
                }
            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload media", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createPost(imageUrl: String?, videoUrl: String?, postText: String) {
        val postId = FirebaseDatabase.getInstance().reference.child("posts").push().key
        val post = Post(
            postId = postId,
            userId = FirebaseAuth.getInstance().uid,
            userName = FirebaseAuth.getInstance().currentUser?.displayName,
            userImage = FirebaseAuth.getInstance().currentUser?.photoUrl.toString(),
            postImage = imageUrl,
            postVideo = videoUrl,
            postText = postText,
            postTime = Date().time
        )

        FirebaseDatabase.getInstance().reference.child("posts")
            .child(postId!!)
            .setValue(post).addOnCompleteListener { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    Toast.makeText(this, "Post created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to create post", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            selectedUri = data?.data
            if (selectedUri != null) {
                binding.selectedImageView.visibility = View.VISIBLE
                Glide.with(this).load(selectedUri).into(binding.selectedImageView)
            } else {
                Toast.makeText(this, "Error selecting media", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun determineFileType(uri: Uri): String? {
        val contentResolver = contentResolver
        val mimeType = contentResolver.getType(uri)
        return mimeType?.substringBefore("/")
    }
}
