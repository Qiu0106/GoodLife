package com.example.smartelderlycare_app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.smartelderlycare_app.component.MedicationAlarmReceiver
import java.util.Calendar

object MedicationAlarmManager {

    private const val TAG = "MedicationAlarmManager"

    fun scheduleAlarm(context: Context, medicationId: Int, medicationName: String, timeStr: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val (hour, minute) = parseTime(timeStr) ?: run {
            Log.e(TAG, "时间格式解析失败: $timeStr")
            return
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("medication_name", medicationName)
            putExtra("medication_id", medicationId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "闹钟已设置: $medicationName @ $timeStr")
        } catch (e: SecurityException) {
            Log.e(TAG, "缺少精确闹钟权限", e)
        }
    }

    fun cancelAlarm(context: Context, medicationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAllAlarms(context: Context, medicationIds: List<Int>) {
        medicationIds.forEach { cancelAlarm(context, it) }
    }

    private fun parseTime(timeStr: String): Pair<Int, Int>? {
        return try {
            val parts = timeStr.split(":")
            if (parts.size != 2) return null
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            if (hour in 0..23 && minute in 0..59) {
                Pair(hour, minute)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}