package com.example.smartelderlycare_app.component.cemetery

import android.content.Intent
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.Cemetery
import com.example.smartelderlycare_app.data.repository.BmobRepository
import kotlinx.coroutines.launch

/**
 * 上海市公墓地图页面（WebView + 高德 JS API 方案）
 *
 * 无需原生高德 SDK，通过 WebView 加载高德 JavaScript API 实现地图功能。
 *
 * 使用前请确保：
 * 1. 已到 https://lbs.amap.com/ 申请 Web 端 JS API Key，替换下方 AMAP_JS_KEY
 * 2. 已在 Bmob 控制台创建 Cemetery 表并录入数据
 */
class CemeteryMapActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CemeteryMapActivity"

        private const val AMAP_JS_KEY = "43b3035328c560d7963690e25be77ae0"
        private const val AMAP_SECURITY_CODE = "588de773ee912ec0bd5041ed398e83ed"

        private const val DEFAULT_CENTER_LAT = 31.2304
        private const val DEFAULT_CENTER_LNG = 121.4737
        private const val DEFAULT_ZOOM = 11
    }

    private lateinit var webView: WebView
    private lateinit var layoutDistrictFilter: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: TextView

    private lateinit var adapter: CemeteryAdapter
    private val repository = BmobRepository()

    private var allCemeteries: List<Cemetery> = emptyList()
    private var filteredCemeteries: List<Cemetery> = emptyList()
    private var currentDistrict: String = "全部"
    private var mapReady: Boolean = false

    private val districtList = listOf(
        "全部", "嘉定区", "青浦区", "浦东新区", "松江区", "奉贤区",
        "金山区", "闵行区", "宝山区", "崇明区"
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cemetery_map)

        initViews()
        initWebView()
        initRecyclerView()
        initDistrictFilter()
        loadCemeteryData()
    }

    private fun initViews() {
        webView = findViewById(R.id.webView)
        layoutDistrictFilter = findViewById(R.id.layoutDistrictFilter)
        recyclerView = findViewById(R.id.recyclerViewCemetery)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.setGeolocationEnabled(true)
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    mapReady = true
                    refreshMapMarkers()
                }
            }
            addJavascriptInterface(MapJsInterface(), "Android")
            val htmlContent = assets.open("amap_cemetery.html")
                .bufferedReader().use { it.readText() }
                .replace("AMAP_KEY_PLACEHOLDER", AMAP_JS_KEY)
                .replace("AMAP_SECURITY_PLACEHOLDER", AMAP_SECURITY_CODE)
            loadDataWithBaseURL("https://webapi.amap.com/", htmlContent, "text/html", "UTF-8", null)
        }
    }

    private fun initRecyclerView() {
        filteredCemeteries = emptyList()

        adapter = CemeteryAdapter(filteredCemeteries) { cemetery, position ->
            onCemeteryItemClicked(cemetery, position)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CemeteryMapActivity)
            adapter = this@CemeteryMapActivity.adapter
        }
    }

    private fun initDistrictFilter() {
        districtList.forEach { district ->
            val chip = TextView(this).apply {
                text = district
                textSize = 14f
                setTextColor(
                    if (district == currentDistrict) Color.WHITE
                    else resources.getColor(R.color.morandi_text_primary, null)
                )
                setBackgroundColor(
                    if (district == currentDistrict) resources.getColor(R.color.morandi_accent_warm, null)
                    else resources.getColor(R.color.elderly_input_bg, null)
                )
                setPadding(24, 10, 24, 10)
                val marginEnd = 12
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, marginEnd, 0)
                }
                layoutParams = params
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    selectDistrict(district)
                }
            }
            layoutDistrictFilter.addView(chip)
        }
    }

    private fun selectDistrict(district: String) {
        currentDistrict = district

        for (i in 0 until layoutDistrictFilter.childCount) {
            val chip = layoutDistrictFilter.getChildAt(i) as? TextView ?: continue
            val isSelected = chip.text == district
            chip.setTextColor(
                if (isSelected) Color.WHITE
                else resources.getColor(R.color.morandi_text_primary, null)
            )
            chip.setBackgroundColor(
                if (isSelected) resources.getColor(R.color.morandi_accent_warm, null)
                else resources.getColor(R.color.elderly_input_bg, null)
            )
        }

        filteredCemeteries = if (district == "全部") {
            allCemeteries
        } else {
            allCemeteries.filter { it.district == district }
        }

        adapter.updateData(filteredCemeteries)
        refreshMapMarkers()
    }

    private fun loadCemeteryData() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = repository.getAllCemeteries()
            progressBar.visibility = View.GONE

            result.onSuccess { cemeteries ->
                allCemeteries = cemeteries
                filteredCemeteries = cemeteries
                adapter.updateData(cemeteries)
                refreshMapMarkers()

                Log.d(TAG, "加载公墓数据成功，共 ${cemeteries.size} 条")
            }.onFailure { e ->
                Log.e(TAG, "加载公墓数据失败", e)
                Toast.makeText(this@CemeteryMapActivity, "加载公墓数据失败，请检查网络", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshMapMarkers() {
        if (!mapReady) return

        val jsonData = buildCemeteryJson(filteredCemeteries)
        webView.evaluateJavascript("javascript:refreshMarkers('$jsonData')", null)
    }

    private fun buildCemeteryJson(list: List<Cemetery>): String {
        val sb = StringBuilder("[")
        list.forEachIndexed { index, cemetery ->
            if (index > 0) sb.append(",")
            sb.append("{")
            sb.append("\"index\":$index,")
            sb.append("\"objectId\":\"${escapeJson(cemetery.objectId ?: "")}\",")
            sb.append("\"name\":\"${escapeJson(cemetery.name)}\",")
            sb.append("\"district\":\"${escapeJson(cemetery.district)}\",")
            sb.append("\"lat\":${cemetery.latitude},")
            sb.append("\"lng\":${cemetery.longitude},")
            sb.append("\"intro\":\"${escapeJson(cemetery.introduction)}\"")
            sb.append("}")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun escapeJson(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun onCemeteryItemClicked(cemetery: Cemetery, position: Int) {
        adapter.setHighlighted(position)

        val js = "javascript:focusMarker($position, ${cemetery.latitude}, ${cemetery.longitude}, " +
                "'${escapeJson(cemetery.name)}', '${escapeJson(cemetery.introduction)}', '${escapeJson(cemetery.objectId ?: "")}')"
        webView.evaluateJavascript(js, null)
    }

    // ==================== JS → Kotlin 回调接口 ====================

    inner class MapJsInterface {
        @JavascriptInterface
        fun onMarkerClick(index: Int) {
            runOnUiThread {
                val cemetery = filteredCemeteries.getOrNull(index) ?: return@runOnUiThread
                recyclerView.smoothScrollToPosition(index)
                adapter.setHighlighted(index)
                Log.d(TAG, "地图 Marker 被点击: ${cemetery.name} (index=$index)")
            }
        }

        @JavascriptInterface
        fun onMapClick() {
            runOnUiThread {
                adapter.setHighlighted(RecyclerView.NO_POSITION)
            }
        }

        @JavascriptInterface
        fun onInfoWindowClick(objectId: String) {
            runOnUiThread {
                Log.d(TAG, "InfoWindow 被点击，objectId=$objectId")
                val intent = Intent(this@CemeteryMapActivity, CemeteryDetailActivity::class.java)
                intent.putExtra("objectId", objectId)
                startActivity(intent)
            }
        }

        @JavascriptInterface
        fun log(message: String) {
            Log.d(TAG, "JS: $message")
        }
    }
}