package com.example.smartelderlycare_app.data.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object OkHttpSingleton {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}
