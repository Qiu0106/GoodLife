package com.example.smartelderlycare_app.component

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.bumptech.glide.Glide
import com.example.smartelderlycare_app.BuildConfig
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.network.WeatherApiService
import com.example.smartelderlycare_app.util.LocationHelper
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var ivAvatar: CircleImageView
    private lateinit var tvGreeting: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvWeatherIcon: TextView
    private lateinit var tvTemperature: TextView

    private lateinit var locationHelper: LocationHelper

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocation || coarseLocation) {
            Log.d(TAG, "定位权限已授予")
            loadWeatherWithLocation()
        } else {
            Log.w(TAG, "定位权限被拒绝，使用默认城市")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate 开始执行...")

        // 检查是否已登录（通过 SharedPreferences）
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        val userId = prefs.getString("userId", null)

        if (userId == null) {
            Log.d(TAG, "用户未登录，跳转到登录页面")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        Log.d(TAG, "用户已登录 (userId=$userId)，显示主界面")

        try {
            setContentView(R.layout.activity_main)
            Log.d(TAG, "setContentView 成功")

            initViews()
            checkLocationPermissionAndLoadWeather()
            loadUserAvatar()
            updateGreetingMessage()
            setupClickListeners()

            Log.d(TAG, "onCreate 执行完成，界面应该已显示")

        } catch (e: Exception) {
            Log.e(TAG, "MainActivity 初始化失败", e)
            e.printStackTrace()
            Toast.makeText(this, "主界面加载失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        ivAvatar = findViewById(R.id.iv_avatar)
        tvGreeting = findViewById(R.id.tv_greeting)
        tvDate = findViewById(R.id.tv_date)
        tvWeatherIcon = findViewById(R.id.tv_weather_icon)
        tvTemperature = findViewById(R.id.tv_temperature)

        Log.d(TAG, "所有组件初始化成功")
    }

    private fun loadWeatherWithLocation() {
        if (!::locationHelper.isInitialized) {
            locationHelper = LocationHelper(this)
        }

        locationHelper.getCurrentLocation(object : LocationHelper.LocationCallback {
            override fun onSuccess(location: android.location.Location) {
                Log.d(TAG, "获取定位成功: ${location.longitude}, ${location.latitude}")
                loadWeather(location.longitude, location.latitude)
            }

            override fun onFailure(error: String) {
                Log.e(TAG, "定位失败: $error")
            }
        })
    }

    private fun loadWeather(longitude: Double, latitude: Double) {
        val apiKey = BuildConfig.WEATHER_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_WEATHER_API_KEY_HERE") {
            Log.w(TAG, "天气 API Key 未配置")
            return
        }

        WeatherApiService.getWeatherInfo(
            longitude = longitude,
            latitude = latitude,
            apiKey = apiKey,
            onSuccess = { weatherInfo ->
                runOnUiThread {
                    tvWeatherIcon.text = weatherInfo.icon
                    tvTemperature.text = "${weatherInfo.temp}°C"
                    Log.d(TAG, "天气加载成功: ${weatherInfo.text}, ${weatherInfo.temp}°C")
                }
            },
            onError = { error ->
                runOnUiThread {
                    Log.e(TAG, "天气加载失败: $error")
                }
            }
        )
    }

    private fun checkLocationPermissionAndLoadWeather() {
        when {
            hasLocationPermission() -> {
                loadWeatherWithLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "需要定位权限来获取当地天气", Toast.LENGTH_SHORT).show()
                requestLocationPermission()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun loadUserAvatar() {
        try {
            val prefs = getSharedPreferences("user", MODE_PRIVATE)
            val avatarUrl = prefs.getString("avatarUrl", null)

            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .circleCrop()
                    .into(ivAvatar)

                Log.d(TAG, "头像加载成功: $avatarUrl")
            } else {
                // 使用默认图标
                ivAvatar.setImageResource(R.mipmap.ic_launcher)
                Log.d(TAG, "使用默认头像")
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载头像失败", e)
            ivAvatar.setImageResource(R.mipmap.ic_launcher)
        }
    }

    private fun updateGreetingMessage() {
        // 获取当前时间
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        // 根据时间生成问候语
        val greeting = when {
            hourOfDay in 6..11 -> "早上好"
            hourOfDay in 12..13 -> "中午好"
            hourOfDay in 14..17 -> "下午好"
            hourOfDay in 18..21 -> "晚上好"
            else -> "夜深了"
        }

        // 获取用户昵称
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        val nickname = prefs.getString("nickname", null) ?: "朋友"

        // 设置问候语
        tvGreeting.text = "$greeting，$nickname"

        // 设置日期（中文格式）
        val dateFormat = SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINESE)
        tvDate.text = dateFormat.format(calendar.time)

        Log.d(TAG, "问候语更新完成: $greeting, $nickname")
    }

    private fun setupClickListeners() {
        // 打卡签到
        findViewById<MaterialCardView>(R.id.card_checkin).setOnClickListener {
            startActivity(Intent(this, CheckinActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_community).setOnClickListener {
            startActivity(Intent(this, CommunityActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_afterlife).setOnClickListener {
            startActivity(Intent(this, AfterlifeCustomActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_starry_sky).setOnClickListener {
            startActivity(Intent(this, StarrySkyActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_mine).setOnClickListener {
            startActivity(Intent(this, MineActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.btn_voice_assistant).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - 刷新数据")
        // 返回时刷新问候语和头像（防止切换日期或更换头像）
        loadUserAvatar()
        updateGreetingMessage()
    }
}
