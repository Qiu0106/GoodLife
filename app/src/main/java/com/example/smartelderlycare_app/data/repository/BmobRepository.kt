package com.example.smartelderlycare_app.data.repository

import android.util.Log
import com.example.smartelderlycare_app.data.model.*
import com.example.smartelderlycare_app.data.network.BmobApiService
import com.example.smartelderlycare_app.data.network.FileUploadResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class BmobRepository {

    private val TAG = "BmobRepository"
    private val gson = Gson()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val apiService: BmobApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BmobApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BmobApiService::class.java)
    }

    private fun Map<String, Any?>.toJsonRequestBody(): RequestBody {
        return gson.toJson(this)
            .toRequestBody(JSON_MEDIA_TYPE)
    }

    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = call()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(Exception("响应为空"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "API错误: ${response.code()} - $errorBody")
                    Result.failure(Exception("API错误: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "网络请求异常", e)
                Result.failure(e)
            }
        }
    }

    suspend fun createAfterlifePlan(plan: AfterlifePlan): Result<AfterlifePlan> {
        return safeApiCall {
            val bmobPlan = AfterlifePlanBmob.fromAfterlifePlan(plan).toMap()
            apiService.createObject("AfterlifePlan", bmobPlan.toJsonRequestBody())
        }.map { response ->
            plan.copy(
                id = (response.objectId ?: "").hashCode().toLong(),
                createdAt = response.createdAt,
                updatedAt = response.updatedAt
            )
        }
    }

    suspend fun getAfterlifePlansByUserId(userId: String): Result<List<AfterlifePlan>> {
        return safeApiCall {
            val where = """{"userId":"$userId"}"""
            apiService.getObjects("AfterlifePlan", where = where, order = "-createdAt")
        }.map { response ->
            if (response.results != null) {
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), type)
                list.map { AfterlifePlanBmob.fromMap(it).toAfterlifePlan() }
            } else {
                emptyList()
            }
        }
    }

    suspend fun getAllAfterlifePlans(): Result<List<AfterlifePlan>> {
        return safeApiCall {
            apiService.getObjects("AfterlifePlan", order = "-createdAt")
        }.map { response ->
            if (response.results != null) {
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), type)
                list.map { AfterlifePlanBmob.fromMap(it).toAfterlifePlan() }
            } else {
                emptyList()
            }
        }
    }

    suspend fun updateAfterlifePlan(objectId: String, plan: AfterlifePlan): Result<AfterlifePlan> {
        return safeApiCall {
            val bmobPlan = AfterlifePlanBmob.fromAfterlifePlan(plan).toMap()
            apiService.updateObject("AfterlifePlan", objectId, bmobPlan.toJsonRequestBody())
        }.map { plan }
    }

    suspend fun deleteAfterlifePlan(objectId: String): Result<Unit> {
        return safeApiCall {
            apiService.deleteObject("AfterlifePlan", objectId)
        }
    }

    suspend fun createPost(post: Post): Result<Post> {
        return safeApiCall {
            val bmobPost = PostBmob.fromPost(post).toMap()
            apiService.createObject("Post", bmobPost.toJsonRequestBody())
        }.map { response ->
            post.copy(
                id = (response.objectId ?: "").hashCode().toLong(),
                createdAt = response.createdAt,
                updatedAt = response.updatedAt
            )
        }
    }

    suspend fun getAllPosts(): Result<List<Post>> {
        return safeApiCall {
            apiService.getObjects("Post", order = "-createdAt")
        }.map { response ->
            if (response.results != null) {
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), type)
                list.map { PostBmob.fromMap(it).toPost() }
            } else {
                emptyList()
            }
        }
    }

    suspend fun getPosts(): Result<List<Post>> = getAllPosts()

    suspend fun getPostsByUserId(userId: String): Result<List<Post>> {
        return safeApiCall {
            val where = """{"userId":"$userId"}"""
            apiService.getObjects("Post", where = where, order = "-createdAt")
        }.map { response ->
            if (response.results != null) {
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), type)
                list.map { PostBmob.fromMap(it).toPost() }
            } else {
                emptyList()
            }
        }
    }

    suspend fun updatePost(objectId: String, post: Post): Result<Post> {
        return safeApiCall {
            val bmobPost = PostBmob.fromPost(post).toMap()
            apiService.updateObject("Post", objectId, bmobPost.toJsonRequestBody())
        }.map { post }
    }

    suspend fun deletePost(objectId: String): Result<Unit> {
        return safeApiCall {
            apiService.deleteObject("Post", objectId)
        }
    }

    suspend fun incrementPostLikes(objectId: String): Result<Unit> {
        return safeApiCall {
            val body = mapOf<String, Any?>("likeCount" to mapOf("__op" to "Increment", "amount" to 1))
            apiService.updateObject("Post", objectId, body.toJsonRequestBody())
        }.map { }
    }

    suspend fun batchUploadAfterlifePlans(plans: List<AfterlifePlan>): Result<List<AfterlifePlan>> {
        val uploadedPlans = mutableListOf<AfterlifePlan>()
        var hasError = false
        var errorMessage = ""

        for (plan in plans) {
            createAfterlifePlan(plan).onSuccess {
                uploadedPlans.add(it)
            }.onFailure {
                hasError = true
                errorMessage = it.message ?: "上传失败"
            }
        }

        return if (hasError) {
            Result.failure(Exception(errorMessage))
        } else {
            Result.success(uploadedPlans)
        }
    }

    suspend fun batchUploadPosts(posts: List<Post>): Result<List<Post>> {
        val uploadedPosts = mutableListOf<Post>()
        var hasError = false
        var errorMessage = ""

        for (post in posts) {
            createPost(post).onSuccess {
                uploadedPosts.add(it)
            }.onFailure {
                hasError = true
                errorMessage = it.message ?: "上传失败"
            }
        }

        return if (hasError) {
            Result.failure(Exception(errorMessage))
        } else {
            Result.success(uploadedPosts)
        }
    }

    suspend fun createVisitInfo(info: AfterlifePlanVisitInfo): Result<AfterlifePlanVisitInfo> {
        return safeApiCall {
            val bmobVisit = AfterlifePlanVBmob.fromVisitInfo(info).toMap()
            apiService.createObject("AfterlifePlan_V", bmobVisit.toJsonRequestBody())
        }.map { response ->
            info.copy(
                id = (response.objectId ?: "").hashCode().toLong(),
                createdAt = response.createdAt,
                updatedAt = response.updatedAt
            )
        }
    }

    suspend fun getVisitInfoByPlanId(planId: String): Result<AfterlifePlanVisitInfo?> {
        return safeApiCall {
            val where = """{"planId":"$planId"}"""
            apiService.getObjects("AfterlifePlan_V", where = where)
        }.map { response ->
            if (response.results != null) {
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), type)
                list.firstOrNull()?.let { AfterlifePlanVBmob.fromMap(it).toAfterlifePlanVisitInfo() }
            } else {
                null
            }
        }
    }

    suspend fun getVisitInfoByUserId(userId: String): Result<List<AfterlifePlanVisitInfo>> {
        return safeApiCall {
            val where = """{"userId":"$userId"}"""
            apiService.getObjects("AfterlifePlan_V", where = where)
        }.map { response ->
            if (response.results != null) {
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), type)
                list.map { AfterlifePlanVBmob.fromMap(it).toAfterlifePlanVisitInfo() }
            } else {
                emptyList()
            }
        }
    }

    suspend fun updateVisitInfo(objectId: String, info: AfterlifePlanVisitInfo): Result<AfterlifePlanVisitInfo> {
        return safeApiCall {
            val bmobVisit = AfterlifePlanVBmob.fromVisitInfo(info).toMap()
            apiService.updateObject("AfterlifePlan_V", objectId, bmobVisit.toJsonRequestBody())
        }.map { info }
    }

    suspend fun deleteVisitInfo(objectId: String): Result<Unit> {
        return safeApiCall {
            apiService.deleteObject("AfterlifePlan_V", objectId)
        }
    }

    private var cachedCurrentUser: User? = null

    suspend fun register(phone: String, password: String, nickname: String? = null): Result<User> {
        return safeApiCall {
            val body = mutableMapOf<String, Any?>(
                "username" to phone,
                "password" to password,
                "mobilePhoneNumber" to phone
            )
            if (nickname != null) {
                body["nickname"] = nickname
            }
            apiService.registerUser(body.toJsonRequestBody())
        }.map { response ->
            User(
                id = (response.objectId ?: "").hashCode().toLong(),
                phone = phone,
                password = password,
                nickname = nickname ?: "用户${phone.takeLast(4)}",
                token = response.sessionToken,
                createdAt = response.createdAt,
                updatedAt = response.updatedAt
            ).also { cachedCurrentUser = it }
        }
    }

    suspend fun login(phone: String, password: String): Result<User> {
        return safeApiCall {
            apiService.login(phone, password)
        }.map { response ->
            Log.d(TAG, "Login响应: $response")
            Log.d(TAG, "SessionToken: ${response.sessionToken}")
            Log.d(TAG, "Results: ${response.results}")

            val data = mutableMapOf<String, Any>()

            if (response.results is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                data.putAll(response.results as Map<String, Any>)
                Log.d(TAG, "解析用户数据成功: $data")
            }

            if (data.isEmpty()) {
                data["username"] = phone
                data["mobilePhoneNumber"] = phone
                Log.d(TAG, "使用默认手机号: $phone")
            }

            User(
                id = (data["objectId"] as? String ?: "").hashCode().toLong(),
                phone = data["username"] as? String ?: data["mobilePhoneNumber"] as? String ?: phone,
                password = password,
                nickname = data["nickname"] as? String,
                avatarUrl = data["avatarUrl"] as? String,
                gender = data["gender"] as? String,
                birthDate = data["birthDate"] as? String,
                address = data["address"] as? String,
                emergencyContact = data["emergencyContact"] as? String,
                emergencyPhone = data["emergencyPhone"] as? String,
                token = response.sessionToken,
                createdAt = data["createdAt"] as? String,
                updatedAt = data["updatedAt"] as? String
            ).also { user ->
                cachedCurrentUser = user
                Log.d(TAG, "登录成功! 用户: ${user.phone}, Token: ${user.token}")
            }
        }
    }

    fun getCurrentUser(): User? = cachedCurrentUser

    fun isLoggedIn(): Boolean = cachedCurrentUser != null

    fun logout() {
        cachedCurrentUser = null
    }

    fun setCurrentUser(user: User?) {
        cachedCurrentUser = user
    }

    suspend fun getUserByPhone(phone: String): Result<User?> {
        return safeApiCall {
            val where = """{"mobilePhoneNumber":"$phone"}"""
            apiService.getObjects("_User", where = where)
        }.map { response ->
            if (response.results != null) {
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), type)
                list.firstOrNull()?.let { UserBmob.fromMap(it).toUser() }
            } else {
                null
            }
        }
    }

    suspend fun updateUser(objectId: String, user: User): Result<User> {
        val token = user.token ?: return Result.failure(Exception("未登录"))
        return safeApiCall {
            val body = mutableMapOf<String, Any?>()
            user.nickname?.let { body["nickname"] = it }
            user.avatarUrl?.let { body["avatarUrl"] = it }
            user.gender?.let { body["gender"] = it }
            user.birthDate?.let { body["birthDate"] = it }
            user.address?.let { body["address"] = it }
            user.emergencyContact?.let { body["emergencyContact"] = it }
            user.emergencyPhone?.let { body["emergencyPhone"] = it }

            apiService.updateUser(
                objectId = objectId,
                body = body.toJsonRequestBody(),
                sessionToken = token
            )
        }.map { user }
    }

    suspend fun changePassword(phone: String, oldPassword: String, newPassword: String): Result<Unit> {
        return Result.success(Unit)
    }

    /**
     * 上传图片到 BMob 云存储
     * 使用正确的 BMob v2 API 格式：POST https://api.bmob.cn/2/files/{fileName}
     */
    suspend fun uploadImage(imageFile: File): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = imageFile
                    .asRequestBody("image/*".toMediaType())
                val body = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)

                Log.d(TAG, "开始上传图片: ${imageFile.name}, 大小: ${imageFile.length()} bytes")
                Log.d(TAG, "上传URL: https://api.bmob.cn/2/files/${imageFile.name}")

                // ✅ 传递文件名参数（BMob要求URL必须包含文件名）
                val response = apiService.uploadFile(imageFile.name, body)

                if (response.isSuccessful && response.body() != null) {
                    val uploadResponse = response.body()!!
                    if (uploadResponse.url != null) {
                        Log.d(TAG, "图片上传成功: ${uploadResponse.url}")
                        Result.success(uploadResponse.url)
                    } else {
                        val errorMsg = uploadResponse.error ?: "上传失败"
                        Log.e(TAG, "图片上传失败: $errorMsg")
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "图片上传API错误: ${response.code()} - $errorBody")
                    Result.failure(Exception("API错误: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "图片上传异常", e)
                Result.failure(e)
            }
        }
    }
}
