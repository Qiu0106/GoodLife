package com.example.smartelderlycare_app.data.model

/**
 * 用户数据模型
 * 用于用户注册、登录和信息管理
 */
data class User(
    val id: Long? = null,
    val phone: String,
    val password: String? = null, // 仅用于注册/登录，不会存储
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val gender: String? = null, // "male", "female", "other"
    val birthDate: String? = null,
    val address: String? = null,
    val emergencyContact: String? = null,
    val emergencyPhone: String? = null,
    val token: String? = null, // JWT token
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val syncStatus: Int = 0 // 0:未同步, 1:已同步, 2:同步失败
)

/**
 * 登录请求
 */
data class LoginRequest(
    val phone: String,
    val password: String
)

/**
 * 注册请求
 */
data class RegisterRequest(
    val phone: String,
    val password: String,
    val nickname: String? = null
)

/**
 * 通用API响应
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
)
