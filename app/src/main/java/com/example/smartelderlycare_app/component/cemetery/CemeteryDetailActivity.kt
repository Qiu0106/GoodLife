package com.example.smartelderlycare_app.component.cemetery

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.Cemetery
import com.example.smartelderlycare_app.data.repository.BmobRepository
import kotlinx.coroutines.launch

class CemeteryDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CemeteryDetailActivity"
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var layoutContent: ScrollView
    private lateinit var viewPagerImages: ViewPager2
    private lateinit var layoutImageContainer: FrameLayout
    private lateinit var tvImageIndicator: TextView
    private lateinit var tvName: TextView
    private lateinit var tvPriceTag: TextView
    private lateinit var tvDistrict: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvIntroduction: TextView
    private lateinit var tvService: TextView
    private lateinit var tvWebsite: TextView
    private lateinit var btnBack: TextView

    private val repository = BmobRepository()
    private var cemetery: Cemetery? = null
    private var imageUrls: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cemetery_detail)

        initViews()
        loadCemeteryDetail()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        layoutContent = findViewById(R.id.layoutContent)
        viewPagerImages = findViewById(R.id.viewPagerImages)
        tvImageIndicator = findViewById(R.id.tvImageIndicator)
        tvName = findViewById(R.id.tvName)
        tvPriceTag = findViewById(R.id.tvPriceTag)
        tvDistrict = findViewById(R.id.tvDistrict)
        tvAddress = findViewById(R.id.tvAddress)
        tvPhone = findViewById(R.id.tvPhone)
        tvIntroduction = findViewById(R.id.tvIntroduction)
        tvService = findViewById(R.id.tvService)
        tvWebsite = findViewById(R.id.tvWebsite)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
    }

    private fun loadCemeteryDetail() {
        val objectId = intent.getStringExtra("objectId")
        if (objectId.isNullOrEmpty()) {
            Toast.makeText(this, "缺少公墓ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        layoutContent.visibility = View.GONE

        lifecycleScope.launch {
            val result = repository.getCemeteryById(objectId)
            progressBar.visibility = View.GONE

            result.onSuccess { data ->
                cemetery = data
                Log.d(TAG, "加载公墓详情成功: ${data.name}")
                bindData(data)
                layoutContent.visibility = View.VISIBLE
            }.onFailure { e ->
                Log.e(TAG, "加载公墓详情失败", e)
                Toast.makeText(this@CemeteryDetailActivity, "加载详情失败，请检查网络", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun bindData(data: Cemetery) {
        tvName.text = data.name

        if (data.price.isNotEmpty()) {
            tvPriceTag.text = data.price
            tvPriceTag.visibility = View.VISIBLE
        } else {
            tvPriceTag.visibility = View.GONE
        }

        tvDistrict.text = data.district

        if (data.address.isNotEmpty()) {
            tvAddress.text = "📍 ${data.address}"
            tvAddress.visibility = View.VISIBLE
        } else {
            tvAddress.visibility = View.GONE
        }

        if (data.phone.isNotEmpty()) {
            tvPhone.text = "📞 ${data.phone}"
            tvPhone.visibility = View.VISIBLE
            val phoneNumber = data.phone
            tvPhone.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$phoneNumber")
                startActivity(intent)
            }
        } else {
            tvPhone.visibility = View.GONE
        }

        if (data.introduction.isNotEmpty()) {
            tvIntroduction.text = data.introduction
            tvIntroduction.visibility = View.VISIBLE
        } else {
            tvIntroduction.visibility = View.GONE
        }

        if (data.service.isNotEmpty()) {
            tvService.text = data.service
            tvService.visibility = View.VISIBLE
        } else {
            tvService.visibility = View.GONE
        }

        if (data.website.isNotEmpty()) {
            tvWebsite.visibility = View.VISIBLE
            tvWebsite.setOnClickListener {
                var url = data.website
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        } else {
            tvWebsite.visibility = View.GONE
        }

        parseAndBindImages(data.imageUrls)
    }

    private fun parseAndBindImages(imageStr: String) {
        if (imageStr.isBlank()) {
            viewPagerImages.visibility = View.GONE
            tvImageIndicator.visibility = View.GONE
            return
        }

        imageUrls = imageStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        if (imageUrls.isEmpty()) {
            viewPagerImages.visibility = View.GONE
            tvImageIndicator.visibility = View.GONE
            return
        }

        viewPagerImages.visibility = View.VISIBLE
        tvImageIndicator.visibility = View.VISIBLE

        val adapter = ImagePagerAdapter(imageUrls) { url, imageView ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(imageView)
        }
        viewPagerImages.adapter = adapter
        viewPagerImages.setOnClickListener {
            val intent = Intent(this, ImagePreviewActivity::class.java).apply {
                putStringArrayListExtra("imageUrls", ArrayList(imageUrls))
                putExtra("position", viewPagerImages.currentItem)
            }
            startActivity(intent)
        }

        if (imageUrls.size > 1) {
            tvImageIndicator.text = "1 / ${imageUrls.size}"
            viewPagerImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    tvImageIndicator.text = "${position + 1} / ${imageUrls.size}"
                }
            })
        } else {
            tvImageIndicator.text = "1 / 1"
        }
    }
}