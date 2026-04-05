package com.example.smartelderlycare_app.component

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.ui.viewmodel.AfterlifePlanViewModel
import com.example.smartelderlycare_app.ui.viewmodel.UserViewModel

class MineActivity : AppCompatActivity() {

    private val TAG = "MineActivity"
    
    private lateinit var tvUserName: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvAfterlifePlan: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button
    private lateinit var progressBar: View

    private val userViewModel: UserViewModel by viewModels()
    private val afterlifeViewModel: AfterlifePlanViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate 开始")
        setContentView(R.layout.activity_mine)

        // 使用 SharedPreferences 检查登录状态（不使用 BmobRepository 实例）
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        val userId = prefs.getString("userId", null)
        
        if (userId == null) {
            Log.d(TAG, "用户未登录，跳转到登录页面")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        Log.d(TAG, "用户已登录 (userId=$userId)，显示个人信息")

        initViews()
        observeViewModels()
        loadData()
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserPhone = findViewById(R.id.tvUserPhone)
        tvAfterlifePlan = findViewById(R.id.tvAfterlifePlan)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnLogout = findViewById(R.id.btnLogout ?: return)
        progressBar = findViewById(R.id.progressBar ?: return)

        btnEditProfile.setOnClickListener {
            Toast.makeText(this, "编辑功能开发中...", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun observeViewModels() {
        userViewModel.currentUser.observe(this, Observer { user ->
            user?.let {
                Log.d(TAG, "用户信息更新: nickname=${it.nickname}, phone=${it.phone}")
                tvUserName.text = it.nickname ?: "用户"
                tvUserPhone.text = it.phone
            }
        })

        afterlifeViewModel.afterlifePlans.observe(this, Observer { plans ->
            if (plans.isNotEmpty()) {
                val plan = plans[0]
                
                // 构建殡葬用品显示文本
                val suppliesList = mutableListOf<String>()
                if (plan.coffin) suppliesList.add("☑️ 棺材")
                if (plan.urn) suppliesList.add("☑️ 骨灰盒")
                if (plan.flowers) suppliesList.add("☑️ 鲜花")
                if (plan.candles) suppliesList.add("☑️ 蜡烛")
                if (plan.photos) suppliesList.add("☑️ 照片")
                val suppliesText = if (suppliesList.isEmpty()) "无" else suppliesList.joinToString(" ")
                
                tvAfterlifePlan.text = """
                    姓名: ${plan.name}
                    年龄: ${plan.age}
                    葬礼风格: ${if (plan.funeralStyle.isNullOrEmpty()) "未设置" else plan.funeralStyle}
                    遗物处理: ${if (plan.relicsHandling.isNullOrEmpty()) "未设置" else plan.relicsHandling}
                    下葬方式: ${if (plan.burialMethod.isNullOrEmpty()) "未设置" else plan.burialMethod}
                    殡葬用品: $suppliesText
                    背景音乐: ${if (plan.bgm.isNullOrEmpty()) "无" else plan.bgm}
                """.trimIndent()
            } else {
                loadFromLocal()
            }
        })

        userViewModel.isLoading.observe(this, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        userViewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                userViewModel.clearError()
            }
        })
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("user", MODE_PRIVATE)
        val phone = sharedPreferences.getString("phone", "") ?: ""
        val userId = sharedPreferences.getString("userId", "") ?: ""
        val nickname = sharedPreferences.getString("nickname", null)

        Log.d(TAG, "加载本地数据: phone=$phone, userId=$userId, nickname=$nickname")

        // 显示本地缓存的基本信息
        tvUserName.text = nickname ?: "用户"
        tvUserPhone.text = if (phone.isEmpty()) "--" else phone

        // 从云端获取最新用户信息
        if (phone.isNotEmpty()) {
            userViewModel.getUserByPhone(phone)
        }

        // 从云端获取身后事计划
        if (userId.isNotEmpty()) {
            afterlifeViewModel.getAfterlifePlansByUserId(userId)
        }
        
        // 如果没有身后事计划数据，显示提示
        if (tvAfterlifePlan.text.isEmpty()) {
            tvAfterlifePlan.text = "未配置身后事计划"
        }
    }

    private fun loadFromLocal() {
        val afterlifeSharedPreferences = getSharedPreferences("afterlife", MODE_PRIVATE)
        val hasPlan = afterlifeSharedPreferences.getBoolean("hasPlan", false)

        if (hasPlan) {
            val name = afterlifeSharedPreferences.getString("name", "--")
            val age = afterlifeSharedPreferences.getString("age", "--")
            val funeralStyle = afterlifeSharedPreferences.getString("funeralStyle", "--")
            val relicsHandling = afterlifeSharedPreferences.getString("relicsHandling", "--")
            val burialMethod = afterlifeSharedPreferences.getString("burialMethod", "--")
            val bgm = afterlifeSharedPreferences.getString("bgm", "--")

            // 读取殡葬用品
            val suppliesSet = afterlifeSharedPreferences.getStringSet("supplies", emptySet()) ?: emptySet()
            val suppliesText = if (suppliesSet.isEmpty()) "无" else suppliesSet.joinToString(" ") { "☑️ $it" }

            tvAfterlifePlan.text = """
                姓名: $name
                年龄: $age
                葬礼风格: $funeralStyle
                遗物处理: $relicsHandling
                下葬方式: $burialMethod
                殡葬用品: $suppliesText
                背景音乐: $bgm
                (本地数据)
            """.trimIndent()
        } else {
            tvAfterlifePlan.text = "未配置身后事计划"
        }
    }

    private fun logout() {
        Log.d(TAG, "用户退出登录")
        
        userViewModel.logout()

        // 清除本地缓存
        getSharedPreferences("user", MODE_PRIVATE).edit().clear().apply()

        // 跳转到登录界面
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - 刷新数据")
        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
    }
}
