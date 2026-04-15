package com.example.smartelderlycare_app.component

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.MemorialStarBmob
import com.example.smartelderlycare_app.data.repository.BmobRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class AddMemorialActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etBirthYear: EditText
    private lateinit var etDeathYear: EditText
    private lateinit var etMessage: EditText
    private lateinit var etStory: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: TextView
    private lateinit var layoutPhoto: FrameLayout
    private lateinit var ivPhoto: ImageView
    private lateinit var layoutAddPhoto: LinearLayout

    private var selectedImageUri: Uri? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    private val bmobRepository = BmobRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_memorial)

        setupImagePicker()
        initViews()
        setupClickListeners()
    }

    private fun setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    selectedImageUri = imageUri
                    ivPhoto.setImageURI(imageUri)
                    ivPhoto.visibility = ImageView.VISIBLE
                    layoutAddPhoto.visibility = LinearLayout.GONE
                }
            }
        }
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etBirthYear = findViewById(R.id.etBirthYear)
        etDeathYear = findViewById(R.id.etDeathYear)
        etMessage = findViewById(R.id.etMessage)
        etStory = findViewById(R.id.etStory)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)
        layoutPhoto = findViewById(R.id.layoutPhoto)
        ivPhoto = findViewById(R.id.ivPhoto)
        layoutAddPhoto = findViewById(R.id.layoutAddPhoto)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        layoutPhoto.setOnClickListener { openImagePicker() }

        btnSubmit.setOnClickListener {
            if (validateInput()) {
                submitForReview()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        pickImageLauncher.launch(Intent.createChooser(intent, "选择照片"))
    }

    private fun validateInput(): Boolean {
        val name = etName.text.toString().trim()
        val deathYear = etDeathYear.text.toString().trim()

        if (name.isEmpty()) {
            showError("请输入逝者姓名")
            etName.requestFocus()
            return false
        }

        if (deathYear.isEmpty()) {
            showError("请输入逝世年份")
            etDeathYear.requestFocus()
            return false
        }

        val deathYearInt = deathYear.toIntOrNull()
        if (deathYearInt == null || deathYearInt < 1900 || deathYearInt > Calendar.getInstance().get(Calendar.YEAR)) {
            showError("逝世年份无效")
            etDeathYear.requestFocus()
            return false
        }

        return true
    }

    private fun submitForReview() {
        val name = etName.text.toString().trim()
        val birthYear = etBirthYear.text.toString().trim()
        val deathYear = etDeathYear.text.toString().trim()
        val message = etMessage.text.toString().trim()
        val story = etStory.text.toString().trim()

        btnSubmit.isEnabled = false
        btnSubmit.text = "提交中..."

        val waitingDialog = BottomSheetDialog(this)
        waitingDialog.setContentView(R.layout.dialog_waiting)
        waitingDialog.setCancelable(false)
        waitingDialog.show()

        lifecycleScope.launch {
            try {
                var avatarUrl: String? = null

                selectedImageUri?.let { uri ->
                    val imageFile = uriToFile(uri)
                    if (imageFile != null) {
                        val uploadResult = bmobRepository.uploadImage(imageFile)
                        uploadResult.onSuccess { url ->
                            avatarUrl = url
                        }.onFailure {
                            // 图片上传失败不影响提交
                        }
                    }
                }

                val lifeYears = "${birthYear.ifEmpty { "?" }}年 - ${deathYear}年"

                val memorialStar = MemorialStarBmob().apply {
                    this.name = name
                    this.lifeYears = lifeYears
                    this.message = message.ifEmpty { null }
                    this.story = story.ifEmpty { null }
                    this.avatarUrl = avatarUrl
                    this.flowerCount = 0
                    this.createdBy = bmobRepository.getCurrentUser()?.objectId
                    this.status = "pending"
                }

                val result = bmobRepository.createMemorialStar(memorialStar)

                runOnUiThread {
                    waitingDialog.dismiss()
                    result.onSuccess {
                        showSuccessDialog(name)
                    }.onFailure { e ->
                        showError("提交失败：${e.message}")
                        btnSubmit.isEnabled = true
                        btnSubmit.text = "提交审核"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    waitingDialog.dismiss()
                    showError("提交失败：${e.message}")
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "提交审核"
                }
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun showSuccessDialog(name: String) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_submit_success, null)

        view.findViewById<TextView>(R.id.tvContent).text =
            "「${name}」的纪念星已成功添加！\n\n" +
            "感谢您对「好好活」的支持。"

        view.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}