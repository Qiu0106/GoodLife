package com.example.smartelderlycare_app.component

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.User
import com.example.smartelderlycare_app.data.repository.BmobRepository
import de.hdodenhof.circleimageview.CircleImageView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class EditProfileActivity : AppCompatActivity() {

    private val TAG = "EditProfileActivity"
    private val repository = BmobRepository()
    private var progressDialog: ProgressDialog? = null

    private lateinit var ivAvatar: CircleImageView
    private lateinit var tvChangeAvatar: TextView
    private lateinit var etNickname: EditText
    private lateinit var etRealName: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var rbMale: RadioButton
    private lateinit var rbFemale: RadioButton
    private lateinit var rbSecret: RadioButton
    private lateinit var tvBirthday: TextView
    private lateinit var layoutBirthday: View
    private lateinit var etEmergencyContact: EditText
    private lateinit var etEmergencyPhone: EditText
    private lateinit var tvBloodType: TextView
    private lateinit var layoutBloodType: View
    private lateinit var cgCardiovascular: ChipGroup
    private lateinit var cgMetabolic: ChipGroup
    private lateinit var cgBoneNerve: ChipGroup
    private lateinit var cgAllergy: ChipGroup
    private lateinit var etMedicalOther: EditText
    private lateinit var etSignature: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var currentUser: User? = null
    private var avatarUrl: String? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initViews()
        setupImagePicker()
        loadUserData()
        setupClickListeners()
    }

    private fun initViews() {
        ivAvatar = findViewById(R.id.ivAvatar)
        tvChangeAvatar = findViewById(R.id.tvChangeAvatar)
        etNickname = findViewById(R.id.etNickname)
        etRealName = findViewById(R.id.etRealName)
        rgGender = findViewById(R.id.rgGender)
        rbMale = findViewById(R.id.rbMale)
        rbFemale = findViewById(R.id.rbFemale)
        rbSecret = findViewById(R.id.rbSecret)
        tvBirthday = findViewById(R.id.tvBirthday)
        layoutBirthday = findViewById(R.id.layoutBirthday)
        etEmergencyContact = findViewById(R.id.etEmergencyContact)
        etEmergencyPhone = findViewById(R.id.etEmergencyPhone)
        tvBloodType = findViewById(R.id.tvBloodType)
        layoutBloodType = findViewById(R.id.layoutBloodType)
        cgCardiovascular = findViewById(R.id.cgCardiovascular)
        cgMetabolic = findViewById(R.id.cgMetabolic)
        cgBoneNerve = findViewById(R.id.cgBoneNerve)
        cgAllergy = findViewById(R.id.cgAllergy)
        etMedicalOther = findViewById(R.id.etMedicalOther)
        etSignature = findViewById(R.id.etSignature)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
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

    private fun loadUserData() {
        val prefs = getSharedPreferences("user", MODE_PRIVATE)

        currentUser = User(
            phone = prefs.getString("phone", "") ?: "",
            objectId = prefs.getString("objectId", null),
            password = "",
            nickname = prefs.getString("nickname", null),
            realName = prefs.getString("realName", null),
            avatarUrl = prefs.getString("avatarUrl", null),
            gender = prefs.getString("gender", null),
            birthDate = prefs.getString("birthDate", null),
            emergencyContact = prefs.getString("emergencyContact", null),
            emergencyPhone = prefs.getString("emergencyPhone", null),
            bloodType = prefs.getString("bloodType", null),
            medicalHistory = prefs.getString("medicalHistory", null),
            signature = prefs.getString("signature", null),
            token = prefs.getString("token", null) ?: prefs.getString("userId", null)
        )

        currentUser?.let { user ->
            if (!user.avatarUrl.isNullOrEmpty()) {
                avatarUrl = user.avatarUrl
                lifecycleScope.launch { loadAvatarImage(user.avatarUrl!!, ivAvatar) }
            }

            etNickname.setText(user.nickname ?: "")
            etRealName.setText(user.realName ?: "")

            when (user.gender) {
                "男" -> rbMale.isChecked = true
                "女" -> rbFemale.isChecked = true
                else -> rbSecret.isChecked = true
            }

            if (!user.birthDate.isNullOrEmpty()) {
                tvBirthday.text = user.birthDate
                tvBirthday.setTextColor(resources.getColor(R.color.morandi_text_primary, theme))
            }

            etEmergencyContact.setText(user.emergencyContact ?: "")
            etEmergencyPhone.setText(user.emergencyPhone ?: "")

            val bloodTypeVal = prefs.getString("bloodType", null)
            if (!bloodTypeVal.isNullOrEmpty()) {
                tvBloodType.text = bloodTypeVal
                tvBloodType.setTextColor(resources.getColor(R.color.morandi_text_primary, theme))
            }

            setMedicalHistoryData(prefs.getString("medicalHistory", "") ?: "")
            etSignature.setText(prefs.getString("signature", "") ?: "")
        }
    }

    private fun setupClickListeners() {
        tvChangeAvatar.setOnClickListener { openImagePicker() }
        ivAvatar.setOnClickListener { openImagePicker() }
        layoutBirthday.setOnClickListener { showDatePicker() }
        layoutBloodType.setOnClickListener { showBloodTypePicker() }
        btnSave.setOnClickListener { saveProfile() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        pickImageLauncher.launch(Intent.createChooser(intent, "选择头像"))
    }

    private fun uploadAvatar(imageUri: Uri) {
        showProgressDialog("正在上传头像...")

        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val tempFile = File(cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { output -> inputStream?.copyTo(output) }
            inputStream?.close()

            val prefs = getSharedPreferences("user", MODE_PRIVATE)
            val userId = prefs.getString("userId", "") ?: ""

            lifecycleScope.launch {
                try {
                    val result = repository.uploadAvatar(tempFile, userId)
                    result.onSuccess { url ->
                        Log.d(TAG, "头像上传成功: $url")
                        avatarUrl = url
                        loadAvatarImage(url, ivAvatar)
                        Toast.makeText(this@EditProfileActivity, "头像更新成功", Toast.LENGTH_SHORT).show()
                    }.onFailure { error ->
                        Log.e(TAG, "头像上传失败", error)
                        Toast.makeText(this@EditProfileActivity, "头像上传失败: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "上传异常", e)
                    Toast.makeText(this@EditProfileActivity, "上传异常", Toast.LENGTH_SHORT).show()
                }
                hideProgressDialog()

                try { tempFile.delete() } catch (_: Exception) {}
            }

        } catch (e: Exception) {
            Log.e(TAG, "处理图片失败", e)
            hideProgressDialog()
            Toast.makeText(this, "处理图片失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val birthdayText = tvBirthday.text.toString()

        if (birthdayText.isNotEmpty() && birthdayText != "请选择出生日期") {
            try {
                val parts = birthdayText.split("-")
                if (parts.size == 3) {
                    calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }
            } catch (e: Exception) { }
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                tvBirthday.text = dateStr
                tvBirthday.setTextColor(resources.getColor(R.color.morandi_text_primary, theme))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showBloodTypePicker() {
        val bloodTypes = arrayOf("A型", "B型", "AB型", "O型", "未知")
        var checkedItem = bloodTypes.indexOf(tvBloodType.text.toString())
        if (checkedItem < 0) checkedItem = 4

        AlertDialog.Builder(this)
            .setTitle("选择血型")
            .setSingleChoiceItems(bloodTypes, checkedItem) { dialog, which ->
                tvBloodType.text = bloodTypes[which]
                tvBloodType.setTextColor(resources.getColor(R.color.morandi_text_primary, theme))
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun saveProfile() {
        val nickname = etNickname.text.toString().trim()
        if (nickname.isEmpty()) {
            etNickname.error = "请输入昵称"
            return
        }

        showProgressDialog("正在保存...")

        val gender = when (rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "男"
            R.id.rbFemale -> "女"
            else -> "保密"
        }

        val birthday = if (tvBirthday.text.toString() == "请选择出生日期") "" else tvBirthday.text.toString()
        val bloodType = if (tvBloodType.text.toString() == "请选择血型") "" else tvBloodType.text.toString()

        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        prefs.edit().apply {
            putString("nickname", nickname)
            putString("realName", etRealName.text.toString().trim())
            putString("gender", gender)
            putString("birthDate", birthday)
            putString("emergencyContact", etEmergencyContact.text.toString().trim())
            putString("emergencyPhone", etEmergencyPhone.text.toString().trim())
            putString("bloodType", bloodType)
            putString("medicalHistory", getMedicalHistoryData())
            putString("signature", etSignature.text.toString().trim())
            if (avatarUrl != null) {
                putString("avatarUrl", avatarUrl)
            }
            apply()
        }

        Log.d(TAG, "个人资料已保存到本地")

        lifecycleScope.launch {
            try {
                val objectId = prefs.getString("objectId", null)
                val token = prefs.getString("userId", null) ?: prefs.getString("token", null)
                if (objectId != null) {
                    val updatedUser = User(
                        phone = prefs.getString("phone", "") ?: "",
                        objectId = objectId,
                        nickname = nickname,
                        realName = etRealName.text.toString().trim(),
                        gender = gender,
                        birthDate = birthday,
                        emergencyContact = etEmergencyContact.text.toString().trim(),
                        emergencyPhone = etEmergencyPhone.text.toString().trim(),
                        bloodType = bloodType,
                        medicalHistory = getMedicalHistoryData(),
                        signature = etSignature.text.toString().trim(),
                        avatarUrl = avatarUrl,
                        token = token
                    )
                    val result = repository.updateUser(objectId, updatedUser)
                    result.onSuccess {
                        Log.d(TAG, "✅ 个人资料已同步到云端")
                    }.onFailure { e ->
                        Log.e(TAG, "❌ 云端同步失败", e)
                    }
                } else {
                    Log.w(TAG, "⚠️ objectId 为空，跳过云端同步")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 云端同步异常", e)
            }

            hideProgressDialog()
            Toast.makeText(this@EditProfileActivity, "✅ 个人资料保存成功！", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
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

    private fun getMedicalHistoryData(): String {
        val selectedItems = mutableListOf<String>()
        val chipGroups = listOf(cgCardiovascular, cgMetabolic, cgBoneNerve, cgAllergy)
        for (chipGroup in chipGroups) {
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as? Chip
                if (chip != null && chip.isChecked) {
                    selectedItems.add(chip.text.toString())
                }
            }
        }
        val otherText = etMedicalOther.text.toString().trim()
        if (otherText.isNotEmpty()) {
            selectedItems.add(otherText)
        }
        return selectedItems.joinToString(",")
    }

    private fun setMedicalHistoryData(historyString: String) {
        if (historyString.isNullOrBlank()) return
        val chipGroups = listOf(cgCardiovascular, cgMetabolic, cgBoneNerve, cgAllergy)
        val historyList = historyString.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        for (chipGroup in chipGroups) {
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as? Chip
                if (chip != null) {
                    val chipText = chip.text.toString()
                    if (historyList.contains(chipText)) {
                        chip.isChecked = true
                        historyList.remove(chipText)
                    }
                }
            }
        }
        if (historyList.isNotEmpty()) {
            etMedicalOther.setText(historyList.joinToString(", "))
        }
    }

    private suspend fun loadAvatarImage(url: String, imageView: CircleImageView) {
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url.replace("https://", "http://")).build()
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful && response.body != null) {
                    val bytes = response.body!!.bytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        withContext(Dispatchers.Main) {
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                }
                response.close()
            } catch (e: Exception) {
                Log.e(TAG, "加载头像失败: $url", e)
            }
        }
    }
}
