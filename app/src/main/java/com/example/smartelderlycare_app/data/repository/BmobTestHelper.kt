package com.example.smartelderlycare_app.data.repository

import android.util.Log
import com.example.smartelderlycare_app.data.network.BmobApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BmobTestHelper {

    private const val TAG = "BmobTestHelper"

    private val apiService: BmobApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BmobApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BmobApiService::class.java)
    }

    suspend fun testAllTables(): List<TableTestResult> {
        val results = mutableListOf<TableTestResult>()

        results.add(testUserTable())
        results.add(testAfterlifePlanTable())
        results.add(testAfterlifePlanVTable())
        results.add(testPostTable())

        return results
    }

    private suspend fun testUserTable(): TableTestResult {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getObjects("_User", limit = 1)
            }
            if (response.isSuccessful) {
                val count = response.body()?.results?.size ?: 0
                TableTestResult(
                    tableName = "_User",
                    status = TestStatus.SUCCESS,
                    message = "连接成功，表中共有记录"
                )
            } else {
                TableTestResult(
                    tableName = "_User",
                    status = TestStatus.FAILED,
                    message = "连接失败: HTTP ${response.code()}"
                )
            }
        } catch (e: Exception) {
            TableTestResult(
                tableName = "_User",
                status = TestStatus.FAILED,
                message = "连接失败: ${e.message}"
            )
        }
    }

    private suspend fun testAfterlifePlanTable(): TableTestResult {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getObjects("AfterlifePlan", limit = 1)
            }
            if (response.isSuccessful) {
                TableTestResult(
                    tableName = "AfterlifePlan",
                    status = TestStatus.SUCCESS,
                    message = "连接成功，表中共有记录"
                )
            } else {
                TableTestResult(
                    tableName = "AfterlifePlan",
                    status = TestStatus.FAILED,
                    message = "连接失败: HTTP ${response.code()}"
                )
            }
        } catch (e: Exception) {
            TableTestResult(
                tableName = "AfterlifePlan",
                status = TestStatus.FAILED,
                message = "连接失败: ${e.message}"
            )
        }
    }

    private suspend fun testAfterlifePlanVTable(): TableTestResult {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getObjects("AfterlifePlan_V", limit = 1)
            }
            if (response.isSuccessful) {
                TableTestResult(
                    tableName = "AfterlifePlan_V",
                    status = TestStatus.SUCCESS,
                    message = "连接成功，表中共有记录"
                )
            } else {
                TableTestResult(
                    tableName = "AfterlifePlan_V",
                    status = TestStatus.FAILED,
                    message = "连接失败: HTTP ${response.code()}"
                )
            }
        } catch (e: Exception) {
            TableTestResult(
                tableName = "AfterlifePlan_V",
                status = TestStatus.FAILED,
                message = "连接失败: ${e.message}"
            )
        }
    }

    private suspend fun testPostTable(): TableTestResult {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getObjects("Post", limit = 1)
            }
            if (response.isSuccessful) {
                TableTestResult(
                    tableName = "Post",
                    status = TestStatus.SUCCESS,
                    message = "连接成功，表中共有记录"
                )
            } else {
                TableTestResult(
                    tableName = "Post",
                    status = TestStatus.FAILED,
                    message = "连接失败: HTTP ${response.code()}"
                )
            }
        } catch (e: Exception) {
            TableTestResult(
                tableName = "Post",
                status = TestStatus.FAILED,
                message = "连接失败: ${e.message}"
            )
        }
    }

    suspend fun testQueryUserTable(): TableTestResult {
        return testUserTable()
    }

    suspend fun testQueryAfterlifePlanTable(): TableTestResult {
        return testAfterlifePlanTable()
    }

    fun checkBmobInitialization(): Boolean {
        return true
    }

    fun printTestReport(results: List<TableTestResult>) {
        Log.d(TAG, "========== Bmob 连接测试报告 ==========")
        var successCount = 0
        var failedCount = 0

        results.forEach { result ->
            val statusIcon = when (result.status) {
                TestStatus.SUCCESS -> "✓"
                TestStatus.FAILED -> "✗"
                TestStatus.WARNING -> "!"
            }
            Log.d(TAG, "[$statusIcon] ${result.tableName}: ${result.message}")

            when (result.status) {
                TestStatus.SUCCESS -> successCount++
                TestStatus.FAILED -> failedCount++
                else -> {}
            }
        }

        Log.d(TAG, "----------------------------------------")
        Log.d(TAG, "总计: ${results.size} 个表，成功: $successCount，失败: $failedCount")
        Log.d(TAG, "=======================================")
    }
}

data class TableTestResult(
    val tableName: String,
    val status: TestStatus,
    val message: String
)

enum class TestStatus {
    SUCCESS,
    FAILED,
    WARNING
}
