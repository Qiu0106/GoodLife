package com.example.smartelderlycare_app.component

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.Post
import com.example.smartelderlycare_app.data.model.PostInteraction
import com.example.smartelderlycare_app.data.repository.BmobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.ArrayList

class PostAdapter(
    private val postList: List<Post>,
    private val lifecycleOwner: LifecycleOwner? = null
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private val repository = BmobRepository()
    private val likedPosts = mutableSetOf<String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.iv_post_cover)
        val ivUserAvatar: ImageView = view.findViewById(R.id.iv_user_avatar)
        val ivLikeIcon: ImageView = view.findViewById(R.id.iv_like_icon)
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
        holder.tvUser.text = post.userName ?: "匿名用户"
        holder.tvLikes.text = formatCount(post.likeCount)

        val postObjectId = post.objectId ?: ""
        if (likedPosts.contains(postObjectId)) {
            holder.ivLikeIcon.setImageResource(R.drawable.ic_heart_filled)
        } else {
            holder.ivLikeIcon.setImageResource(R.drawable.ic_heart_outline)
        }

        if (!post.coverImageUrl.isNullOrEmpty()) {
            loadImage(post.coverImageUrl!!, holder.ivCover)
        } else {
            holder.ivCover.setImageResource(R.mipmap.ic_launcher)
        }

        if (!post.userAvatarUrl.isNullOrEmpty()) {
            loadImage(post.userAvatarUrl!!, holder.ivUserAvatar)
        } else {
            holder.ivUserAvatar.setImageResource(R.mipmap.ic_launcher_round)
        }

        holder.ivLikeIcon.setOnClickListener {
            toggleLike(post, holder)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val imageUrlList = parseImageUrls(post.imageUrls)

            val intent = Intent(context, PostDetailActivity::class.java).apply {
                putExtra("postId", post.id?.toString() ?: "")
                putExtra("postObjectId", postObjectId)
                putExtra("title", post.title)
                putExtra("userName", post.userName ?: "匿名用户")
                putExtra("userAvatarUrl", post.userAvatarUrl)
                putExtra("content", post.content)
                putExtra("coverImageUrl", post.coverImageUrl)
                putStringArrayListExtra("imageUrls", ArrayList(imageUrlList))
                putExtra("likeCount", post.likeCount)
                putExtra("favoriteCount", post.favoriteCount)
            }
            context.startActivity(intent)
        }
    }

    private fun getUserObjectId(context: android.content.Context): String {
        return context.getSharedPreferences("user", android.content.Context.MODE_PRIVATE)
            .getString("userObjectId", null)
            ?: context.getSharedPreferences("user", android.content.Context.MODE_PRIVATE)
                .getString("objectId", null) ?: ""
    }

    private fun toggleLike(post: Post, holder: ViewHolder) {
        val context = holder.itemView.context
        val postObjectId = post.objectId ?: return
        val userId = getUserObjectId(context)
        if (userId.isEmpty()) return

        if (likedPosts.contains(postObjectId)) {
            lifecycleOwner?.lifecycleScope?.launch {
                val interactionResult = repository.getInteraction(userId, postObjectId, "like")
                interactionResult.onSuccess { interaction ->
                    if (interaction != null) {
                        repository.removeInteraction(interaction.objectId ?: "")
                        repository.decrementPostLikes(postObjectId)
                        likedPosts.remove(postObjectId)
                        holder.ivLikeIcon.setImageResource(R.drawable.ic_heart_outline)
                        val newCount = (post.likeCount - 1).coerceAtLeast(0)
                        holder.tvLikes.text = formatCount(newCount)
                    }
                }
            }
        } else {
            lifecycleOwner?.lifecycleScope?.launch {
                val interaction = PostInteraction(
                    userId = userId,
                    postId = postObjectId,
                    type = "like"
                )
                repository.addInteraction(interaction)
                repository.incrementPostLikes(postObjectId)
                likedPosts.add(postObjectId)
                holder.ivLikeIcon.setImageResource(R.drawable.ic_heart_filled)
                holder.tvLikes.text = formatCount(post.likeCount + 1)

                holder.ivLikeIcon.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).withEndAction {
                    holder.ivLikeIcon.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                }.start()
            }
        }
    }

    fun loadLikeStatus(userId: String) {
        lifecycleOwner?.lifecycleScope?.launch {
            for (post in postList) {
                val postObjectId = post.objectId ?: continue
                val result = repository.getInteraction(userId, postObjectId, "like")
                result.onSuccess { interaction ->
                    if (interaction != null) {
                        likedPosts.add(postObjectId)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 10000 -> "${count / 10000}w"
            count >= 1000 -> "${count / 1000}k"
            else -> count.toString()
        }
    }

    private fun parseImageUrls(imageUrls: String?): List<String> {
        if (imageUrls.isNullOrEmpty()) return emptyList()
        return try {
            val jsonArray = JSONArray(imageUrls)
            (0 until jsonArray.length()).map { jsonArray.getString(it) }
        } catch (_: Exception) { listOf(imageUrls) }
    }

    private fun loadImage(url: String, imageView: ImageView) {
        val httpUrl = url.replace("https://", "http://")
        Glide.with(imageView.context)
            .load(httpUrl)
            .apply(RequestOptions()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .centerCrop())
            .into(imageView)
    }

    override fun getItemCount() = postList.size
}
