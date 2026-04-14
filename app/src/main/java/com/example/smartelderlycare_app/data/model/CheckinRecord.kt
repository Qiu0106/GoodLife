package com.example.smartelderlycare_app.data.model

data class CheckinRecord(
    val id: Long? = null,
    val objectId: String? = null,
    val userId: String,
    val nickname: String,
    val avatarUrl: String? = null,
    val totalPoints: Int = 0,
    val consecutiveDays: Int = 0,
    val lastCheckinDate: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

class CheckinRecordBmob {
    var objectId: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null
    var userId: String? = null
    var nickname: String? = null
    var avatarUrl: String? = null
    var totalPoints: Int? = null
    var consecutiveDays: Int? = null
    var lastCheckinDate: String? = null

    fun toCheckinRecord(): CheckinRecord {
        return CheckinRecord(
            id = objectId?.hashCode()?.toLong(),
            objectId = objectId,
            userId = userId ?: "",
            nickname = nickname ?: "",
            avatarUrl = avatarUrl,
            totalPoints = totalPoints ?: 0,
            consecutiveDays = consecutiveDays ?: 0,
            lastCheckinDate = lastCheckinDate ?: "",
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "nickname" to nickname,
            "avatarUrl" to avatarUrl,
            "totalPoints" to totalPoints,
            "consecutiveDays" to consecutiveDays,
            "lastCheckinDate" to lastCheckinDate
        )
    }

    companion object {
        fun fromCheckinRecord(record: CheckinRecord): CheckinRecordBmob {
            return CheckinRecordBmob().apply {
                objectId = record.objectId
                userId = record.userId
                nickname = record.nickname
                avatarUrl = record.avatarUrl
                totalPoints = record.totalPoints
                consecutiveDays = record.consecutiveDays
                lastCheckinDate = record.lastCheckinDate
            }
        }

        fun fromMap(map: Map<String, Any>): CheckinRecordBmob {
            return CheckinRecordBmob().apply {
                objectId = map["objectId"] as? String
                createdAt = map["createdAt"] as? String
                updatedAt = map["updatedAt"] as? String
                userId = map["userId"] as? String
                nickname = map["nickname"] as? String
                avatarUrl = map["avatarUrl"] as? String
                totalPoints = (map["totalPoints"] as? Number)?.toInt()
                consecutiveDays = (map["consecutiveDays"] as? Number)?.toInt()
                lastCheckinDate = map["lastCheckinDate"] as? String
            }
        }
    }
}
