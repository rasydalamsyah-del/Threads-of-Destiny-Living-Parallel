package com.valeriusgames.threadsofdestiny.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.sin

/** Ikon HP di pojok kanan bawah. Saat ada pesan: bergetar, layar menyala, ada titik merah. */
class PhoneIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val dp = resources.displayMetrics.density
    private val body = RectF()
    private val screen = RectF()
    private val notch = RectF()

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * dp
        color = 0xFF8A8A92.toInt()
    }
    private val screenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF050507.toInt() }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val notchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF000000.toInt() }
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFF3B4E.toInt() }
    private val badgeRim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * dp
        color = 0xFFFFFFFF.toInt()
    }

    var ringing = false
        private set
    private var phase = 0f
    private var anim: ValueAnimator? = null

    init {
        isClickable = true
        isFocusable = true
        contentDescription = "HP"
    }

    fun setRinging(on: Boolean) {
        if (ringing == on) return
        ringing = on
        anim?.cancel()
        anim = null
        rotation = 0f
        if (on) {
            anim = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 1600
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener {
                    phase = it.animatedValue as Float
                    // getar di 40% awal tiap siklus
                    rotation = if (phase < 0.4f)
                        sin(phase / 0.4f * 6f * PI).toFloat() * 7f * (1f - phase / 0.4f)
                    else 0f
                    invalidate()
                }
                start()
            }
        }
        invalidate()
    }

    override fun onDetachedFromWindow() {
        anim?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val pad = 7f * dp
        body.set(pad, pad, w - pad, h - pad)
        val inset = 3f * dp
        screen.set(body.left + inset, body.top + inset, body.right - inset, body.bottom - inset)
        val nw = body.width() * 0.32f
        notch.set(body.centerX() - nw / 2, screen.top + 2f * dp, body.centerX() + nw / 2, screen.top + 6f * dp)
        bodyPaint.shader = LinearGradient(
            body.left, body.top, body.right, body.bottom,
            0xFF3A3A40.toInt(), 0xFF0C0C0F.toInt(), Shader.TileMode.CLAMP
        )
        glowPaint.shader = RadialGradient(
            screen.centerX(), screen.centerY() - screen.height() * 0.08f, screen.width() * 0.7f,
            intArrayOf(0xFFE8ECF5.toInt(), 0x66A9B4CC, 0x00000000),
            floatArrayOf(0f, 0.35f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val r = 11f * dp
        canvas.drawRoundRect(body, r, r, bodyPaint)
        canvas.drawRoundRect(body, r, r, rimPaint)
        canvas.drawRoundRect(screen, r * 0.75f, r * 0.75f, screenPaint)

        val glow = if (ringing) 0.55f + 0.45f * ((sin(phase * 2f * PI) + 1f) / 2f).toFloat() else 0.35f
        glowPaint.alpha = (glow * 255).toInt()
        canvas.save()
        canvas.clipRect(screen)
        canvas.drawRoundRect(screen, r * 0.75f, r * 0.75f, glowPaint)
        canvas.restore()
        canvas.drawRoundRect(notch, 3f * dp, 3f * dp, notchPaint)

        if (ringing) {
            val bx = body.right - 1f * dp
            val by = body.top + 1f * dp
            canvas.drawCircle(bx, by, 6f * dp, badgePaint)
            canvas.drawCircle(bx, by, 6f * dp, badgeRim)
        }
    }
}
