package com.example.smartelderlycare_app

import android.app.Application
// import com.iflytek.cloud.SpeechConstant
// import com.iflytek.cloud.SpeechUtility

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 这里的 12345678 换成你在讯飞官网申请到的那个数字 ID
        // SpeechUtility.createUtility(this, SpeechConstant.APPID + "=f6311dbe ")
    }
}
