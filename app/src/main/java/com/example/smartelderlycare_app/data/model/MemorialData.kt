package com.example.smartelderlycare_app.data.model

/**
 * 纪念星数据模型
 * 用于前端展示
 */
data class MemorialData(
    val id: String,
    val name: String,
    val lifeYears: String,
    val message: String,
    val story: String,
    val imageUrl: String?,
    val flowerCount: Int = 0
)