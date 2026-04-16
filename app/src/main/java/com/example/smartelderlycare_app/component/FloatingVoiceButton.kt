package com.example.smartelderlycare_app.component

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.smartelderlycare_app.R

class FloatingVoiceButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnVoiceClickListener {
        fun onVoiceClick()
    }

    private var voiceClickListener: OnVoiceClickListener? = null

    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 233, 30, 99)
        style = Paint.Style.FILL
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL)
    }

    private var buttonRadius = 0f
    private var centerX = 0f
    private var centerY = 0f

    private var touchStartX = 0f
    private var touchStartY = 0f
    private var touchStartTime = 0L
    private var isDragging = false

    private val touchSlop = 15f
    private val clickTimeThreshold = 200L
    private val clickDistanceThreshold = 30f

    private val edgeMargin = dpToPx(16f).toInt()
    private var currentX = 0f
    private var targetX = 0f
    private var isAnimating = false
    private var animator: ValueAnimator? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        buttonRadius = minOf(w, h) / 2f - dpToPx(4f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(centerX, centerY, buttonRadius, buttonPaint)

        drawAiText(canvas)
    }

    private fun drawAiText(canvas: Canvas) {
        val text = "AI"
        iconPaint.textSize = buttonRadius * 0.7f
        iconPaint.textAlign = Paint.Align.CENTER

        val textY = centerY - (iconPaint.descent() + iconPaint.ascent()) / 2

        canvas.drawText(text, centerX, textY, iconPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.rawX
                touchStartY = event.rawY
                touchStartTime = System.currentTimeMillis()
                isDragging = false
                animator?.cancel()
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - touchStartX
                val deltaY = event.rawY - touchStartY
                val distance = Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble())

                if (distance > touchSlop) {
                    isDragging = true
                }

                if (isDragging) {
                    val newX = event.rawX - width / 2
                    val newY = event.rawY - height / 2

                    currentX = newX.coerceIn(edgeMargin.toFloat(), (parent as View).width - width - edgeMargin.toFloat())

                    val parentHeight = (parent as View).height
                    val newCenterY = newY.coerceIn(edgeMargin.toFloat(), parentHeight - height - edgeMargin.toFloat())

                    x = currentX
                    y = newCenterY
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                parent.requestDisallowInterceptTouchEvent(false)

                val deltaX = event.rawX - touchStartX
                val deltaY = event.rawY - touchStartY
                val distance = Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble())
                val timeDelta = System.currentTimeMillis() - touchStartTime

                if (!isDragging && distance < clickDistanceThreshold && timeDelta < clickTimeThreshold) {
                    performClick()
                    voiceClickListener?.onVoiceClick()
                } else {
                    snapToEdge()
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                snapToEdge()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun snapToEdge() {
        val screenWidth = (parent as View).width
        val currentCenterX = x + width / 2

        val distanceToLeft = currentCenterX
        val distanceToRight = screenWidth - currentCenterX

        targetX = if (distanceToLeft < distanceToRight) {
            edgeMargin.toFloat()
        } else {
            (screenWidth - width - edgeMargin).toFloat()
        }

        animateToX(targetX)
    }

    private fun animateToX(targetX: Float) {
        animator?.cancel()

        animator = ValueAnimator.ofFloat(x, targetX).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                x = animation.animatedValue as Float
            }
            start()
        }

        this.targetX = targetX
    }

    fun playClickAnimation(onAnimationEnd: (() -> Unit)? = null) {
        isAnimating = true
        val normalScale = 1.0f
        val shrinkScale = 0.7f

        ValueAnimator.ofFloat(normalScale, shrinkScale, normalScale).apply {
            duration = 400
            interpolator = DecelerateInterpolator(2f)
            addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                scaleX = scale
                scaleY = scale
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    isAnimating = false
                    onAnimationEnd?.invoke()
                }
            })
            start()
        }
    }

    fun setOnVoiceClickListener(listener: OnVoiceClickListener) {
        this.voiceClickListener = listener
    }

    fun setOnVoiceClickListener(listener: (() -> Unit)?) {
        this.voiceClickListener = object : OnVoiceClickListener {
            override fun onVoiceClick() {
                listener?.invoke()
            }
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
