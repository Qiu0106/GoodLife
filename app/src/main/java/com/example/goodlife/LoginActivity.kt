package com.example.goodlife

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.goodlife.ui.theme.GoodLifeTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoodLifeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        onLoginSuccess = { _, _ ->
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

private enum class LoginStep {
    PhoneVerification,
    SetPassword
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String, String) -> Unit = { _, _ -> }
) {
    var currentStep by remember { mutableStateOf(LoginStep.PhoneVerification) }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (currentStep) {
            LoginStep.PhoneVerification -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "手机号登录")

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "手机号") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "验证码（测试用 123456）") },
                        singleLine = true
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            errorMessage = null

                            if (phone.length != 11) {
                                errorMessage = "请输入 11 位手机号"
                                return@Button
                            }

                            if (code.isBlank()) {
                                errorMessage = "验证码不能为空，请重新输入（测试验证码：123456）"
                                return@Button
                            }

                            if (code != "123456") {
                                errorMessage = "验证码错误，请重新输入（测试验证码：123456）"
                                return@Button
                            }

                            currentStep = LoginStep.SetPassword
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "下一步")
                    }
                }
            }

            LoginStep.SetPassword -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "设置密码")

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "密码（至少 6 位）") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "再次输入密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            errorMessage = null

                            if (password.length < 6) {
                                errorMessage = "密码至少需要 6 位"
                                return@Button
                            }

                            if (password != confirmPassword) {
                                errorMessage = "两次输入的密码不一致，请重新输入"
                                return@Button
                            }

                            onLoginSuccess(phone, password)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "完成登录")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    GoodLifeTheme {
        LoginScreen()
    }
}

