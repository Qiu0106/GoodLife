package com.example.smartelderlycare_app.data.network

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface BmobApiService {

    companion object {
        const val BASE_URL = "https://api.bmobcloud.com/1/"
        const val APPLICATION_ID = "b9ab00843ca0d46f85a3d1f8c317164a"
        const val REST_API_KEY = "fb2a1df255a59958df971f18472cb2b4"
    }

    @GET("classes/{tableName}")
    suspend fun getObjects(
        @Path("tableName") tableName: String,
        @Query("where") where: String? = null,
        @Query("order") order: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("skip") skip: Int? = null,
        @Header("X-Bmob-Application-Id") appId: String = APPLICATION_ID,
        @Header("X-Bmob-REST-API-Key") apiKey: String = REST_API_KEY
    ): Response<BmobResponse<List<Any>>>

    @POST("classes/{tableName}")
    suspend fun createObject(
        @Path("tableName") tableName: String,
        @Body body: RequestBody,
        @Header("X-Bmob-Application-Id") appId: String = APPLICATION_ID,
        @Header("X-Bmob-REST-API-Key") apiKey: String = REST_API_KEY
    ): Response<BmobResponse<Map<String, Any>>>

    @PUT("classes/{tableName}/{objectId}")
    suspend fun updateObject(
        @Path("tableName") tableName: String,
        @Path("objectId") objectId: String,
        @Body body: RequestBody,
        @Header("X-Bmob-Application-Id") appId: String = APPLICATION_ID,
        @Header("X-Bmob-REST-API-Key") apiKey: String = REST_API_KEY
    ): Response<BmobResponse<Map<String, Any>>>

    @DELETE("classes/{tableName}/{objectId}")
    suspend fun deleteObject(
        @Path("tableName") tableName: String,
        @Path("objectId") objectId: String,
        @Header("X-Bmob-Application-Id") appId: String = APPLICATION_ID,
        @Header("X-Bmob-REST-API-Key") apiKey: String = REST_API_KEY
    ): Response<Unit>

    @GET("classes/{tableName}/{objectId}")
    suspend fun getObject(
        @Path("tableName") tableName: String,
        @Path("objectId") objectId: String,
        @Header("X-Bmob-Application-Id") appId: String = APPLICATION_ID,
        @Header("X-Bmob-REST-API-Key") apiKey: String = REST_API_KEY
    ): Response<BmobResponse<Map<String, Any>>>

    @POST("users")
    suspend fun registerUser(
        @Body body: RequestBody,
        @Header("X-Bmob-Application-Id") appId: String = APPLICATION_ID,
        @Header("X-Bmob-REST-API-Key") apiKey: String = REST_API_KEY
    ): Response<BmobResponse<Map<String, Any>>>

    @GET("login")
    suspend fun login(
        @Query("username") username: String,
        @Query("password") password: String,
        @Header("X-Bmob-Application-Id") appId: String = APPLICATION_ID,
        @Header("X-Bmob-REST-API-Key") apiKey: String = REST_API_KEY
    ): Response<BmobResponse<Map<String, Any>>>

    @GET("users/{objectId}")
    suspend fun getUser(
        @Path("objectId") objectId: String,
        @Header("X-Bmob-Application-Id") appId: String = APPLICATION_ID,
        @Header("X-Bmob-REST-API-Key") apiKey: String = REST_API_KEY
    ): Response<BmobResponse<Map<String, Any>>>

    @PUT("users/{objectId}")
    suspend fun updateUser(
        @Path("objectId") objectId: String,
        @Body body: RequestBody,
        @Header("X-Bmob-Application-Id") appId: String = APPLICATION_ID,
        @Header("X-Bmob-REST-API-Key") apiKey: String = REST_API_KEY,
        @Header("X-Bmob-Session-Token") sessionToken: String
    ): Response<BmobResponse<Map<String, Any>>>
}

data class BmobResponse<T>(
    val results: T? = null,
    val objectId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val sessionToken: String? = null,
    val code: Int? = null,
    val error: String? = null
)
