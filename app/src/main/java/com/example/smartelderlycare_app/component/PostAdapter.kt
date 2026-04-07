package com.example.smartelderlycare_app.component

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.Post

class PostAdapter(private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.iv_post_cover)
        val tvTitle: TextView = view.findViewById(R.id.tv_post_title)
        val tvUser: TextView = view.findViewById(R.id.tv_user_name)
        val tvLikes: TextView = view.findViewById(R.id.tv_like_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_waterfall_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postList[position]
        holder.tvTitle.text = post.title
        holder.tvUser.text = post.userName
        holder.tvLikes.text = post.likeCount.toString()

        // 使用 Glide 加载封面图片
        if (!post.coverImageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.coverImageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .centerCrop()
                .into(holder.ivCover)
        } else {
            // 无图片时使用默认图标
            holder.ivCover.setImageResource(R.mipmap.ic_launcher)
        }

        // 添加点击事件
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra("title", post.title)
            intent.putExtra("userName", post.userName)
            intent.putExtra("likeCount", post.likeCount)
            intent.putExtra("content", post.content)
            intent.putExtra("coverImageUrl", post.coverImageUrl)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = postList.size
}