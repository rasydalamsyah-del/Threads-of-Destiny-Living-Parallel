package com.valeriusgames.threadsofdestiny.life

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

/**
 * Panel kecil di pojok kiri atas: 5 bar kebutuhan + uang.
 * Diketuk untuk membuka layar Status & Tas.
 */
class LifeHudView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val dp = resources.displayMetrics.density
    private var state: LifeState? = null

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xE6FAF7F2.toInt() }
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x26000000 }
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f * dp }
    private val moneyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12.5f * dp
        color = 0xFF2A2A2E.toInt()
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
    }
    private val rect = RectF()

    private val rowH = 15f * dp
    private val pad = 9f * dp
    private val barW = 62f * dp

    init {
        isClickable = true
        isFocusable = true
        contentDescription = "Status"
        elevation = 3f * dp
    }

    fun bind(s: LifeState) {
        state = s
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = (pad * 2 + 18f * dp + barW).toInt()
        val h = (pad * 2 + rowH * LifeState.Stat.values().size + 18f * dp).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val s = state ?: return
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, 14f * dp, 14f * dp, bgPaint)

        var y = pad
        LifeState.Stat.values().forEach { stat ->
            val v = s[stat]
            canvas.drawText(stat.icon, pad, y + rowH * 0.72f, iconPaint)
            val bx = pad + 18f * dp
            val by = y + rowH * 0.32f
            val bh = 5.5f * dp
            rect.set(bx, by, bx + barW, by + bh)
            canvas.drawRoundRect(rect, bh, bh, trackPaint)
            barPaint.color = colorFor(v)
            rect.set(bx, by, bx + barW * (v / 100f), by + bh)
            if (v > 0) canvas.drawRoundRect(rect, bh, bh, barPaint)
            y += rowH
        }
        canvas.drawText("💰 " + rupiah(s.money), pad, y + 13f * dp, moneyPaint)
    }

    private fun colorFor(v: Int): Int = when {
        v >= 60 -> 0xFF4CAF7A.toInt()
        v >= 30 -> 0xFFE0A93B.toInt()
        else -> 0xFFE0554B.toInt()
    }
}
