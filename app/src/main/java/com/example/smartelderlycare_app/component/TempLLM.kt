package com.example.smartelderlycare_app.component
import com.iflytek.sparkchain.core.LLM
import com.iflytek.sparkchain.core.LLMCallbacks
import com.iflytek.sparkchain.core.LLMConfig
import com.iflytek.sparkchain.core.LLMResult

class TempLLM {
    fun test() {
        val config = LLMConfig.builder()
        val llm = LLM(config)
    }
}
