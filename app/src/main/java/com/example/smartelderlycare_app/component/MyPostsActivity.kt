package com.example.smartelderlycare_app.component

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.repository.BmobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyPostsActivity : AppCompatActivity() {

    private val repository = BmobRepository()
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_posts)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.adapter = PostAdapter(emptyList(), this)

        loadMyPosts()
    }

    private fun loadMyPosts() {
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        val userId = prefs.getString("userObjectId", null)
            ?: prefs.getString("objectId", null)
        if (userId.isNullOrEmpty()) return
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = repository.getPostsByUserId(userId)
                result.onSuccess { posts ->
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        if (posts.isEmpty()) {
                            tvEmpty.visibility = View.VISIBLE
                        } else {
                            tvEmpty.visibility = View.GONE
                            val adapter = PostAdapter(posts, this@MyPostsActivity)
                            recyclerView.adapter = adapter
                            adapter.loadLikeStatus(userId)
                        }
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
