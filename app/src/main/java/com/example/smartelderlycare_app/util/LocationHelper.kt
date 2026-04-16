package com.example.smartelderlycare_app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LocationHelper(private val context: Context) {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    interface LocationCallback {
        fun onSuccess(location: Location)
        fun onFailure(error: String)
    }

    fun getCurrentLocation(callback: LocationCallback) {
        if (!hasLocationPermission()) {
            callback.onFailure("没有定位权限")
            return
        }

        if (!isLocationEnabled()) {
            callback.onFailure("定位服务未开启")
            return
        }

        try {
            val location = getLastKnownLocation()
            if (location != null && isLocationValid(location)) {
                callback.onSuccess(location)
                return
            }

            requestLocationUpdate(callback)
        } catch (e: SecurityException) {
            callback.onFailure("定位权限被拒绝: ${e.message}")
        } catch (e: Exception) {
            callback.onFailure("定位失败: ${e.message}")
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun getLastKnownLocation(): Location? {
        return try {
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            when {
                gpsLocation != null && networkLocation != null -> {
                    if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
                }
                gpsLocation != null -> gpsLocation
                networkLocation != null -> networkLocation
                else -> null
            }
        } catch (e: SecurityException) {
            null
        }
    }

    private fun isLocationValid(location: Location): Boolean {
        val fiveMinutes = 5 * 60 * 1000L
        return System.currentTimeMillis() - location.time < fiveMinutes
    }

    private fun requestLocationUpdate(callback: LocationCallback) {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                callback.onSuccess(location)
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        try {
            val provider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                    LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                    LocationManager.NETWORK_PROVIDER
                else -> {
                    callback.onFailure("没有可用的定位provider")
                    return
                }
            }

            locationManager.requestSingleUpdate(
                provider,
                listener,
                Looper.getMainLooper()
            )

        } catch (e: SecurityException) {
            callback.onFailure("定位权限被拒绝")
        }
    }

    companion object {
        fun convertToLocationId(longitude: Double, latitude: Double): String {
            return "${longitude.toInt()},${latitude.toInt()}"
        }
    }
}
