package com.example.smartelderlycare_app.utils

import android.view.View
import java.util.concurrent.TimeUnit

class DebounceUtils {
    companion object {
        private const val DEFAULT_DEBOUNCE_TIME = 1000L

        fun View.setDebounceClick(action: () -> Unit) {
            setDebounceClick(DEFAULT_DEBOUNCE_TIME, action)
        }

        fun View.setDebounceClick(debounceTime: Long, action: () -> Unit) {
            var lastClickTime = 0L
            setOnClickListener {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime >= debounceTime) {
                    lastClickTime = currentTime
                    action()
                }
            }
        }
    }
}
