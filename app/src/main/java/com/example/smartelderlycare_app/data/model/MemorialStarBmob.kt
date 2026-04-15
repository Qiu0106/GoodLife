package com.example.smartelderlycare_app.data.model

/**
 * 纪念星数据模型 - REST API版本
 * 用于Bmob REST API交互
 * 对应Bmob表名: MemorialStar
 *
 * 字段说明:
 * - name: 逝者姓名
 * - lifeYears: 生卒年（如 "1945年 - 2023年"）
 * - message: 寄语
 * - story: 生平事迹
 * - avatarUrl: 头像URL（可空）
 * - flowerCount: 献花次数
 * - createdBy: 创建者用户ID
 * - status: 审核状态（pending=待审核, approved=已通过, rejected=已拒绝）
 */
class MemorialStarBmob {

    var objectId: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null

    var name: String? = null
    var lifeYears: String? = null
    var message: String? = null
    var story: String? = null
    var avatarUrl: String? = null
    var flowerCount: Int = 0
    var createdBy: String? = null
    var status: String = "pending"

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "lifeYears" to lifeYears,
            "message" to message,
            "story" to story,
            "avatarUrl" to avatarUrl,
            "flowerCount" to flowerCount,
            "createdBy" to createdBy,
            "status" to status
        )
    }

    fun toMemorialData(): MemorialData {
        return MemorialData(
            id = objectId ?: "",
            name = name ?: "",
            lifeYears = lifeYears ?: "",
            message = message ?: "",
            story = story ?: "",
            imageUrl = avatarUrl,
            flowerCount = flowerCount
        )
    }

    companion object {
        fun fromMemorialData(data: MemorialData): MemorialStarBmob {
            return MemorialStarBmob().apply {
                name = data.name
                lifeYears = data.lifeYears
                message = data.message
                story = data.story
                avatarUrl = data.imageUrl
                flowerCount = data.flowerCount
            }
        }

        fun fromMap(map: Map<String, Any>): MemorialStarBmob {
            return MemorialStarBmob().apply {
                objectId = map["objectId"] as? String
                createdAt = map["createdAt"] as? String
                updatedAt = map["updatedAt"] as? String
                name = map["name"] as? String
                lifeYears = map["lifeYears"] as? String
                message = map["message"] as? String
                story = map["story"] as? String
                avatarUrl = map["avatarUrl"] as? String
                flowerCount = (map["flowerCount"] as? Number)?.toInt() ?: 0
                createdBy = map["createdBy"] as? String
                status = map["status"] as? String ?: "pending"
            }
        }
    }
}