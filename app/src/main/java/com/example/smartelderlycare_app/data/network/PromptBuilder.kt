package com.example.smartelderlycare_app.data.network

import org.json.JSONArray
import org.json.JSONObject

/**
 * 适老化 Prompt 构建工具类
 *
 * 功能：
 * 1. 接收长辈实时状态数据（姓名、年龄、步数、用药状态等）
 * 2. 构建带有适老化人设的 System Prompt
 * 3. 组合历史上下文（最近5轮对话）生成 Kimi API 所需格式
 */
object PromptBuilder {

    private const val MAX_HISTORY_TURNS = 5

    /**
     * 长辈实时状态数据类
     */
    data class ElderlyStatus(
        val name: String,
        val age: Int,
        val gender: String = "未知",
        val todaySteps: Int = 0,
        val stepGoal: Int = 6000,
        val medicationStatus: MedicationStatus = MedicationStatus.NONE,
        val healthNotes: String = "",
        val mood: String = "正常"
    )

    /**
     * 用药提醒状态
     */
    enum class MedicationStatus {
        NONE,           // 无用药
        TAKEN,          // 已服药
        PENDING,        // 待服药
        MISSED          // 漏服
    }

    /**
     * 对话消息
     */
    data class ChatMessage(
        val role: String,    // "user" 或 "assistant"
        val content: String
    )

    /**
     * 构建适老化人设 System Prompt
     */
    fun buildSystemPrompt(status: ElderlyStatus): String {
        val medicationHint = when (status.medicationStatus) {
            MedicationStatus.TAKEN -> "据记录，您已于今日服药。"
            MedicationStatus.PENDING -> "【重要提醒】您今日尚有药物未服用，请注意按时服药。"
            MedicationStatus.MISSED -> "【紧急提醒】您今日漏服药物，请关注健康状态。"
            MedicationStatus.NONE -> "您今日无用药记录。"
        }

        val stepsPercent = if (status.stepGoal > 0) {
            (status.todaySteps * 100 / status.stepGoal).coerceIn(0, 100)
        } else 0

        return """
你是「好好活」智能养老助手的 AI 语音助手。你正在与用户（通常是老年人）对话，他们关心自己的健康与日常生活。

【老人今日概况】
- 老人姓名：${status.name}
- 年龄：${status.age}岁（${status.gender}性）
- 今日步数：${status.todaySteps}步（目标 ${status.stepGoal} 步，完成率 ${stepsPercent}%）
- 用药状态：$medicationHint
- 情绪状态：${status.mood}
${if (status.healthNotes.isNotEmpty()) "- 健康备注：${status.healthNotes}（如有）" else ""}

【服务要求】
1. 语言风格：温和、耐心、简洁，避免过于专业的医学术语
2. 回复长度：针对中老年人习惯，回复控制在50-150字以内
3. 如涉及健康建议，明确提示「仅供参考，请遵医嘱」
4. 积极回应用户对生活的积极态度，适时给予肯定和安慰
5. 可以结合老人当日的活动状态，给出贴心的问候和建议

请根据以上信息，回答用户的问题。
        """.trimIndent()
    }

    /**
     * 将 System Prompt 和用户消息组合成 Kimi API 所需的 JSON 格式
     *
     * @param status 长辈实时状态
     * @param userMessage 用户输入的最新消息
     * @param history 历史对话（最近5轮），每轮包含 user 和 assistant 两条消息
     * @return JSONObject，包含 model、messages 等字段，可直接转为 JSON 字符串发送
     */
    fun buildKimiRequestJson(
        status: ElderlyStatus,
        userMessage: String,
        history: List<ChatMessage> = emptyList()
    ): JSONObject {
        val messages = JSONArray()

        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", buildSystemPrompt(status))
        })

        val limitedHistory = history.takeLast(MAX_HISTORY_TURNS * 2)
        for (msg in limitedHistory) {
            messages.put(JSONObject().apply {
                put("role", msg.role)
                put("content", msg.content)
            })
        }

        messages.put(JSONObject().apply {
            put("role", "user")
            put("content", userMessage)
        })

        return JSONObject().apply {
            put("model", "kimi-k2-0905-preview")
            put("messages", messages)
            put("temperature", 0.7)
            put("max_tokens", 1000)
        }
    }

    /**
     * 将历史对话列表转换为 ChatMessage 列表
     * 适用于从数据库或 SharedPreferences 加载历史记录
     */
    fun parseHistoryToChatMessages(historyPairs: List<Pair<String, String>>): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        for ((userMsg, assistantMsg) in historyPairs) {
            messages.add(ChatMessage("user", userMsg))
            messages.add(ChatMessage("assistant", assistantMsg))
        }
        return messages
    }

    /**
     * 将 ChatMessage 列表转换为可存储的历史对列表
     */
    fun chatMessagesToHistoryPairs(messages: List<ChatMessage>): List<Pair<String, String>> {
        val pairs = mutableListOf<Pair<String, String>>()
        val iterator = messages.iterator()

        var userMsg = ""
        var hasUser = false

        while (iterator.hasNext()) {
            val msg = iterator.next()
            if (msg.role == "user") {
                userMsg = msg.content
                hasUser = true
            } else if (msg.role == "assistant" && hasUser) {
                pairs.add(Pair(userMsg, msg.content))
                hasUser = false
            }
        }
        return pairs
    }
}