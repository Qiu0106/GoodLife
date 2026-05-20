package com.example.smartelderlycare_app.component.cemetery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImagePagerAdapter(
    private val imageUrls: List<String>,
    private val onBindImage: (String, ImageView) -> Unit
) : RecyclerView.Adapter<ImagePagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return ViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBindImage(imageUrls[position], holder.imageView)
    }

    override fun getItemCount(): Int = imageUrls.size

    class ViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}