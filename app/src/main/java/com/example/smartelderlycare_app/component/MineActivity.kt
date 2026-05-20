package com.example.smartelderlycare_app.component

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.User
import com.example.smartelderlycare_app.data.repository.BmobRepository
import com.example.smartelderlycare_app.ui.viewmodel.AfterlifePlanViewModel
import com.example.smartelderlycare_app.ui.viewmodel.UserViewModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class MineActivity : AppCompatActivity() {

    private val TAG = "MineActivity"
    private val REQUEST_CODE_EDIT_PROFILE = 1001

    private lateinit var ivUserAvatar: CircleImageView
    private lateinit var btnUploadAvatar: CircleImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvPersonalInfo: TextView
    private lateinit var tvAfterlifePlan: TextView
    private lateinit var layoutMedicationDisplay: LinearLayout
    private lateinit var tvNoMedicationDisplay: TextView
    private lateinit var btnMyPosts: com.google.android.material.button.MaterialButton
    private lateinit var btnMyFavorites: com.google.android.material.button.MaterialButton
    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button
    private lateinit var progressBar: View

    private val userViewModel: UserViewModel by viewModels()
    private val afterlifeViewModel: AfterlifePlanViewModel by viewModels()
    private val repository = BmobRepository()
    private var progressDialog: ProgressDialog? = null
    private var isUploadingAvatar = false

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    private suspend fun loadImageToView(url: String, imageView: ImageView) {
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful && response.body != null) {
                    val bytes = response.body!!.bytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    withContext(Dispatchers.Main) {
                        if (bitmap != null && !isDestroyed) {
                            imageView.setImageBitmap(bitmap)
                            Log.d(TAG, "✅ 图片加载成功(OkHttp): $url")
                        }
                    }
                } else {
                    Log.e(TAG, "❌ 图片加载失败(HTTP ${response.code}): $url")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 图片加载异常: $url", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate 开始")
        setContentView(R.layout.activity_mine)

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
        setupImagePicker()
        observeViewModels()
        loadData()
    }

    private fun initViews() {
        ivUserAvatar = findViewById(R.id.ivUserAvatar)
        btnUploadAvatar = findViewById(R.id.btnUploadAvatar)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserPhone = findViewById(R.id.tvUserPhone)
        tvPersonalInfo = findViewById(R.id.tvPersonalInfo)
        tvAfterlifePlan = findViewById(R.id.tvAfterlifePlan)
        layoutMedicationDisplay = findViewById(R.id.layout_medication_display)
        tvNoMedicationDisplay = findViewById(R.id.tvNoMedicationDisplay)
        btnMyPosts = findViewById(R.id.btnMyPosts)
        btnMyFavorites = findViewById(R.id.btnMyFavorites)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnLogout = findViewById(R.id.btnLogout)
        progressBar = findViewById(R.id.progressBar)

        btnUploadAvatar.setOnClickListener { openImagePicker() }
        btnMyPosts.setOnClickListener {
            startActivity(Intent(this, MyPostsActivity::class.java))
        }
        btnMyFavorites.setOnClickListener {
            startActivity(Intent(this, MyFavoritesActivity::class.java))
        }
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE)
        }
        btnLogout.setOnClickListener { logout() }
    }

    private fun setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    uploadAvatar(imageUri)
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        pickImageLauncher.launch(Intent.createChooser(intent, "选择头像"))
    }

    private fun uploadAvatar(imageUri: Uri) {
        showProgressDialog("正在上传头像...")
        isUploadingAvatar = true

        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val tempFile = File(cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { output -> inputStream?.copyTo(output) }
            inputStream?.close()

            val prefs = getSharedPreferences("user", MODE_PRIVATE)
            val userId = prefs.getString("userId", "") ?: ""
            val objectId = prefs.getString("objectId", "") ?: ""

            Log.d(TAG, "开始上传头像 - userId: $userId, objectId: $objectId")

            lifecycleScope.launch {
                try {
                    val result = repository.uploadAvatar(tempFile, userId)
                    result.onSuccess { avatarUrl ->
                        Log.d(TAG, "头像上传成功: $avatarUrl")

                        prefs.edit()
                            .putString("avatarUrl", avatarUrl)
                            .apply()

                        val currentUser = repository.getCurrentUser()
                        if (currentUser != null) {
                            repository.setCurrentUser(currentUser.copy(avatarUrl = avatarUrl))
                        }

                        loadImageToView(avatarUrl, ivUserAvatar)

                        if (objectId.isNotEmpty()) {
                            val token = prefs.getString("userId", null)
                            val phone = prefs.getString("phone", "") ?: ""
                            val nickname = prefs.getString("nickname", null)
                            val userToUpdate = User(
                                phone = phone,
                                objectId = objectId,
                                nickname = nickname,
                                avatarUrl = avatarUrl,
                                token = token
                            )
                            repository.updateUser(objectId, userToUpdate)
                                .onSuccess { 
                                    Log.d(TAG, "✅ 头像已同步到Bmob云端")
                                }
                                .onFailure { e -> 
                                    Log.e(TAG, "❌ 同步头像到Bmob失败", e)
                                }
                        } else {
                            Log.w(TAG, "⚠️ objectId 为空，跳过云端同步")
                        }

                        Toast.makeText(this@MineActivity, "头像更新成功 ✅", Toast.LENGTH_SHORT).show()
                    }.onFailure { error ->
                        Log.e(TAG, "❌ 头像上传失败", error)
                        Toast.makeText(this@MineActivity, "头像上传失败: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 上传过程异常", e)
                    Toast.makeText(this@MineActivity, "上传异常: ${e.message}", Toast.LENGTH_LONG).show()
                }
                hideProgressDialog()

                try { tempFile.delete() } catch (_: Exception) {}

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    isUploadingAvatar = false
                }, 2000)
            }

        } catch (e: Exception) {
            Log.e(TAG, "处理图片失败", e)
            hideProgressDialog()
            isUploadingAvatar = false
            Toast.makeText(this, "处理图片失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModels() {
        userViewModel.currentUser.observe(this, Observer { user ->
            user?.let {
                Log.d(TAG, "用户信息更新: nickname=${it.nickname}, phone=${it.phone}")
                tvUserName.text = it.nickname ?: "用户"
                tvUserPhone.text = it.phone

                if (!isUploadingAvatar) {
                    val prefs = getSharedPreferences("user", MODE_PRIVATE)
                    val avatarUrl = it.avatarUrl ?: prefs.getString("avatarUrl", null)

                    if (!avatarUrl.isNullOrEmpty()) {
                        lifecycleScope.launch { loadImageToView(avatarUrl, ivUserAvatar) }
                    }
                } else {
                    Log.d(TAG, "正在上传头像，跳过 ViewModel 头像更新")
                }

                val userId = it.objectId ?: getSharedPreferences("user", MODE_PRIVATE)
                    .getString("userObjectId", null)
                    ?: getSharedPreferences("user", MODE_PRIVATE)
                        .getString("objectId", null)
                    ?: getSharedPreferences("user", MODE_PRIVATE)
                        .getString("userId", null) ?: ""
                loadPersonalInfo(userId)
            }
        })

        afterlifeViewModel.afterlifePlans.observe(this, Observer { plans ->
            if (plans.isNotEmpty()) {
                val plan = plans[0]

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
        val userId = sharedPreferences.getString("userObjectId", null)
            ?: sharedPreferences.getString("objectId", null)
            ?: sharedPreferences.getString("userId", null) ?: ""
        val nickname = sharedPreferences.getString("nickname", null)
        val avatarUrl = sharedPreferences.getString("avatarUrl", null)

        Log.d(TAG, "加载本地数据: phone=$phone, userId=$userId, nickname=$nickname, avatarUrl=$avatarUrl")

        tvUserName.text = nickname ?: "用户"
        tvUserPhone.text = if (phone.isEmpty()) "--" else phone

        if (!avatarUrl.isNullOrEmpty()) {
            lifecycleScope.launch { loadImageToView(avatarUrl, ivUserAvatar) }
        }

        // 加载个人资料信息
        loadPersonalInfo(userId)

        if (phone.isNotEmpty()) {
            userViewModel.getUserByPhone(phone)
        }

        if (userId.isNotEmpty()) {
            afterlifeViewModel.getAfterlifePlansByUserId(userId)
            loadMedications(userId)
        }

        if (tvAfterlifePlan.text.isEmpty()) {
            tvAfterlifePlan.text = "未配置身后事计划"
        }
    }

    private fun loadMedications(userId: String) {
        lifecycleScope.launch {
            val result = repository.getMedicationsByUserId(userId)
            result.onSuccess { medications ->
                layoutMedicationDisplay.removeAllViews()
                if (medications.isEmpty()) {
                    tvNoMedicationDisplay.visibility = View.VISIBLE
                } else {
                    tvNoMedicationDisplay.visibility = View.GONE
                    medications.forEach { med ->
                        val medView = TextView(this@MineActivity).apply {
                            text = buildString {
                                append("• ${med.name}")
                                append("  (${med.frequency})")
                                if (med.times.isNotEmpty()) {
                                    append("\n   时间: ${med.times.joinToString(", ")}")
                                }
                            }
                            textSize = 16f
                            setTextColor(resources.getColor(R.color.morandi_text_primary, null))
                            setLineSpacing(0f, 1.4f)
                            setPadding(0, 8, 0, 8)
                        }
                        layoutMedicationDisplay.addView(medView)
                    }
                }
            }.onFailure {
                Log.e(TAG, "加载药物数据失败")
                tvNoMedicationDisplay.visibility = View.VISIBLE
            }
        }
    }

    private fun loadPersonalInfo(userId: String) {
        val prefs = getSharedPreferences("user", MODE_PRIVATE)

        val nickname = prefs.getString("nickname", null) ?: "未设置"
        val realName = prefs.getString("realName", null)
        val gender = prefs.getString("gender", null) ?: "未设置"
        val birthDate = prefs.getString("birthDate", null)
        val emergencyContact = prefs.getString("emergencyContact", null)
        val emergencyPhone = prefs.getString("emergencyPhone", null)
        val emergencyRela = prefs.getString("emergencyRela", null)
        val bloodType = prefs.getString("bloodType", null)
        val medicalHistory = prefs.getString("medicalHistory", null)
        val signature = prefs.getString("signature", null)

        // 计算年龄（如果知道出生日期）
        val ageStr = if (!birthDate.isNullOrEmpty()) {
            try {
                val parts = birthDate.split("-")
                if (parts.size == 3) {
                    val birthYear = parts[0].toInt()
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val age = currentYear - birthYear
                    "$age 岁"
                } else null
            } catch (e: Exception) { null }
        } else null

        val infoBuilder = StringBuilder()

        // 基础信息
        if (!realName.isNullOrEmpty()) {
            infoBuilder.append("真实姓名: $realName\n")
        }
        infoBuilder.append("性别: $gender\n")
        if (ageStr != null) {
            infoBuilder.append("年龄: $ageStr\n")
        } else if (!birthDate.isNullOrEmpty()) {
            infoBuilder.append("出生日期: $birthDate\n")
        }

        // 安全信息
        if (!emergencyContact.isNullOrEmpty() || !emergencyPhone.isNullOrEmpty()) {
            infoBuilder.append("\n紧急联系人:\n")
            if (!emergencyContact.isNullOrEmpty()) {
                infoBuilder.append("  姓名：$emergencyContact\n")
            }
            if (!emergencyPhone.isNullOrEmpty()) {
                infoBuilder.append("  电话：$emergencyPhone\n")
            }
            if (!emergencyRela.isNullOrEmpty()) {
                infoBuilder.append("  关系：$emergencyRela\n")
            }
        }

        // 健康信息
        if (!bloodType.isNullOrEmpty()) {
            infoBuilder.append("\n血型: $bloodType\n")
        }
        if (!medicalHistory.isNullOrEmpty()) {
            infoBuilder.append("健康状况: $medicalHistory\n")
        }

        // 个性签名
        if (!signature.isNullOrEmpty()) {
            infoBuilder.append("\n个性签名: \"$signature\"")
        }

        val finalInfo = infoBuilder.toString().trim()
        tvPersonalInfo.text = if (finalInfo.isEmpty()) "暂无个人资料，请点击下方按钮编辑" else finalInfo

        // 加载用药信息并追加到个人资料中
        if (userId.isNotEmpty()) {
            loadAndAppendMedicationInfo(userId, finalInfo)
        }
    }

    private fun loadAndAppendMedicationInfo(userId: String, currentInfo: String) {
        lifecycleScope.launch {
            val result = repository.getMedicationsByUserId(userId)
            result.onSuccess { medications ->
                if (medications.isNotEmpty()) {
                    val medInfo = medications.joinToString("\n") { med ->
                        "• ${med.name} (${med.frequency})"
                    }
                    val updatedInfo = if (currentInfo.isEmpty()) {
                        "用药提醒:\n$medInfo"
                    } else {
                        "$currentInfo\n\n用药提醒:\n$medInfo"
                    }
                    tvPersonalInfo.text = updatedInfo
                }
            }.onFailure {
                Log.e(TAG, "加载用药信息失败（个人资料显示）")
            }
        }
    }

    private fun loadFromLocal() {
        val afterlifeSharedPreferences = getSharedPreferences("afterlife", MODE_PRIVATE)
        val hasPlan = afterlifeSharedPreferences.getBoolean("hasPlan", false)
        val currentUserId = getSharedPreferences("user", MODE_PRIVATE).getString("userId", "") ?: ""
        val cachedUserId = afterlifeSharedPreferences.getString("cachedUserId", "")

        if (hasPlan && cachedUserId == currentUserId) {
            val name = afterlifeSharedPreferences.getString("name", "--")
            val age = afterlifeSharedPreferences.getString("age", "--")
            val funeralStyle = afterlifeSharedPreferences.getString("funeralStyle", "--")
            val relicsHandling = afterlifeSharedPreferences.getString("relicsHandling", "--")
            val burialMethod = afterlifeSharedPreferences.getString("burialMethod", "--")
            val bgm = afterlifeSharedPreferences.getString("bgm", "--")

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

        getSharedPreferences("user", MODE_PRIVATE).edit().clear().apply()
        getSharedPreferences("afterlife", MODE_PRIVATE).edit().clear().apply()
        getSharedPreferences("chat_prefs", MODE_PRIVATE).edit().clear().apply()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            Log.d(TAG, "从编辑资料返回，刷新数据")
            loadData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
    }

    private fun showProgressDialog(message: String) {
        progressDialog?.dismiss()
        progressDialog = ProgressDialog(this).apply {
            setMessage(message)
            setCancelable(false)
            show()
        }
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }
}

