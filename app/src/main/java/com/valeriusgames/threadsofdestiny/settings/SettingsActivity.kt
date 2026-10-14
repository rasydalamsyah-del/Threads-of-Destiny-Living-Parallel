package com.valeriusgames.threadsofdestiny.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.valeriusgames.threadsofdestiny.FantasyButton
import com.valeriusgames.threadsofdestiny.R
import com.valeriusgames.threadsofdestiny.enterImmersiveMode
import com.valeriusgames.threadsofdestiny.game.GameActivity
import com.valeriusgames.threadsofdestiny.save.SaveStore

/** Halaman Pengaturan: tampilan, teks, suara, dan lainnya. Tersimpan otomatis. */
class SettingsActivity : Activity() {

    private var v = GameSettings.Values()
    private val handler = Handler(Looper.getMainLooper())
    private val dp by lazy { resources.displayMetrics.density }

    private val gold = 0xFFD2AE62.toInt()
    private val goldLight = 0xFFF2D58C.toInt()
    private val cream = 0xFFF6E7C8.toInt()
    private val muted = 0xFFA9A398.toInt()

    private lateinit var list: LinearLayout
    private lateinit var previewBox: View
    private lateinit var previewText: TextView
    private lateinit var previewName: TextView
    private val rebuilders = mutableListOf<() -> Unit>()

    private var previewFull = ""
    private var previewTyped = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        enterImmersiveMode()

        findViewById<TextView>(R.id.header_title).setText(R.string.settings_title)
        findViewById<View>(R.id.header_back).setOnClickListener { close() }

        list = findViewById(R.id.settings_list)
        previewBox = findViewById(R.id.preview_box)
        previewText = findViewById(R.id.preview_text)
        previewName = findViewById(R.id.preview_name)
        previewFull = getString(R.string.settings_preview)
        loadPreviewBackground()

        v = GameSettings.load(this)
        buildRows()
        updatePreview(restartTyping = true)

        findViewById<FantasyButton>(R.id.settings_reset).setOnClickListener {
            v = GameSettings.Values()
            GameSettings.save(this, v)
            rebuilders.forEach { it() }
            updatePreview(restartTyping = true)
            Toast.makeText(this, R.string.settings_reset_done, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersiveMode()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() = close()

    private fun close() {
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun update(change: (GameSettings.Values) -> GameSettings.Values, restartTyping: Boolean = false) {
        v = change(v)
        GameSettings.save(this, v)
        updatePreview(restartTyping)
    }

    // ================================================================== daftar pengaturan

    private fun buildRows() {
        section(R.string.sec_display)
        choiceRow(
            R.string.set_theme, getString(R.string.set_theme_desc),
            GameSettings.Theme.values().map { it.label }, { v.theme.ordinal }
        ) { i -> update({ it.copy(theme = GameSettings.Theme.values()[i]) }) }
        choiceRow(R.string.set_font_size, null, GameSettings.FONT_SIZE_LABELS, { v.fontSize }) { i ->
            update({ it.copy(fontSize = i) })
        }
        choiceRow(
            R.string.set_font_style, null,
            GameSettings.FontStyle.values().map { it.label }, { v.fontStyle.ordinal },
            optionTypeface = { i -> GameSettings.typeface(this, GameSettings.FontStyle.values()[i], false) }
        ) { i -> update({ it.copy(fontStyle = GameSettings.FontStyle.values()[i]) }) }
        sliderRow(R.string.set_box_opacity, 40, 100, { v.boxOpacity }) { p ->
            update({ it.copy(boxOpacity = p) })
        }

        section(R.string.sec_text)
        choiceRow(R.string.set_text_speed, null, GameSettings.TEXT_SPEED_LABELS, { v.textSpeed }) { i ->
            update({ it.copy(textSpeed = i) }, restartTyping = true)
        }

        section(R.string.sec_audio)
        sliderRow(R.string.set_music, 0, 100, { v.music }) { p -> update({ it.copy(music = p) }) }
        sliderRow(R.string.set_sfx, 0, 100, { v.sfx }) { p -> update({ it.copy(sfx = p) }) }

        section(R.string.sec_other)
        switchRow(R.string.set_vibration, R.string.set_vibration_desc, { v.vibration }) { on ->
            update({ it.copy(vibration = on) })
        }
        switchRow(R.string.set_skip_intro, R.string.set_skip_intro_desc, { v.skipIntro }) { on ->
            update({ it.copy(skipIntro = on) })
        }

        section(R.string.sec_data)
        dangerRow(R.string.set_delete_data) { confirmDeleteData() }
    }

    private fun section(titleRes: Int) {
        list.addView(TextView(this).apply {
            setText(titleRes)
            typeface = resources.getFont(R.font.cinzel)
            letterSpacing = 0.08f
            setTextColor(goldLight)
            textSize = 16f
            setPadding(0, (22 * dp).toInt(), 0, (4 * dp).toInt())
        })
        list.addView(View(this).apply { setBackgroundResource(R.drawable.title_divider) },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (1 * dp).toInt()).apply {
                bottomMargin = (6 * dp).toInt()
            })
    }

    private fun label(textRes: Int, desc: String?): LinearLayout {
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, (12 * dp).toInt(), 0, (8 * dp).toInt())
        }
        col.addView(TextView(this).apply {
            setText(textRes)
            setTextColor(cream)
            textSize = 16f
        })
        if (desc != null) {
            col.addView(TextView(this).apply {
                text = desc
                setTextColor(muted)
                textSize = 12.5f
                setPadding(0, (2 * dp).toInt(), 0, 0)
            })
        }
        return col
    }

