package com.example.crimealert.nav_fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.crimealert.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.*


class SettingFragment : Fragment() {
    private lateinit var profileImage: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var mobileNoTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var communityTextView: TextView
    private lateinit var editButton: ImageButton

    private lateinit var firestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storageReference: FirebaseStorage

    private lateinit var photoURI: Uri
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val REQUEST_CAMERA_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImage = view.findViewById(R.id.profile_image)
        nameTextView = view.findViewById(R.id.name)
        mobileNoTextView = view.findViewById(R.id.mobile_no)
        emailTextView = view.findViewById(R.id.email)
        communityTextView = view.findViewById(R.id.community)
        editButton = view.findViewById(R.id.image_edit)

        loadUserProfile()

        editButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                // Request Camera Permission
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            } else {
                // Show image picker options
                showImagePickerOptions()
            }
        }

        return view
    }

    private fun loadUserProfile() {
        val userId = mAuth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name")
                    val mobileNo = document.getString("mobileNo")
                    val email = document.getString("email")
                    val community = document.get("joined_community") as? List<String> ?: emptyList()
                    val imageUrl = document.getString("imageUrl") ?: DEFAULT_PROFILE_IMAGE_URL

                    nameTextView.text = name
                    mobileNoTextView.text = mobileNo
                    emailTextView.text = email
                    communityTextView.text = community.joinToString(", ")
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile) // Placeholder image
                        .error(R.drawable.ic_profile) // Error image
                        .into(profileImage)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> dispatchPickPictureIntent()
                }
            }
            .show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
            val photoFile = createImageFile()
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun createImageFile(): File? {
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    private fun dispatchPickPictureIntent() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    handleImageFromUri(photoURI)
                }
                REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    imageUri?.let {
                        handleImageFromUri(it)
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleImageFromUri(uri: Uri) {
        val contentResolver = requireContext().contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val croppedBitmap = cropImage(bitmap)
        uploadImageToFirebase(croppedBitmap)
        profileImage.setImageBitmap(croppedBitmap)
    }

    private fun cropImage(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val cropSize = minOf(width, height)
        return Bitmap.createBitmap(bitmap, (width - cropSize) / 2, (height - cropSize) / 2, cropSize, cropSize)
    }

    private fun compressImage(bitmap: Bitmap): ByteArrayOutputStream {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // 80% quality
        return outputStream
    }

    private fun uploadImageToFirebase(bitmap: Bitmap) {
        val storageReference = FirebaseStorage.getInstance().reference.child("profile_images/${UUID.randomUUID()}.jpg")

        val byteArrayOutputStream = compressImage(bitmap)
        val data = byteArrayOutputStream.toByteArray()

        val uploadTask = storageReference.putBytes(data)

        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                val userId = mAuth.currentUser?.uid ?: return@addOnSuccessListener
                firestore.collection("users").document(userId)
                    .update("imageUrl", uri.toString())
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val DEFAULT_PROFILE_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/crimealert-20d52.appspot.com/o/profile.png?alt=media&token=a25bbbff-d8ee-4bc1-962f-9b52e814f38a"
    }

}