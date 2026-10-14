package com.valeriusgames.threadsofdestiny

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.valeriusgames.threadsofdestiny.game.GameActivity
import com.valeriusgames.threadsofdestiny.save.SaveLoadActivity
import com.valeriusgames.threadsofdestiny.save.SaveStore
import com.valeriusgames.threadsofdestiny.settings.SettingsActivity

/**
 * Halaman utama: judul, tombol START GAME / CONTINUE / LOAD DATA / SETTINGS.
 */
class MainMenu(private val activity: Activity, private val root: View) {

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

        btnStart.setOnClickListener { startGame() }
        btnContinue.setOnClickListener { continueGame() }
        btnLoad.setOnClickListener {
            open(Intent(activity, SaveLoadActivity::class.java)
                .putExtra(SaveLoadActivity.EXTRA_MODE, SaveLoadActivity.MODE_LOAD)
                .putExtra(SaveLoadActivity.EXTRA_FROM_MENU, true))
        }
        btnSettings.setOnClickListener { open(Intent(activity, SettingsActivity::class.java)) }

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
        val hasSave = SaveStore.hasAny(activity)
        btnContinue.isEnabled = hasSave
        btnContinue.subLabel =
            if (hasSave) null else activity.getString(R.string.btn_continue_empty)
    }

    private fun startGame() = open(Intent(activity, GameActivity::class.java))

    /** CONTINUE: lanjutkan dari data simpanan yang paling baru. */
    private fun continueGame() {
        val slot = SaveStore.latestSlot(activity) ?: return
        open(Intent(activity, GameActivity::class.java).putExtra(GameActivity.EXTRA_LOAD_SLOT, slot))
    }

    private fun open(intent: Intent) {
        activity.startActivity(intent)
        @Suppress("DEPRECATION")
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
