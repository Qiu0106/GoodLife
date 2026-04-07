package com.example.smartelderlycare_app.component

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.smartelderlycare_app.R
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var ivAvatar: CircleImageView
    private lateinit var tvGreeting: TextView
    private lateinit var tvDate: TextView

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

        Log.d(TAG, "用户已登录 (userId=$userId)，显示主界面")

        try {
            setContentView(R.layout.activity_main)
            Log.d(TAG, "setContentView 成功")

            initViews()
            loadUserAvatar()
            updateGreetingMessage()
            setupClickListeners()

            Log.d(TAG, "onCreate 执行完成，界面应该已显示")

        } catch (e: Exception) {
            Log.e(TAG, "MainActivity 初始化失败", e)
            e.printStackTrace()
            Toast.makeText(this, "主界面加载失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        ivAvatar = findViewById(R.id.iv_avatar)
        tvGreeting = findViewById(R.id.tv_greeting)
        tvDate = findViewById(R.id.tv_date)

        Log.d(TAG, "所有组件初始化成功")
    }

    private fun loadUserAvatar() {
        try {
            val prefs = getSharedPreferences("user", MODE_PRIVATE)
            val avatarUrl = prefs.getString("avatarUrl", null)

            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .circleCrop()
                    .into(ivAvatar)

                Log.d(TAG, "头像加载成功: $avatarUrl")
            } else {
                // 使用默认图标
                ivAvatar.setImageResource(R.mipmap.ic_launcher)
                Log.d(TAG, "使用默认头像")
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载头像失败", e)
            ivAvatar.setImageResource(R.mipmap.ic_launcher)
        }
    }

    private fun updateGreetingMessage() {
        // 获取当前时间
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        // 根据时间生成问候语
        val greeting = when {
            hourOfDay in 6..11 -> "早上好"
            hourOfDay in 12..13 -> "中午好"
            hourOfDay in 14..17 -> "下午好"
            hourOfDay in 18..21 -> "晚上好"
            else -> "夜深了"
        }

        // 获取用户昵称
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        val nickname = prefs.getString("nickname", null) ?: "朋友"

        // 设置问候语
        tvGreeting.text = "$greeting，$nickname"

        // 设置日期（中文格式）
        val dateFormat = SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINESE)
        tvDate.text = dateFormat.format(calendar.time)

        Log.d(TAG, "问候语更新完成: $greeting, $nickname")
    }

    private fun setupClickListeners() {
        // 打卡签到
        findViewById<CardView>(R.id.card_checkin).setOnClickListener {
            startActivity(Intent(this, CheckinActivity::class.java))
        }

        // 社区动态
        findViewById<CardView>(R.id.card_community).setOnClickListener {
            startActivity(Intent(this, CommunityActivity::class.java))
        }

        // 身后事定制
        findViewById<CardView>(R.id.card_afterlife).setOnClickListener {
            startActivity(Intent(this, AfterlifeCustomActivity::class.java))
        }

        // 星空纪念馆
        findViewById<CardView>(R.id.card_starry_sky).setOnClickListener {
            startActivity(Intent(this, StarMemorialComposeActivity::class.java))
        }

        // 我的中心
        findViewById<CardView>(R.id.card_mine).setOnClickListener {
            startActivity(Intent(this, MineActivity::class.java))
        }

        // 语音助手
        findViewById<CardView>(R.id.btn_voice_assistant).setOnClickListener {
            showToast("正在唤醒：语音助手...")
            // TODO: 集成语音助手功能
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - 刷新数据")
        // 返回时刷新问候语和头像（防止切换日期或更换头像）
        loadUserAvatar()
        updateGreetingMessage()
    }
}
