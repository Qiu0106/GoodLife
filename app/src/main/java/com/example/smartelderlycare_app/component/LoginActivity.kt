package com.example.smartelderlycare_app.component

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.repository.BmobTestHelper
import com.example.smartelderlycare_app.data.repository.TestStatus
import com.example.smartelderlycare_app.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvTestBmob: TextView

    private val viewModel: UserViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        tvTestBmob = findViewById(R.id.tvTestBmob)

        // 观察ViewModel状态
        observeViewModel()

        btnLogin.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (!validateInput(phone, password)) return@setOnClickListener

            // 调用Bmob登录
            viewModel.login(phone, password)
        }

        btnRegister.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (!validateInput(phone, password)) return@setOnClickListener

            // 调用Bmob注册
            viewModel.register(phone, password)
        }

        // 测试Bmob连接
        tvTestBmob.setOnClickListener {
            testBmobConnection()
        }
    }

    /**
     * 测试 Bmob 连接
     */
    private fun testBmobConnection() {
        lifecycleScope.launch {
            Toast.makeText(this@LoginActivity, "正在测试 Bmob 连接...", Toast.LENGTH_SHORT).show()

            // 检查初始化状态
            val isInitialized = BmobTestHelper.checkBmobInitialization()
            if (!isInitialized) {
                Toast.makeText(this@LoginActivity, "Bmob 未初始化，请检查 MyApplication", Toast.LENGTH_LONG).show()
                return@launch
            }

            // 测试 User 表
            val userResult = BmobTestHelper.testQueryUserTable()

            // 测试 AfterlifePlan 表
            val planResult = BmobTestHelper.testQueryAfterlifePlanTable()

            // 显示结果
            val message = buildString {
                appendLine("_User 表: ${if (userResult.status == TestStatus.SUCCESS) "✓" else "✗"}")
                appendLine(userResult.message)
                appendLine()
                appendLine("AfterlifePlan 表: ${if (planResult.status == TestStatus.SUCCESS) "✓" else "✗"}")
                appendLine(planResult.message)
                appendLine()
                appendLine("点击确定进入应用")
            }

            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()

            // 打印详细日志
            BmobTestHelper.printTestReport(listOf(userResult, planResult))
        }
    }

    private fun observeViewModel() {
        // 观察加载状态
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        })

        // 观察错误信息
        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        })

        // 观察成功信息（登录/注册成功）
        viewModel.successMessage.observe(this, Observer { successMessage ->
            successMessage?.let {
                android.util.Log.d("LoginActivity", "收到成功消息: $it")
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()

                val user = viewModel.currentUser.value
                android.util.Log.d("LoginActivity", "当前用户: $user")

                getSharedPreferences("user", MODE_PRIVATE).edit()
                    .putString("phone", user?.phone ?: "")
                    .putString("userId", user?.token)
                    .putString("nickname", user?.nickname)
                    .apply()

                android.util.Log.d("LoginActivity", "准备跳转到 MainActivity...")
                
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
        })
    }

    private fun validateInput(phone: String, password: String): Boolean {
        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入手机号和密码", Toast.LENGTH_SHORT).show()
            return false
        }
        if (phone.length != 11) {
            Toast.makeText(this, "请输入11位手机号", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "密码长度不能少于6位", Toast.LENGTH_SHORT).show()
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
