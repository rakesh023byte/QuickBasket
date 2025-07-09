package com.example.myapplication.models

data class CartItem(
    val productId: String = "",
    var quantity: Int = 1,
    val name: String = "",
    val pricePerUnit: Double = 0.0,
    val imageUrl: String = ""
)
