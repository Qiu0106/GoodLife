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
            Post("今天和张大爷下棋，险胜！", "老王", R.mipmap.ic_launcher, 12, "今天下午和张大爷在社区活动中心下棋，经过激烈的对决，我终于险胜一局。张大爷的棋艺真是越来越好了，我们约定明天再战。下棋不仅能锻炼大脑，还能增进邻里感情，真是一举两得。"),
            Post("分享一个适合老年人的养生食谱，营养又好消化。", "营养师小李", R.mipmap.ic_launcher_round, 88, "今天给大家分享一个适合老年人的养生食谱：南瓜粥。南瓜富含维生素和膳食纤维，有助于消化和增强免疫力。做法简单：将南瓜切块，和大米一起煮成粥，加入适量冰糖调味即可。每天早上喝一碗，对身体非常有益。"),
            Post("社区早操打卡第30天，身体越来越好了。", "晚霞红", R.mipmap.ic_launcher, 56, "今天是我参加社区早操的第30天，感觉身体越来越好了。每天早上8点，我们在社区广场集合，跟着老师做各种伸展运动。坚持锻炼后，我的睡眠质量提高了，走路也更有精神了。希望更多的邻居能加入我们，一起保持健康。"),
            Post("夕阳红摄影展，大家看看我拍得怎么样？", "光影捕捉者", R.mipmap.ic_launcher_round, 102, "最近参加了社区组织的夕阳红摄影展，这是我拍摄的作品。我喜欢捕捉生活中的美好瞬间，比如落日余晖、盛开的花朵、孩子们的笑脸。摄影让我的退休生活变得更加充实和有意义。"),
            Post("如何使用这款智能养老App？新手指南看这里。", "官方助手", R.mipmap.ic_launcher, 999, "很多老年人朋友问我如何使用这款智能养老App，今天就给大家做一个简单的指南。首先，打开App后，你会看到四个主要功能：打卡、社区、语音助手和我的中心。打卡功能可以记录你的每日活动并获得积分；社区功能可以浏览和发布帖子；语音助手可以帮你完成各种操作；我的中心可以管理你的个人信息。如果有任何问题，随时联系我们的客服。"),
            Post("今天天气真好，出来晒晒太阳。", "退休生活", R.mipmap.ic_launcher_round, 45, "今天天气晴朗，阳光明媚，我决定到公园晒晒太阳。公园里人很多，有下棋的、跳舞的、散步的，非常热闹。我找了一个安静的地方坐下，享受温暖的阳光，感觉整个人都放松了。退休生活就应该这样，慢下来，享受生活的美好。")
        )

        recyclerView.adapter = PostAdapter(mockData)

        findViewById<FloatingActionButton>(R.id.fab_add_post).setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
    }
}