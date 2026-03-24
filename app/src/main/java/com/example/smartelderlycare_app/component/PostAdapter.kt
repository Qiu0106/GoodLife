package com.example.smartelderlycare_app.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartelderlycare_app.R

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
        // 暂时先用系统图标占位，防止内存溢出
        holder.ivCover.setImageResource(post.coverResId)
    }

    override fun getItemCount() = postList.size
}