package com.example.smartelderlycare_app.data.model

/**
 * 身后事计划祭拜信息数据模型 - REST API版本
 * 用于Bmob REST API交互
 * 对应Bmob表名: AfterlifePlan_V
 */
class AfterlifePlanVBmob {

    var objectId: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null

    // 关联的身后事计划ID
    var planId: String? = null

    // 用户ID
    var userId: String? = null

    // 祭拜日期
    var visitDate: String? = null

    // 祭拜时间
    var visitTime: String? = null

    // 祭拜备注
    var visitNotes: String? = null

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "planId" to planId,
            "userId" to userId,
            "visitDate" to visitDate,
            "visitTime" to visitTime,
            "visitNotes" to visitNotes
        )
    }

    fun toAfterlifePlanVisitInfo(): AfterlifePlanVisitInfo {
        return AfterlifePlanVisitInfo(
            planId = planId ?: "",
            userId = userId ?: "",
            visitDate = visitDate,
            visitTime = visitTime,
            visitNotes = visitNotes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromVisitInfo(info: AfterlifePlanVisitInfo): AfterlifePlanVBmob {
            return AfterlifePlanVBmob().apply {
                objectId = info.id?.toString()
                planId = info.planId
                userId = info.userId
                visitDate = info.visitDate
                visitTime = info.visitTime
                visitNotes = info.visitNotes
            }
        }

        fun fromMap(map: Map<String, Any>): AfterlifePlanVBmob {
            return AfterlifePlanVBmob().apply {
                objectId = map["objectId"] as? String
                createdAt = map["createdAt"] as? String
                updatedAt = map["updatedAt"] as? String
                planId = map["planId"] as? String
                userId = map["userId"] as? String
                visitDate = map["visitDate"] as? String
                visitTime = map["visitTime"] as? String
                visitNotes = map["visitNotes"] as? String
            }
        }
    }
}

data class AfterlifePlanVisitInfo(
    val id: Long? = null,
    val planId: String,
    val userId: String,
    val visitDate: String? = null,
    val visitTime: String? = null,
    val visitNotes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
