package com.example.smartelderlycare_app.component

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.Comment
import com.example.smartelderlycare_app.data.model.PostInteraction
import com.example.smartelderlycare_app.data.repository.BmobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class PostDetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var toolbar: Toolbar
    private lateinit var llImagesContainer: LinearLayout
    private lateinit var ivUserAvatar: ImageView
    private lateinit var tvPostTitle: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvPostContent: TextView
    private lateinit var btnVoice: ImageButton
    private lateinit var etCommentInput: EditText
    private lateinit var btnSendComment: ImageButton
    private lateinit var layoutLike: LinearLayout
    private lateinit var ivLike: ImageView
    private lateinit var tvLikeCount: TextView
    private lateinit var layoutFavorite: LinearLayout
    private lateinit var ivFavorite: ImageView
    private lateinit var tvFavoriteCount: TextView
    private lateinit var layoutShare: LinearLayout
    private lateinit var rvComments: RecyclerView

    private lateinit var textToSpeech: TextToSpeech
    private var isSpeaking = false
    private var isLiked = false
    private var isFavorite = false
    private var likeInteractionId: String? = null
    private var favoriteInteractionId: String? = null

    private val repository = BmobRepository()
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()

    private var postObjectId: String = ""
    private var title: String = ""
    private var userName: String = ""
    private var content: String = ""
    private var coverImageUrl: String = ""
    private var userAvatarUrl: String = ""
    private var imageUrls: ArrayList<String> = arrayListOf()
    private var likeCount: Int = 0
    private var favoriteCount: Int = 0
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        initViews()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        userId = prefs.getString("userObjectId", null)
            ?: prefs.getString("objectId", null) ?: ""

        receiveIntentData()
        displayPostData()
        setupCommentRecyclerView()

        textToSpeech = TextToSpeech(this, this)
        loadInteractionStatus()
        loadComments()
        setupButtonListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        llImagesContainer = findViewById(R.id.ll_images_container)
        ivUserAvatar = findViewById(R.id.iv_user_avatar)
        tvPostTitle = findViewById(R.id.tv_post_title)
        tvUserName = findViewById(R.id.tv_user_name)
        tvPostContent = findViewById(R.id.tv_post_content)
        btnVoice = findViewById(R.id.btn_voice)
        etCommentInput = findViewById(R.id.et_comment_input)
        btnSendComment = findViewById(R.id.btn_send_comment)
        layoutLike = findViewById(R.id.layout_like)
        ivLike = findViewById(R.id.iv_like)
        tvLikeCount = findViewById(R.id.tv_like_count)
        layoutFavorite = findViewById(R.id.layout_favorite)
        ivFavorite = findViewById(R.id.iv_favorite)
        tvFavoriteCount = findViewById(R.id.tv_favorite_count)
        layoutShare = findViewById(R.id.layout_share)
        rvComments = findViewById(R.id.rv_comments)
    }

    private fun setupCommentRecyclerView() {
        commentAdapter = CommentAdapter(commentList)
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter
    }

    private fun receiveIntentData() {
        intent.apply {
            postObjectId = getStringExtra("postObjectId") ?: ""
            title = getStringExtra("title") ?: ""
            userName = getStringExtra("userName") ?: "匿名用户"
            content = getStringExtra("content") ?: ""
            coverImageUrl = getStringExtra("coverImageUrl") ?: ""
            userAvatarUrl = getStringExtra("userAvatarUrl") ?: ""
            imageUrls = getStringArrayListExtra("imageUrls") ?: arrayListOf()
            likeCount = getIntExtra("likeCount", 0)
            favoriteCount = getIntExtra("favoriteCount", 0)
        }
    }

    private fun displayPostData() {
        tvPostTitle.text = title
        tvUserName.text = userName
        tvPostContent.text = content
        tvLikeCount.text = likeCount.toString()
        tvFavoriteCount.text = favoriteCount.toString()

        if (coverImageUrl.isNotEmpty()) {
            if (!imageUrls.contains(coverImageUrl)) {
                imageUrls.add(0, coverImageUrl)
            }
        }

        lifecycleScope.launch { loadUserAvatar() }
        lifecycleScope.launch { loadPostImages() }
    }

    private fun loadComments() {
        if (postObjectId.isEmpty()) return
        lifecycleScope.launch {
            repository.getCommentsByPostId(postObjectId).onSuccess { comments ->
                commentList.clear()
                commentList.addAll(comments)
                commentAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadInteractionStatus() {
        if (userId.isEmpty() || postObjectId.isEmpty()) return
        lifecycleScope.launch {
            repository.getInteraction(userId, postObjectId, "like").onSuccess { interaction ->
                if (interaction != null) {
                    isLiked = true
                    likeInteractionId = interaction.objectId
                    runOnUiThread { updateLikeIcon() }
                }
            }
            repository.getInteraction(userId, postObjectId, "favorite").onSuccess { interaction ->
                if (interaction != null) {
                    isFavorite = true
                    favoriteInteractionId = interaction.objectId
                    runOnUiThread { updateFavoriteIcon() }
                }
            }
        }
    }

    private suspend fun loadUserAvatar() {
        withContext(Dispatchers.Main) {
            if (userAvatarUrl.isNotEmpty()) {
                val httpUrl = userAvatarUrl.replace("https://", "http://")
                Glide.with(this@PostDetailActivity)
                    .load(httpUrl)
                    .apply(RequestOptions()
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.mipmap.ic_launcher_round)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop())
                    .into(ivUserAvatar)
            }
        }
    }

    private suspend fun loadPostImages() {
        withContext(Dispatchers.Main) {
            llImagesContainer.removeAllViews()

            for ((index, url) in imageUrls.withIndex()) {
                val httpUrl = url.replace("https://", "http://")
                val imageView = ImageView(this@PostDetailActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 8.dpToPx()
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = true
                    setPadding(4.dpToPx())
                }

                Glide.with(this@PostDetailActivity)
                    .load(httpUrl)
                    .apply(RequestOptions()
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop())
                    .into(imageView)

                imageView.setOnClickListener { showImagePreview(index) }
                llImagesContainer.addView(imageView)
            }

            if (imageUrls.isEmpty()) {
                llImagesContainer.visibility = View.GONE
            }
        }
    }

    private fun showImagePreview(index: Int) {
        val intent = Intent(this, ImagePreviewActivity::class.java).apply {
            putStringArrayListExtra("images", imageUrls)
            putExtra("currentIndex", index)
        }
        startActivity(intent)
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun updateLikeIcon() {
        ivLike.setImageResource(if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
    }

    private fun updateFavoriteIcon() {
        ivFavorite.setImageResource(if (isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline_yellow)
    }

    private fun toggleLike() {
        if (userId.isEmpty() || postObjectId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            if (isLiked) {
                if (!likeInteractionId.isNullOrEmpty()) {
                    repository.removeInteraction(likeInteractionId!!)
                    repository.decrementPostLikes(postObjectId)
                    isLiked = false
                    likeInteractionId = null
                    likeCount = (likeCount - 1).coerceAtLeast(0)
                }
            } else {
                val interaction = PostInteraction(userId = userId, postId = postObjectId, type = "like")
                repository.addInteraction(interaction).onSuccess { saved ->
                    likeInteractionId = saved.objectId
                }
                repository.incrementPostLikes(postObjectId)
                isLiked = true
                likeCount += 1
                ivLike.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).withEndAction {
                    ivLike.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                }.start()
            }
            runOnUiThread {
                updateLikeIcon()
                tvLikeCount.text = likeCount.toString()
            }
        }
    }

    private fun toggleFavorite() {
        if (userId.isEmpty() || postObjectId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            if (isFavorite) {
                if (!favoriteInteractionId.isNullOrEmpty()) {
                    repository.removeInteraction(favoriteInteractionId!!)
                    repository.decrementPostFavorites(postObjectId)
                    isFavorite = false
                    favoriteInteractionId = null
                    favoriteCount = (favoriteCount - 1).coerceAtLeast(0)
                }
            } else {
                val interaction = PostInteraction(userId = userId, postId = postObjectId, type = "favorite")
                repository.addInteraction(interaction).onSuccess { saved ->
                    favoriteInteractionId = saved.objectId
                    repository.incrementPostFavorites(postObjectId)
                    isFavorite = true
                    favoriteCount += 1
                    ivFavorite.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).withEndAction {
                        ivFavorite.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                    }.start()
                    runOnUiThread {
                        updateFavoriteIcon()
                        tvFavoriteCount.text = favoriteCount.toString()
                        Toast.makeText(this@PostDetailActivity, "已加入收藏", Toast.LENGTH_SHORT).show()
                    }
                }.onFailure {
                    runOnUiThread {
                        Toast.makeText(this@PostDetailActivity, "收藏失败，请重试", Toast.LENGTH_SHORT).show()
                    }
                }
                return@launch
            }
            runOnUiThread {
                updateFavoriteIcon()
                tvFavoriteCount.text = favoriteCount.toString()
            }
        }
    }

    private fun sharePost() {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n\n$content\n\n作者：$userName")
            startActivity(Intent.createChooser(this, "分享到"))
        }
    }

    private fun setupButtonListeners() {
        btnVoice.setOnClickListener {
            if (isSpeaking) {
                textToSpeech.stop()
                isSpeaking = false
                btnVoice.setImageResource(R.drawable.ic_audio_vol)
            } else {
                speakText()
                isSpeaking = true
                btnVoice.setImageResource(R.drawable.ic_media_pause)
            }
        }
        layoutShare.setOnClickListener { sharePost() }
        layoutLike.setOnClickListener { toggleLike() }
        layoutFavorite.setOnClickListener { toggleFavorite() }
        btnSendComment.setOnClickListener { sendComment() }
        etCommentInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendComment()
                true
            } else {
                false
            }
        }
    }

    private fun sendComment() {
        val commentContent = etCommentInput.text.toString().trim()
        if (commentContent.isEmpty()) {
            Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show()
            return
        }
        if (userId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
            return
        }

        btnSendComment.isEnabled = false
        lifecycleScope.launch {
            val prefs = getSharedPreferences("user", MODE_PRIVATE)
            val comment = Comment(
                postId = postObjectId,
                authorName = prefs.getString("nickname", "匿名用户") ?: "匿名用户",
                authorAvatar = prefs.getString("avatarUrl", null),
                content = commentContent
            )

            repository.createComment(comment).onSuccess {
                runOnUiThread {
                    etCommentInput.text.clear()
                    Toast.makeText(this@PostDetailActivity, "评论发送成功", Toast.LENGTH_SHORT).show()
                    loadComments()
                }
            }.onFailure {
                runOnUiThread {
                    Toast.makeText(this@PostDetailActivity, "评论发送失败: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
            runOnUiThread {
                btnSendComment.isEnabled = true
            }
        }
    }

    private fun speakText() {
        val text = "$title。$content"
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}