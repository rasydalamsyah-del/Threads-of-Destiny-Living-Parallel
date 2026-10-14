package com.valeriusgames.threadsofdestiny.life

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

/**
 * Panel status (laci geser dari kiri): 5 bar kebutuhan lengkap dengan nama & angka, plus uang.
 * Diketuk untuk membuka layar Status & Tas.
 */
class LifeHudView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val dp = resources.displayMetrics.density
    private var state: LifeState? = null

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xF2FAF7F2.toInt() }
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x22000000 }
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f * dp }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11.5f * dp
        color = 0xFF3A3A40.toInt()
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f * dp
        color = 0xFF6A6A72.toInt()
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    private val moneyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 13f * dp
        color = 0xFF2A2A2E.toInt()
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 9.5f * dp
        color = 0xFF9A9AA2.toInt()
    }
    private val rect = RectF()

    private val rowH = 21f * dp
    private val pad = 11f * dp
    private val iconW = 20f * dp
    private val labelW = 70f * dp
    private val barW = 58f * dp
    private val valueW = 30f * dp

    init {
        isClickable = true
        isFocusable = true
        contentDescription = "Status"
    }

    fun bind(s: LifeState) {
        state = s
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = (pad * 2 + iconW + labelW + barW + valueW).toInt()
        val h = (pad * 2 + rowH * LifeState.Stat.values().size + 22f * dp + 14f * dp).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val s = state ?: return
        // sudut kiri rata (menempel tepi layar), sudut kanan bulat
        rect.set(-20f * dp, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, 16f * dp, 16f * dp, bgPaint)

        var y = pad
        LifeState.Stat.values().forEach { stat ->
            val v = s[stat]
            val base = y + rowH * 0.68f
            canvas.drawText(stat.icon, pad, base, iconPaint)
            canvas.drawText(stat.label, pad + iconW, base, labelPaint)
            val bx = pad + iconW + labelW
            val bh = 6f * dp
            val by = y + rowH * 0.5f - bh / 2
            rect.set(bx, by, bx + barW, by + bh)
            canvas.drawRoundRect(rect, bh, bh, trackPaint)
            barPaint.color = colorFor(v)
            rect.set(bx, by, bx + barW * (v / 100f), by + bh)
            if (v > 0) canvas.drawRoundRect(rect, bh, bh, barPaint)
            canvas.drawText(v.toString(), width - pad, base, valuePaint)
            y += rowH
        }
        canvas.drawText("💰 " + rupiah(s.money), pad, y + 17f * dp, moneyPaint)
        canvas.drawText("Ketuk untuk Status & Tas", pad, y + 32f * dp, hintPaint)
    }

    /** Ada kebutuhan yang kritis (untuk tanda di tombol pemicu). */
    fun critical(): Boolean = state?.let { s -> LifeState.Stat.values().any { s[it] < 25 } } ?: false

    private fun colorFor(v: Int): Int = when {
        v >= 60 -> 0xFF4CAF7A.toInt()
        v >= 30 -> 0xFFE0A93B.toInt()
        else -> 0xFFE0554B.toInt()
    }
}
