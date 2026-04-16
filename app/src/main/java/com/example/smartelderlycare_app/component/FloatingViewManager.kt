package com.example.smartelderlycare_app.component

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.smartelderlycare_app.R

class FloatingViewManager private constructor() : Application.ActivityLifecycleCallbacks {

    private var application: Application? = null
    private var floatingButton: FloatingVoiceButton? = null
    private var isInitialized = false

    private var posX = 0
    private var posY = 0

    private val excludedActivities = setOf(
        "ImagePreviewActivity",
        "StarrySkyActivity",
        "StarFieldActivity",
        "FullScreenGalleryActivity",
        "LoginActivity"
    )

    companion object {
        @Volatile
        private var instance: FloatingViewManager? = null
        private const val TAG = "FloatingViewManager"

        fun getInstance(): FloatingViewManager {
            return instance ?: synchronized(this) {
                instance ?: FloatingViewManager().also { instance = it }
            }
        }
    }

    fun init(app: Application) {
        if (isInitialized) return
        application = app
        app.registerActivityLifecycleCallbacks(this)
        isInitialized = true
        Log.d(TAG, "FloatingViewManager initialized")
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        if (shouldShowFloatingView(activity)) {
            showFloatingView(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        savePosition()
        floatingButton?.let { button ->
            if (button.parent != null) {
                val container = button.parent as? ViewGroup
                container?.removeView(button)
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    private fun shouldShowFloatingView(activity: Activity): Boolean {
        val activityName = activity.javaClass.simpleName
        val shouldShow = activityName !in excludedActivities
        Log.d(TAG, "shouldShowFloatingView($activityName) = $shouldShow")
        return shouldShow
    }

    private fun showFloatingView(activity: Activity) {
        val context = activity.baseContext

        if (floatingButton == null) {
            floatingButton = FloatingVoiceButton(context).apply {
                setOnVoiceClickListener(object : FloatingVoiceButton.OnVoiceClickListener {
                    override fun onVoiceClick() {
                        animateAndOpenChat(activity)
                    }
                })
            }
            Log.d(TAG, "Created new FloatingVoiceButton")
        }

        floatingButton?.let { button ->
            val container = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

            if (container == null) {
                Log.e(TAG, "Container is null!")
                return
            }

            val size = dpToPx(context, 80)
            val params = FrameLayout.LayoutParams(size, size)

            button.layoutParams = params

            if (container.indexOfChild(button) < 0) {
                container.addView(button)
                Log.d(TAG, "Added floating button to container")
            }

            container.post {
                val containerWidth = container.width
                val containerHeight = container.height

                Log.d(TAG, "Container size: ${containerWidth}x${containerHeight}")

                if (containerWidth == 0 || containerHeight == 0) {
                    Log.e(TAG, "Container has zero size, cannot position button")
                    return@post
                }

                if (posX == 0 && posY == 0) {
                    posX = containerWidth - dpToPx(context, 86)
                    posY = containerHeight / 3
                }

                button.x = posX.toFloat()
                button.y = posY.toFloat()

                Log.d(TAG, "Button positioned at: $posX, $posY")
            }
        }
    }

    private fun savePosition() {
        floatingButton?.let { button ->
            posX = button.x.toInt()
            posY = button.y.toInt()
            Log.d(TAG, "Saved position: $posX, $posY")
        }
    }

    private fun openChatActivity(activity: Activity) {
        val intent = Intent(activity, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
    }

    private fun animateAndOpenChat(activity: Activity) {
        floatingButton?.playClickAnimation {
            openChatActivity(activity)
        }
    }

    private fun dpToPx(context: android.content.Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
