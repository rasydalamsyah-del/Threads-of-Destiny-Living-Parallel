package com.valeriusgames.threadsofdestiny.game

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import com.valeriusgames.threadsofdestiny.R
import kotlin.math.min

/**
 * Layar HP yang muncul di tengah saat ikon HP diketuk.
 * Sementara: layar kunci dengan jam besar dan daftar notifikasi pesan.
 */
class PhoneScreen(private val overlay: View, private val onClosed: () -> Unit) {

    private val frame: View = overlay.findViewById(R.id.phone_frame)
    private val dateView: TextView = overlay.findViewById(R.id.phone_date)
    private val timeView: TextView = overlay.findViewById(R.id.phone_time)
    private val list: LinearLayout = overlay.findViewById(R.id.phone_notifs)
    private val empty: View = overlay.findViewById(R.id.phone_empty)
    private val dp = overlay.resources.displayMetrics.density

    var isOpen = false
        private set

    init {
        overlay.setOnClickListener { close() }                       // ketuk di luar HP
        overlay.findViewById<View>(R.id.phone_home).setOnClickListener { close() }
        frame.setOnClickListener { /* ketukan di dalam HP tidak menutup */ }
    }

    /** clockText contoh: "Sabtu, 07:12 ☀️" → tanggal "Sabtu", jam "07:12". */
    fun setClock(clockText: String?) {
        val time = clockText?.let { Regex("\\d{1,2}[:.]\\d{2}").find(it)?.value }
        timeView.text = time ?: "--:--"
        dateView.text = clockText?.substringBefore(',')?.trim().orEmpty()
    }

    fun addMessage(sender: String, text: String) {
        val ctx = overlay.context
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.phone_notif)
            setPadding((14 * dp).toInt(), (11 * dp).toInt(), (14 * dp).toInt(), (12 * dp).toInt())
        }
        card.addView(TextView(ctx).apply {
            setText(R.string.phone_app_messages)
            textSize = 11f
            letterSpacing = 0.04f
            setTextColor(0xFF6B6B73.toInt())
        })
        card.addView(TextView(ctx).apply {
            this.text = sender
            textSize = 15f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(0xFF16161A.toInt())
            setPadding(0, (3 * dp).toInt(), 0, 0)
        })
        card.addView(TextView(ctx).apply {
            this.text = text
            textSize = 14f
            setTextColor(0xFF2A2A30.toInt())
            setPadding(0, (2 * dp).toInt(), 0, 0)
        })
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = (8 * dp).toInt() }
        list.addView(card, 0, lp)   // pesan terbaru di atas
        empty.visibility = View.GONE
    }

    fun open() {
        if (isOpen) return
        isOpen = true
        fitToScreen()
        overlay.visibility = View.VISIBLE
        overlay.alpha = 0f
        overlay.animate().alpha(1f).setDuration(220).start()
        frame.scaleX = 0.86f
        frame.scaleY = 0.86f
        frame.translationY = 120 * dp
        frame.animate()
            .scaleX(1f).scaleY(1f).translationY(0f)
            .setDuration(380)
            .setInterpolator(DecelerateInterpolator(2f))
            .start()
    }

    fun close() {
        if (!isOpen) return
        isOpen = false
        frame.animate()
            .scaleX(0.9f).scaleY(0.9f).translationY(90 * dp)
            .setDuration(240)
            .start()
        overlay.animate().alpha(0f).setDuration(240).withEndAction {
            overlay.visibility = View.GONE
            onClosed()
        }.start()
    }

    /** Ukuran HP menyesuaikan tinggi layar. */
    private fun fitToScreen() {
        val h = (overlay.parent as? View)?.height ?: 0
        if (h == 0) return
        val maxH = (590 * dp).toInt()
        val targetH = min(maxH, (h * 0.8f).toInt())
        val lp = frame.layoutParams
        lp.height = targetH
        lp.width = (targetH * 0.49f).toInt()
        frame.layoutParams = lp
    }
}
