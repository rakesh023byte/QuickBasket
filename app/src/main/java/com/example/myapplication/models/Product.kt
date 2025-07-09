package com.example.myapplication.models

data class Product(
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val productId: String = "",
    val category: String = "",
    val stockQuantity: Int = 0,
    val description: String = ""
)
