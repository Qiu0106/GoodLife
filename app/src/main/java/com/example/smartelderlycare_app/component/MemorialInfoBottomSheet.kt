package com.example.smartelderlycare_app.component

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Path
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.MemorialData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.imageview.ShapeableImageView
import coil.load
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MemorialInfoBottomSheet : BottomSheetDialogFragment() {

    private var memorialData: MemorialData? = null
    private var currentFlowerCount: Int = 0
    private val repository = com.example.smartelderlycare_app.data.repository.BmobRepository()

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_NAME = "arg_name"
        private const val ARG_LIFE_YEARS = "arg_life_years"
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_STORY = "arg_story"
        private const val ARG_IMAGE_URL = "arg_image_url"
        private const val ARG_FLOWER_COUNT = "arg_flower_count"

        fun newInstance(data: MemorialData): MemorialInfoBottomSheet {
            return MemorialInfoBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, data.id)
                    putString(ARG_NAME, data.name)
                    putString(ARG_LIFE_YEARS, data.lifeYears)
                    putString(ARG_MESSAGE, data.message)
                    putString(ARG_STORY, data.story)
                    putString(ARG_IMAGE_URL, data.imageUrl)
                    putInt(ARG_FLOWER_COUNT, data.flowerCount)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MemorialBottomSheetDialog)

        arguments?.let { args ->
            memorialData = MemorialData(
                id = args.getString(ARG_ID, ""),
                name = args.getString(ARG_NAME, ""),
                lifeYears = args.getString(ARG_LIFE_YEARS, ""),
                message = args.getString(ARG_MESSAGE, ""),
                story = args.getString(ARG_STORY, ""),
                imageUrl = args.getString(ARG_IMAGE_URL),
                flowerCount = args.getInt(ARG_FLOWER_COUNT, 0)
            )
            currentFlowerCount = memorialData?.flowerCount ?: 0
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_memorial_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        memorialData?.let { data ->
            setupViews(view, data)
            setupFlowerAction(view, data)
        }

        view.findViewById<Button>(R.id.btnClose).setOnClickListener {
            dismiss()
        }
    }

    private fun setupViews(view: View, data: MemorialData) {
        val ivProfileImage = view.findViewById<ShapeableImageView>(R.id.ivProfileImage)

        if (!data.imageUrl.isNullOrEmpty()) {
            ivProfileImage.load(data.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_avatar_placeholder)
                error(R.drawable.bg_avatar_placeholder)
                transformations(CircleCropTransformation())
            }
        } else {
            ivProfileImage.setImageResource(R.drawable.bg_avatar_placeholder)
        }

        view.findViewById<TextView>(R.id.tvName).text = data.name
        view.findViewById<TextView>(R.id.tvLifeYears).text = data.lifeYears
        view.findViewById<TextView>(R.id.tvMessage).text = data.message
        view.findViewById<TextView>(R.id.tvStory).text = data.story
        view.findViewById<TextView>(R.id.tvFlowerCount).text = data.flowerCount.toString()
    }

    private fun setupFlowerAction(view: View, data: MemorialData) {
        val layoutFlowerAction = view.findViewById<LinearLayout>(R.id.layoutFlowerAction)
        val ivFlowerIcon = view.findViewById<ImageView>(R.id.ivFlowerIcon)
        val tvFlowerCount = view.findViewById<TextView>(R.id.tvFlowerCount)

        layoutFlowerAction.setOnClickListener {
            performFlowerAnimation(ivFlowerIcon, tvFlowerCount)
            sendFlowerToBmob(data.id, tvFlowerCount)
        }
    }

    private fun performFlowerAnimation(flowerIcon: ImageView, countTextView: TextView) {
        // 1. 花朵图标缩放动画
        val scaleXUp = ObjectAnimator.ofFloat(flowerIcon, "scaleX", 1f, 1.4f)
        val scaleYUp = ObjectAnimator.ofFloat(flowerIcon, "scaleY", 1f, 1.4f)
        val scaleXDown = ObjectAnimator.ofFloat(flowerIcon, "scaleX", 1.4f, 0.9f, 1f)
        val scaleYDown = ObjectAnimator.ofFloat(flowerIcon, "scaleY", 1.4f, 0.9f, 1f)

        val scaleUp = AnimatorSet().apply {
            playTogether(scaleXUp, scaleYUp)
            duration = 150
            interpolator = AccelerateInterpolator()
        }

        val scaleDown = AnimatorSet().apply {
            playTogether(scaleXDown, scaleYDown)
            duration = 200
            interpolator = OvershootInterpolator()
        }

        AnimatorSet().apply {
            playSequentially(scaleUp, scaleDown)
            start()
        }

        // 2. 数字弹跳动画
        val countAnimator = ValueAnimator.ofInt(currentFlowerCount, currentFlowerCount + 1).apply {
            duration = 300
            interpolator = OvershootInterpolator()
            addUpdateListener { animator ->
                countTextView.text = animator.animatedValue.toString()
            }
        }
        countAnimator.start()

        // 3. 数字放大闪动
        val countScaleX = ObjectAnimator.ofFloat(countTextView, "scaleX", 1f, 1.3f, 1f)
        val countScaleY = ObjectAnimator.ofFloat(countTextView, "scaleY", 1f, 1.3f, 1f)
        AnimatorSet().apply {
            playTogether(countScaleX, countScaleY)
            duration = 400
            start()
        }

        currentFlowerCount++
    }

    private fun sendFlowerToBmob(starId: String, countTextView: TextView) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.incrementMemorialFlowers(starId)
            }

            result.onSuccess {
                // Bmob 更新成功，数字已经在动画中更新
            }.onFailure { error ->
                // 如果 Bmob 更新失败，回退数字显示
                currentFlowerCount--
                countTextView.text = currentFlowerCount.toString()
                Toast.makeText(context, "献花失败，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        }
    }
}