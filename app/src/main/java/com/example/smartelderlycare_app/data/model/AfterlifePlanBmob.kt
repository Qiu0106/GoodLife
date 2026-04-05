package com.example.smartelderlycare_app.data.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AfterlifePlanBmob {

    var objectId: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null

    var userId: String? = null

    // 基础信息（4个字段）
    var name: String? = null
    var age: String? = null
    var contact: String? = null
    var biography: String? = null

    // 葬礼信息 - 合并为JSON字符串（1个字段代替4个）
    var funeralInfo: String? = null

    // 遗物处理信息 - 合并为JSON字符串（1个字段代替3个）
    var relicsInfo: String? = null

    // 殡葬用品 - 合并为JSON字符串（1个字段代替6个）
    var supplies: String? = null

    // 背景音乐信息 - 合并为JSON字符串（1个字段代替2个）
    var bgmInfo: String? = null

    private val gson = Gson()

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "age" to age,
            "contact" to contact,
            "biography" to biography,
            "funeralInfo" to funeralInfo,
            "relicsInfo" to relicsInfo,
            "supplies" to supplies,
            "bgmInfo" to bgmInfo
        )
    }

    fun toAfterlifePlan(): AfterlifePlan {
        val funeralMap = parseJson(funeralInfo)
        val relicsMap = parseJson(relicsInfo)
        val suppliesMap = parseJson(supplies)
        val bgmMap = parseJson(bgmInfo)

        return AfterlifePlan(
            id = objectId?.hashCode()?.toLong(),
            userId = userId ?: "",
            name = name ?: "",
            age = age ?: "",
            contact = contact ?: "",
            biography = biography,
            funeralStyle = funeralMap["style"] as? String ?: "",
            funeralLocation = funeralMap["location"] as? String,
            funeralDate = funeralMap["date"] as? String,
            funeralTime = funeralMap["time"] as? String,
            relicsHandling = relicsMap["handling"] as? String ?: "",
            burialMethod = relicsMap["method"] as? String ?: "",
            burialLocation = relicsMap["location"] as? String,
            coffin = suppliesMap["coffin"] as? Boolean ?: false,
            urn = suppliesMap["urn"] as? Boolean ?: false,
            flowers = suppliesMap["flowers"] as? Boolean ?: false,
            candles = suppliesMap["candles"] as? Boolean ?: false,
            photos = suppliesMap["photos"] as? Boolean ?: false,
            otherSupplies = suppliesMap["other"] as? String,
            bgm = bgmMap["name"] as? String ?: "",
            bgmNotes = bgmMap["notes"] as? String,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        private val gson = Gson()

        fun fromAfterlifePlan(plan: AfterlifePlan): AfterlifePlanBmob {
            return AfterlifePlanBmob().apply {
                objectId = plan.id?.toString()
                userId = plan.userId
                name = plan.name
                age = plan.age
                contact = plan.contact
                biography = plan.biography

                funeralInfo = gson.toJson(mapOf(
                    "style" to plan.funeralStyle,
                    "location" to plan.funeralLocation,
                    "date" to plan.funeralDate,
                    "time" to plan.funeralTime
                ))

                relicsInfo = gson.toJson(mapOf(
                    "handling" to plan.relicsHandling,
                    "method" to plan.burialMethod,
                    "location" to plan.burialLocation
                ))

                supplies = gson.toJson(mapOf(
                    "coffin" to plan.coffin,
                    "urn" to plan.urn,
                    "flowers" to plan.flowers,
                    "candles" to plan.candles,
                    "photos" to plan.photos,
                    "other" to plan.otherSupplies
                ))

                bgmInfo = gson.toJson(mapOf(
                    "name" to plan.bgm,
                    "notes" to plan.bgmNotes
                ))
                // 注意：visitDate, visitTime, visitNotes 保存在独立的 AfterlifePlan_V 表
            }
        }

        fun fromMap(map: Map<String, Any>): AfterlifePlanBmob {
            return AfterlifePlanBmob().apply {
                objectId = map["objectId"] as? String
                createdAt = map["createdAt"] as? String
                updatedAt = map["updatedAt"] as? String
                userId = map["userId"] as? String
                name = map["name"] as? String
                age = map["age"] as? String
                contact = map["contact"] as? String
                biography = map["biography"] as? String
                funeralInfo = map["funeralInfo"] as? String
                relicsInfo = map["relicsInfo"] as? String
                supplies = map["supplies"] as? String
                bgmInfo = map["bgmInfo"] as? String
            }
        }

        private fun parseJson(json: String?): Map<String, Any> {
            return if (json.isNullOrBlank()) {
                emptyMap()
            } else {
                try {
                    val type = object : TypeToken<Map<String, Any>>() {}.type
                    gson.fromJson(json, type) ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }
            }
        }
    }
}
