package com.valeriusgames.threadsofdestiny

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button

/**
 * Tombol bergaya fantasi: bentuk heksagon memanjang dengan bingkai emas
 * dan hiasan berlian di kedua ujung (meniru desain menu utama).
 *
 * primary = true  -> tombol utama (biru, bercahaya), contoh: START GAME
 * isEnabled=false -> tombol abu-abu redup, contoh: CONTINUE tanpa save data
 */
class FantasyButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var label: String = ""
        set(value) { field = value; contentDescription = value; invalidate() }

    var subLabel: String? = null
        set(value) { field = value; invalidate() }

    var primary: Boolean = false
        set(value) { field = value; rebuildShaders(); invalidate() }

    private val dp = resources.displayMetrics.density
    private fun sp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, resources.displayMetrics)

    private val outerPath = Path()
    private val innerPath = Path()
    private val leftGem = Path()
    private val rightGem = Path()

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * dp
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * dp
        strokeJoin = Paint.Join.MITER
    }
    private val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * dp
    }
    private val gemPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x26FFFFFF }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = sp(20f)
        letterSpacing = 0.04f
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = sp(12f)
    }

    init {
        // Diperlukan agar efek cahaya (shadow layer) pada garis terlihat
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        isClickable = true
        isFocusable = true

        val base = resources.getFont(R.font.cinzel)
        val semiBold = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            Typeface.create(base, 600, false) else Typeface.create(base, Typeface.BOLD)
        labelPaint.typeface = semiBold
        subPaint.typeface = base

        val a = context.obtainStyledAttributes(attrs, R.styleable.FantasyButton)
        label = a.getString(R.styleable.FantasyButton_label) ?: ""
        subLabel = a.getString(R.styleable.FantasyButton_subLabel)
        primary = a.getBoolean(R.styleable.FantasyButton_primary, false)
        a.recycle()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        rebuildShaders()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildPaths(w.toFloat(), h.toFloat())
        rebuildShaders()
    }

    private fun buildPaths(w: Float, h: Float) {
        val pad = 4f * dp            // ruang untuk cahaya di tepi
        val tip = (h - 2 * pad) * 0.42f
        hexagon(outerPath, pad, pad, w - pad, h - pad, tip)

        val inset = 5f * dp
        hexagon(innerPath, pad + inset, pad + inset, w - pad - inset, h - pad - inset, tip - inset * 0.55f)

        val gem = 4.2f * dp
        val cy = h / 2f
        diamond(leftGem, pad + tip * 0.62f, cy, gem)
        diamond(rightGem, w - pad - tip * 0.62f, cy, gem)
    }

    private fun hexagon(p: Path, l: Float, t: Float, r: Float, b: Float, tip: Float) {
        val cy = (t + b) / 2f
        p.reset()
        p.moveTo(l, cy)
        p.lineTo(l + tip, t)
        p.lineTo(r - tip, t)
        p.lineTo(r, cy)
        p.lineTo(r - tip, b)
        p.lineTo(l + tip, b)
        p.close()
    }

    private fun diamond(p: Path, cx: Float, cy: Float, s: Float) {
        p.reset()
        p.moveTo(cx, cy - s)
        p.lineTo(cx + s * 0.7f, cy)
        p.lineTo(cx, cy + s)
        p.lineTo(cx - s * 0.7f, cy)
        p.close()
    }

    private fun rebuildShaders() {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val fill = when {
            !isEnabled -> intArrayOf(0xCC6B6F79.toInt(), 0xCC4A4E58.toInt(), 0xCC3A3D46.toInt())
            primary -> intArrayOf(0xFF5A7CC4.toInt(), 0xFF2B4588.toInt(), 0xFF1A2A5E.toInt())
            else -> intArrayOf(0xEB1B2648.toInt(), 0xEB111a38.toInt(), 0xEB0A1029.toInt())
        }
        fillPaint.shader = LinearGradient(0f, 0f, 0f, h, fill, floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)

        val gold = if (isEnabled)
            intArrayOf(0xFF8C6A2C.toInt(), 0xFFF8E3A8.toInt(), 0xFFD2AE62.toInt(), 0xFFF8E3A8.toInt(), 0xFF8C6A2C.toInt())
        else
            intArrayOf(0xFF6E7078.toInt(), 0xFFB6B8BE.toInt(), 0xFF8D8F96.toInt(), 0xFFB6B8BE.toInt(), 0xFF6E7078.toInt())
        val stops = floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f)
        borderPaint.shader = LinearGradient(0f, 0f, w, 0f, gold, stops, Shader.TileMode.CLAMP)
        gemPaint.shader = LinearGradient(0f, 0f, w, 0f, gold, stops, Shader.TileMode.CLAMP)

        innerBorderPaint.color = if (isEnabled) 0x80D9B66E.toInt() else 0x55A0A3AA
        shinePaint.shader = LinearGradient(
            0f, 0f, 0f, h / 2f,
            intArrayOf(0x33FFFFFF, 0x00FFFFFF), null, Shader.TileMode.CLAMP
        )

        glowPaint.color = 0xFFF2D58C.toInt()
        val glowAlpha = if (primary && isEnabled) 0xCC else 0x55
        glowPaint.setShadowLayer(if (primary) 9f * dp else 5f * dp, 0f, 0f,
            Color.argb(if (isEnabled) glowAlpha else 0, 0xF2, 0xD5, 0x8C))
        glowPaint.alpha = if (isEnabled) 0x66 else 0

        labelPaint.color = if (isEnabled) 0xFFFFF7E6.toInt() else 0xFFC4C6CC.toInt()
        labelPaint.setShadowLayer(4f * dp, 0f, 1f * dp, 0xAA000000.toInt())
        subPaint.color = 0xFFB4B7BE.toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(outerPath, glowPaint)
        canvas.drawPath(outerPath, fillPaint)
        canvas.save()
        canvas.clipPath(outerPath)
        canvas.drawRect(0f, 0f, width.toFloat(), height / 2f, shinePaint)
        canvas.restore()
        if (isPressed) canvas.drawPath(outerPath, pressPaint)
        canvas.drawPath(innerPath, innerBorderPaint)
        canvas.drawPath(outerPath, borderPaint)
        canvas.drawPath(leftGem, gemPaint)
        canvas.drawPath(rightGem, gemPaint)

        val cx = width / 2f
        val sub = subLabel
        if (sub.isNullOrEmpty()) {
            val y = height / 2f - (labelPaint.descent() + labelPaint.ascent()) / 2f
            canvas.drawText(label, cx, y, labelPaint)
        } else {
            val labelH = labelPaint.descent() - labelPaint.ascent()
            val subH = subPaint.descent() - subPaint.ascent()
            val top = (height - (labelH + subH * 0.9f)) / 2f
            canvas.drawText(label, cx, top - labelPaint.ascent(), labelPaint)
            canvas.drawText(sub, cx, top + labelH * 0.92f - subPaint.ascent() * 0.9f, subPaint)
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val target = if (isPressed && isEnabled) 0.96f else 1f
        if (scaleX != target) animate().scaleX(target).scaleY(target).setDuration(90).start()
        invalidate()
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = Button::class.java.name
    }
}
