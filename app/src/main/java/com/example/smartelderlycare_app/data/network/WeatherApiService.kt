package com.example.smartelderlycare_app.data.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class WeatherApiService {

    companion object {
        private const val TAG = "WeatherApiService"
        private const val WEATHER_URL = "https://api.open-meteo.com/v1/forecast"

        private val client = OkHttpSingleton.client

        private val WMO_CODE_MAP = mapOf(
            0 to "☀️", 1 to "🌤️", 2 to "⛅", 3 to "☁️",
            45 to "🌫️", 48 to "🌫️",
            51 to "🌧️", 53 to "🌧️", 55 to "🌧️",
            61 to "🌧️", 63 to "🌧️", 65 to "🌧️",
            71 to "❄️", 73 to "❄️", 75 to "❄️",
            80 to "🌧️", 81 to "🌧️", 82 to "🌧️",
            95 to "⛈️", 96 to "⛈️", 99 to "⛈️"
        )

        fun getWeatherInfo(
            longitude: Double,
            latitude: Double,
            onSuccess: (WeatherInfo) -> Unit,
            onError: (String) -> Unit
        ) {
            Thread {
                try {
                    Log.d(TAG, "开始获取天气 - 经纬度: $longitude, $latitude")
                    getWeatherByCoordinates(longitude, latitude, onSuccess, onError)
                } catch (e: Exception) {
                    Log.e(TAG, "天气获取异常: ${e.message}")
                    onError("网络异常: ${e.message}")
                }
            }.start()
        }

        private fun getWeatherByCoordinates(
            longitude: Double,
            latitude: Double,
            onSuccess: (WeatherInfo) -> Unit,
            onError: (String) -> Unit
        ) {
            try {
                val url = "$WEATHER_URL?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m&timezone=auto"
                Log.d(TAG, "请求天气 URL: $url")

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d(TAG, "天气响应: $responseBody")

                if (responseBody == null) {
                    onError("响应体为空")
                    return
                }

                val json = JSONObject(responseBody)
                val current = json.optJSONObject("current")

                if (current == null) {
                    onError("无法解析天气数据")
                    return
                }

                val weatherCode = current.optInt("weather_code", -1)
                val temp = current.optString("temperature_2m", "--")
                val humidity = current.optString("relative_humidity_2m", "--")
                val windSpeed = current.optString("wind_speed_10m", "--")
                val windDir = current.optInt("wind_direction_10m", 0)

                val weatherInfo = WeatherInfo(
                    temp = temp,
                    feelsLike = "--",
                    text = getWeatherText(weatherCode),
                    icon = WMO_CODE_MAP[weatherCode] ?: "🌤️",
                    humidity = humidity,
                    windDir = getWindDirection(windDir),
                    updateTime = ""
                )

                Log.d(TAG, "天气信息解析成功: ${weatherInfo.text}, ${weatherInfo.temp}°C")
                onSuccess(weatherInfo)

            } catch (e: Exception) {
                Log.e(TAG, "获取天气异常: ${e.message}")
                onError("网络异常: ${e.message}")
            }
        }

        private fun getWeatherText(code: Int): String {
            return when (code) {
                0 -> "晴"
                1, 2, 3 -> "多云"
                45, 48 -> "雾"
                51, 53, 55 -> "毛毛雨"
                61, 63, 65 -> "小雨"
                71, 73, 75 -> "小雪"
                80, 81, 82 -> "阵雨"
                95, 96, 99 -> "雷阵雨"
                else -> "未知"
            }
        }

        private fun getWindDirection(degrees: Int): String {
            return when {
                degrees < 45 || degrees >= 315 -> "北"
                degrees < 135 -> "东"
                degrees < 225 -> "南"
                else -> "西"
            }
        }
    }

    data class WeatherInfo(
        val temp: String,
        val feelsLike: String,
        val text: String,
        val icon: String,
        val humidity: String,
        val windDir: String,
        val updateTime: String
    )
}
