package com.example.smartelderlycare_app

import android.app.Application
import com.example.smartelderlycare_app.component.FloatingViewManager

class MyApplication : Application() {

    companion object {
        const val BMOB_APPLICATION_ID = "b9ab00843ca0d46f85a3d1f8c317164a"
        const val BMOB_REST_API_KEY = "fb2a1df255a59958df971f18472cb2b4"
        const val BMOB_API_ENDPOINT = "https://api.bmobcloud.com"
    }

    override fun onCreate() {
        super.onCreate()
        FloatingViewManager.getInstance().init(this)
    }
}
