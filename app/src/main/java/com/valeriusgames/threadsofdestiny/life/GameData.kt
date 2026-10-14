package com.valeriusgames.threadsofdestiny.life

import android.content.Context

/**
 * Pembaca file data di assets/data (aktivitas.txt, barang.txt).
 *
 * Format tiap entri:
 *   [id_entri]
 *   kunci = nilai
 * Baris kosong dan baris diawali # diabaikan.
 */
class GameData private constructor(
    val activities: List<ActivityDef>,
    val items: Map<String, ItemDef>,
    val characters: Map<String, CharacterDef>,
    val relations: List<RelationDef>,
    val topics: Map<String, TopicDef>,
) {

    data class CharacterDef(
        val id: String,
        val name: String,
        val gender: String,
        val baseAge: Int,
        val role: String,
        val loyal: Boolean,
        val romanceable: Boolean,
        val places: Set<String>,
        val slots: Set<String>,
        val meetable: Boolean,
        val loveStart: Int,
        val trustStart: Int,
        val likeTopics: Set<String>,
        val hateTopics: Set<String>,
        val likeItems: Set<String>,
        val hateItems: Set<String>,
        val text: Map<String, String>,
        val color: Int,
    )

    data class RelationDef(val a: String, val b: String, val kind: String, val strength: Int)

    data class TopicDef(val id: String, val name: String, val icon: String, val line: String)


    data class ActivityDef(
        val id: String,
        val name: String,
        val places: Set<String>,
        val slots: Set<String>,
        val effects: Map<String, Long>,
        val condition: String?,
        val text: String,
        val icon: String,
    )

    data class ItemDef(
        val id: String,
        val name: String,
        val price: Long,
        val shelfDays: Int,
        val places: Set<String>,
        val effects: Map<String, Long>,
        val icon: String,
    )

    companion object {
        @Volatile private var cached: GameData? = null

        fun get(ctx: Context): GameData = cached ?: load(ctx).also { cached = it }

        private fun load(ctx: Context): GameData {
            val acts = readSections(ctx, "data/aktivitas.txt").map { (id, m) ->
                ActivityDef(
                    id = id,
                    name = m["nama"] ?: id,
                    places = list(m["tempat"]).ifEmpty { setOf("semua") },
                    slots = list(m["waktu"]).ifEmpty { setOf("semua") },
                    effects = effects(m["efek"]),
                    condition = m["syarat"]?.takeIf { it.isNotBlank() },
                    text = m["teks"] ?: "",
                    icon = m["ikon"] ?: "•",
                )
            }
            val items = readSections(ctx, "data/barang.txt").associate { (id, m) ->
                id to ItemDef(
                    id = id,
                    name = m["nama"] ?: id,
                    price = m["harga"]?.filter { it.isDigit() }?.toLongOrNull() ?: 0L,
                    shelfDays = m["tahan"]?.trim()?.toIntOrNull() ?: 1,
                    places = list(m["tempat"]).ifEmpty { setOf("semua") },
                    effects = effects(m["efek"]),
                    icon = m["ikon"] ?: "•",
                )
            }
            val chars = readSections(ctx, "data/karakter.txt").associate { (id, m) ->
                id to CharacterDef(
                    id = id,
                    name = m["nama"] ?: id,
                    gender = (m["kelamin"] ?: "").lowercase(),
                    baseAge = m["usia"]?.trim()?.toIntOrNull() ?: 15,
                    role = m["peran"] ?: "",
                    loyal = (m["pandangan"] ?: "setia").lowercase() != "terbuka",
                    romanceable = (m["bisa_dipacari"] ?: "tidak").lowercase() == "ya",
                    places = list(m["tempat"]).ifEmpty { setOf("semua") },
                    slots = list(m["waktu"]).ifEmpty { setOf("semua") },
                    meetable = (m["ditemui"] ?: "ya").lowercase() != "tidak",
                    loveStart = m["cinta_awal"]?.trim()?.toIntOrNull() ?: 20,
                    trustStart = m["percaya_awal"]?.trim()?.toIntOrNull() ?: 40,
                    likeTopics = list(m["suka_topik"]),
                    hateTopics = list(m["benci_topik"]),
                    likeItems = list(m["suka_barang"]),
                    hateItems = list(m["benci_barang"]),
                    text = m,
                    color = runCatching { android.graphics.Color.parseColor(m["warna"]?.trim()) }
                        .getOrDefault(0xFF1E1E22.toInt()),
                )
            }
            val rels = readLines(ctx, "data/hubungan.txt").mapNotNull { line ->
                val r = Regex("^(\\w+)\\s*-\\s*(\\w+)\\s*=\\s*(\\w+)\\s*(\\d+)?$").find(line) ?: return@mapNotNull null
                RelationDef(
                    r.groupValues[1].lowercase(), r.groupValues[2].lowercase(),
                    r.groupValues[3].lowercase(), r.groupValues[4].toIntOrNull() ?: 50,
                )
            }
            val topics = readSections(ctx, "data/topik.txt").associate { (id, m) ->
                id to TopicDef(id, m["nama"] ?: id, m["ikon"] ?: "💬", m["kalimat"] ?: (m["nama"] ?: id))
            }
            return GameData(acts, items, chars, rels, topics)
        }

        private fun readLines(ctx: Context, path: String): List<String> = try {
            ctx.assets.open(path).bufferedReader().use { r ->
                r.readLines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
            }
        } catch (_: Exception) {
            emptyList()
        }

        private fun readSections(ctx: Context, path: String): List<Pair<String, Map<String, String>>> {
            val lines = try {
                ctx.assets.open(path).bufferedReader().use { it.readLines() }
            } catch (_: Exception) {
                return emptyList()
            }
            val out = mutableListOf<Pair<String, MutableMap<String, String>>>()
            for (raw in lines) {
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#")) continue
                if (line.startsWith("[") && line.endsWith("]")) {
                    out += line.substring(1, line.length - 1).trim() to mutableMapOf()
                } else if (line.contains('=') && out.isNotEmpty()) {
                    out.last().second[line.substringBefore('=').trim().lowercase()] =
                        line.substringAfter('=').trim()
                }
            }
            return out
        }

        private fun list(v: String?): Set<String> =
            v?.split(',')?.map { it.trim().lowercase() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

        /** "energi -20, uang +15000" → {energi=-20, uang=15000} */
        private fun effects(v: String?): Map<String, Long> {
            if (v.isNullOrBlank()) return emptyMap()
            return v.split(',').mapNotNull { part ->
                val m = Regex("^\\s*(\\w+)\\s*([+-]?\\d+)\\s*$").find(part.replace(".", "")) ?: return@mapNotNull null
                m.groupValues[1].lowercase() to m.groupValues[2].removePrefix("+").toLong()
            }.toMap()
        }
    }

    fun activitiesFor(place: String, slot: LifeState.Slot): List<ActivityDef> =
        activities.filter { a ->
            ("semua" in a.places || place.lowercase() in a.places) &&
                ("semua" in a.slots || slot.label.lowercase() in a.slots)
        }

    fun shopFor(place: String): List<ItemDef> =
        items.values.filter { "semua" in it.places || place.lowercase() in it.places }

    /** Cari karakter dari id atau nama tampilnya (dipakai untuk mencocokkan nama pembicara). */
    fun findCharacter(key: String): CharacterDef? {
        val k = key.trim().lowercase()
        return characters[k] ?: characters.values.firstOrNull { it.name.lowercase() == k }
    }

    fun relationsOf(id: String): List<Pair<String, RelationDef>> =
        relations.mapNotNull { r ->
            when (id) {
                r.a -> r.b to r
                r.b -> r.a to r
                else -> null
            }
        }
}
