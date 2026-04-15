package com.example.smartelderlycare_app.component

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.MemorialData
import com.example.smartelderlycare_app.data.repository.BmobRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class StarrySkyActivity : AppCompatActivity() {

    private lateinit var starrySkyView: StarrySkyView
    private lateinit var tvHint: TextView

    private val bmobRepository = BmobRepository()
    private var memorialDataMap = mutableMapOf<String, MemorialData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starry_sky)

        starrySkyView = findViewById(R.id.starrySkyView)
        tvHint = findViewById(R.id.tvHint)

        setupFab()
        setupHintFadeOut()
        setupStarClick()
    }

    override fun onResume() {
        super.onResume()
        starrySkyView.resumeAnimation()
        loadMemorialStars()
    }

    override fun onPause() {
        super.onPause()
        starrySkyView.pauseAnimation()
    }

    private fun setupHintFadeOut() {
        starrySkyView.onFirstUserScrollListener = {
            ObjectAnimator.ofFloat(tvHint, "alpha", 1f, 0f).apply {
                duration = 1000
                start()
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        tvHint.visibility = View.GONE
                    }
                })
            }
        }
    }

    private fun setupFab() {
        val fabAddStar = findViewById<FloatingActionButton>(R.id.fabAddStar)
        fabAddStar.setOnClickListener {
            startActivity(Intent(this, AddMemorialActivity::class.java))
        }
    }

    private fun setupStarClick() {
        starrySkyView.onStarClickListener = { star ->
            memorialDataMap[star.id]?.let { data ->
                showMemorialInfoDialog(data)
            }
        }
    }

    private fun loadMemorialStars() {
        lifecycleScope.launch {
            val result = bmobRepository.getApprovedMemorialStars()

            result.onSuccess { memorialStars ->
                memorialDataMap.clear()
                memorialStars.forEach { star ->
                    memorialDataMap[star.objectId ?: ""] = star.toMemorialData()
                }

                val memorialDataList = memorialStars.map { it.toMemorialData() }
                starrySkyView.setMemorialData(memorialDataList)
            }.onFailure { e ->
                Toast.makeText(
                    this@StarrySkyActivity,
                    "加载纪念星失败：${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showMemorialInfoDialog(data: MemorialData) {
        val dialog = MemorialInfoBottomSheet.newInstance(data)
        dialog.show(supportFragmentManager, "memorial_info")
    }
}