    /** Baris pilihan berbentuk tombol-tombol kecil (chip). */
    private fun choiceRow(
        titleRes: Int,
        desc: String?,
        options: List<String>,
        selected: () -> Int,
        optionTypeface: ((Int) -> Typeface)? = null,
        onSelect: (Int) -> Unit,
    ) {
        val col = label(titleRes, desc)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (8 * dp).toInt(), 0, 0)
        }
        val chips = options.mapIndexed { i, text ->
            TextView(this).apply {
                this.text = text
                gravity = Gravity.CENTER
                textSize = 13f
                maxLines = 1
                setPadding((2 * dp).toInt(), 0, (2 * dp).toInt(), 0)
                optionTypeface?.let { typeface = it(i) }
                setOnClickListener {
                    onSelect(i)
                    rebuilders.forEach { r -> r() }
                }
            }
        }
        chips.forEachIndexed { i, chip ->
            row.addView(chip, LinearLayout.LayoutParams(0, (40 * dp).toInt(), 1f).apply {
                if (i > 0) marginStart = (6 * dp).toInt()
            })
        }
        col.addView(row)
        list.addView(col)

        val refresh = {
            val sel = selected()
            chips.forEachIndexed { i, chip ->
                val on = i == sel
                chip.background = chipBackground(on)
                chip.setTextColor(if (on) 0xFF1A1206.toInt() else cream)
                if (optionTypeface == null) {
                    chip.typeface = if (on) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                }
            }
        }
        refresh()
        rebuilders += refresh
    }

    private fun chipBackground(selected: Boolean) = GradientDrawable().apply {
        cornerRadius = 12 * dp
        if (selected) {
            orientation = GradientDrawable.Orientation.TOP_BOTTOM
            colors = intArrayOf(0xFFF8E3A8.toInt(), 0xFFD2AE62.toInt())
        } else {
            setColor(0x40101A3A)
            setStroke((1 * dp).toInt(), 0x80D2AE62.toInt())
        }
    }

    private fun sliderRow(titleRes: Int, min: Int, max: Int, value: () -> Int, onChange: (Int) -> Unit) {
        val col = label(titleRes, null)
        val head = col.getChildAt(0) as TextView
        val valueText = TextView(this).apply {
            setTextColor(goldLight)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }
        // judul di kiri, angka persen di kanan
        col.removeView(head)
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        top.addView(head, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        top.addView(valueText)
        col.addView(top, 0)

        val seek = SeekBar(this).apply {
            this.max = max - min
            progressTintList = ColorStateList.valueOf(gold)
            thumbTintList = ColorStateList.valueOf(goldLight)
            progressBackgroundTintList = ColorStateList.valueOf(0x66FFFFFF)
            setPadding((10 * dp).toInt(), (10 * dp).toInt(), (10 * dp).toInt(), (6 * dp).toInt())
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                    valueText.text = "${progress + min}%"
                    if (fromUser) onChange(progress + min)
                }
                override fun onStartTrackingTouch(sb: SeekBar) = Unit
                override fun onStopTrackingTouch(sb: SeekBar) = Unit
            })
        }
        col.addView(seek, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        list.addView(col)

        val refresh = {
            seek.progress = value() - min
            valueText.text = "${value()}%"
        }
        refresh()
        rebuilders += refresh
    }

    @Suppress("UseSwitchCompatOrMaterialCode")
    private fun switchRow(titleRes: Int, descRes: Int, value: () -> Boolean, onChange: (Boolean) -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        row.addView(label(titleRes, getString(descRes)), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val sw = Switch(this).apply {
            thumbTintList = ColorStateList(states, intArrayOf(goldLight, 0xFFBDBDBD.toInt()))
            trackTintList = ColorStateList(states, intArrayOf(0xAAD2AE62.toInt(), 0x55FFFFFF))
        }
        row.addView(sw)
        list.addView(row)

        val refresh = {
            sw.setOnCheckedChangeListener(null)
            sw.isChecked = value()
            sw.setOnCheckedChangeListener { _, on -> onChange(on) }
        }
        refresh()
        rebuilders += refresh
    }

    private fun dangerRow(titleRes: Int, onClick: () -> Unit) {
        list.addView(TextView(this).apply {
            setText(titleRes)
            setTextColor(0xFFFF8A8A.toInt())
            textSize = 16f
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = 12 * dp
                setColor(0x26FF5A5A)
                setStroke((1 * dp).toInt(), 0x80FF8A8A.toInt())
            }
            setOnClickListener { onClick() }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (48 * dp).toInt()).apply {
            topMargin = (12 * dp).toInt()
        })
    }

    private fun confirmDeleteData() {
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(R.string.set_delete_data)
            .setMessage(R.string.set_delete_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                SaveStore.clearAll(this)
                getSharedPreferences(GameActivity.PREFS, Context.MODE_PRIVATE).edit().clear().apply()
                Toast.makeText(this, R.string.set_delete_done, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.no, null)
            .setOnDismissListener { enterImmersiveMode() }
            .show()
    }

    // ================================================================== contoh dialog

    private fun loadPreviewBackground() {
        try {
            val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
            val bmp = assets.open("latar/kamar_pagi.jpg").use { BitmapFactory.decodeStream(it, null, opts) }
            findViewById<ImageView>(R.id.preview_bg).setImageBitmap(bmp)
        } catch (_: Exception) {
        }
    }

    private fun updatePreview(restartTyping: Boolean) {
        val dark = v.theme == GameSettings.Theme.DARK
        previewBox.setBackgroundResource(if (dark) R.drawable.dialog_dark else R.drawable.dialog_light)
        previewBox.background.mutate().alpha = (v.boxOpacity * 255 / 100).coerceIn(40, 255)
        previewName.setBackgroundResource(if (dark) R.drawable.name_tag_dark else R.drawable.name_tag_light)
        previewName.setTextColor(if (dark) 0xFFF3E3BF.toInt() else 0xFF1E1E22.toInt())
        previewText.setTextColor(if (dark) 0xFFF3E9D6.toInt() else 0xFF2A2A2E.toInt())
        previewText.typeface = GameSettings.typeface(this, v.fontStyle, false)
        previewText.setTextSize(TypedValue.COMPLEX_UNIT_SP, v.fontSizeSp)

        if (restartTyping) {
            handler.removeCallbacks(typeTick)
            previewTyped = 0
            if (v.typeDelayMs <= 0L) {
                previewTyped = previewFull.length
                previewText.text = previewFull
            } else {
                previewText.text = typedPreview()
                handler.postDelayed(typeTick, v.typeDelayMs)
            }
        }
    }

    private val typeTick = object : Runnable {
        override fun run() {
            previewTyped++
            previewText.text = typedPreview()
            if (previewTyped < previewFull.length) {
                handler.postDelayed(this, v.typeDelayMs.coerceAtLeast(1L))
            } else {
                // ulangi contoh setelah jeda
                handler.postDelayed({ updatePreview(restartTyping = true) }, 1800)
            }
        }
    }

    private fun typedPreview(): CharSequence {
        val s = SpannableString(previewFull)
        if (previewTyped < s.length) {
            s.setSpan(ForegroundColorSpan(Color.TRANSPARENT), previewTyped, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return s
    }
}
