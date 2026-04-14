package com.example.smartelderlycare_app.component

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.smartelderlycare_app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tvIndicator: TextView
    private lateinit var gestureDetector: GestureDetector
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private var images: ArrayList<String> = arrayListOf()
    private var currentIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        viewPager = findViewById(R.id.vp_images)
        tvIndicator = findViewById(R.id.tv_indicator)

        intent.apply {
            images = getStringArrayListExtra("images") ?: arrayListOf()
            currentIndex = getIntExtra("currentIndex", 0)
        }

        setupViewPager()
        setupGestureDetector()
        
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun setupViewPager() {
        val adapter = ImagePagerAdapter(this, images, httpClient)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(currentIndex, false)

        tvIndicator.text = if (images.size > 1) "${currentIndex + 1} / ${images.size}" else ""

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentIndex = position
                tvIndicator.text = if (images.size > 1) "${position + 1} / ${images.size}" else ""
                tvIndicator.isVisible = images.size > 1
            }
        })
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                finish()
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { gestureDetector.onTouchEvent(it) }
        return super.onTouchEvent(event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}

class ImagePagerAdapter(
    private val activity: AppCompatActivity,
    private val images: List<String>,
    private val httpClient: OkHttpClient
) : androidx.recyclerview.widget.RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_preview_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_preview, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = images[position].replace("https://", "http://")
        
        activity.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful && response.body != null) {
                    val bytes = response.body!!.bytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    withContext(Dispatchers.Main) {
                        if (!activity.isDestroyed && !activity.isFinishing && bitmap != null) {
                            holder.imageView.setImageBitmap(bitmap)
                        }
                    }
                }
                response.close()
            } catch (_: Exception) {}
        }
    }

    override fun getItemCount() = images.size
}