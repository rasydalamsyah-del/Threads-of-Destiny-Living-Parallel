package com.valeriusgames.threadsofdestiny

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Partikel cahaya kecil yang berkelip dan melayang pelan di langit menu,
 * agar background terasa hidup.
 */
class SparkleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private class Particle(
        var x: Float,
        var y: Float,
        val radius: Float,
        val speed: Float,
        val period: Float,
        val phase: Float,
    )

    private val dp = resources.displayMetrics.density
    private val particles = mutableListOf<Particle>()
    private val random = Random(7)
    private var running = false
    private var lastFrame = 0L

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFF3CF.toInt() }
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF2D58C.toInt() }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        particles.clear()
        repeat(42) { particles += newParticle(w, h, anywhere = true) }
    }

    private fun newParticle(w: Int, h: Int, anywhere: Boolean): Particle {
        // Partikel hanya di area langit (sekitar 65% bagian atas layar)
        val skyBottom = h * 0.65f
        return Particle(
            x = random.nextFloat() * w,
            y = if (anywhere) random.nextFloat() * skyBottom else skyBottom,
            radius = (0.6f + random.nextFloat() * 1.4f) * dp,
            speed = (2f + random.nextFloat() * 6f) * dp,
            period = 1.8f + random.nextFloat() * 3.2f,
            phase = random.nextFloat() * 2f * PI.toFloat(),
        )
    }

    fun start() {
        if (running) return
        running = true
        lastFrame = SystemClock.uptimeMillis()
        postInvalidateOnAnimation()
    }

    fun stop() {
        running = false
    }

    override fun onDetachedFromWindow() {
        running = false
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = SystemClock.uptimeMillis()
        val dt = ((now - lastFrame).coerceIn(0L, 100L)) / 1000f
        lastFrame = now
        val t = now / 1000f

        for (i in particles.indices) {
            val p = particles[i]
            p.y -= p.speed * dt
            if (p.y < -10f) particles[i] = newParticle(width, height, anywhere = false)

            val twinkle = (sin(t * 2f * PI.toFloat() / p.period + p.phase) + 1f) / 2f
            val a = (40 + twinkle * 215).toInt()
            haloPaint.alpha = (a * 0.25f).toInt()
            corePaint.alpha = a
            canvas.drawCircle(p.x, p.y, p.radius * 3f, haloPaint)
            canvas.drawCircle(p.x, p.y, p.radius, corePaint)
        }

        if (running) postInvalidateOnAnimation()
    }
}
