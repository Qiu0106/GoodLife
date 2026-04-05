package com.example.smartelderlycare_app.data.network

import com.example.smartelderlycare_app.data.model.AfterlifePlan
import com.example.smartelderlycare_app.data.model.ApiResponse
import com.example.smartelderlycare_app.data.model.LoginRequest
import com.example.smartelderlycare_app.data.model.Post
import com.example.smartelderlycare_app.data.model.RegisterRequest
import com.example.smartelderlycare_app.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * LeanCloud API接口定义
 * 定义所有与LeanCloud服务器通信的接口
 * 作为Bmob的备选方案
 */
interface LeanCloudApiService {

    // ==================== 用户相关接口 ====================

    /**
     * 用户注册
     */
    @POST("classes/User")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<User>>

    /**
     * 用户登录
     * LeanCloud使用登录接口: /login
     */
    @GET("login")
    suspend fun login(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<ApiResponse<User>>

    /**
     * 获取用户信息
     */
    @GET("classes/User/{objectId}")
    suspend fun getUserById(@Path("objectId") objectId: String): Response<ApiResponse<User>>

    /**
     * 更新用户信息
     */
    @PUT("classes/User/{objectId}")
    suspend fun updateUser(
        @Path("objectId") objectId: String,
        @Body user: User
    ): Response<ApiResponse<User>>

    // ==================== 身后事计划接口 ====================

    /**
     * 创建身后事计划
     */
    @POST("classes/AfterlifePlan")
    suspend fun createAfterlifePlan(@Body plan: AfterlifePlan): Response<ApiResponse<AfterlifePlan>>

    /**
     * 获取用户的所有身后事计划
     */
    @GET("classes/AfterlifePlan")
    suspend fun getAfterlifePlansByUserId(
        @Query("where") where: String
    ): Response<ApiResponse<List<AfterlifePlan>>>

    /**
     * 获取单个身后事计划
     */
    @GET("classes/AfterlifePlan/{objectId}")
    suspend fun getAfterlifePlanById(
        @Path("objectId") objectId: String
    ): Response<ApiResponse<AfterlifePlan>>

    /**
     * 更新身后事计划
     */
    @PUT("classes/AfterlifePlan/{objectId}")
    suspend fun updateAfterlifePlan(
        @Path("objectId") objectId: String,
        @Body plan: AfterlifePlan
    ): Response<ApiResponse<AfterlifePlan>>

    /**
     * 删除身后事计划
     */
    @DELETE("classes/AfterlifePlan/{objectId}")
    suspend fun deleteAfterlifePlan(@Path("objectId") objectId: String): Response<ApiResponse<Void>>

    // ==================== 帖子相关接口 ====================

    /**
     * 创建帖子
     */
    @POST("classes/Post")
    suspend fun createPost(@Body post: Post): Response<ApiResponse<Post>>

    /**
     * 获取所有帖子（分页）
     */
    @GET("classes/Post")
    suspend fun getPosts(
        @Query("order") order: String = "-createdAt",
        @Query("limit") limit: Int = 100
    ): Response<ApiResponse<List<Post>>>

    /**
     * 获取用户的帖子
     */
    @GET("classes/Post")
    suspend fun getPostsByUserId(
        @Query("where") where: String
    ): Response<ApiResponse<List<Post>>>

    /**
     * 获取单个帖子
     */
    @GET("classes/Post/{objectId}")
    suspend fun getPostById(@Path("objectId") objectId: String): Response<ApiResponse<Post>>

    /**
     * 更新帖子
     */
    @PUT("classes/Post/{objectId}")
    suspend fun updatePost(
        @Path("objectId") objectId: String,
        @Body post: Post
    ): Response<ApiResponse<Post>>

    /**
     * 删除帖子
     */
    @DELETE("classes/Post/{objectId}")
    suspend fun deletePost(@Path("objectId") objectId: String): Response<ApiResponse<Void>>
}
