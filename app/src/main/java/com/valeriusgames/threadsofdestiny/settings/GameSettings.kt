package com.valeriusgames.threadsofdestiny.settings

import android.content.Context
import android.graphics.Typeface
import com.valeriusgames.threadsofdestiny.R
import kotlin.math.pow

/** Semua pengaturan pemain, disimpan di HP. */
object GameSettings {

    private const val PREFS = "settings"

    enum class Theme(val label: String) {
        AUTO("Otomatis"), LIGHT("Terang"), DARK("Gelap")
    }

    enum class FontStyle(val label: String) {
        MODERN("Modern"), ROUNDED("Bulat"), CLASSIC("Klasik"), THIN("Tipis")
    }

    val FONT_SIZE_LABELS = listOf("Kecil", "Sedang", "Besar", "Ekstra")
    private val FONT_SIZES = floatArrayOf(15f, 18f, 21f, 24f)

    val TEXT_SPEED_LABELS = listOf("Lambat", "Normal", "Cepat", "Kilat", "Instan")
    private val TEXT_SPEEDS = longArrayOf(55L, 30L, 16L, 7L, 0L)

    data class Values(
        val theme: Theme = Theme.AUTO,
        val fontSize: Int = 1,
        val fontStyle: FontStyle = FontStyle.MODERN,
        val textSpeed: Int = 1,
        val boxOpacity: Int = 92,
        val music: Int = 70,
        val sfx: Int = 80,
        val vibration: Boolean = true,
        val skipIntro: Boolean = false,
    ) {
        val fontSizeSp: Float get() = FONT_SIZES[fontSize.coerceIn(0, FONT_SIZES.size - 1)]
        val typeDelayMs: Long get() = TEXT_SPEEDS[textSpeed.coerceIn(0, TEXT_SPEEDS.size - 1)]
        val musicVolume: Float get() = volume(music)
        val sfxVolume: Float get() = volume(sfx)
    }

    /** Persen (0–100) → volume yang terdengar natural di telinga. */
    fun volume(percent: Int): Float = (percent.coerceIn(0, 100) / 100f).pow(1.7f)

    fun load(ctx: Context): Values {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val d = Values()
        return Values(
            theme = enumOr(p.getString("theme", null), d.theme),
            fontSize = p.getInt("font_size", d.fontSize),
            fontStyle = enumOr(p.getString("font_style", null), d.fontStyle),
            textSpeed = p.getInt("text_speed", d.textSpeed),
            boxOpacity = p.getInt("box_opacity", d.boxOpacity),
            music = p.getInt("music", d.music),
            sfx = p.getInt("sfx", d.sfx),
            vibration = p.getBoolean("vibration", d.vibration),
            skipIntro = p.getBoolean("skip_intro", d.skipIntro),
        )
    }

    fun save(ctx: Context, v: Values) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("theme", v.theme.name)
            .putInt("font_size", v.fontSize)
            .putString("font_style", v.fontStyle.name)
            .putInt("text_speed", v.textSpeed)
            .putInt("box_opacity", v.boxOpacity)
            .putInt("music", v.music)
            .putInt("sfx", v.sfx)
            .putBoolean("vibration", v.vibration)
            .putBoolean("skip_intro", v.skipIntro)
            .apply()
    }

    /** Huruf untuk teks dialog sesuai pilihan gaya font. */
    fun typeface(ctx: Context, style: FontStyle, italic: Boolean): Typeface {
        val base: Typeface = when (style) {
            FontStyle.MODERN -> Typeface.SANS_SERIF
            FontStyle.ROUNDED -> ctx.resources.getFont(R.font.nunito)
            FontStyle.CLASSIC -> ctx.resources.getFont(R.font.lora)
            FontStyle.THIN -> Typeface.create("sans-serif-light", Typeface.NORMAL)
        }
        return Typeface.create(base, if (italic) Typeface.ITALIC else Typeface.NORMAL)
    }

    private inline fun <reified T : Enum<T>> enumOr(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default
}
