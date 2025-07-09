package com.example.myapplication.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.activity.EditProfileActivity
import com.example.myapplication.activity.MainActivity
import com.example.myapplication.adapters.AddressBottomSheet
import com.example.myapplication.auth.LoginFragment
import com.example.myapplication.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val PICK_IMAGE = 1
    private val CAMERA_IMAGE = 2
    private val storageRef = FirebaseStorage.getInstance().reference
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        loadUserData()

        binding.imageProfile.setOnClickListener {
            showImagePickerDialog()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)

            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }


        binding.btnAddress.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val bottomSheet = AddressBottomSheet(requireContext(), userId)
                bottomSheet.show()
            }
        }

        return binding.root
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            // Show email from Firebase Auth
            binding.tvUserEmail.text = user.email ?: "Email"

            // Load user name and profile image from Firestore
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        val imageUrl = document.getString("profileImage")

                        binding.tvUserName.text = name ?: "User Name"

                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .centerCrop()
                                .into(binding.imageProfile)
                        }
                    } else {
                        Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Profile Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PICK_IMAGE -> {
                    val imageUri = data.data
                    imageUri?.let {
                        binding.imageProfile.setImageURI(it)
                        uploadImage(it)
                    }
                }
                CAMERA_IMAGE -> {
                    val bitmap = data.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        val uri = getImageUriFromBitmap(it)
                        binding.imageProfile.setImageBitmap(it)
                        uploadImage(uri)
                    }
                }
            }
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            requireActivity().contentResolver, bitmap, "ProfileImage", null
        )
        return Uri.parse(path)
    }

    private fun uploadImage(uri: Uri) {
        binding.profileUploadProgress.visibility = View.VISIBLE
        val userId = auth.currentUser?.uid ?: return
        val fileRef = storageRef.child("profile_images/$userId.jpg")

        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    db.collection("users").document(userId)
                        .update("profileImage", downloadUri.toString())
                        .addOnSuccessListener {
                            Glide.with(this)
                                .load(downloadUri)
                                .centerCrop()
                                .into(binding.imageProfile)
                            Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT).show()
                        }
                }
                binding.profileUploadProgress.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                binding.profileUploadProgress.visibility = View.GONE
            }
    }
}
