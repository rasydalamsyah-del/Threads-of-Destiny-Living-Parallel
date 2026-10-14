package com.valeriusgames.threadsofdestiny.life

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Hubungan pemain dengan setiap karakter.
 *
 * - Cinta (teman)    : rasa sayang/akrab sebagai teman, 0–100
 * - Cinta (pasangan) : rasa sayang romantis, hanya setelah resmi berpacaran
 * - Kepercayaan      : turun karena bohong (bila ketahuan), menghindar, ingkar janji
 *
 * Romansa hanya antara orang dewasa: pemain DAN karakter harus berusia 18 tahun ke atas.
 */
class SocialState {

    enum class Stage(val label: String) {
        ASING("Belum kenal"), KENALAN("Kenalan"), TEMAN("Teman"), TEMAN_DEKAT("Teman dekat"),
        SAHABAT("Sahabat"), PACARAN("Pacar"), TUNANGAN("Tunangan"), MENIKAH("Pasangan hidup"),
        MUSUH("Bermusuhan"),
    }

    class Rel(
        var met: Boolean = false,
        var love: Int = 0,
        var romance: Int = 0,
        var trust: Int = 0,
        var romanticStage: String = "",   // "", pacaran, tunangan, menikah, putus
        var lastGiftDay: Int = -1,
        var lastTalkDay: Int = -1,
        val counters: MutableMap<String, Int> = mutableMapOf(),
    )

    /** Kebohongan yang belum/sudah terbongkar: kode → daftar karakter yang dibohongi. */
    val lies = mutableMapOf<String, MutableList<String>>()
    val rels = mutableMapOf<String, Rel>()

    fun rel(def: GameData.CharacterDef): Rel = rels.getOrPut(def.id) {
        Rel(love = def.loveStart, trust = def.trustStart)
    }

    fun stageOf(def: GameData.CharacterDef): Stage {
        val r = rel(def)
        if (!r.met) return Stage.ASING
        when (r.romanticStage) {
            "menikah" -> return Stage.MENIKAH
            "tunangan" -> return Stage.TUNANGAN
            "pacaran" -> return Stage.PACARAN
        }
        return when {
            r.love < 15 || r.trust < 10 -> Stage.MUSUH
            r.love >= 85 && r.trust >= 60 -> Stage.SAHABAT
            r.love >= 60 -> Stage.TEMAN_DEKAT
            r.love >= 30 -> Stage.TEMAN
            else -> Stage.KENALAN
        }
    }

    fun isPartner(def: GameData.CharacterDef) = rel(def).romanticStage in setOf("pacaran", "tunangan", "menikah")

    /** Usia karakter sekarang = usia awal + berapa tahun pemain sudah bertambah sejak 15. */
    fun ageOf(def: GameData.CharacterDef, playerAge: Int): Int = def.baseAge + (playerAge - 15).coerceAtLeast(0)

    /** Romansa hanya bila keduanya dewasa (18+), lawan jenis, dan karakter memang bisa dipacari. */
    fun canRomance(def: GameData.CharacterDef, playerAge: Int, playerGender: String): Boolean =
        def.romanceable &&
            playerAge >= 18 &&
            ageOf(def, playerAge) >= 18 &&
            def.gender.isNotEmpty() && def.gender != playerGender

    /**
     * Ubah Cinta. Saat sudah berpacaran, yang berubah adalah Cinta (pasangan).
     * Perubahan negatif ikut "menyebar" ke orang-orang dekat karakter ini.
     * Mengembalikan daftar (karakter, perubahan) untuk ditampilkan.
     */
    fun changeLove(data: GameData, id: String, delta: Int, spread: Boolean = true): List<Pair<GameData.CharacterDef, Int>> {
        val def = data.characters[id] ?: return emptyList()
        val r = rel(def)
        r.met = true
        if (isPartner(def)) r.romance = (r.romance + delta).coerceIn(0, 100)
        else r.love = (r.love + delta).coerceIn(0, 100)
        val out = mutableListOf(def to delta)
        if (spread && delta < 0) out += spreadTo(data, id, delta) { other, d -> rel(other).love = (rel(other).love + d).coerceIn(0, 100) }
        return out
    }

    fun changeTrust(data: GameData, id: String, delta: Int, spread: Boolean = true): List<Pair<GameData.CharacterDef, Int>> {
        val def = data.characters[id] ?: return emptyList()
        val r = rel(def)
        r.met = true
        r.trust = (r.trust + delta).coerceIn(0, 100)
        val out = mutableListOf(def to delta)
        if (spread && delta < 0) out += spreadTo(data, id, delta) { other, d -> rel(other).trust = (rel(other).trust + d).coerceIn(0, 100) }
        return out
    }

