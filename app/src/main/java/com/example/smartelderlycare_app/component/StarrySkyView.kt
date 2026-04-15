package com.example.smartelderlycare_app.component

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import com.example.smartelderlycare_app.data.model.MemorialData
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class StarrySkyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ==================== 背景星星数据类（带物理属性）====================
    data class BackgroundStar(
        var x: Float,              // 当前位置 X
        var y: Float,              // 当前位置 Y
        var homeX: Float,         // 当前目标位置 X（会被手指临时改变）
        var homeY: Float,         // 当前目标位置 Y（会被手指临时改变）
        val originalHomeX: Float, // 原始（家）位置 X
        val originalHomeY: Float,  // 原始（家）位置 Y
        val radius: Float,
        var alpha: Float = 1f,
        var twinkleOffset: Float = Random.nextFloat(),
        var twinkleSpeed: Float = 0.5f + Random.nextFloat() * 1.5f
    )

    // ==================== 主星星数据类 ====================
    inner class Star(
        val id: String,
        val name: String,
        var x: Float,
        var y: Float,
        val baseRadius: Float,
        val glowRadius: Float,
        val starColor: Int,
        val starSize: Float,
        val glowIntensity: Float,
        var isSelected: Boolean = false,
        var clickScale: Float = 1.0f  // 点击缩放反馈
    ) {
        val radiusPx: Float
            get() = baseRadius * resources.displayMetrics.density * starSize * clickScale

        val glowRadiusPx: Float
            get() = glowRadius * resources.displayMetrics.density * glowIntensity * clickScale

        val touchRadiusPx: Float
            get() = radiusPx * 1.5f
    }

    // ==================== 物理参数（可调整）====================
    // Lerp 插值参数（控制归位平滑度）
    private val lerpFactor = 0.02f           // 越小越缓慢平滑，绝不会回弹
    private val fingerAttractionRadius = 400f // 手指吸引范围(px)

    // ==================== 画笔 ====================
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 42f
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 0f, 0f, Color.BLACK)
    }

    private val nameBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#99000000")
    }

    // ==================== 矩阵和平移 ====================
    private var translateX = 0f
    private var translateY = 0f

    // ==================== 手势检测 ====================
    private val gestureDetector = GestureDetector(context, GestureListener())

    // ==================== 触摸状态 ====================
    private var isFingerDown = false
    private var fingerX = 0f
    private var fingerY = 0f

    // ==================== 星星集合 ====================
    private val stars = mutableListOf<Star>()
    private val backgroundStars = mutableListOf<BackgroundStar>()

    // ==================== 滑动状态 ====================
    private var hasScrolled = false
    var onFirstUserScrollListener: (() -> Unit)? = null

    // ==================== 动画 ====================
    private var breathAnimator: ValueAnimator? = null
    private var twinkleAnimator: ValueAnimator? = null
    private val breathDuration = 2500L
    private val twinkleDuration = 3000L

    // ==================== 点击回调 ====================
    var onStarClickListener: ((Star) -> Unit)? = null

    // ==================== 柔和星空色板 ====================
    private val starColorPalette = intArrayOf(
        Color.parseColor("#FFE4B5"),
        Color.parseColor("#E6E6FA"),
        Color.parseColor("#87CEEB"),
        Color.parseColor("#D8BFD8"),
        Color.parseColor("#FFB6C1"),
        Color.parseColor("#98FB98"),
        Color.parseColor("#F0E68C"),
        Color.parseColor("#ADD8E6"),
        Color.parseColor("#FFDAB9"),
        Color.parseColor("#B0E0E6")
    )

    private fun getColorForStar(starId: String): Int {
        val index = kotlin.math.abs(starId.hashCode()) % starColorPalette.size
        return starColorPalette[index]
    }

    init {
        initBreathAnimator()
        initTwinkleAnimator()
    }

    private fun initBreathAnimator() {
        breathAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = breathDuration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            addUpdateListener { invalidate() }
        }
        breathAnimator?.start()
    }

    private fun initTwinkleAnimator() {
        twinkleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = twinkleDuration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { invalidate() }
        }
        twinkleAnimator?.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        translateX = w / 2f
        translateY = h / 2f
        if (backgroundStars.isEmpty()) {
            generateBackgroundStars()
        }
    }

    private fun generateBackgroundStars() {
        backgroundStars.clear()
        val count = 100 + Random.nextInt(51)

        for (i in 0 until count) {
            val radius = 1f + Random.nextFloat() * 2f
            val x = Random.nextFloat() * width
            val y = Random.nextFloat() * height

            backgroundStars.add(BackgroundStar(
                x = x,
                y = y,
                homeX = x,
                homeY = y,
                originalHomeX = x,
                originalHomeY = y,
                radius = radius,
                alpha = 0.3f + Random.nextFloat() * 0.7f,
                twinkleOffset = Random.nextFloat(),
                twinkleSpeed = 0.5f + Random.nextFloat() * 1.5f
            ))
        }
    }

    /**
     * 生成单颗星星的安全坐标（严格防重叠算法）
     *
     * @param baseRadius 星星基础半径（dp）
     * @param padding 屏幕边缘安全边距（dp）
     * @param name 逝者姓名（用于计算文字占位空间）
     * @param attempts 最大尝试次数，默认100次
     * @return Pair(x, y) 安全的屏幕坐标
     */
    private fun generateStarPosition(
        baseRadius: Float,
        padding: Float,
        name: String,
        attempts: Int = 100
    ): Pair<Float, Float> {
        val density = resources.displayMetrics.density

        // ========== 1. 定义安全间距常量 ==========
        // 安全距离系数：两星星 touchRadius 之和乘以此系数
        val MIN_DISTANCE_MULTIPLIER = 1.8f
        // 额外文字保护间距（dp转px）
        val EXTRA_PADDING_FOR_TEXT = 120f * density

        // ========== 2. 计算新星星的触摸半径 ==========
        val newStarTouchRadius = baseRadius * density * 1.5f  // touchRadius = radius * 1.5

        // ========== 3. 计算文字区域占位 ==========
        // 文字宽度 + 两边 padding
        val textWidth = textPaint.measureText(name)
        val nameBlockWidth = textWidth + 64f * density
        // 文字区域高度（包含上下 padding）
        val nameBlockHeight = 80f * density
        // 名字中心在星星下方多远的位置
        val nameYOffset = baseRadius * density + 50f * density

        // ========== 4. 计算屏幕边界（严格防溢出） ==========
        // X轴边界：左右各留 safeMargin + 文字宽度的一半
        val safeMarginX = padding + nameBlockWidth / 2f
        val minX = safeMarginX
        val maxX = width - safeMarginX

        // Y轴边界：上下留 safeMargin，底部额外留出文字区域
        val safeMarginY = padding + nameBlockHeight
        val minY = safeMarginY
        val maxY = height - safeMarginY

        // 如果可用范围无效，直接使用屏幕中心
        if (maxX <= minX || maxY <= minY) {
            return Pair(width / 2f, height / 2f)
        }

        // ========== 5. 严格 While 循环防重叠检测 ==========
        var attemptCount = 0
        while (attemptCount < attempts) {
            attemptCount++

            // 【Step A】生成随机候选坐标（在严格边界内）
            val candidateX = minX + Random.nextFloat() * (maxX - minX)
            val candidateY = minY + Random.nextFloat() * (maxY - minY)

            // 【Step B】严格遍历比对所有已存在的星星
            var hasOverlap = false

            for (existingStar in stars) {
                // 计算新候选点到已存在星星中心的距离
                val dx = existingStar.x - candidateX
                val dy = existingStar.y - candidateY
                val distance = sqrt(dx * dx + dy * dy)

                // 【核心公式】最小允许距离
                // = (两星星 touchRadius 之和) * 系数 + 额外文字保护间距
                val minAllowedDistance = (
                    existingStar.touchRadiusPx + newStarTouchRadius
                ) * MIN_DISTANCE_MULTIPLIER + EXTRA_PADDING_FOR_TEXT

                // 检测星星主体是否重叠
                if (distance < minAllowedDistance) {
                    hasOverlap = true
                    break  // 跳出 for，继续 while 循环重新生成
                }

                // 【额外检测】新星星的文字区域是否与已存在星星重叠
                // 计算新星星名字区域的中心点（相对于星星中心）
                val newNameCenterX = candidateX
                val newNameCenterY = candidateY + nameYOffset

                // 已存在星星的名字区域中心点
                val existingNameCenterX = existingStar.x
                val existingNameCenterY = existingStar.y + nameYOffset

                // 两名字区域中心之间的距离
                val nameDx = existingNameCenterX - newNameCenterX
                val nameDy = existingNameCenterY - newNameCenterY
                val nameDistance = sqrt(nameDx * nameDx + nameDy * nameDy)

                // 名字区域的安全距离 = 两星星 touchRadius + 名字宽度一半 + 额外间距
                val nameSafeDistance = (
                    existingStar.touchRadiusPx + newStarTouchRadius +
                    nameBlockWidth / 2f + EXTRA_PADDING_FOR_TEXT
                )

                if (nameDistance < nameSafeDistance) {
                    hasOverlap = true
                    break
                }

                // 【边界溢出手游检测】名字区域四角是否进入星星范围
                // 名字区域是矩形，判断其四角到星星的距离
                val halfNameW = nameBlockWidth / 2f
                val halfNameH = nameBlockHeight / 2f
                val corners = arrayOf(
                    Pair(newNameCenterX - halfNameW, newNameCenterY - halfNameH),
                    Pair(newNameCenterX + halfNameW, newNameCenterY - halfNameH),
                    Pair(newNameCenterX - halfNameW, newNameCenterY + halfNameH),
                    Pair(newNameCenterX + halfNameW, newNameCenterY + halfNameH)
                )
                for ((cx, cy) in corners) {
                    val cornerDx = existingStar.x - cx
                    val cornerDy = existingStar.y - cy
                    val cornerDist = sqrt(cornerDx * cornerDx + cornerDy * cornerDy)
                    if (cornerDist < existingStar.touchRadiusPx + 20f * density) {
                        hasOverlap = true
                        break
                    }
                }
                if (hasOverlap) break
            }

            // 【Step C】没有重叠，安全加入
            if (!hasOverlap) {
                return Pair(candidateX, candidateY)
            }
        }

        // ========== 6. 兜底策略：100次都失败，使用九宫格强制分散 ==========
        val index = stars.size
        val col = index % 3          // 0, 1, 2 列
        val row = index / 3           // 0, 1, 2... 行
        val cellWidth = width / 3f
        val cellHeight = height / 4f

        // 落在每个格子中心附近
        val fallbackX = (col + 0.5f) * cellWidth
        val fallbackY = (row + 0.5f) * cellHeight

        return Pair(fallbackX, fallbackY)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.parseColor("#0D0D1A"))

        // 更新并绘制背景星星（带物理效果）
        updateAndDrawBackgroundStars(canvas)

        canvas.save()
        canvas.translate(translateX, translateY)

        for (star in stars) {
            drawStar(canvas, star)
        }

        canvas.restore()
    }

    private fun updateAndDrawBackgroundStars(canvas: Canvas) {
        val twinkleProgress = twinkleAnimator?.animatedValue as? Float ?: 0f

        for (bgStar in backgroundStars) {
            // ========== 手指吸引：将 home 临时移向手指 ==========
            if (isFingerDown) {
                val dx = fingerX - bgStar.homeX
                val dy = fingerY - bgStar.homeY
                val distanceToFinger = sqrt(dx * dx + dy * dy)

                if (distanceToFinger < fingerAttractionRadius) {
                    val targetX = fingerX - (fingerX - bgStar.homeX) * 0.15f
                    val targetY = fingerY - (fingerY - bgStar.homeY) * 0.15f
                    bgStar.homeX = lerp(bgStar.homeX, targetX, 0.08f)
                    bgStar.homeY = lerp(bgStar.homeY, targetY, 0.08f)
                } else {
                    bgStar.homeX = lerp(bgStar.homeX, bgStar.originalHomeX, 0.02f)
                    bgStar.homeY = lerp(bgStar.homeY, bgStar.originalHomeY, 0.02f)
                }
            } else {
                // 手指抬起：Lerp 缓慢平滑归位，绝不会回弹
                bgStar.homeX = lerp(bgStar.homeX, bgStar.originalHomeX, lerpFactor)
                bgStar.homeY = lerp(bgStar.homeY, bgStar.originalHomeY, lerpFactor)
            }

            // ========== Lerp 插值更新位置 ==========
            bgStar.x = lerp(bgStar.x, bgStar.homeX, lerpFactor)
            bgStar.y = lerp(bgStar.y, bgStar.homeY, lerpFactor)

            // ========== 绘制 ==========
            val phase = (twinkleProgress + bgStar.twinkleOffset) % 1f
            val alphaMultiplier = 0.3f + 0.7f * (0.5f + 0.5f * sin(phase * Math.PI.toFloat() * 2 * bgStar.twinkleSpeed).toFloat())

            backgroundPaint.alpha = (bgStar.alpha * alphaMultiplier * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(bgStar.x, bgStar.y, bgStar.radius, backgroundPaint)
        }
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }

    private fun drawStar(canvas: Canvas, star: Star) {
        canvas.save()
        canvas.translate(star.x, star.y)

        val density = resources.displayMetrics.density
        val breathProgress = breathAnimator?.animatedValue as? Float ?: 0f
        val glowPulse = 0.8f + 0.4f * sin(breathProgress * Math.PI.toFloat() * 2).toFloat()

        val currentGlowRadius = star.glowRadiusPx * glowPulse
        val currentStarRadius = star.radiusPx

        // 第一层：外层大光晕（极低Alpha）
        val outerGlowAlpha = (0.15f * star.glowIntensity).coerceAtMost(0.25f)
        val outerGlow = RadialGradient(
            0f, 0f, currentGlowRadius * 1.5f,
            intArrayOf(
                adjustAlpha(star.starColor, outerGlowAlpha * 0.5f),
                adjustAlpha(star.starColor, outerGlowAlpha * 0.3f),
                adjustAlpha(star.starColor, outerGlowAlpha * 0.1f),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.3f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        glowPaint.shader = outerGlow
        canvas.drawCircle(0f, 0f, currentGlowRadius * 1.5f, glowPaint)

        // 第二层：中层光晕
        val midGlowAlpha = (0.35f * star.glowIntensity).coerceAtMost(0.5f)
        val midGlow = RadialGradient(
            0f, 0f, currentGlowRadius,
            intArrayOf(
                adjustAlpha(star.starColor, midGlowAlpha),
                adjustAlpha(star.starColor, midGlowAlpha * 0.5f),
                adjustAlpha(star.starColor, midGlowAlpha * 0.15f),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.4f, 0.8f, 1f),
            Shader.TileMode.CLAMP
        )
        glowPaint.shader = midGlow
        canvas.drawCircle(0f, 0f, currentGlowRadius, glowPaint)

        // 第三层：中心星星实体
        val coreGradient = RadialGradient(
            0f, 0f, currentStarRadius * 1.2f,
            intArrayOf(
                Color.WHITE,
                Color.parseColor("#FFE8E8"),
                adjustAlpha(star.starColor, 0.6f),
                adjustAlpha(star.starColor, 0.2f),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.3f, 0.6f, 0.85f, 1f),
            Shader.TileMode.CLAMP
        )
        starPaint.shader = coreGradient
        canvas.drawCircle(0f, 0f, currentStarRadius, starPaint)

        // 第四层：名字胶囊型底托 + 文字
        val nameText = star.name
        val textWidth = textPaint.measureText(nameText)
        val textHeight = textPaint.textSize

        // 胶囊型底托参数
        val pillPaddingH = 32f * density  // 水平方向 padding
        val pillPaddingV = 12f * density   // 垂直方向 padding
        val pillCornerRadius = (textHeight + pillPaddingV * 2) / 2f  // 圆角 = 高度的一半，形成完美胶囊

        // 底托尺寸
        val pillWidth = textWidth + pillPaddingH * 2
        val pillHeight = textHeight + pillPaddingV * 2
        val pillLeft = -pillWidth / 2f
        val pillTop = currentStarRadius + 20f
        val pillBottom = pillTop + pillHeight
        val pillRight = pillWidth / 2f

        // 绘制胶囊型半透明深色底托
        canvas.drawRoundRect(
            pillLeft, pillTop,
            pillRight, pillBottom,
            pillCornerRadius, pillCornerRadius,
            nameBgPaint
        )

        // 绘制白色文字（居中）
        val textY = pillTop + pillPaddingV + textHeight * 0.85f
        canvas.drawText(nameText, 0f, textY, textPaint)

        // 选中状态
        if (star.isSelected) {
            starPaint.shader = null
            starPaint.color = Color.parseColor("#FFD700")
            starPaint.alpha = 200
            starPaint.style = Paint.Style.STROKE
            starPaint.strokeWidth = 4f
            canvas.drawCircle(0f, 0f, currentStarRadius * 1.4f, starPaint)
            starPaint.style = Paint.Style.FILL
            starPaint.color = Color.WHITE
        }

        canvas.restore()
    }

    private fun adjustAlpha(color: Int, alpha: Float): Int {
        val alphaInt = (alpha * 255).toInt().coerceIn(0, 255)
        return Color.argb(alphaInt, Color.red(color), Color.green(color), Color.blue(color))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isFingerDown = true
                fingerX = event.x
                fingerY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                fingerX = event.x
                fingerY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isFingerDown = false
            }
        }
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // 首次滑动触发回调
            if (!hasScrolled) {
                hasScrolled = true
                onFirstUserScrollListener?.invoke()
            }
            translateX -= distanceX
            translateY -= distanceY
            invalidate()
            return true
        }

        override fun onDown(e: MotionEvent): Boolean = true

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val worldX = e.x - translateX
            val worldY = e.y - translateY

            for (star in stars) {
                val distance = sqrt((star.x - worldX).let { it * it } + (star.y - worldY).let { it * it })
                if (distance <= star.touchRadiusPx) {
                    // 触发点击缩放动画
                    playClickAnimation(star)
                    onStarClickListener?.invoke(star)
                    return true
                }
            }
            return false
        }
    }

    private fun playClickAnimation(star: Star) {
        val animator = ValueAnimator.ofFloat(1f, 0.8f, 1.05f, 1f).apply {
            duration = 250
            interpolator = OvershootInterpolator(2f)
            addUpdateListener { animation ->
                star.clickScale = animation.animatedValue as Float
                invalidate()
            }
        }
        animator.start()
    }

    fun pauseAnimation() {
        breathAnimator?.pause()
        twinkleAnimator?.pause()
    }

    fun resumeAnimation() {
        breathAnimator?.resume()
        twinkleAnimator?.resume()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        breathAnimator?.cancel()
        twinkleAnimator?.cancel()
        breathAnimator = null
        twinkleAnimator = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (breathAnimator == null) initBreathAnimator()
        if (twinkleAnimator == null) initTwinkleAnimator()
    }

    fun getStars(): List<Star> = stars.toList()

    fun setStarSelected(starId: String, selected: Boolean) {
        stars.find { it.id == starId }?.isSelected = selected
        invalidate()
    }

    fun setMemorialData(memorialDataList: List<MemorialData>) {
        if (width == 0 || height == 0) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    layoutStarsWithData(memorialDataList)
                }
            })
        } else {
            layoutStarsWithData(memorialDataList)
        }
    }

    private fun layoutStarsWithData(memorialDataList: List<MemorialData>) {
        stars.clear()

        val padding = 80f * resources.displayMetrics.density
        val minRadius = 28f
        val maxRadius = 48f
        val minGlow = 3.5f
        val maxGlow = 5.5f

        memorialDataList.forEach { data ->
            val baseRadius = minRadius + Random.nextFloat() * (maxRadius - minRadius)
            val glowIntensity = 0.6f + Random.nextFloat() * 0.8f

            val (x, y) = generateStarPosition(baseRadius, padding, data.name)

            stars.add(Star(
                id = data.id,
                name = data.name,
                x = x,
                y = y,
                baseRadius = baseRadius,
                glowRadius = baseRadius * (minGlow + Random.nextFloat() * (maxGlow - minGlow)),
                starColor = getColorForStar(data.id),
                starSize = 1f,
                glowIntensity = glowIntensity
            ))
        }

        generateBackgroundStars()
        invalidate()
    }
}