package com.example.smartelderlycare_app.data.model

/**
 * 帖子数据模型
 * 用于社区帖子功能
 */
data class Post(
    val id: Long? = null,
    val userId: String,
    val title: String,
    val content: String,
    val coverImageUrl: String? = null,
    val userName: String,
    val userAvatarUrl: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val syncStatus: Int = 0 // 0:未同步, 1:已同步, 2:同步失败
)
