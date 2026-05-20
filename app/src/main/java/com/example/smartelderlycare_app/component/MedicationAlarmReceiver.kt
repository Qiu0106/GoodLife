package com.example.smartelderlycare_app.component

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smartelderlycare_app.R

class MedicationAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MedicationAlarmReceiver"
        private const val CHANNEL_ID = "medication_reminder_channel"
        private const val CHANNEL_NAME = "用药提醒"
        private const val NOTIFICATION_ID_BASE = 9000
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicationName = intent.getStringExtra("medication_name") ?: "未知药物"
        val medicationId = intent.getIntExtra("medication_id", 0)
        val notificationId = NOTIFICATION_ID_BASE + medicationId

        Log.d(TAG, "收到用药提醒: $medicationName")

        acquireWakeLock(context)
        createNotificationChannel(context)
        playAlarmSoundAndVibrate(context)
        sendNotification(context, medicationName, medicationId, notificationId)
        launchDialogActivity(context, medicationName, medicationId, notificationId)
    }

    private fun acquireWakeLock(context: Context) {
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "MedicationAlarm:WakeLock"
            )
            wakeLock.acquire(10 * 1000L)
        } catch (e: Exception) {
            Log.e(TAG, "获取唤醒锁失败", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "药物服用提醒通知"
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    null
                )
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun playAlarmSoundAndVibrate(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 500, 500, 500, 500),
                        intArrayOf(0, 255, 0, 255, 0, 255),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 500, 500, 500, 500, 500),
                            intArrayOf(0, 255, 0, 255, 0, 255),
                            -1
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 500, 500, 500, 500, 500), -1)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "震动失败", e)
        }
    }

    private fun sendNotification(
        context: Context,
        medicationName: String,
        medicationId: Int,
        notificationId: Int
    ) {
        val dialogIntent = Intent(context, MedicationDialogActivity::class.java).apply {
            putExtra("medication_name", medicationName)
            putExtra("medication_id", medicationId)
            putExtra("notification_id", notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            medicationId,
            dialogIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ 用药提醒")
            .setContentText("该吃药了：$medicationName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setOngoing(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 500, 500, 500, 500))
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun launchDialogActivity(
        context: Context,
        medicationName: String,
        medicationId: Int,
        notificationId: Int
    ) {
        val dialogIntent = Intent(context, MedicationDialogActivity::class.java).apply {
            putExtra("medication_name", medicationName)
            putExtra("medication_id", medicationId)
            putExtra("notification_id", notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(dialogIntent)
    }
}