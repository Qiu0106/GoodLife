package com.example.smartelderlycare_app.data.model

/**
 * 帖子数据模型
 * 用于社区帖子功能
 */
data class Post(
    val id: Long? = null,
    val objectId: String? = null,
    val userId: String,
    val title: String,
    val content: String,
    val category: String = "",
    val coverImageUrl: String? = null,
    val imageUrls: String? = null,
    val userName: String,
    val userAvatarUrl: String? = null,
    val likeCount: Int = 0,
    val favoriteCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val syncStatus: Int = 0
)