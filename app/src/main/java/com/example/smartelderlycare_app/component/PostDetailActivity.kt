package com.example.smartelderlycare_app.component

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.smartelderlycare_app.R
import java.util.*

class PostDetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var toolbar: Toolbar
    private lateinit var ivPostImage: ImageView
    private lateinit var ivUserAvatar: ImageView
    private lateinit var tvPostTitle: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvPostContent: TextView
    private lateinit var btnVoice: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var btnFavorite: ImageButton
    
    private lateinit var textToSpeech: TextToSpeech
    private var isSpeaking = false
    private var isFavorite = false
    
    private var title: String = ""
    private var userName: String = ""
    private var coverResId: Int = 0
    private var content: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // 初始化视图
        initViews()
        
        // 设置Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        // 接收Intent数据
        receiveIntentData()
        
        // 显示帖子数据
        displayPostData()
        
        // 初始化TextToSpeech
        textToSpeech = TextToSpeech(this, this)
        
        // 初始化收藏状态
        initFavoriteStatus()
        
        // 设置按钮点击事件
        setupButtonListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        ivPostImage = findViewById(R.id.iv_post_image)
        ivUserAvatar = findViewById(R.id.iv_user_avatar)
        tvPostTitle = findViewById(R.id.tv_post_title)
        tvUserName = findViewById(R.id.tv_user_name)
        tvPostContent = findViewById(R.id.tv_post_content)
        btnVoice = findViewById(R.id.btn_voice)
        btnShare = findViewById(R.id.btn_share)
        btnFavorite = findViewById(R.id.btn_favorite)
    }

    private fun receiveIntentData() {
        val intent = intent
        title = intent.getStringExtra("title") ?: ""
        userName = intent.getStringExtra("userName") ?: ""
        coverResId = intent.getIntExtra("coverResId", R.mipmap.ic_launcher)
        content = intent.getStringExtra("content") ?: ""
    }

    private fun displayPostData() {
        tvPostTitle.text = title
        tvUserName.text = userName
        tvPostContent.text = content
        ivPostImage.setImageResource(coverResId)
        
        // 使用 Glide 加载头像
        val avatarUrl = "https://randomuser.me/api/portraits/men/32.jpg" // 示例头像 URL
        Glide.with(this)
            .load(avatarUrl)
            .circleCrop()
            .error(android.R.drawable.ic_menu_report_image)
            .into(ivUserAvatar)
    }
    
    private fun initFavoriteStatus() {
        val sharedPreferences = getSharedPreferences("favorites", MODE_PRIVATE)
        // 使用标题作为 Key（如果没有 postId）
        val key = title
        isFavorite = sharedPreferences.getBoolean(key, false)
        // 更新收藏图标
        updateFavoriteIcon()
    }
    
    private fun updateFavoriteIcon() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_star_filled)
        } else {
            btnFavorite.setImageResource(R.drawable.ic_star_outline)
        }
    }
    
    private fun toggleFavorite() {
        isFavorite = !isFavorite
        // 保存到 SharedPreferences
        val sharedPreferences = getSharedPreferences("favorites", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val key = title
        editor.putBoolean(key, isFavorite)
        editor.apply()
        
        // 更新图标
        updateFavoriteIcon()
        
        // 动画效果
        btnFavorite.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .withEndAction {
                btnFavorite.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
        
        // 显示 Toast
        if (isFavorite) {
            Toast.makeText(this, "已加入收藏", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtonListeners() {
        // 语音播报按钮
        btnVoice.setOnClickListener {
            if (isSpeaking) {
                // 停止播报
                textToSpeech.stop()
                isSpeaking = false
                btnVoice.setImageResource(R.drawable.ic_audio_vol)
            } else {
                // 开始播报
                speakText()
                isSpeaking = true
                btnVoice.setImageResource(R.drawable.ic_media_pause)
            }
        }
        
        // 分享按钮
        btnShare.setOnClickListener {
            sharePost()
        }
        
        // 收藏按钮
        btnFavorite.setOnClickListener {
            toggleFavorite()
        }
    }

    private fun speakText() {
        val text = "$title。$content"
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun sharePost() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        shareIntent.putExtra(Intent.EXTRA_TEXT, "$title\n\n$content\n\n作者：$userName")
        startActivity(Intent.createChooser(shareIntent, "分享到"))
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // 设置语言为中文
            val result = textToSpeech.setLanguage(Locale.CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "不支持中文语音", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "语音初始化失败", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}