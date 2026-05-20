package com.example.smartelderlycare_app.component

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.Medication
import com.example.smartelderlycare_app.data.model.User
import com.example.smartelderlycare_app.data.repository.BmobRepository
import com.example.smartelderlycare_app.util.MedicationAlarmManager
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
    private lateinit var etEmergencyRela: EditText
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

    private lateinit var layoutMedicationList: LinearLayout
    private lateinit var tvNoMedication: TextView
    private lateinit var btnAddMedication: Button

    private val medicationList = mutableListOf<MedicationViewData>()
    private var currentUserId: String = ""

    private val frequencyOptions = arrayOf("每天1次", "每天2次", "每天3次", "每周1次", "每周2次", "每周3次")

    private data class MedicationViewData(
        val id: Int,
        var name: String,
        var frequency: String,
        var times: MutableList<String>
    )

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
        etEmergencyRela = findViewById(R.id.etEmergencyRela)
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

        layoutMedicationList = findViewById(R.id.layout_medication_list)
        tvNoMedication = findViewById(R.id.tv_no_medication)
        btnAddMedication = findViewById(R.id.btn_add_medication)
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
            etEmergencyRela.setText(user.emergencyRela ?: "")

            val bloodTypeVal = prefs.getString("bloodType", null)
            if (!bloodTypeVal.isNullOrEmpty()) {
                tvBloodType.text = bloodTypeVal
                tvBloodType.setTextColor(resources.getColor(R.color.morandi_text_primary, theme))
            }

            setMedicalHistoryData(prefs.getString("medicalHistory", "") ?: "")
            etSignature.setText(prefs.getString("signature", "") ?: "")
        }

        currentUserId = prefs.getString("userObjectId", null)
            ?: prefs.getString("objectId", null)
            ?: prefs.getString("userId", null) ?: ""
        loadMedications()
    }

    private fun setupClickListeners() {
        tvChangeAvatar.setOnClickListener { openImagePicker() }
        ivAvatar.setOnClickListener { openImagePicker() }
        layoutBirthday.setOnClickListener { showDatePicker() }
        layoutBloodType.setOnClickListener { showBloodTypePicker() }
        btnSave.setOnClickListener { saveProfile() }
        btnCancel.setOnClickListener { finish() }

        btnAddMedication.setOnClickListener { showAddMedicationDialog() }
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
            putString("emergencyRela", etEmergencyRela.text.toString().trim())
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
                        emergencyRela = etEmergencyRela.text.toString().trim(),
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

                try {
                    saveMedicationsToCloud()
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 药物数据同步失败", e)
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

    private fun loadMedications() {
        if (currentUserId.isEmpty()) return
        lifecycleScope.launch {
            val result = repository.getMedicationsByUserId(currentUserId)
            result.onSuccess { medications ->
                medicationList.clear()
                medications.forEachIndexed { index, med ->
                    medicationList.add(
                        MedicationViewData(
                            id = med.objectId?.hashCode() ?: index,
                            name = med.name,
                            frequency = med.frequency,
                            times = med.times.toMutableList()
                        )
                    )
                }
                refreshMedicationViews()
            }.onFailure {
                Log.e(TAG, "加载药物数据失败")
            }
        }
    }

    private fun showAddMedicationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.item_medication, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_medication_name)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner_frequency)
        val layoutTimeList = dialogView.findViewById<LinearLayout>(R.id.layout_time_list)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, frequencyOptions)
        spinner.adapter = adapter

        val tempTimes = mutableListOf("08:00")
        refreshTimePickersInDialog(layoutTimeList, tempTimes, 1)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val count = getTimeCountFromFrequency(frequencyOptions[pos])
                while (tempTimes.size < count) tempTimes.add("08:00")
                while (tempTimes.size > count) tempTimes.removeAt(tempTimes.lastIndex)
                refreshTimePickersInDialog(layoutTimeList, tempTimes, count)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        AlertDialog.Builder(this)
            .setTitle("添加药物")
            .setView(dialogView)
            .setPositiveButton("添加") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "请输入药物名称", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val newId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                medicationList.add(
                    MedicationViewData(
                        id = newId,
                        name = name,
                        frequency = spinner.selectedItem.toString(),
                        times = tempTimes.toMutableList()
                    )
                )
                refreshMedicationViews()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun refreshTimePickersInDialog(container: LinearLayout, times: MutableList<String>, count: Int) {
        container.removeAllViews()
        val hours = (0..23).map { String.format("%02d", it) }
        val minutes = (0..59).map { String.format("%02d", it) }
        for (i in 0 until count) {
            val timeView = LayoutInflater.from(this).inflate(R.layout.item_medication_time, container, false)
            val tvLabel = timeView.findViewById<TextView>(R.id.tv_time_label)
            val spinnerHour = timeView.findViewById<Spinner>(R.id.spinner_hour)
            val spinnerMinute = timeView.findViewById<Spinner>(R.id.spinner_minute)

            tvLabel.text = "第${i + 1}次："
            val hourAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, hours)
            val minuteAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, minutes)
            spinnerHour.adapter = hourAdapter
            spinnerMinute.adapter = minuteAdapter

            val parts = times.getOrNull(i)?.split(":") ?: listOf("08", "00")
            val hIndex = hours.indexOf(parts.getOrNull(0) ?: "08").coerceAtLeast(0)
            val mIndex = minutes.indexOf(parts.getOrNull(1) ?: "00").coerceAtLeast(0)
            spinnerHour.setSelection(hIndex)
            spinnerMinute.setSelection(mIndex)

            spinnerHour.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    times[i] = "${hours[pos]}:${spinnerMinute.selectedItem}"
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            spinnerMinute.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    times[i] = "${spinnerHour.selectedItem}:${minutes[pos]}"
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            container.addView(timeView)
        }
    }

    private fun getTimeCountFromFrequency(freq: String): Int {
        return when {
            freq.contains("1次") -> 1
            freq.contains("2次") -> 2
            freq.contains("3次") -> 3
            else -> 1
        }
    }

    private fun refreshMedicationViews() {
        layoutMedicationList.removeAllViews()
        if (medicationList.isEmpty()) {
            tvNoMedication.visibility = View.VISIBLE
        } else {
            tvNoMedication.visibility = View.GONE
            medicationList.forEachIndexed { index, med ->
                val view = LayoutInflater.from(this).inflate(R.layout.item_medication, layoutMedicationList, false)
                val etName = view.findViewById<EditText>(R.id.et_medication_name)
                val spinner = view.findViewById<Spinner>(R.id.spinner_frequency)
                val layoutTimeList = view.findViewById<LinearLayout>(R.id.layout_time_list)
                val btnDelete = view.findViewById<ImageButton>(R.id.btn_delete_medication)

                etName.setText(med.name)
                val freqAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, frequencyOptions)
                spinner.adapter = freqAdapter
                val freqIndex = frequencyOptions.indexOf(med.frequency).coerceAtLeast(0)
                spinner.setSelection(freqIndex)

                val currentIndex = index
                val hours = (0..23).map { String.format("%02d", it) }
                val minutes = (0..59).map { String.format("%02d", it) }

                fun refreshTimes(count: Int) {
                    layoutTimeList.removeAllViews()
                    while (med.times.size < count) med.times.add("08:00")
                    while (med.times.size > count) med.times.removeAt(med.times.lastIndex)
                    for (i in 0 until count) {
                        val timeView = LayoutInflater.from(this).inflate(R.layout.item_medication_time, layoutTimeList, false)
                        val tvLabel = timeView.findViewById<TextView>(R.id.tv_time_label)
                        val spinnerHour = timeView.findViewById<Spinner>(R.id.spinner_hour)
                        val spinnerMinute = timeView.findViewById<Spinner>(R.id.spinner_minute)

                        tvLabel.text = "第${i + 1}次："
                        spinnerHour.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, hours)
                        spinnerMinute.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, minutes)

                        val parts = med.times.getOrNull(i)?.split(":") ?: listOf("08", "00")
                        spinnerHour.setSelection(hours.indexOf(parts.getOrNull(0) ?: "08").coerceAtLeast(0))
                        spinnerMinute.setSelection(minutes.indexOf(parts.getOrNull(1) ?: "00").coerceAtLeast(0))

                        spinnerHour.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                                med.times[i] = "${hours[pos]}:${spinnerMinute.selectedItem}"
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                        spinnerMinute.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                                med.times[i] = "${spinnerHour.selectedItem}:${minutes[pos]}"
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                        layoutTimeList.addView(timeView)
                    }
                }

                refreshTimes(getTimeCountFromFrequency(med.frequency))

                etName.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        medicationList[currentIndex].name = etName.text.toString().trim()
                    }
                }

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                        val newFreq = frequencyOptions[pos]
                        medicationList[currentIndex].frequency = newFreq
                        refreshTimes(getTimeCountFromFrequency(newFreq))
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                btnDelete.setOnClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("确认删除")
                        .setMessage("确定要删除「${med.name}」的用药提醒吗？")
                        .setPositiveButton("删除") { _, _ ->
                            medicationList.removeAt(currentIndex)
                            refreshMedicationViews()
                        }
                        .setNegativeButton("取消", null)
                        .show()
                }

                layoutMedicationList.addView(view)
            }
        }
    }

    private suspend fun saveMedicationsToCloud() {
        if (currentUserId.isEmpty()) return
        val existing = repository.getMedicationsByUserId(currentUserId).getOrNull() ?: emptyList()
        existing.forEach { med ->
            med.objectId?.let { repository.deleteMedication(it) }
        }
        medicationList.forEach { medView ->
            if (medView.name.isNotEmpty()) {
                val medication = Medication(
                    userId = currentUserId,
                    name = medView.name,
                    frequency = medView.frequency,
                    times = medView.times
                )
                repository.createMedication(medication)
                medView.times.forEachIndexed { idx, timeStr ->
                    if (timeStr.isNotEmpty()) {
                        val alarmId = medView.id + idx
                        MedicationAlarmManager.scheduleAlarm(this@EditProfileActivity, alarmId, medView.name, timeStr)
                    }
                }
            }
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
