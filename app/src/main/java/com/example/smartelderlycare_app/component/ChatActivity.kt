package com.example.smartelderlycare_app.component

import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.network.KimiApiService
import com.example.smartelderlycare_app.data.network.PromptBuilder
import com.example.smartelderlycare_app.data.repository.ElderlyDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class ChatActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "ChatActivity"
        private const val API_KEY = "sk-LCflVRDM0MbKLAcxuOYIMU4VPFNmbISPJ9mjpfLptcpznWkp"
        private const val PREFS_NAME = "chat_prefs"
        private const val KEY_CHAT_HISTORY = "chat_history"
    }

    private lateinit var rvMessages: RecyclerView
    private lateinit var etInput: EditText
    private lateinit var btnSend: Button
    private lateinit var btnBack: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var elderlyDataRepository: ElderlyDataRepository
    private lateinit var prefs: SharedPreferences

    private var isAiTyping = false
    private lateinit var textToSpeech: TextToSpeech
    private var isSpeaking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        elderlyDataRepository = ElderlyDataRepository.getInstance(this)
        textToSpeech = TextToSpeech(this, this)
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        initViews()
        setupRecyclerView()
        setupInputArea()
        setupClickListeners()

        loadChatHistory()
    }

    override fun onPause() {
        super.onPause()
        saveChatHistory()
    }

    private fun initViews() {
        rvMessages = findViewById(R.id.rvMessages)
        etInput = findViewById(R.id.etInput)
        btnSend = findViewById(R.id.btnSend)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter { messageContent ->
            speakText(messageContent)
        }
        layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        rvMessages.apply {
            adapter = chatAdapter
            layoutManager = this@ChatActivity.layoutManager
            itemAnimator = null
        }
    }

    private fun setupInputArea() {
        etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnSend.isEnabled = !s.isNullOrBlank() && !isAiTyping
            }
        })

        etInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
    }

    private fun setupClickListeners() {
        btnSend.setOnClickListener { sendMessage() }
        btnBack.setOnClickListener { finish() }
    }

    private fun addWelcomeMessage() {
        val user = elderlyDataRepository.getCurrentUser()
        val name = user?.nickname ?: user?.realName ?: "长辈"
        val welcomeText = "您好！我是您的 AI 养老助手。我了解 ${name} 的健康状况和生活情况，可以为您提供健康建议和日常关怀帮助。请告诉我有什么可以帮您的？"
        chatAdapter.addAiMessage(welcomeText, isComplete = true)
        scrollToBottom()
    }

    private fun saveChatHistory() {
        val messages = chatAdapter.getAllMessages()
        val jsonArray = JSONArray()
        for (msg in messages) {
            val jsonObj = JSONObject().apply {
                put("id", msg.id)
                put("content", msg.content)
                put("isUser", msg.isUser)
                put("timestamp", msg.timestamp)
                put("isComplete", msg.isComplete)
            }
            jsonArray.put(jsonObj)
        }
        prefs.edit().putString(KEY_CHAT_HISTORY, jsonArray.toString()).apply()
    }

    private fun loadChatHistory() {
        val jsonStr = prefs.getString(KEY_CHAT_HISTORY, null)
        if (jsonStr != null) {
            try {
                val jsonArray = JSONArray(jsonStr)
                val messages = mutableListOf<ChatAdapter.ChatMessage>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObj = jsonArray.getJSONObject(i)
                    messages.add(ChatAdapter.ChatMessage(
                        id = jsonObj.getString("id"),
                        content = jsonObj.getString("content"),
                        isUser = jsonObj.getBoolean("isUser"),
                        timestamp = jsonObj.getLong("timestamp"),
                        isComplete = jsonObj.getBoolean("isComplete")
                    ))
                }
                if (messages.isNotEmpty()) {
                    chatAdapter.setMessages(messages)
                    scrollToBottom()
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载聊天记录失败", e)
            }
        }
        addWelcomeMessage()
    }

    private fun sendMessage() {
        val userMessage = etInput.text.toString().trim()
        if (userMessage.isEmpty()) return
        if (isAiTyping) {
            Toast.makeText(this, "AI 正在回复，请稍候...", Toast.LENGTH_SHORT).show()
            return
        }

        etInput.text.clear()
        chatAdapter.addUserMessage(userMessage)
        scrollToBottom()

        val aiMessageId = chatAdapter.addAiMessage("正在思考...", isComplete = false)
        scrollToBottom()

        isAiTyping = true
        btnSend.isEnabled = false

        lifecycleScope.launch {
            requestAiResponse(userMessage, aiMessageId)
        }
    }

    private suspend fun requestAiResponse(userMessage: String, aiMessageId: String) {
        try {
            val elderlyStatus = elderlyDataRepository.getElderlyStatus()

            val history = chatAdapter.getMessages()
                .filter { it.content != "正在思考..." }
                .takeLast(10)
                .map { PromptBuilder.ChatMessage(if (it.isUser) "user" else "assistant", it.content) }

            val prompt = buildPrompt(elderlyStatus, userMessage, history)

            val result = KimiApiService.chat(userMessage = prompt, apiKey = API_KEY)

            withContext(Dispatchers.Main) {
                result.onSuccess { reply ->
                    typewriterEffect(aiMessageId, reply)
                }.onFailure { error ->
                    Log.e(TAG, "AI 请求失败", error)
                    updateAiMessage(aiMessageId, "抱歉，AI 服务暂时不可用，请稍后再试。\n错误信息：${error.message}")
                    isAiTyping = false
                    btnSend.isEnabled = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "请求异常", e)
            withContext(Dispatchers.Main) {
                updateAiMessage(aiMessageId, "抱歉，发生异常：${e.message}", isComplete = true)
                isAiTyping = false
                btnSend.isEnabled = true
            }
        }
    }

    private suspend fun typewriterEffect(messageId: String, fullText: String) {
        val delayMs = 30L

        withContext(Dispatchers.Main) {
            for (i in 1..fullText.length) {
                val partialText = fullText.substring(0, i)
                updateAiMessage(messageId, partialText, isComplete = false)
                scrollToBottom()
                delay(delayMs)
            }
            updateAiMessage(messageId, fullText, isComplete = true)
            chatAdapter.notifyDataSetChanged()
            scrollToBottom()
            delay(50)
            isAiTyping = false
            btnSend.isEnabled = true
        }
    }

    private fun updateAiMessage(messageId: String, content: String, isComplete: Boolean = false) {
        chatAdapter.updateAiMessage(messageId, content, isComplete)
        if (messageId == chatAdapter.getLastAiMessageId()) {
            scrollToBottom()
        }
    }

    private fun buildPrompt(
        status: PromptBuilder.ElderlyStatus,
        userMessage: String,
        history: List<PromptBuilder.ChatMessage>
    ): String {
        val systemPrompt = PromptBuilder.buildSystemPrompt(status)
        val historyText = if (history.isNotEmpty()) {
            "\n\n【对话历史】\n" + history.joinToString("\n") {
                "${if (it.role == "user") "用户" else "助手"}：${it.content}"
            } + "\n\n请根据以上对话历史和长辈情况，继续回答。"
        } else ""

        return "$systemPrompt$historyText\n\n【用户最新消息】\n$userMessage"
    }

    private fun scrollToBottom() {
        rvMessages.post {
            val itemCount = chatAdapter.itemCount
            if (itemCount > 0) {
                layoutManager.scrollToPosition(itemCount - 1)
            }
        }
    }

    private fun speakText(text: String) {
        if (isSpeaking) {
            textToSpeech.stop()
            isSpeaking = false
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            isSpeaking = true
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "不支持中文语音", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "语音初始化失败", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onDestroy()
    }
}