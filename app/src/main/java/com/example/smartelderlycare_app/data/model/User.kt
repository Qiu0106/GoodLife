package com.example.smartelderlycare_app.data.model

/**
 * 用户数据模型
 * 用于用户注册、登录和信息管理
 */
data class User(
    val id: Long? = null,
    val objectId: String? = null,
    val phone: String,
    val password: String? = null,
    val nickname: String? = null,
    val realName: String? = null,
    val avatarUrl: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val address: String? = null,
    val emergencyContact: String? = null,
    val emergencyPhone: String? = null,
    val bloodType: String? = null,
    val medicalHistory: String? = null,
    val signature: String? = null,
    val token: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val syncStatus: Int = 0
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
