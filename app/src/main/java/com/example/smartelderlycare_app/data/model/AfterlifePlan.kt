package com.example.smartelderlycare_app.data.model

/**
 * 身后事计划数据模型
 * 用于存储和上传用户的身后事规划信息
 */
data class AfterlifePlan(
    val id: Long? = null,
    val userId: String,
    
    // 基础信息
    val name: String,
    val age: String,
    val contact: String,
    val biography: String? = null,
    
    // 葬礼风格
    val funeralStyle: String,
    val funeralLocation: String? = null,
    val funeralDate: String? = null,
    val funeralTime: String? = null,
    
    // 遗物处理
    val relicsHandling: String,
    val burialMethod: String,
    val burialLocation: String? = null,
    
    // 殡葬用品
    val coffin: Boolean = false,
    val urn: Boolean = false,
    val flowers: Boolean = false,
    val candles: Boolean = false,
    val photos: Boolean = false,
    val otherSupplies: String? = null,
    
    // 背景音乐
    val bgm: String,
    val bgmNotes: String? = null,
    
    // 祭拜信息
    val visitDate: String? = null,
    val visitTime: String? = null,
    val visitNotes: String? = null,
    
    // 元数据
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val syncStatus: Int = 0 // 0:未同步, 1:已同步, 2:同步失败
)
