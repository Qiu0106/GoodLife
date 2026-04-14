package com.example.smartelderlycare_app.component

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.ui.viewmodel.PostViewModel
import com.google.android.material.card.MaterialCardView

class CommunityActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: PostAdapter
    private lateinit var tabActive: MaterialCardView
    private lateinit var tabEnded: MaterialCardView
    private lateinit var tvTabActive: TextView
    private lateinit var tvTabEnded: TextView

    private var isActiveTab = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        recyclerView = findViewById(R.id.recyclerView_community)
        progressBar = findViewById(R.id.progressBar)
        tabActive = findViewById(R.id.tabActive)
        tabEnded = findViewById(R.id.tabEnded)
        tvTabActive = findViewById(R.id.tvTabActive)
        tvTabEnded = findViewById(R.id.tvTabEnded)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        adapter = PostAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        setupTabs()
        observeViewModel()
        viewModel.getAllPosts()

        findViewById<com.google.android.material.button.MaterialButton>(R.id.fab_add_post).setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
    }

    private fun performHapticFeedback(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            @Suppress("DEPRECATION")
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    private fun setupTabs() {
        tabActive.setOnClickListener {
            performHapticFeedback(it)
            isActiveTab = true
            updateTabUI()
        }
        tabEnded.setOnClickListener {
            performHapticFeedback(it)
            isActiveTab = false
            updateTabUI()
        }
    }

    private fun updateTabUI() {
        if (isActiveTab) {
            tabActive.setCardBackgroundColor(resources.getColor(R.color.elderly_brand, null))
            tabActive.strokeColor = resources.getColor(R.color.elderly_brand, null)
            tvTabActive.setTextColor(resources.getColor(R.color.white, null))

            tabEnded.setCardBackgroundColor(resources.getColor(R.color.elderly_surface, null))
            tabEnded.strokeColor = resources.getColor(R.color.elderly_divider, null)
            tvTabEnded.setTextColor(resources.getColor(R.color.elderly_text_secondary, null))
        } else {
            tabEnded.setCardBackgroundColor(resources.getColor(R.color.elderly_brand, null))
            tabEnded.strokeColor = resources.getColor(R.color.elderly_brand, null)
            tvTabEnded.setTextColor(resources.getColor(R.color.white, null))

            tabActive.setCardBackgroundColor(resources.getColor(R.color.elderly_surface, null))
            tabActive.strokeColor = resources.getColor(R.color.elderly_divider, null)
            tvTabActive.setTextColor(resources.getColor(R.color.elderly_text_secondary, null))
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

    override fun onResume() {
        super.onResume()
        viewModel.getAllPosts()
    }
}
