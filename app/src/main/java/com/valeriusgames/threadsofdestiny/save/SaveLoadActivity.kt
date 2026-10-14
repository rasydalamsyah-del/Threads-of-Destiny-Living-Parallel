package com.valeriusgames.threadsofdestiny.save

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.valeriusgames.threadsofdestiny.R
import com.valeriusgames.threadsofdestiny.enterImmersiveMode
import com.valeriusgames.threadsofdestiny.game.GameActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Halaman Simpan / Muat dengan 1 slot otomatis + 6 slot manual.
 * - MODE_SAVE : dari menu dalam game, membawa data keadaan cerita (EXTRA_STATE)
 * - MODE_LOAD : dari menu dalam game (hasil dikembalikan) atau dari menu utama (EXTRA_FROM_MENU)
 */
class SaveLoadActivity : Activity() {

    companion object {
        const val EXTRA_MODE = "mode"
        const val EXTRA_STATE = "state"
        const val EXTRA_SLOT = "slot"
        const val EXTRA_FROM_MENU = "from_menu"
        const val MODE_SAVE = "save"
        const val MODE_LOAD = "load"
    }

    private val dp by lazy { resources.displayMetrics.density }
    private lateinit var list: LinearLayout
    private var saving = false
    private var pending: SaveData? = null
    private val dateFormat = SimpleDateFormat("d MMM yyyy · HH:mm", Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_load)
        enterImmersiveMode()

        saving = intent.getStringExtra(EXTRA_MODE) == MODE_SAVE
        pending = intent.getStringExtra(EXTRA_STATE)?.let { SaveData.fromJson(it) }

        findViewById<TextView>(R.id.header_title).setText(if (saving) R.string.save_title else R.string.load_title)
        findViewById<TextView>(R.id.slot_hint).setText(if (saving) R.string.slot_hint_save else R.string.slot_hint_load)
        findViewById<View>(R.id.header_back).setOnClickListener { close() }
        list = findViewById(R.id.slot_list)
        render()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersiveMode()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() = close()

    private fun close() {
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun slotName(slot: Int): String =
        if (slot == SaveStore.AUTO_SLOT) getString(R.string.slot_auto) else getString(R.string.slot_manual, slot)

    private fun render() {
        list.removeAllViews()
        for (slot in SaveStore.allSlots) {
            list.addView(slotCard(slot, SaveStore.read(this, slot)),
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = (10 * dp).toInt()
                })
        }
    }

    private fun slotCard(slot: Int, data: SaveData?): View {
        val isAuto = slot == SaveStore.AUTO_SLOT
        val disabled = (saving && isAuto) || (!saving && data == null)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((10 * dp).toInt(), (10 * dp).toInt(), (14 * dp).toInt(), (10 * dp).toInt())
            background = GradientDrawable().apply {
                cornerRadius = 16 * dp
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
                colors = intArrayOf(0xE6182246.toInt(), 0xE60B1027.toInt())
                setStroke((1.2f * dp).toInt(), if (isAuto) 0x99A9B4CC.toInt() else 0xCCD2AE62.toInt())
            }
            alpha = if (disabled) 0.55f else 1f
        }

        // Gambar kecil (thumbnail) dari background saat disimpan
        val thumb = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                cornerRadius = 10 * dp
                setColor(0xFF05070F.toInt())
            }
            clipToOutline = true
            data?.bg?.let { name -> loadThumb(name)?.let { setImageBitmap(it) } }
            if (data?.memory == true) {
                colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0.3f) })
            }
        }
        card.addView(thumb, LinearLayout.LayoutParams((62 * dp).toInt(), (100 * dp).toInt()))

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((14 * dp).toInt(), 0, 0, 0)
        }
        col.addView(TextView(this).apply {
            text = slotName(slot)
            typeface = resources.getFont(R.font.cinzel)
            setTextColor(if (isAuto) 0xFFC9D2E6.toInt() else 0xFFF2D58C.toInt())
            textSize = 16f
        })
        if (data == null) {
            col.addView(TextView(this).apply {
                setText(R.string.slot_empty)
                setTextColor(0x99E8E0CF.toInt())
                textSize = 14f
                setPadding(0, (6 * dp).toInt(), 0, 0)
            })
        } else {
            col.addView(TextView(this).apply {
                text = buildString {
                    append(dateFormat.format(Date(data.savedAt)))
                    data.clock?.let { append("   ·   ").append(it) }
                }
                setTextColor(0xB3E8E0CF.toInt())
                textSize = 12f
                setPadding(0, (3 * dp).toInt(), 0, 0)
            })
            col.addView(TextView(this).apply {
                text = data.preview
                setTextColor(0xFFF6F1E6.toInt())
                textSize = 14f
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                typeface = Typeface.DEFAULT
                setPadding(0, (6 * dp).toInt(), 0, 0)
            })
        }
        card.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        card.setOnClickListener {
            when {
                saving && isAuto -> Toast.makeText(this, R.string.slot_auto_readonly, Toast.LENGTH_SHORT).show()
                saving && data != null -> confirm(getString(R.string.slot_overwrite, slotName(slot))) { saveTo(slot) }
                saving -> saveTo(slot)
                data != null -> loadFrom(slot)
            }
        }
        if (data != null) {
            card.setOnLongClickListener {
                confirm(getString(R.string.slot_delete, slotName(slot))) {
                    SaveStore.delete(this, slot)
                    render()
                }
                true
            }
        }
        return card
    }

    private fun saveTo(slot: Int) {
        val d = pending ?: return
        SaveStore.write(this, slot, d.copy(savedAt = System.currentTimeMillis()))
        Toast.makeText(this, getString(R.string.slot_saved, slotName(slot)), Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        close()
    }

    private fun loadFrom(slot: Int) {
        if (intent.getBooleanExtra(EXTRA_FROM_MENU, false)) {
            startActivity(Intent(this, GameActivity::class.java).putExtra(GameActivity.EXTRA_LOAD_SLOT, slot))
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            setResult(RESULT_OK, Intent().putExtra(EXTRA_SLOT, slot))
            close()
        }
    }

    private fun confirm(message: String, onYes: () -> Unit) {
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setMessage(message)
            .setPositiveButton(R.string.yes) { _, _ -> onYes() }
            .setNegativeButton(R.string.no, null)
            .setOnDismissListener { enterImmersiveMode() }
            .show()
    }

    private fun loadThumb(name: String): Bitmap? {
        val opts = BitmapFactory.Options().apply { inSampleSize = 8 }
        for (ext in listOf("jpg", "jpeg", "png", "webp")) {
            try {
                return assets.open("latar/$name.$ext").use { BitmapFactory.decodeStream(it, null, opts) }
            } catch (_: Exception) {
            }
        }
        return null
    }
}
