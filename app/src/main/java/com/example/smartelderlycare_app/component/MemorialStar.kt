package com.example.smartelderlycare_app.component

data class MemorialStar(
    val id: String,
    val name: String,
    val description: String,
    val profileImageUrl: String?,
    var x: Float,
    var y: Float,
    var radius: Float = 40f,
    var speedX: Float = 0f,
    var speedY: Float = 0f,    // 加上 = 0f 赋予默认值
    var alpha: Float = 255f    // 加上 = 255f 赋予默认值
)
