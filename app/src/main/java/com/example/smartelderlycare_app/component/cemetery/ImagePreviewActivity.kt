package com.example.smartelderlycare_app.component.cemetery

import android.os.Bundle
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.smartelderlycare_app.R

class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tvIndicator: View
    private lateinit var btnClose: View

    private var imageUrls: List<String> = emptyList()
    private var currentPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_image_preview)

        imageUrls = intent.getStringArrayListExtra("imageUrls") ?: emptyList()
        currentPosition = intent.getIntExtra("position", 0)

        if (imageUrls.isEmpty()) {
            finish()
            return
        }

        viewPager = findViewById(R.id.viewPagerPreview)
        tvIndicator = findViewById(R.id.tvPreviewIndicator)
        btnClose = findViewById(R.id.btnPreviewClose)

        viewPager.adapter = PreviewPagerAdapter(imageUrls)
        viewPager.setCurrentItem(currentPosition, false)
        updateIndicator(currentPosition)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicator(position)
            }
        })

        btnClose.setOnClickListener { finish() }
    }

    private fun updateIndicator(position: Int) {
        if (imageUrls.size > 1) {
            tvIndicator.visibility = View.VISIBLE
            (tvIndicator as? android.widget.TextView)?.text = "${position + 1} / ${imageUrls.size}"
        } else {
            tvIndicator.visibility = View.GONE
        }
    }
}

class PreviewPagerAdapter(
    private val imageUrls: List<String>
) : androidx.recyclerview.widget.RecyclerView.Adapter<PreviewPagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val imageView = android.widget.ImageView(parent.context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            background = ColorDrawable(Color.BLACK)
        }
        return ViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.imageView)
            .load(imageUrls[position])
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imageUrls.size

    class ViewHolder(val imageView: android.widget.ImageView) : androidx.recyclerview.widget.RecyclerView.ViewHolder(imageView)
}