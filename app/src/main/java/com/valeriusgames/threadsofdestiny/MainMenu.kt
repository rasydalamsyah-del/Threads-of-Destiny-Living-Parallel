package com.valeriusgames.threadsofdestiny

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast

/**
 * Halaman utama: judul, tombol START GAME / CONTINUE / LOAD DATA / SETTINGS.
 */
class MainMenu(private val activity: Activity, private val root: View) {

    companion object {
        private const val PREFS = "save_data"
        /** Diisi true oleh sistem save nanti, saat pemain sudah punya data simpanan. */
        const val KEY_HAS_SAVE = "has_save"
    }

    private val titleBlock: View = root.findViewById(R.id.title_block)
    private val panel: View = root.findViewById(R.id.panel)
    private val sparkles: SparkleView = root.findViewById(R.id.sparkles)
    private val btnStart: FantasyButton = root.findViewById(R.id.btn_start)
    private val btnContinue: FantasyButton = root.findViewById(R.id.btn_continue)
    private val btnLoad: FantasyButton = root.findViewById(R.id.btn_load)
    private val btnSettings: FantasyButton = root.findViewById(R.id.btn_settings)
    private val buttons = listOf(btnStart, btnContinue, btnLoad, btnSettings)

    init {
        root.findViewById<TextView>(R.id.version).text =
            activity.getString(R.string.version_format, BuildConfig.VERSION_NAME)

        btnStart.setOnClickListener { comingSoon(R.string.btn_start) }
        btnContinue.setOnClickListener { comingSoon(R.string.btn_continue) }
        btnLoad.setOnClickListener { comingSoon(R.string.btn_load) }
        btnSettings.setOnClickListener { comingSoon(R.string.btn_settings) }

        refreshSaveState()
    }

    fun show(animate: Boolean) {
        refreshSaveState()
        root.visibility = View.VISIBLE
        sparkles.start()
        if (!animate) {
            root.alpha = 1f
            return
        }

        val density = activity.resources.displayMetrics.density
        val ease = DecelerateInterpolator(1.6f)

        root.alpha = 0f
        root.animate().alpha(1f).setDuration(MainActivity.VIDEO_TO_MENU_FADE_MS).start()

        titleBlock.alpha = 0f
        titleBlock.translationY = -24f * density
        titleBlock.animate()
            .alpha(1f).translationY(0f)
            .setStartDelay(450).setDuration(900).setInterpolator(ease)
            .start()

        panel.alpha = 0f
        panel.animate().alpha(1f).setStartDelay(650).setDuration(700).start()

        buttons.forEachIndexed { i, b ->
            b.alpha = 0f
            b.translationY = 22f * density
            b.animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(800L + i * 110L).setDuration(550).setInterpolator(ease)
                .start()
        }
    }

    fun onResume() {
        refreshSaveState()
        if (root.visibility == View.VISIBLE) sparkles.start()
    }

    fun onPause() = sparkles.stop()

    /** CONTINUE hanya aktif kalau ada data simpanan. */
    private fun refreshSaveState() {
        val hasSave = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAS_SAVE, false)
        btnContinue.isEnabled = hasSave
        btnContinue.subLabel =
            if (hasSave) null else activity.getString(R.string.btn_continue_empty)
    }

    private fun comingSoon(labelRes: Int) {
        Toast.makeText(
            activity,
            activity.getString(R.string.coming_soon, activity.getString(labelRes)),
            Toast.LENGTH_SHORT
        ).show()
    }
}
