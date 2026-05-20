package com.example.smartelderlycare_app.data.model

/**
 * 药物提醒数据模型
 * Bmob 表名: Medication
 * times 字段在 Bmob 中以逗号分隔的字符串存储，如 "08:00,12:00,18:00"
 */
data class Medication(
    val objectId: String? = null,
    val userId: String,
    val name: String,
    val frequency: String,
    val times: List<String>,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /**
     * 将 times 列表转换为 Bmob 存储用的逗号分隔字符串
     */
    fun timesToString(): String = times.joinToString(",")

    companion object {
        /**
         * 从 Bmob 返回的逗号分隔字符串解析为 times 列表
         */
        fun parseTimes(timeStr: String?): List<String> {
            if (timeStr.isNullOrBlank()) return emptyList()
            return timeStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
}
