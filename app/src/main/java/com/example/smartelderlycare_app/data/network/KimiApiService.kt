package com.example.smartelderlycare_app.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object KimiApiService {

    private const val BASE_URL = "https://api.moonshot.cn/v1/chat/completions"

    // TODO: 请在此处设置您的 Moonshot API Key
    // 推荐方式：在 Application 类或配置文件中管理，通过 BuildConfig 或秘密存储方案获取
    private const val API_KEY_PLACEHOLDER = "sk-LCflVRDM0MbKLAcxuOYIMU4VPFNmbISPJ9mjpfLptcpznWkp"

    private val client: OkHttpClient = OkHttpSingleton.client

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * 发送消息给 Kimi 大模型并获取回复
     *
     * @param userMessage 用户输入的文本
     * @param apiKey Moonshot API Key（可选，不传则使用默认值）
     * @param model 模型名称，默认 "moonshot-v1-8k"
     * @return AI 的回复文本，失败返回 null
     */
    suspend fun chat(
        userMessage: String,
        apiKey: String = API_KEY_PLACEHOLDER,
        model: String = "moonshot-v1-8k"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = buildRequestBody(userMessage, model)
            println("正在使用的 API Key 是: [${apiKey}]")
            val request = Request.Builder()
                .url(BASE_URL)
                // 注意这里加了 .trim()
                .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("请求失败: ${response.code} - ${response.message}")
                )
            }

            val responseBody = response.body?.string()
                ?: return@withContext Result.failure(IOException("响应体为空"))

            val reply = parseResponse(responseBody)
            Result.success(reply)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 构建请求体
     */
    private fun buildRequestBody(message: String, model: String): okhttp3.RequestBody {
        val json = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", message)
                })
            })
            put("temperature", 0.7)
            put("max_tokens", 1000)
        }
        return json.toString().toRequestBody(mediaType)
    }

    /**
     * 解析 API 响应，提取 AI 回复内容
     */
    private fun parseResponse(responseBody: String): String {
        val json = JSONObject(responseBody)

        if (json.has("error")) {
            val error = json.getJSONObject("error")
            throw IOException("API 错误: ${error.optString("message", "未知错误")}")
        }

        val choices = json.optJSONArray("choices")
        if (choices == null || choices.length() == 0) {
            throw IOException("响应中无 choices 字段")
        }

        val firstChoice = choices.getJSONObject(0)
        val message = firstChoice.getJSONObject("message")
        return message.getString("content")
    }

    /**
     * 设置默认 API Key（推荐在 Application 初始化时调用一次）
     */
    fun setDefaultApiKey(apiKey: String) {
        // 此处可结合 SharedPreferences 或加密存储实现持久化
        // 为安全起见，API Key 不建议明文硬编码
    }
}