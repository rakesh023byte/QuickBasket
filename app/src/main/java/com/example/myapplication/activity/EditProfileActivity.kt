package com.example.myapplication.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid.orEmpty()
    private val genderOptions = listOf("Male", "Female", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        binding.rgGender.setAdapter(genderAdapter)

        loadUserData()

        binding.etDob.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadUserData() {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    binding.etName.setText(doc.getString("name"))
                    binding.etPhone.setText(doc.getString("phone"))
                    binding.etAddress.setText(doc.getString("address"))
                    binding.etDob.setText(doc.getString("dob"))
                    binding.etBio.setText(doc.getString("bio"))
                    binding.rgGender.setText(doc.getString("gender") ?: "", false)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveChanges() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val dob = binding.etDob.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()
        val gender = binding.rgGender.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedData = mapOf(
            "name" to name,
            "phone" to phone,
            "address" to address,
            "dob" to dob,
            "bio" to bio,
            "gender" to gender
        )

        db.collection("users").document(userId)
            .update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val dob = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.etDob.setText(dob)
        }, year, month, day)

        datePicker.show()
    }
}
