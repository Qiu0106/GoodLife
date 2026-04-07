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
    val realName: String? = null, // 真实姓名
    val avatarUrl: String? = null,
    val gender: String? = null, // "男", "女", "保密"
    val birthDate: String? = null, // 格式: "1950-01-01"
    val address: String? = null,
    val emergencyContact: String? = null, // 紧急联系人姓名
    val emergencyPhone: String? = null, // 紧急联系人电话
    val bloodType: String? = null, // "A型", "B型", "AB型", "O型", "未知"
    val medicalHistory: String? = null, // 过敏史/慢性病简述
    val signature: String? = null, // 个性签名/寄语
    val token: String? = null, // Session Token
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
