package com.valeriusgames.threadsofdestiny.save

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Satu data simpanan: posisi di naskah + keadaan layar saat itu. */
data class SaveData(
    val script: String,
    val index: Int,
    val playerName: String,
    val bg: String?,
    val memory: Boolean,
    val motion: String,
    val clock: String?,
    val music: String?,
    val phoneIcon: Boolean,
    val ringing: Boolean,
    val messages: List<Pair<String, String>>,
    val log: List<Pair<String?, String>>,
    val preview: String,
    val savedAt: Long,
    /** Variabel cerita (kelamin, usia, jalur, dll). */
    val vars: Map<String, String> = emptyMap(),
    /** Keadaan hidup (bar, uang, waktu, tas) dalam JSON. */
    val life: String = "",
    /** Hubungan dengan semua karakter dalam JSON. */
    val social: String = "",
    /** Isi HP (chat, janji, lamaran, dokumen, dompet, catatan) dalam JSON. */
    val phone: String = "",
) {
    fun toJson(): String = JSONObject().apply {
        put("script", script)
        put("index", index)
        put("name", playerName)
        put("bg", bg ?: "")
        put("memory", memory)
        put("motion", motion)
        put("clock", clock ?: "")
        put("music", music ?: "")
        put("phoneIcon", phoneIcon)
        put("ringing", ringing)
        put("messages", JSONArray().apply {
            messages.forEach { put(JSONObject().put("from", it.first).put("text", it.second)) }
        })
        put("log", JSONArray().apply {
            log.forEach { put(JSONObject().put("who", it.first ?: "").put("text", it.second)) }
        })
        put("preview", preview)
        put("savedAt", savedAt)
        put("vars", JSONObject().apply { vars.forEach { (k, v) -> put(k, v) } })
        put("life", life)
        put("social", social)
        put("phone", phone)
    }.toString()

    companion object {
        fun fromJson(s: String): SaveData? = try {
            val o = JSONObject(s)
            val msgs = o.optJSONArray("messages") ?: JSONArray()
            val log = o.optJSONArray("log") ?: JSONArray()
            SaveData(
                script = o.getString("script"),
                index = o.getInt("index"),
                playerName = o.optString("name"),
                bg = o.optString("bg").ifEmpty { null },
                memory = o.optBoolean("memory"),
                motion = o.optString("motion", "NONE"),
                clock = o.optString("clock").ifEmpty { null },
                music = o.optString("music").ifEmpty { null },
                phoneIcon = o.optBoolean("phoneIcon"),
                ringing = o.optBoolean("ringing"),
                messages = (0 until msgs.length()).map {
                    val m = msgs.getJSONObject(it)
                    m.optString("from") to m.optString("text")
                },
                log = (0 until log.length()).map {
                    val l = log.getJSONObject(it)
                    l.optString("who").ifEmpty { null } to l.optString("text")
                },
                preview = o.optString("preview"),
                savedAt = o.optLong("savedAt"),
                vars = o.optJSONObject("vars")?.let { vo ->
                    vo.keys().asSequence().associateWith { k -> vo.optString(k) }
                } ?: emptyMap(),
                life = o.optString("life"),
                social = o.optString("social"),
                phone = o.optString("phone"),
            )
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Slot simpanan. Slot 0 = simpan otomatis, slot 1..[MANUAL_SLOTS] = simpan manual.
 */
object SaveStore {

    const val AUTO_SLOT = 0
    const val MANUAL_SLOTS = 6
    private const val PREFS = "saves"

    val allSlots: IntRange get() = AUTO_SLOT..MANUAL_SLOTS

    fun read(ctx: Context, slot: Int): SaveData? =
        prefs(ctx).getString("slot_$slot", null)?.let { SaveData.fromJson(it) }

    fun write(ctx: Context, slot: Int, data: SaveData) {
        prefs(ctx).edit().putString("slot_$slot", data.toJson()).apply()
    }

    fun delete(ctx: Context, slot: Int) {
        prefs(ctx).edit().remove("slot_$slot").apply()
    }

    fun clearAll(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }

    fun hasAny(ctx: Context): Boolean = latestSlot(ctx) != null

    /** Slot yang paling baru disimpan (untuk tombol CONTINUE). */
    fun latestSlot(ctx: Context): Int? =
        allSlots.mapNotNull { s -> read(ctx, s)?.let { s to it.savedAt } }
            .maxByOrNull { it.second }?.first

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
