package com.example.smartelderlycare_app.data.sync

import android.content.Context
import android.content.SharedPreferences
import com.example.smartelderlycare_app.data.model.AfterlifePlan
import com.example.smartelderlycare_app.data.model.Post
import com.example.smartelderlycare_app.data.repository.BmobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 数据同步管理器
 * 负责本地数据与Bmob服务器的数据同步
 */
class DataSyncManager private constructor(context: Context) {

    private val repository = BmobRepository()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("data_sync", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: DataSyncManager? = null

        fun getInstance(context: Context): DataSyncManager {
            return instance ?: synchronized(this) {
                instance ?: DataSyncManager(context.applicationContext).also {
                    instance = it
                }
            }
        }

        // 同步状态常量
        const val SYNC_STATUS_PENDING = "pending"
        const val SYNC_STATUS_SYNCED = "synced"
        const val SYNC_STATUS_FAILED = "failed"
    }

    /**
     * 同步所有本地数据到服务器
     */
    suspend fun syncAllData(): SyncResult = withContext(Dispatchers.IO) {
        val results = mutableListOf<SyncResult>()

        // 同步身后事计划
        results.add(syncAfterlifePlans())

        // 同步帖子
        results.add(syncPosts())

        // 合并结果
        val hasError = results.any { !it.success }
        val totalUploaded = results.sumOf { it.uploadedCount }
        val totalFailed = results.sumOf { it.failedCount }

        SyncResult(
            success = !hasError,
            uploadedCount = totalUploaded,
            failedCount = totalFailed,
            message = if (hasError) "部分数据同步失败" else "所有数据同步成功"
        )
    }

    /**
     * 同步身后事计划
     */
    suspend fun syncAfterlifePlans(): SyncResult = withContext(Dispatchers.IO) {
        var uploadedCount = 0
        var failedCount = 0
        val failedItems = mutableListOf<String>()

        // 获取所有待同步的身后事计划
        val pendingPlans = getPendingAfterlifePlans()

        for (plan in pendingPlans) {
            repository.createAfterlifePlan(plan)
                .onSuccess {
                    uploadedCount++
                    markAfterlifePlanAsSynced(plan.id?.toString() ?: "")
                }
                .onFailure {
                    failedCount++
                    failedItems.add(plan.name)
                    markAfterlifePlanAsFailed(plan.id?.toString() ?: "")
                }
        }

        SyncResult(
            success = failedCount == 0,
            uploadedCount = uploadedCount,
            failedCount = failedCount,
            message = if (failedCount > 0) "身后事计划同步失败: ${failedItems.joinToString(", ")}" else "身后事计划同步成功"
        )
    }

    /**
     * 同步帖子
     */
    suspend fun syncPosts(): SyncResult = withContext(Dispatchers.IO) {
        var uploadedCount = 0
        var failedCount = 0
        val failedItems = mutableListOf<String>()

        // 获取所有待同步的帖子
        val pendingPosts = getPendingPosts()

        for (post in pendingPosts) {
            repository.createPost(post)
                .onSuccess {
                    uploadedCount++
                    markPostAsSynced(post.id?.toString() ?: "")
                }
                .onFailure {
                    failedCount++
                    failedItems.add(post.title)
                    markPostAsFailed(post.id?.toString() ?: "")
                }
        }

        SyncResult(
            success = failedCount == 0,
            uploadedCount = uploadedCount,
            failedCount = failedCount,
            message = if (failedCount > 0) "帖子同步失败: ${failedItems.joinToString(", ")}" else "帖子同步成功"
        )
    }

    /**
     * 从服务器获取最新数据
     */
    suspend fun fetchLatestData(): SyncResult = withContext(Dispatchers.IO) {
        var success = true
        var message = ""

        // 获取最新的身后事计划
        repository.getAllAfterlifePlans()
            .onSuccess { plans ->
                // 保存到本地
                saveAfterlifePlansToLocal(plans)
            }
            .onFailure {
                success = false
                message = "获取身后事计划失败: ${it.message}"
            }

        // 获取最新的帖子
        repository.getAllPosts()
            .onSuccess { posts ->
                // 保存到本地
                savePostsToLocal(posts)
            }
            .onFailure {
                success = false
                message = if (message.isEmpty()) "获取帖子失败: ${it.message}" else "$message, 获取帖子失败: ${it.message}"
            }

        SyncResult(
            success = success,
            uploadedCount = 0,
            failedCount = if (success) 0 else 1,
            message = if (success) "数据获取成功" else message
        )
    }

    // ==================== 本地数据管理 ====================

    private fun getPendingAfterlifePlans(): List<AfterlifePlan> {
        // 从SharedPreferences获取待同步的身后事计划
        // 实际项目中可能需要从数据库获取
        return emptyList()
    }

    private fun getPendingPosts(): List<Post> {
        // 从SharedPreferences获取待同步的帖子
        // 实际项目中可能需要从数据库获取
        return emptyList()
    }

    private fun saveAfterlifePlansToLocal(plans: List<AfterlifePlan>) {
        // 保存到本地
        // 实际项目中可能需要保存到数据库
    }

    private fun savePostsToLocal(posts: List<Post>) {
        // 保存到本地
        // 实际项目中可能需要保存到数据库
    }

    private fun markAfterlifePlanAsSynced(id: String) {
        sharedPreferences.edit()
            .putString("afterlife_plan_$id", SYNC_STATUS_SYNCED)
            .apply()
    }

    private fun markAfterlifePlanAsFailed(id: String) {
        sharedPreferences.edit()
            .putString("afterlife_plan_$id", SYNC_STATUS_FAILED)
            .apply()
    }

    private fun markPostAsSynced(id: String) {
        sharedPreferences.edit()
            .putString("post_$id", SYNC_STATUS_SYNCED)
            .apply()
    }

    private fun markPostAsFailed(id: String) {
        sharedPreferences.edit()
            .putString("post_$id", SYNC_STATUS_FAILED)
            .apply()
    }

    /**
     * 检查数据同步状态
     */
    fun getSyncStatus(id: String, type: String): String {
        return sharedPreferences.getString("${type}_$id", SYNC_STATUS_PENDING) ?: SYNC_STATUS_PENDING
    }

    /**
     * 清除同步记录
     */
    fun clearSyncRecords() {
        sharedPreferences.edit().clear().apply()
    }
}

/**
 * 同步结果数据类
 */
data class SyncResult(
    val success: Boolean,
    val uploadedCount: Int,
    val failedCount: Int,
    val message: String
)