    /** Kabar menyebar: orang dekat ikut terpengaruh sebagian (bukan saingan/mantan). */
    private fun spreadTo(
        data: GameData, id: String, delta: Int,
        apply: (GameData.CharacterDef, Int) -> Unit,
    ): List<Pair<GameData.CharacterDef, Int>> {
        val out = mutableListOf<Pair<GameData.CharacterDef, Int>>()
        data.relationsOf(id).forEach { (otherId, r) ->
            if (r.kind == "saingan" || r.kind == "mantan") return@forEach
            val other = data.characters[otherId] ?: return@forEach
            val d = (delta * r.strength / 100f * 0.45f).roundToInt()
            if (d != 0) {
                apply(other, d)
                out += other to d
            }
        }
        return out
    }

    /**
     * Apakah karakter ini sedang dilarang bertemu pemain oleh orang dekatnya?
     * Mengembalikan karakter yang melarang, atau null.
     */
    fun blockedBy(data: GameData, def: GameData.CharacterDef): GameData.CharacterDef? {
        data.relationsOf(def.id).forEach { (otherId, r) ->
            if (r.strength < 60 || r.kind == "saingan" || r.kind == "mantan") return@forEach
            val other = data.characters[otherId] ?: return@forEach
            val ro = rel(other)
            if (ro.met && ro.love < 25) return other
        }
        return null
    }

    /** Catat perilaku buruk & akibatnya. */
    fun behave(data: GameData, id: String, kind: String): List<Pair<GameData.CharacterDef, Int>> {
        val def = data.characters[id] ?: return emptyList()
        val r = rel(def)
        r.counters[kind] = (r.counters[kind] ?: 0) + 1
        val (dl, dt) = when (kind) {
            "menghindar" -> -3 to -5
            "ingkar_janji" -> -6 to -15
            "abaikan_chat" -> -2 to -3
            "kasar" -> -8 to -4
            "bohong_ketahuan" -> -10 to -25
            "selingkuh_ketahuan" -> -40 to -50
            "menepati_janji" -> 3 to 6
            "jujur" -> 2 to 5
            "membantu" -> 5 to 4
            else -> 0 to 0
        }
        val out = mutableListOf<Pair<GameData.CharacterDef, Int>>()
        if (dl != 0) out += changeLove(data, id, dl)
        if (dt != 0) changeTrust(data, id, dt)
        return out
    }

    /**
     * Ungkapkan perasaan. Peluang diterima bergantung pada Cinta & Kepercayaan:
     * di bawah 50 hampir pasti ditolak. Hanya mungkin bila canRomance() benar.
     */
    fun confess(def: GameData.CharacterDef, playerAge: Int, playerGender: String): Boolean {
        if (!canRomance(def, playerAge, playerGender)) return false
        val r = rel(def)
        val chance = when {
            r.love < 50 -> 3
            else -> ((r.love - 50) * 2 + (r.trust - 50)).coerceIn(5, 100)
        }
        val ok = Random.nextInt(100) < chance
        if (ok) {
            r.romanticStage = "pacaran"
            r.romance = 30
        } else {
            r.love = (r.love - 5).coerceAtLeast(0)
            r.trust = (r.trust - 3).coerceAtLeast(0)
        }
        return ok
    }

    fun toJson(): String = JSONObject().apply {
        put("rels", JSONObject().apply {
            rels.forEach { (id, r) ->
                put(id, JSONObject().apply {
                    put("met", r.met); put("love", r.love); put("romance", r.romance); put("trust", r.trust)
                    put("stage", r.romanticStage); put("gift", r.lastGiftDay); put("talk", r.lastTalkDay)
                    put("counters", JSONObject().apply { r.counters.forEach { (k, v) -> put(k, v) } })
                })
            }
        })
        put("lies", JSONObject().apply { lies.forEach { (k, v) -> put(k, JSONArray(v)) } })
    }.toString()

    companion object {
        fun fromJson(s: String?): SocialState {
            val st = SocialState()
            if (s.isNullOrEmpty()) return st
            try {
                val o = JSONObject(s)
                o.optJSONObject("rels")?.let { ro ->
                    ro.keys().forEach { id ->
                        val j = ro.getJSONObject(id)
                        val r = Rel(
                            met = j.optBoolean("met"), love = j.optInt("love"), romance = j.optInt("romance"),
                            trust = j.optInt("trust"), romanticStage = j.optString("stage"),
                            lastGiftDay = j.optInt("gift", -1), lastTalkDay = j.optInt("talk", -1),
                        )
                        j.optJSONObject("counters")?.let { c -> c.keys().forEach { k -> r.counters[k] = c.optInt(k) } }
                        st.rels[id] = r
                    }
                }
                o.optJSONObject("lies")?.let { lo ->
                    lo.keys().forEach { k ->
                        val a = lo.getJSONArray(k)
                        st.lies[k] = MutableList(a.length()) { i -> a.getString(i) }
                    }
                }
            } catch (_: Exception) {
            }
            return st
        }
    }
}
