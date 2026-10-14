package com.valeriusgames.threadsofdestiny

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.widget.TextView

/**
 * Teks judul dengan isi gradasi emas dan garis tepi gelap,
 * supaya tetap terbaca di atas langit senja.
 */
class GoldTitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : TextView(context, attrs) {

    private var cachedBaseline = -1
    private var cachedSize = -1f
    private var goldShader: Shader? = null
    private var outlineShader: Shader? = null

    private fun ensureShaders() {
        val baseline = baseline
        val size = textSize
        if (baseline == cachedBaseline && size == cachedSize && goldShader != null) return
        cachedBaseline = baseline
        cachedSize = size

        val top = baseline - size * 0.78f
        val bottom = baseline + size * 0.06f
        goldShader = LinearGradient(
            0f, top, 0f, bottom,
            intArrayOf(
                0xFFFFF4D2.toInt(),
                0xFFF2D08A.toInt(),
                0xFFC3913A.toInt(),
                0xFFE9C77E.toInt(),
                0xFFFFF0C4.toInt(),
            ),
            floatArrayOf(0f, 0.35f, 0.58f, 0.8f, 1f),
            Shader.TileMode.CLAMP
        )
        outlineShader = LinearGradient(
            0f, top, 0f, bottom,
            0xFF2A1A08.toInt(), 0xFF1A1006.toInt(),
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        ensureShaders()
        val p = paint

        // 1) Garis tepi gelap (juga membawa bayangan dari atribut shadow)
        p.style = Paint.Style.STROKE
        p.strokeWidth = textSize * 0.07f
        p.strokeJoin = Paint.Join.ROUND
        p.shader = outlineShader
        p.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)
        super.onDraw(canvas)

        // 2) Isi emas
        p.style = Paint.Style.FILL
        p.shader = goldShader
        p.clearShadowLayer()
        super.onDraw(canvas)
    }
}
