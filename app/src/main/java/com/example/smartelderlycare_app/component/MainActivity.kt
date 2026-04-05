package com.example.smartelderlycare_app.component

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.smartelderlycare_app.R

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate 开始执行...")

        // 检查是否已登录（通过 SharedPreferences）
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        val userId = prefs.getString("userId", null)
        
        if (userId == null) {
            Log.d(TAG, "用户未登录，跳转到登录页面")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        Log.d(TAG, "用户已登录，显示主界面 (userId=$userId)")

        try {
            setContentView(R.layout.activity_main)
            Log.d(TAG, "setContentView 成功")

            val cardCheckin = findViewById<CardView>(R.id.card_checkin)
            val cardCommunity = findViewById<CardView>(R.id.card_community)
            val cardAfterlife = findViewById<CardView>(R.id.card_afterlife)
            val cardStarrySky = findViewById<CardView>(R.id.card_starry_sky)
            val cardVoiceAssistant = findViewById<CardView>(R.id.card_voice_assistant)
            val cardMine = findViewById<CardView>(R.id.card_mine)
            Log.d(TAG, "所有组件初始化成功")

            cardCheckin.setOnClickListener {
                val intent = Intent(this, CheckinActivity::class.java)
                startActivity(intent)
            }
            cardCommunity.setOnClickListener {
                val intent = Intent(this, CommunityActivity::class.java)
                startActivity(intent)
            }

            cardVoiceAssistant.setOnClickListener {
                showToast("正在唤醒：语音助手...")
            }

            cardAfterlife.setOnClickListener {
                val intent = Intent(this, AfterlifeCustomActivity::class.java)
                startActivity(intent)
            }

            cardStarrySky.setOnClickListener {
                val intent = Intent(this, StarMemorialComposeActivity::class.java)
                startActivity(intent)
            }

            cardMine.setOnClickListener {
                val intent = Intent(this, MineActivity::class.java)
                startActivity(intent)
            }

            Log.d(TAG, "onCreate 执行完成，界面应该已显示")

        } catch (e: Exception) {
            Log.e(TAG, "MainActivity 初始化失败", e)
            e.printStackTrace()
            Toast.makeText(this, "主界面加载失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - 界面可见")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
