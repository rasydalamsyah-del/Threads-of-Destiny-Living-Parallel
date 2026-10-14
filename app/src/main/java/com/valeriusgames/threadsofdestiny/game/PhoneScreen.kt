package com.valeriusgames.threadsofdestiny.game

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.valeriusgames.threadsofdestiny.R
import kotlin.math.min

/**
 * Layar HP: layar utama (jam, notifikasi, ikon aplikasi) dan halaman aplikasi.
 * Isi setiap aplikasi digambar oleh GameActivity lewat [onAppOpen].
 */
class PhoneScreen(private val overlay: View, private val onClosed: () -> Unit) {

    data class App(val id: String, val icon: String, val label: String, val color: Int)

    private val frame: View = overlay.findViewById(R.id.phone_frame)
    private val dateView: TextView = overlay.findViewById(R.id.phone_date)
    private val timeView: TextView = overlay.findViewById(R.id.phone_time)
    private val list: LinearLayout = overlay.findViewById(R.id.phone_notifs)
    private val empty: View = overlay.findViewById(R.id.phone_empty)
    private val appsBox: LinearLayout = overlay.findViewById(R.id.phone_apps)
    private val homePage: View = overlay.findViewById(R.id.phone_home_page)
    private val appPage: View = overlay.findViewById(R.id.phone_app_page)
    private val appTitle: TextView = overlay.findViewById(R.id.phone_app_title)
    private val appScroll: ScrollView = overlay.findViewById(R.id.phone_app_scroll)
    val content: LinearLayout = overlay.findViewById(R.id.phone_app_content)
    val footer: LinearLayout = overlay.findViewById(R.id.phone_app_footer)
    private val dp = overlay.resources.displayMetrics.density

    /** Dipanggil saat ikon aplikasi diketuk. */
    var onAppOpen: ((String) -> Unit)? = null
    /** Dipanggil setiap HP dibuka / kembali ke layar utama (untuk memperbarui lencana). */
    var onHomeShown: (() -> Unit)? = null

    /** Tombol kembali di dalam aplikasi (null = kembali ke layar utama). */
    var backHandler: (() -> Unit)? = null

    var isOpen = false
        private set
    var currentApp: String? = null
        private set

    init {
        overlay.setOnClickListener { close() }                       // ketuk di luar HP
        overlay.findViewById<View>(R.id.phone_home).setOnClickListener { if (currentApp != null) back() else close() }
        overlay.findViewById<View>(R.id.phone_app_back).setOnClickListener { back() }
        frame.setOnClickListener { /* ketukan di dalam HP tidak menutup */ }
    }

    /** Jam di layar utama, contoh "Sen, 13 Jul · Pagi ☀️". */
    fun setClock(clockText: String?) {
        if (clockText == null) {
            timeView.text = "--:--"
            dateView.text = ""
            return
        }
        val explicit = Regex("\\d{1,2}[:.]\\d{2}").find(clockText)?.value
        if (explicit != null) {
            timeView.text = explicit
            dateView.text = clockText.substringBefore(',').trim()
        } else {
            timeView.text = clockText.substringAfter('·', clockText).trim()
            dateView.text = clockText.substringBefore('·').trim()
        }
    }

