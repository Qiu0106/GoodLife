package com.example.smartelderlycare_app.component

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.smartelderlycare_app.R

/**
 * 药物提醒弹窗 Activity
 * 以 Dialog 样式全屏展示，醒目提醒用户服药
 */
class MedicationDialogActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MedicationDialogActivity"
    }

    private var medicationName: String = ""
    private var medicationId: Int = 0
    private var notificationId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication_dialog)

        medicationName = intent.getStringExtra("medication_name") ?: "未知药物"
        medicationId = intent.getIntExtra("medication_id", 0)
        notificationId = intent.getIntExtra("notification_id", 0)

        setupWindow()

        val tvMedicationName = findViewById<TextView>(R.id.tv_medication_name)
        val btnTaken = findViewById<Button>(R.id.btn_taken)
        val btnRemindLater = findViewById<Button>(R.id.btn_remind_later)

        tvMedicationName.text = "该吃药了：$medicationName"

        btnTaken.setOnClickListener {
            cancelNotification()
            finish()
        }

        btnRemindLater.setOnClickListener {
            cancelNotification()
            finish()
        }
    }

    /**
     * 设置窗口属性：全屏、锁屏显示、保持屏幕亮起
     */
    private fun setupWindow() {
        window.apply {
            addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
    }

    /**
     * 取消通知
     */
    private fun cancelNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    override fun onBackPressed() {
        // 禁止返回键关闭弹窗，必须点击按钮
    }
}