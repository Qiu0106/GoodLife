package com.example.smartelderlycare_app.component

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.ui.viewmodel.PostViewModel
import com.google.android.material.tabs.TabLayout

class CommunityActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: PostAdapter
    private lateinit var tabLayout: TabLayout

    private val categories = listOf("推荐", "健康养生", "日常闲聊", "社区活动")
    private var currentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        recyclerView = findViewById(R.id.recyclerView_community)
        progressBar = findViewById(R.id.progressBar)
        tabLayout = findViewById(R.id.tab_layout)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        adapter = PostAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        setupTabLayout()
        observeViewModel()
        viewModel.getAllPosts()

        findViewById<com.google.android.material.button.MaterialButton>(R.id.fab_add_post).setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
    }

    private fun setupTabLayout() {
        categories.forEach { category ->
            tabLayout.addTab(tabLayout.newTab().setText(category))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    performHapticFeedback(it.view)
                    val position = it.position
                    currentCategory = if (position == 0) null else categories[position]
                    loadPostsByCategory(currentCategory)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadPostsByCategory(category: String?) {
        if (category == null) {
            viewModel.getAllPosts()
        } else {
            viewModel.getPostsByCategory(category)
        }
    }

    private fun observeViewModel() {
        viewModel.posts.observe(this, Observer { posts ->
            adapter = PostAdapter(posts, this)
            recyclerView.adapter = adapter
            val prefs = getSharedPreferences("user", MODE_PRIVATE)
            val userObjectId = prefs.getString("userObjectId", null)
                ?: prefs.getString("objectId", null)
            if (userObjectId != null) {
                adapter.loadLikeStatus(userObjectId)
            }
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        })
    }

    private fun performHapticFeedback(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            @Suppress("DEPRECATION")
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    override fun onResume() {
        super.onResume()
        loadPostsByCategory(currentCategory)
    }
}