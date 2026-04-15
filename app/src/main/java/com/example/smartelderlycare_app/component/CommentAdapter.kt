package com.example.smartelderlycare_app.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.Comment

class CommentAdapter(
    private val comments: List<Comment>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        val tvAuthorName: TextView = itemView.findViewById(R.id.tv_author_name)
        val tvCommentContent: TextView = itemView.findViewById(R.id.tv_comment_content)
        val tvCommentTime: TextView = itemView.findViewById(R.id.tv_comment_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        holder.tvAuthorName.text = comment.authorName
        holder.tvCommentContent.text = comment.content
        holder.tvCommentTime.text = comment.createdAt ?: ""

        if (!comment.authorAvatar.isNullOrEmpty()) {
            val httpUrl = comment.authorAvatar.replace("https://", "http://")
            Glide.with(holder.itemView.context)
                .load(httpUrl)
                .apply(RequestOptions()
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop())
                .into(holder.ivAvatar)
        } else {
            holder.ivAvatar.setImageResource(R.mipmap.ic_launcher_round)
        }
    }

    override fun getItemCount(): Int = comments.size
}