    /** Ikon aplikasi di layar utama (3 per baris), dengan angka lencana. */
    fun setApps(apps: List<App>, badges: Map<String, Int>) {
        appsBox.removeAllViews()
        val ctx = overlay.context
        apps.chunked(3).forEach { rowApps ->
            val row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
            rowApps.forEach { app ->
                val cell = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                    setPadding(0, (4 * dp).toInt(), 0, (4 * dp).toInt())
                    setOnClickListener { openApp(app) }
                }
                val iconBox = FrameLayout(ctx)
                iconBox.addView(TextView(ctx).apply {
                    text = app.icon
                    textSize = 22f
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        cornerRadius = 14 * dp
                        setColor(app.color)
                    }
                }, FrameLayout.LayoutParams((48 * dp).toInt(), (48 * dp).toInt()))
                val badge = badges[app.id] ?: 0
                if (badge > 0) {
                    iconBox.addView(TextView(ctx).apply {
                        text = if (badge > 9) "9+" else badge.toString()
                        textSize = 10f
                        setTextColor(0xFFFFFFFF.toInt())
                        typeface = Typeface.DEFAULT_BOLD
                        gravity = Gravity.CENTER
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setColor(0xFFFF3B4E.toInt())
                        }
                    }, FrameLayout.LayoutParams((18 * dp).toInt(), (18 * dp).toInt(), Gravity.TOP or Gravity.END))
                }
                cell.addView(iconBox, LinearLayout.LayoutParams((52 * dp).toInt(), (52 * dp).toInt()))
                cell.addView(TextView(ctx).apply {
                    text = app.label
                    textSize = 10.5f
                    setTextColor(0xFFFFFFFF.toInt())
                    setShadowLayer(4f, 0f, 1f, 0xAA000000.toInt())
                    gravity = Gravity.CENTER
                    maxLines = 1
                })
                row.addView(cell, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            }
            repeat(3 - rowApps.size) { row.addView(View(ctx), LinearLayout.LayoutParams(0, 1, 1f)) }
            appsBox.addView(row)
        }
    }

    /** Kembali satu langkah: halaman dalam aplikasi → daftar → layar utama. */
    fun back() {
        val b = backHandler
        if (b != null) b() else showHome()
    }

    fun setTitle(text: String) {
        appTitle.text = text
    }

    private fun openApp(app: App) {
        backHandler = null
        currentApp = app.id
        appTitle.text = "${app.icon}  ${app.label}"
        homePage.visibility = View.GONE
        appPage.visibility = View.VISIBLE
        appPage.alpha = 0f
        appPage.translationY = 24 * dp
        appPage.animate().alpha(1f).translationY(0f).setDuration(200).start()
        onAppOpen?.invoke(app.id)
    }

    /** Kosongkan halaman aplikasi sebelum digambar ulang. */
    fun clearApp() {
        content.removeAllViews()
        footer.removeAllViews()
    }

    fun scrollToBottom() = appScroll.post { appScroll.fullScroll(View.FOCUS_DOWN) }
    fun scrollToTop() = appScroll.post { appScroll.scrollTo(0, 0) }

    fun showHome() {
        backHandler = null
        currentApp = null
        appPage.visibility = View.GONE
        homePage.visibility = View.VISIBLE
        onHomeShown?.invoke()
    }

    /** Notifikasi di layar utama (paling baru di atas). */
    fun addMessage(sender: String, text: String) {
        val ctx = overlay.context
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.phone_notif)
            setPadding((12 * dp).toInt(), (9 * dp).toInt(), (12 * dp).toInt(), (10 * dp).toInt())
        }
        card.addView(TextView(ctx).apply {
            setText(R.string.phone_app_messages)
            textSize = 10f
            letterSpacing = 0.04f
            setTextColor(0xFF6B6B73.toInt())
        })
        card.addView(TextView(ctx).apply {
            this.text = sender
            textSize = 14f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(0xFF16161A.toInt())
            setPadding(0, (2 * dp).toInt(), 0, 0)
        })
        card.addView(TextView(ctx).apply {
            this.text = text
            textSize = 13f
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            setTextColor(0xFF2A2A30.toInt())
            setPadding(0, (2 * dp).toInt(), 0, 0)
        })
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = (6 * dp).toInt() }
        list.addView(card, 0, lp)
        while (list.childCount > 4) list.removeViewAt(list.childCount - 1)
        empty.visibility = View.GONE
    }

    fun clearNotifications() {
        list.removeAllViews()
        empty.visibility = View.VISIBLE
    }

    fun open() {
        if (isOpen) return
        isOpen = true
        fitToScreen()
        showHome()
        overlay.bringToFront()
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
        val maxH = (620 * dp).toInt()
        val targetH = min(maxH, (h * 0.84f).toInt())
        val lp = frame.layoutParams
        lp.height = targetH
        lp.width = (targetH * 0.5f).toInt()
        frame.layoutParams = lp
    }
}
