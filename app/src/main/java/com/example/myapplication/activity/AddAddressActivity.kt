package com.example.myapplication.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAddAddressBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddAddressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAddressBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var addressId: String? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getStringExtra("mode")?.let {
            if (it == "edit") {
                isEditMode = true
                binding.btnSaveAddress.text = "Update Address"
                addressId = intent.getStringExtra("id")
                binding.etFullName.setText(intent.getStringExtra("fullName"))
                binding.etPhone.setText(intent.getStringExtra("phone"))
                binding.etAddressLine.setText(intent.getStringExtra("addressLine"))
                binding.etCity.setText(intent.getStringExtra("city"))
                binding.etPincode.setText(intent.getStringExtra("pincode"))
            }
        }

        binding.btnSaveAddress.setOnClickListener {
            saveAddress()
        }
    }

    private fun saveAddress() {
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val addressLine = binding.etAddressLine.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val pincode = binding.etPincode.text.toString().trim()

        if (fullName.isEmpty() || phone.isEmpty() || addressLine.isEmpty() || city.isEmpty() || pincode.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val addressesRef = db.collection("users").document(userId).collection("addresses")
        val addressData = hashMapOf(
            "fullName" to fullName,
            "phone" to phone,
            "addressLine" to addressLine,
            "city" to city,
            "pincode" to pincode
        )

        if (isEditMode && addressId != null) {
            addressesRef.document(addressId!!).update(addressData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Address updated", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to update address", Toast.LENGTH_SHORT).show()
                }
        } else {
            addressesRef.get().addOnSuccessListener { result ->
                val isFirst = result.isEmpty
                addressData["isDefault"] = isFirst.toString()
                addressesRef.add(addressData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Address added", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to add address", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
