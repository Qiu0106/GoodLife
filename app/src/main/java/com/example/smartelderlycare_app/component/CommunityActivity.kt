package com.example.smartelderlycare_app.component

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.ui.viewmodel.PostViewModel

class CommunityActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        recyclerView = findViewById(R.id.recyclerView_community)
        progressBar = findViewById(R.id.progressBar)

        // 设置瀑布流布局
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager

        // 初始化空的adapter
        adapter = PostAdapter(emptyList())
        recyclerView.adapter = adapter

        // 观察ViewModel状态
        observeViewModel()

        // 加载帖子数据
        viewModel.getAllPosts()

        // 发布新帖子按钮
        findViewById<FloatingActionButton>(R.id.fab_add_post).setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
    }

    private fun observeViewModel() {
        // 观察帖子列表
        viewModel.posts.observe(this, Observer { posts ->
            adapter = PostAdapter(posts)
            recyclerView.adapter = adapter
        })

        // 观察加载状态
        viewModel.isLoading.observe(this, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        // 观察错误信息
        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // 返回页面时刷新数据
        viewModel.getAllPosts()
    }
}