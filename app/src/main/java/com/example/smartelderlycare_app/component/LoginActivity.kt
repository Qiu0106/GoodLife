package com.example.smartelderlycare_app.component

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.ui.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tilPhone: TextInputLayout
    private lateinit var tilPassword: TextInputLayout

    private val viewModel: UserViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        tilPhone = findViewById(R.id.tilPhone)
        tilPassword = findViewById(R.id.tilPassword)

        observeViewModel()

        btnLogin.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (!validateInput(phone, password)) return@setOnClickListener

            viewModel.login(phone, password)
        }

        btnRegister.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (!validateInput(phone, password)) return@setOnClickListener

            viewModel.register(phone, password)
        }

        etPhone.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val phone = etPhone.text.toString().trim()
                if (phone.isNotEmpty() && phone.length != 11) {
                    setInputError(etPhone, true)
                } else {
                    setInputError(etPhone, false)
                }
            }
        }

        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = etPassword.text.toString().trim()
                if (password.isNotEmpty() && password.length < 6) {
                    setInputError(etPassword, true)
                } else {
                    setInputError(etPassword, false)
                }
            }
        }
    }

    private fun setInputError(editText: EditText, isError: Boolean) {
        if (isError) {
            editText.setBackgroundResource(R.drawable.bg_elderly_input_error)
            editText.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            editText.setBackgroundResource(R.drawable.bg_elderly_input)
        }
    }

    private fun clearInputErrors() {
        etPhone.setBackgroundResource(R.drawable.bg_elderly_input)
        etPassword.setBackgroundResource(R.drawable.bg_elderly_input)
    }

    private fun performHapticFeedback(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            @Suppress("DEPRECATION")
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                showProgressDialog()
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
                android.util.Log.d("LoginActivity", "收到成功消息: $it")
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()

                val user = viewModel.currentUser.value
                android.util.Log.d("LoginActivity", "当前用户: $user, token=${user?.token?.take(10)}, objectId=${user?.objectId}")

                val repository = com.example.smartelderlycare_app.data.repository.BmobRepository()
                
                if (user != null && user.phone != null) {
                    lifecycleScope.launch {
                        try {
                            val cloudUserResult = repository.getUserByPhone(user.phone!!)
                            cloudUserResult.onSuccess { cloudUser ->
                                if (cloudUser != null) {
                                    val finalAvatarUrl = cloudUser.avatarUrl ?: user.avatarUrl
                                    val finalToken = user.token ?: cloudUser.token
                                    val finalObjectId = cloudUser.objectId ?: user.objectId
                                    android.util.Log.d("LoginActivity", "✅ 从云端获取用户信息 - avatarUrl: $finalAvatarUrl, objectId: $finalObjectId")
                                    
                                    val prefsEdit = getSharedPreferences("user", MODE_PRIVATE).edit()
                                        .putString("phone", cloudUser.phone ?: user.phone)
                                        .putString("userId", finalToken)
                                        .putString("token", finalToken)
                                        .putString("userObjectId", finalObjectId)
                                        .putString("nickname", cloudUser.nickname ?: user.nickname)
                                        .putString("avatarUrl", finalAvatarUrl)
                                        .putString("realName", cloudUser.realName ?: user.realName)
                                        .putString("gender", cloudUser.gender ?: user.gender)
                                        .putString("birthDate", cloudUser.birthDate ?: user.birthDate)
                                        .putString("emergencyContact", cloudUser.emergencyContact ?: user.emergencyContact)
                                        .putString("emergencyPhone", cloudUser.emergencyPhone ?: user.emergencyPhone)
                                        .putString("bloodType", cloudUser.bloodType ?: user.bloodType)
                                        .putString("medicalHistory", cloudUser.medicalHistory ?: user.medicalHistory)
                                        .putString("signature", cloudUser.signature ?: user.signature)

                                    if (!finalObjectId.isNullOrEmpty()) {
                                        prefsEdit.putString("objectId", finalObjectId!!)
                                    }

                                    prefsEdit.apply()
                                    android.util.Log.d("LoginActivity", "✅ 用户信息已保存(含云端数据), userObjectId=$finalObjectId")
                                } else {
                                    saveUserInfoLocally(user)
                                }
                            }.onFailure { e ->
                                android.util.Log.w("LoginActivity", "⚠️ 获取云端用户信息失败，使用本地数据", e)
                                saveUserInfoLocally(user)
                            }
                            
                            navigateToMain()
                        } catch (e: Exception) {
                            android.util.Log.e("LoginActivity", "❌ 获取云端信息异常", e)
                            saveUserInfoLocally(user)
                            navigateToMain()
                        }
                    }
                } else {
                    saveUserInfoLocally(user)
                    navigateToMain()
                }
            }
        })
    }

    private fun saveUserInfoLocally(user: com.example.smartelderlycare_app.data.model.User?) {
        val prefsEdit = getSharedPreferences("user", MODE_PRIVATE).edit()
            .putString("phone", user?.phone ?: "")
            .putString("userId", user?.token)
            .putString("token", user?.token)
            .putString("userObjectId", user?.objectId)
            .putString("nickname", user?.nickname)
            .putString("avatarUrl", user?.avatarUrl)
            .putString("realName", user?.realName)
            .putString("gender", user?.gender)
            .putString("birthDate", user?.birthDate)
            .putString("emergencyContact", user?.emergencyContact)
            .putString("emergencyPhone", user?.emergencyPhone)
            .putString("bloodType", user?.bloodType)
            .putString("medicalHistory", user?.medicalHistory)
            .putString("signature", user?.signature)

        if (!user?.objectId.isNullOrEmpty()) {
            prefsEdit.putString("objectId", user!!.objectId!!)
            android.util.Log.d("LoginActivity", "已保存 objectId: ${user.objectId}")
        } else {
            android.util.Log.w("LoginActivity", "⚠️ objectId 为空")
        }

        prefsEdit.apply()
        android.util.Log.d("LoginActivity", "✅ 用户信息已保存(本地) - userObjectId: ${user?.objectId}, token: ${user?.token?.take(10)}")
    }

    private fun navigateToMain() {
        window.decorView.postDelayed({
            try {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                android.util.Log.d("LoginActivity", "startActivity 已调用")
                finish()
                android.util.Log.d("LoginActivity", "finish() 已调用")
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "跳转失败", e)
                Toast.makeText(this@LoginActivity, "跳转失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, 300)
    }

    private fun validateInput(phone: String, password: String): Boolean {
        clearInputErrors()
        
        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入手机号和密码", Toast.LENGTH_SHORT).show()
            if (phone.isEmpty()) setInputError(etPhone, true)
            if (password.isEmpty()) setInputError(etPassword, true)
            return false
        }
        if (phone.length != 11) {
            Toast.makeText(this, "请输入11位手机号", Toast.LENGTH_SHORT).show()
            setInputError(etPhone, true)
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "密码长度不能少于6位", Toast.LENGTH_SHORT).show()
            setInputError(etPassword, true)
            return false
        }
        return true
    }

    private fun showProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = ProgressDialog(this).apply {
            setMessage("正在处理...")
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
