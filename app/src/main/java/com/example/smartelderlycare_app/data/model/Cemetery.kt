package com.example.smartelderlycare_app.data.model

data class Cemetery(
    val objectId: String? = null,
    val name: String,
    val district: String,
    val latitude: Double,
    val longitude: Double,
    val introduction: String,
    val address: String = "",
    val price: String = "",
    val service: String = "",
    val phone: String = "",
    val imageUrls: String = "",
    val website: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null
)