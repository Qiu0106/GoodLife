package com.example.smartelderlycare_app.component

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.Post
import com.example.smartelderlycare_app.data.repository.BmobRepository
import com.example.smartelderlycare_app.ui.viewmodel.PostViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File

class CreatePostActivity : AppCompatActivity() {

    private lateinit var btnCancel: TextView
    private lateinit var btnPublish: MaterialButton
    private lateinit var etPostContent: EditText
    private lateinit var imagesContainer: LinearLayout
    private lateinit var btnAddImage: FrameLayout

    private val viewModel: PostViewModel by viewModels()
    private val repository = BmobRepository()
    private var progressDialog: ProgressDialog? = null

    private val selectedImageUris = mutableListOf<Uri>()
    private val uploadedImageUrls = mutableListOf<String>()

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        initViews()
        setupImagePicker()
        observeViewModel()
    }

    private fun initViews() {
        btnCancel = findViewById(R.id.btn_cancel)
        btnPublish = findViewById(R.id.btn_publish)
        etPostContent = findViewById(R.id.et_post_content)
        imagesContainer = findViewById(R.id.images_container)
        btnAddImage = findViewById(R.id.btn_add_image)

        btnCancel.setOnClickListener {
            finish()
        }

        btnPublish.setOnClickListener {
            publishPost()
        }

        btnAddImage.setOnClickListener {
            openImagePicker()
        }
    }

    private fun setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    addSelectedImage(imageUri)
                }
            }
        }
    }

    private fun openImagePicker() {
        if (selectedImageUris.size >= 9) {
            Toast.makeText(this, "最多上传9张图片", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(Intent.createChooser(intent, "选择图片"))
    }

    private fun addSelectedImage(imageUri: Uri) {
        selectedImageUris.add(imageUri)

        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                100.dpToPx(),
                100.dpToPx()
            ).apply {
                setMargins(0, 0, 8.dpToPx(), 0)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP

            try {
                contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e("CreatePostActivity", "加载图片失败", e)
            }

            setOnClickListener {
                removeImage(imageUri, this)
            }
        }

        imagesContainer.addView(imageView, imagesContainer.childCount - 1)
        Toast.makeText(this, "已选择 ${selectedImageUris.size}/9 张图片", Toast.LENGTH_SHORT).show()
    }

    private fun removeImage(imageUri: Uri, imageView: ImageView) {
        val index = selectedImageUris.indexOf(imageUri)
        if (index != -1) {
            selectedImageUris.removeAt(index)
            imagesContainer.removeView(imageView)
            Toast.makeText(this, "已删除，剩余 ${selectedImageUris.size} 张图片", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                showProgressDialog("正在发布...")
            } else {
                hideProgressDialog()
            }
        })

        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        })

        viewModel.successMessage.observe(this, Observer { successMessage ->
            successMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
                finish()
            }
        })
    }

    private fun publishPost() {
        val content = etPostContent.text.toString().trim()

        if (content.isEmpty()) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = getSharedPreferences("user", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userObjectId", null)
            ?: sharedPreferences.getString("objectId", null)
            ?: sharedPreferences.getString("userId", "anonymous") ?: "anonymous"
        val userName = sharedPreferences.getString("nickname", "匿名用户") ?: "匿名用户"
        val userAvatarUrl = sharedPreferences.getString("avatarUrl", null)

        if (selectedImageUris.isNotEmpty()) {
            uploadImagesAndPublish(content, userId, userName, userAvatarUrl)
        } else {
            publishPostDirectly(content, userId, userName, null, null, userAvatarUrl)
        }
    }

    private fun uploadImagesAndPublish(content: String, userId: String, userName: String, userAvatarUrl: String?) {
        progressDialog?.dismiss()
        progressDialog = ProgressDialog(this).apply {
            setMessage("正在上传图片 (0/${selectedImageUris.size})...")
            setCancelable(false)
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            max = selectedImageUris.size
            progress = 0
            show()
        }

        CoroutineScope(Dispatchers.Main).launch {
            uploadedImageUrls.clear()
            val tempFiles = mutableListOf<File>()

            for ((index, imageUri) in selectedImageUris.withIndex()) {
                try {
                    val file = getFileFromUri(imageUri)
                    tempFiles.add(file)

                    repository.uploadImage(file)
                        .onSuccess { url ->
                            uploadedImageUrls.add(url)
                            Log.d("CreatePostActivity", "图片 ${index + 1} 上传成功: $url")
                        }
                        .onFailure { error ->
                            Log.e("CreatePostActivity", "图片 ${index + 1} 上传失败", error)
                        }

                    progressDialog?.progress = index + 1
                    progressDialog?.setMessage("正在上传图片 (${index + 1}/${selectedImageUris.size})...")

                } catch (e: Exception) {
                    Log.e("CreatePostActivity", "处理图片失败", e)
                }
            }

            for (file in tempFiles) {
                try { file.delete() } catch (_: Exception) {}
            }

            hideProgressDialog()

            if (uploadedImageUrls.isEmpty() && selectedImageUris.isNotEmpty()) {
                Toast.makeText(this@CreatePostActivity, "图片上传失败，请重试", Toast.LENGTH_LONG).show()
                return@launch
            }

            val coverUrl = uploadedImageUrls.firstOrNull()
            val imageUrlsJson = if (uploadedImageUrls.size > 1) {
                JSONArray().apply {
                    uploadedImageUrls.forEach { put(it) }
                }.toString()
            } else null

            publishPostDirectly(content, userId, userName, coverUrl, imageUrlsJson, userAvatarUrl)
        }
    }

    private fun publishPostDirectly(content: String, userId: String, userName: String, coverUrl: String?, imageUrls: String? = null, userAvatarUrl: String? = null) {
        val post = Post(
            userId = userId,
            title = content.take(20) + if (content.length > 20) "..." else "",
            content = content,
            coverImageUrl = coverUrl,
            imageUrls = imageUrls,
            userName = userName,
            userAvatarUrl = userAvatarUrl,
            likeCount = 0,
            commentCount = 0
        )

        viewModel.createPost(post)
    }

    private fun getFileFromUri(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        tempFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }
        inputStream?.close()
        return tempFile
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
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

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
    }
}