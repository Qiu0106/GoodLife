package com.example.smartelderlycare_app.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class StarFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint()
    private val linePaint = Paint()
    private val particleCount = 100
    private val particleSize = 2f
    private val lineDistance = 150f
    private val lineWidth = 0.5f
    private val particleSpeed = 0.5f
    private var touchPoint: PointF? = null
    private val attractionRadius = 200f
    private val attractionForce = 0.8f

    init {
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.alpha = 150

        linePaint.color = Color.WHITE
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = lineWidth
        linePaint.alpha = 100

        // 初始化粒子
        for (i in 0 until particleCount) {
            particles.add(Particle(
                Random.nextFloat() * 1000,
                Random.nextFloat() * 1000,
                Random.nextFloat() * 2 - 1,
                Random.nextFloat() * 2 - 1
            ))
        }

        // 启动动画
        postInvalidateOnAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 重新初始化粒子位置
        particles.clear()
        for (i in 0 until particleCount) {
            particles.add(Particle(
                Random.nextFloat() * w,
                Random.nextFloat() * h,
                Random.nextFloat() * 2 - 1,
                Random.nextFloat() * 2 - 1
            ))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制背景
        canvas.drawColor(Color.BLACK)

        // 绘制粒子间的连线
        for (i in 0 until particles.size) {
            for (j in i + 1 until particles.size) {
                val p1 = particles[i]
                val p2 = particles[j]
                val distance = sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))
                if (distance < lineDistance) {
                    // 根据距离调整线条透明度
                    linePaint.alpha = ((1 - distance / lineDistance) * 100).toInt()
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, linePaint)
                }
            }
        }

        // 处理触摸交互
        touchPoint?.let { touch ->
            for (particle in particles) {
                val distance = sqrt((particle.x - touch.x).pow(2) + (particle.y - touch.y).pow(2))
                if (distance < attractionRadius) {
                    // 计算吸引力方向
                    val dx = touch.x - particle.x
                    val dy = touch.y - particle.y
                    val force = (1 - distance / attractionRadius) * attractionForce
                    particle.vx += dx / distance * force
                    particle.vy += dy / distance * force
                }
            }
        }

        // 更新和绘制粒子
        for (particle in particles) {
            // 更新粒子位置
            particle.x += particle.vx * particleSpeed
            particle.y += particle.vy * particleSpeed

            // 边界检测
            if (particle.x < 0 || particle.x > width) {
                particle.vx *= -1
            }
            if (particle.y < 0 || particle.y > height) {
                particle.vy *= -1
            }

            // 绘制粒子
            canvas.drawCircle(particle.x, particle.y, particleSize, paint)
        }

        // 继续动画
        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                touchPoint = PointF(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                touchPoint = null
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float
    )
}
