package com.valeriusgames.threadsofdestiny.game

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.valeriusgames.threadsofdestiny.story.StoryScript.Motion

/**
 * Dua lapis gambar background yang bergantian, untuk transisi halus.
 * - kenangan : warna dibuat pudar kecokelatan (efek masa lalu)
 * - gerak    : kamera zoom & geser pelan terus-menerus
 * - naik     : kamera seperti bangkit (gambar lama naik keluar, gambar baru naik masuk)
 */
class BackgroundLayer(private val container: FrameLayout) {

    companion object {
        const val CROSSFADE_MS = 1100L
        const val RISE_MS = 1300L
        private const val PAN_MS = 16000L
    }

    private val a = ImageView(container.context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
    private val b = ImageView(container.context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
    private var front: ImageView = a
    private var motionAnim: ValueAnimator? = null

    private val memoryFilter: ColorMatrixColorFilter by lazy {
        val m = ColorMatrix().apply { setSaturation(0.28f) }
        val warm = ColorMatrix(
            floatArrayOf(
                1.06f, 0f, 0f, 0f, 20f,
                0f, 0.98f, 0f, 0f, 10f,
                0f, 0f, 0.84f, 0f, -4f,
                0f, 0f, 0f, 1f, 0f,
            )
        )
        m.postConcat(warm)
        ColorMatrixColorFilter(m)
    }

    init {
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        container.addView(a, lp)
        container.addView(b, FrameLayout.LayoutParams(lp))
        a.alpha = 0f
        b.alpha = 0f
    }

    fun show(bitmap: Bitmap?, memory: Boolean, motion: Motion, instant: Boolean, done: () -> Unit) {
        val incoming = if (front === a) b else a
        val outgoing = front
        front = incoming

        motionAnim?.cancel()
        motionAnim = null
        incoming.animate().cancel()
        outgoing.animate().cancel()

        incoming.setImageBitmap(bitmap)
        incoming.colorFilter = if (memory) memoryFilter else null
        reset(incoming)
        incoming.bringToFront()

        if (instant) {
            incoming.alpha = 1f
            outgoing.alpha = 0f
            reset(outgoing)
            if (motion == Motion.PAN) startPan(incoming)
            done()
            return
        }

        val h = container.height.takeIf { it > 0 }?.toFloat() ?: 1920f
        when (motion) {
            Motion.RISE -> {
                val shift = h * 0.22f
                incoming.alpha = 0f
                incoming.translationY = shift
                incoming.scaleX = 1.08f
                incoming.scaleY = 1.08f
                incoming.animate()
                    .alpha(1f).translationY(0f).scaleX(1f).scaleY(1f)
                    .setDuration(RISE_MS)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        outgoing.alpha = 0f
                        reset(outgoing)
                        done()
                    }
                    .start()
                outgoing.animate()
                    .translationY(-shift)
                    .setDuration(RISE_MS)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }
            else -> {
                incoming.alpha = 0f
                if (motion == Motion.PAN) startPan(incoming)
                incoming.animate()
                    .alpha(1f)
                    .setDuration(CROSSFADE_MS)
                    .setInterpolator(DecelerateInterpolator())
                    .withEndAction {
                        outgoing.alpha = 0f
                        reset(outgoing)
                        done()
                    }
                    .start()
            }
        }
    }

    /** Buram (0 = jelas). Hanya Android 12 ke atas; di bawahnya diabaikan. */
    fun setBlur(radius: Float) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        container.setRenderEffect(
            if (radius < 0.5f) null
            else RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
        )
    }

    fun release() {
        motionAnim?.cancel()
        a.animate().cancel()
        b.animate().cancel()
    }

    private fun startPan(view: ImageView) {
        val w = container.width.takeIf { it > 0 }?.toFloat() ?: 1080f
        motionAnim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = PAN_MS
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                val t = it.animatedValue as Float
                val s = 1.06f + 0.08f * t
                view.scaleX = s
                view.scaleY = s
                view.translationX = w * 0.025f * (t * 2f - 1f)
            }
            start()
        }
    }

    private fun reset(v: ImageView) {
        v.translationX = 0f
        v.translationY = 0f
        v.scaleX = 1f
        v.scaleY = 1f
    }
}
