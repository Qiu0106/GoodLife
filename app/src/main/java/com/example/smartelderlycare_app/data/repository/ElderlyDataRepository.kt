package com.example.smartelderlycare_app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.smartelderlycare_app.data.model.CheckinRecord
import com.example.smartelderlycare_app.data.model.User
import com.example.smartelderlycare_app.data.network.PromptBuilder
import java.text.SimpleDateFormat
import java.util.*

/**
 * 长辈数据仓库
 *
 * 统一管理 AI 语音助手所需的所有用户/长辈数据
 * 数据来源：SharedPreferences（用户信息）、Bmob（打卡记录、身后事计划等）
 */
class ElderlyDataRepository(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("user", Context.MODE_PRIVATE)
    }

    private val bmobRepository = BmobRepository()

    /**
     * 获取 AI Prompt 所需的长辈状态
     */
    suspend fun getElderlyStatus(): PromptBuilder.ElderlyStatus {
        val user = getCurrentUser()
        val checkinRecord = getTodayCheckinRecord()

        return PromptBuilder.ElderlyStatus(
            name = user?.nickname ?: user?.realName ?: "长辈",
            age = calculateAge(user?.birthDate),
            gender = user?.gender ?: "未知",
            todaySteps = checkinRecord?.totalPoints ?: 0,
            stepGoal = 6000,
            medicationStatus = getMedicationStatus(),
            healthNotes = user?.medicalHistory ?: "",
            mood = checkinRecord?.let { "正常" } ?: "未知"
        )
    }

    /**
     * 获取当前登录用户
     */
    fun getCurrentUser(): User? {
        val userId = prefs.getString("userId", null) ?: return null

        return User(
            id = userId,
            objectId = userId,
            phone = prefs.getString("phone", "") ?: "",
            nickname = prefs.getString("nickname", null),
            realName = prefs.getString("realName", null),
            avatarUrl = prefs.getString("avatarUrl", null),
            gender = prefs.getString("gender", null),
            birthDate = prefs.getString("birthDate", null),
            address = prefs.getString("address", null),
            emergencyContact = prefs.getString("emergencyContact", null),
            emergencyPhone = prefs.getString("emergencyPhone", null),
            bloodType = prefs.getString("bloodType", null),
            medicalHistory = prefs.getString("medicalHistory", null),
            signature = prefs.getString("signature", null)
        )
    }

    /**
     * 获取当前用户 ID
     */
    fun getCurrentUserId(): String? {
        return prefs.getString("userId", null)
    }

    /**
     * 获取今日打卡记录
     */
    private suspend fun getTodayCheckinRecord(): CheckinRecord? {
        val userId = getCurrentUserId() ?: return null
        return try {
            bmobRepository.getCheckinRecord(userId).getOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取用药状态
     * TODO: 实际应从 AfterlifePlan 或专门的用药记录表查询
     */
    private fun getMedicationStatus(): PromptBuilder.MedicationStatus {
        val lastMedicationTime = prefs.getLong("lastMedicationTime", 0L)
        if (lastMedicationTime == 0L) return PromptBuilder.MedicationStatus.NONE

        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
        val lastDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(lastMedicationTime))

        return when {
            today == lastDate -> PromptBuilder.MedicationStatus.TAKEN
            else -> PromptBuilder.MedicationStatus.PENDING
        }
    }

    /**
     * 记录今日用药
     */
    fun recordMedicationTaken() {
        prefs.edit().putLong("lastMedicationTime", System.currentTimeMillis()).apply()
    }

    /**
     * 根据出生日期计算年龄
     */
    private fun calculateAge(birthDate: String?): Int {
        if (birthDate.isNullOrEmpty()) return 0

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birth = sdf.parse(birthDate) ?: return 0
            val now = Calendar.getInstance()

            var age = now.get(Calendar.YEAR) - birth.year - 1900

            val birthDayOfYear = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(birth).let {
                val parts = it.split("-")
                (parts[1].toInt() * 100 + parts[2].toInt())
            }
            val todayDayOfYear = now.get(Calendar.MONTH) * 100 + now.get(Calendar.DAY_OF_MONTH)

            if (todayDayOfYear < birthDayOfYear) {
                age--
            }
            age.coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 获取用户头像 URL
     */
    fun getAvatarUrl(): String? {
        return prefs.getString("avatarUrl", null)
    }

    /**
     * 获取用户昵称
     */
    fun getNickname(): String? {
        return prefs.getString("nickname", null)
    }

    /**
     * 获取用户真实姓名
     */
    fun getRealName(): String? {
        return prefs.getString("realName", null)
    }

    /**
     * 更新本地用户缓存
     */
    fun updateUserCache(user: User) {
        prefs.edit().apply {
            user.id?.let { putString("userId", it.toString()) }
            user.objectId?.let { putString("userId", it) }
            user.nickname?.let { putString("nickname", it) }
            user.realName?.let { putString("realName", it) }
            user.avatarUrl?.let { putString("avatarUrl", it) }
            user.gender?.let { putString("gender", it) }
            user.birthDate?.let { putString("birthDate", it) }
            user.address?.let { putString("address", it) }
            user.emergencyContact?.let { putString("emergencyContact", it) }
            user.emergencyPhone?.let { putString("emergencyPhone", it) }
            user.bloodType?.let { putString("bloodType", it) }
            user.medicalHistory?.let { putString("medicalHistory", it) }
            user.signature?.let { putString("signature", it) }
            apply()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ElderlyDataRepository? = null

        fun getInstance(context: Context): ElderlyDataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ElderlyDataRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}