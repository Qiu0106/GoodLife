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

    private val fileUploadService: BmobApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BmobApiService.FILE_UPLOAD_BASE_URL)
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
                id = response.objectId?.let { it.hashCode().toLong() } ?: 0L,
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
                objectId = response.objectId,
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

    suspend fun getPostsByCategory(category: String): Result<List<Post>> {
        return safeApiCall {
            val where = """{"category":"$category"}"""
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

    suspend fun decrementPostLikes(objectId: String): Result<Unit> {
        return safeApiCall {
            val body = mapOf<String, Any?>("likeCount" to mapOf("__op" to "Increment", "amount" to -1))
            apiService.updateObject("Post", objectId, body.toJsonRequestBody())
        }.map { }
    }

    suspend fun incrementPostFavorites(objectId: String): Result<Unit> {
        return safeApiCall {
            val body = mapOf<String, Any?>("favoriteCount" to mapOf("__op" to "Increment", "amount" to 1))
            apiService.updateObject("Post", objectId, body.toJsonRequestBody())
        }.map { }
    }

    suspend fun decrementPostFavorites(objectId: String): Result<Unit> {
        return safeApiCall {
            val body = mapOf<String, Any?>("favoriteCount" to mapOf("__op" to "Increment", "amount" to -1))
            apiService.updateObject("Post", objectId, body.toJsonRequestBody())
        }.map { }
    }

    /**
     * 纪念星献花 - 原子性递增 flowerCount
     * 使用 Bmob 的 Increment 操作防止并发冲突
     */
    suspend fun incrementMemorialFlowers(objectId: String): Result<Unit> {
        return safeApiCall {
            val body = mapOf<String, Any?>("flowerCount" to mapOf("__op" to "Increment", "amount" to 1))
            apiService.updateObject("MemorialStar", objectId, body.toJsonRequestBody())
        }.map { }
    }

    suspend fun addInteraction(interaction: PostInteraction): Result<PostInteraction> {
        return safeApiCall {
            val body = PostInteractionBmob.fromPostInteraction(interaction).toMap()
            apiService.createObject("PostInteraction", body.toJsonRequestBody())
        }.map { response ->
            interaction.copy(
                id = (response.objectId ?: "").hashCode().toLong(),
                objectId = response.objectId,
                createdAt = response.createdAt
            )
        }
    }

    suspend fun removeInteraction(objectId: String): Result<Unit> {
        return safeApiCall {
            apiService.deleteObject("PostInteraction", objectId)
        }.map { }
    }

    suspend fun getInteraction(userId: String, postId: String, type: String): Result<PostInteraction?> {
        return safeApiCall {
            val where = """{"userId":"$userId","postId":"$postId","type":"$type"}"""
            apiService.getObjects("PostInteraction", where = where)
        }.map { response ->
            if (response.results != null) {
                val typeToken = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), typeToken)
                list.firstOrNull()?.let { PostInteractionBmob.fromMap(it).toPostInteraction() }
            } else {
                null
            }
        }
    }

    suspend fun getUserInteractions(userId: String, type: String): Result<List<PostInteraction>> {
        return safeApiCall {
            val where = """{"userId":"$userId","type":"$type"}"""
            apiService.getObjects("PostInteraction", where = where, order = "-createdAt")
        }.map { response ->
            if (response.results != null) {
                val typeToken = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), typeToken)
                list.map { PostInteractionBmob.fromMap(it).toPostInteraction() }
            } else {
                emptyList()
            }
        }
    }

    suspend fun getPostById(objectId: String): Result<Post?> {
        return safeApiCall {
            apiService.getObject("Post", objectId)
        }.map { response ->
            if (response.isNotEmpty() && response["objectId"] != null) {
                PostBmob.fromMap(response).toPost()
            } else {
                null
            }
        }
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
                id = response.objectId,
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

            val objectIdStr = data["objectId"] as? String ?: ""

            User(
                id = objectIdStr.ifEmpty { null },
                objectId = objectIdStr.ifEmpty { null },
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
                Log.d(TAG, "登录成功! 用户: ${user.phone}, objectId: ${user.objectId}, Token: ${user.token}")
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
            val where = """{"username":"$phone"}"""
            Log.d(TAG, "查询用户: where=$where")
            apiService.getObjects("_User", where = where)
        }.map { response ->
            Log.d(TAG, "查询用户响应: results=${response.results}")
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
        val token = user.token ?: return Result.failure(Exception("未登录 - token为空"))
        return safeApiCall {
            val body = mutableMapOf<String, Any?>()
            user.nickname?.let { body["nickname"] = it }
            user.avatarUrl?.let { body["avatarUrl"] = it }
            user.realName?.let { body["realName"] = it }
            user.gender?.let { body["gender"] = it }
            user.birthDate?.let { body["birthDate"] = it }
            user.address?.let { body["address"] = it }
            user.emergencyContact?.let { body["emergencyContact"] = it }
            user.emergencyPhone?.let { body["emergencyPhone"] = it }
            user.bloodType?.let { body["bloodType"] = it }
            user.medicalHistory?.let { body["medicalHistory"] = it }
            user.signature?.let { body["signature"] = it }

            Log.d(TAG, "更新用户数据: objectId=$objectId, body=$body, token=${token.take(10)}...")

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

    suspend fun uploadImage(imageFile: File): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (!imageFile.exists()) {
                    Log.e(TAG, "图片文件不存在: ${imageFile.absolutePath}")
                    return@withContext Result.failure(Exception("图片文件不存在"))
                }

                val requestBody = imageFile
                    .asRequestBody("image/*".toMediaType())
                val body = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)

                Log.d(TAG, "开始上传图片: ${imageFile.name}, 大小: ${imageFile.length()} bytes")
                Log.d(TAG, "文件路径: ${imageFile.absolutePath}")
                Log.d(TAG, "上传URL: ${BmobApiService.FILE_UPLOAD_BASE_URL}files/${imageFile.name}")

                val response = fileUploadService.uploadFile(imageFile.name, body)

                Log.d(TAG, "API响应码: ${response.code()}, 成功: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val uploadResponse = response.body()!!
                    Log.d(TAG, "响应内容: $uploadResponse")

                    if (uploadResponse.url != null) {
                        var finalUrl = uploadResponse.url
                        if (!finalUrl.startsWith("http")) {
                            finalUrl = BmobApiService.CDN_BASE_URL + finalUrl
                        }
                        finalUrl = finalUrl.replace("https://", "http://")
                        Log.d(TAG, "图片上传成功(HTTP): $finalUrl")
                        Result.success(finalUrl)
                    } else if (uploadResponse.cdn != null) {
                        var cdnUrl = uploadResponse.cdn + (uploadResponse.filename ?: imageFile.name)
                        cdnUrl = cdnUrl.replace("https://", "http://")
                        Log.d(TAG, "图片上传成功(CDN-HTTP): $cdnUrl")
                        Result.success(cdnUrl)
                    } else {
                        val errorMsg = uploadResponse.error ?: "上传失败，未返回URL"
                        Log.e(TAG, "图片上传失败: $errorMsg")
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "图片上传API错误:")
                    Log.e(TAG, "  - HTTP状态码: ${response.code()}")
                    Log.e(TAG, "  - 错误信息: $errorBody")
                    Log.e(TAG, "  - 响应消息: ${response.message()}")
                    Result.failure(Exception("API错误(${response.code()}): $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "图片上传异常", e)
                Log.e(TAG, "异常类型: ${e.javaClass.simpleName}")
                Log.e(TAG, "异常消息: ${e.message}")
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    suspend fun uploadImageWithProgress(
        imageFile: File,
        onProgress: (Int) -> Unit = {}
    ): Result<String> = uploadImage(imageFile)

    suspend fun uploadAvatar(imageFile: File, userId: String): Result<String> {
        return uploadImage(imageFile).onSuccess { avatarUrl ->
            Log.d(TAG, "头像上传完成，URL: $avatarUrl")
        }
    }

    suspend fun uploadPostImages(imageFiles: List<File>): Result<List<String>> {
        val urls = mutableListOf<String>()
        var hasError = false
        var errorMessage = ""

        for ((index, file) in imageFiles.withIndex()) {
            uploadImage(file).onSuccess { url ->
                urls.add(url)
                Log.d(TAG, "帖子图片 ${index + 1}/${imageFiles.size} 上传成功: $url")
            }.onFailure { e ->
                hasError = true
                errorMessage = e.message ?: "第${index + 1}张图片上传失败"
                Log.e(TAG, "帖子图片 ${index + 1}/${imageFiles.size} 上传失败", e)
            }
        }

        return if (hasError && urls.isEmpty()) {
            Result.failure(Exception(errorMessage))
        } else if (hasError) {
            Log.w(TAG, "部分图片上传失败，已成功: ${urls.size}/${imageFiles.size}")
            Result.success(urls)
        } else {
            Result.success(urls)
        }
    }

    suspend fun getCheckinRecord(userId: String): Result<CheckinRecord?> {
        return safeApiCall {
            val where = """{"userId":"$userId"}"""
            apiService.getObjects("CheckinRecord", where = where, limit = 1)
        }.map { response ->
            if (response.results != null) {
                val typeToken = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), typeToken)
                list.firstOrNull()?.let { CheckinRecordBmob.fromMap(it).toCheckinRecord() }
            } else {
                null
            }
        }
    }

    suspend fun createCheckinRecord(record: CheckinRecord): Result<CheckinRecord> {
        return safeApiCall {
            val body = CheckinRecordBmob.fromCheckinRecord(record).toMap()
            apiService.createObject("CheckinRecord", body.toJsonRequestBody())
        }.map { response ->
            record.copy(
                id = (response.objectId ?: "").hashCode().toLong(),
                objectId = response.objectId,
                createdAt = response.createdAt,
                updatedAt = response.updatedAt
            )
        }
    }

    suspend fun updateCheckinRecord(objectId: String, record: CheckinRecord): Result<CheckinRecord> {
        return safeApiCall {
            val body = CheckinRecordBmob.fromCheckinRecord(record).toMap()
            apiService.updateObject("CheckinRecord", objectId, body.toJsonRequestBody())
        }.map { record }
    }

    suspend fun getLeaderboard(limit: Int = 10): Result<List<CheckinRecord>> {
        return safeApiCall {
            apiService.getObjects("CheckinRecord", order = "-totalPoints", limit = limit)
        }.map { response ->
            if (response.results != null) {
                val typeToken = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), typeToken)
                list.map { CheckinRecordBmob.fromMap(it).toCheckinRecord() }
            } else {
                emptyList()
            }
        }
    }

    suspend fun createMemorialStar(star: MemorialStarBmob): Result<MemorialStarBmob> {
        return safeApiCall {
            val body = star.toMap()
            apiService.createObject("MemorialStar", body.toJsonRequestBody())
        }.map { response ->
            star.apply {
                objectId = response.objectId
                createdAt = response.createdAt
                updatedAt = response.updatedAt
            }
        }
    }

    suspend fun getApprovedMemorialStars(): Result<List<MemorialStarBmob>> {
        return safeApiCall {
            val where = """{"status":"approved"}"""
            apiService.getObjects("MemorialStar", where = where, order = "-createdAt")
        }.map { response ->
            if (response.results != null) {
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), type)
                list.map { MemorialStarBmob.fromMap(it) }
            } else {
                emptyList()
            }
        }
    }

    suspend fun getMemorialStarById(objectId: String): Result<MemorialStarBmob?> {
        return safeApiCall {
            apiService.getObject("MemorialStar", objectId)
        }.map { response ->
            if (response.isNotEmpty() && response["objectId"] != null) {
                MemorialStarBmob.fromMap(response)
            } else {
                null
            }
        }
    }

    suspend fun createComment(comment: Comment): Result<Comment> {
        return safeApiCall {
            val bmobComment = CommentBmob.fromComment(comment).toMap()
            apiService.createObject("Comment", bmobComment.toJsonRequestBody())
        }.map { response ->
            comment.copy(
                id = (response.objectId ?: "").hashCode().toLong(),
                objectId = response.objectId,
                createdAt = response.createdAt,
                updatedAt = response.updatedAt
            )
        }
    }

    suspend fun getCommentsByPostId(postId: String): Result<List<Comment>> {
        return safeApiCall {
            val where = """{"postId":"$postId"}"""
            apiService.getObjects("Comment", where = where, order = "createdAt")
        }.map { response ->
            if (response.results != null) {
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val list: List<Map<String, Any>> = gson.fromJson(gson.toJson(response.results), type)
                list.map { CommentBmob.fromMap(it).toComment() }
            } else {
                emptyList()
            }
        }
    }

    suspend fun deleteComment(objectId: String): Result<Unit> {
        return safeApiCall {
            apiService.deleteObject("Comment", objectId)
        }.map { }
    }

    suspend fun incrementPostCommentCount(objectId: String): Result<Unit> {
        return safeApiCall {
            val body = mapOf<String, Any?>("commentCount" to mapOf("__op" to "Increment", "amount" to 1))
            apiService.updateObject("Post", objectId, body.toJsonRequestBody())
        }.map { }
    }

    suspend fun decrementPostCommentCount(objectId: String): Result<Unit> {
        return safeApiCall {
            val body = mapOf<String, Any?>("commentCount" to mapOf("__op" to "Increment", "amount" to -1))
            apiService.updateObject("Post", objectId, body.toJsonRequestBody())
        }.map { }
    }
}
