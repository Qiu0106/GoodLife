package com.example.smartelderlycare_app.component

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.Post
import com.example.smartelderlycare_app.ui.viewmodel.PostViewModel
import com.google.android.material.button.MaterialButton

class CreatePostActivity : AppCompatActivity() {

    private lateinit var btnCancel: TextView
    private lateinit var btnPublish: MaterialButton
    private lateinit var etPostContent: EditText

    private val viewModel: PostViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        btnCancel = findViewById(R.id.btn_cancel)
        btnPublish = findViewById(R.id.btn_publish)
        etPostContent = findViewById(R.id.et_post_content)

        btnCancel.setOnClickListener {
            finish()
        }

        btnPublish.setOnClickListener {
            publishPost()
        }
    }

    private fun observeViewModel() {
        // 观察加载状态
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        })

        // 观察错误信息
        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        })

        // 观察成功信息
        viewModel.successMessage.observe(this, Observer { successMessage ->
            successMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
                finish() // 发布成功后关闭页面
            }
        })
    }

    private fun publishPost() {
        val content = etPostContent.text.toString().trim()

        if (content.isEmpty()) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
            return
        }

        // 获取当前用户ID和用户名
        val sharedPreferences = getSharedPreferences("user", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "anonymous") ?: "anonymous"
        val userName = sharedPreferences.getString("userName", "匿名用户") ?: "匿名用户"

        // 创建Post对象
        val post = Post(
            userId = userId,
            title = content.take(20) + if (content.length > 20) "..." else "", // 取前20字作为标题
            content = content,
            coverImageUrl = null, // 暂时不支持图片上传
            userName = userName,
            userAvatarUrl = null,
            likeCount = 0,
            commentCount = 0
        )

        // 上传到Bmob服务器
        viewModel.createPost(post)
    }

    private fun showProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = ProgressDialog(this).apply {
            setMessage("正在发布...")
            setCancelable(false)
            show()
        }
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
    }
}
