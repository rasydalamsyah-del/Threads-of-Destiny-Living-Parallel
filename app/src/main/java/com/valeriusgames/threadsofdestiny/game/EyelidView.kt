package com.valeriusgames.threadsofdestiny.game

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * "Kelopak mata": layar hitam dengan celah lonjong di tengah.
 * openness 0 = mata tertutup (hitam), 1 = terbuka penuh.
 */
class EyelidView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var openness = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    private val dp = resources.displayMetrics.density
    private val path = Path().apply { fillType = Path.FillType.EVEN_ODD }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        maskFilter = BlurMaskFilter(34f * dp, BlurMaskFilter.Blur.NORMAL)
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // BlurMaskFilter butuh software layer
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (openness <= 0.001f) {
            canvas.drawColor(Color.BLACK)
            return
        }
        val pad = 80f * dp
        val cx = w / 2f
        val cy = h / 2f
        val halfH = openness * h * 0.85f
        val halfW = w * (0.75f + openness * 0.6f)
        path.reset()
        path.addRect(-pad, -pad, w + pad, h + pad, Path.Direction.CW)
        path.addOval(cx - halfW, cy - halfH, cx + halfW, cy + halfH, Path.Direction.CW)
        canvas.drawPath(path, paint)
    }
}
