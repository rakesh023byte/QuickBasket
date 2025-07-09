package com.example.myapplication.models

data class Address(
    val id: String = "",
    val fullName: String = "",
    val phone: String = "",
    val addressLine: String = "",
    val city: String = "",
    val pincode: String = "",
    var isDefault: Boolean = false
)

