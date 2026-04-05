package com.example.smartelderlycare_app.component

data class MemorialStar(
    val id: String,
    val name: String,
    val description: String,
    val profileImageUrl: String?,
    var x: Float,
    var y: Float,
    val radius: Float,
    var speedX: Float,
    var speedY: Float,
    var alpha: Float
)
