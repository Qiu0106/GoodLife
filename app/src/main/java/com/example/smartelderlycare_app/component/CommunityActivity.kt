package com.example.smartelderlycare_app.component

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.smartelderlycare_app.R

class CommunityActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_community)

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager

        val mockData = listOf(
            Post("今天和张大爷下棋，险胜！", "老王", R.mipmap.ic_launcher, 12),
            Post("分享一个适合老年人的养生食谱，营养又好消化。", "营养师小李", R.mipmap.ic_launcher_round, 88),
            Post("社区早操打卡第30天，身体越来越好了。", "晚霞红", R.mipmap.ic_launcher, 56),
            Post("夕阳红摄影展，大家看看我拍得怎么样？", "光影捕捉者", R.mipmap.ic_launcher_round, 102),
            Post("如何使用这款智能养老App？新手指南看这里。", "官方助手", R.mipmap.ic_launcher, 999),
            Post("今天天气真好，出来晒晒太阳。", "退休生活", R.mipmap.ic_launcher_round, 45)
        )

        recyclerView.adapter = PostAdapter(mockData)

        findViewById<FloatingActionButton>(R.id.fab_add_post).setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
    }
}