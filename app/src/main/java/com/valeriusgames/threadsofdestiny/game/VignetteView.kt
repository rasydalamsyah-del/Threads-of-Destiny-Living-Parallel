package com.valeriusgames.threadsofdestiny.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.hypot

/** Tepi layar menggelap + kabut hangat tipis, dipakai saat adegan kenangan. */
class VignetteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val haze = Paint().apply { color = 0x1FF5D9A8 }
    private val edge = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val r = hypot(w / 2f, h / 2f)
        edge.shader = RadialGradient(
            w / 2f, h * 0.45f, r,
            intArrayOf(0x00000000, 0x00000000, 0x66140C04, 0xD90A0602.toInt()),
            floatArrayOf(0f, 0.45f, 0.78f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPaint(haze)
        canvas.drawPaint(edge)
    }
}
