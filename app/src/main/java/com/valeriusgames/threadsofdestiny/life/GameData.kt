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
    private val baseCharacters: Map<String, CharacterDef>,
    val relations: List<RelationDef>,
    val topics: Map<String, TopicDef>,
    val jobs: Map<String, JobDef>,
    val housing: Map<String, KosDef>,
    val documents: Map<String, DocDef>,
    val interview: List<InterviewQ>,
    val events: List<EventDef>,
) {

    /**
     * Karakter yang nama/jenis kelaminnya menyesuaikan pemain (kolom nama_l / nama_p,
     * kelamin = lawan). Diatur ulang lewat [applyPlayerGender].
     */
    var characters: Map<String, CharacterDef> = baseCharacters
        private set

    fun applyPlayerGender(playerGender: String) {
        val female = playerGender == "perempuan"
        characters = baseCharacters.mapValues { (_, c) ->
            val nl = c.text["nama_l"]
            val np = c.text["nama_p"]
            if (nl == null && np == null && c.gender != "lawan") return@mapValues c
            val g = if (c.gender == "lawan") (if (female) "laki-laki" else "perempuan") else c.gender
            val name = if (g == "perempuan") (np ?: c.name) else (nl ?: c.name)
            c.copy(name = name, gender = g)
        }
    }

    /** Pilihan dalam kejadian: teks tombol, efek, teks hasil. */
    data class EventOption(val label: String, val effects: Map<String, Long>, val result: String)

    /** Kejadian acak / rintangan (data/kejadian.txt). */
    data class EventDef(
        val id: String,
        val title: String,
        val icon: String,
        val places: Set<String>,
        val chance: Int,
        val condition: String?,
        val text: String,
        val effects: Map<String, Long>,
        val options: List<EventOption>,
        val onceOnly: Boolean,
        val note: String?,
    )

    data class DocDef(
        val id: String,
        val name: String,
        val cost: Long,
        val days: Int,
        val minAge: Int,
        val initial: Boolean,
        val desc: String,
    )

    data class InterviewAnswer(val text: String, val score: Int, val lie: Boolean)
    data class InterviewQ(val id: String, val question: String, val answers: List<InterviewAnswer>)


    /**
     * Kegiatan rutin: sekolah atau pekerjaan.
     * jenis: sekolah, tetap, paruh_waktu, harian
     */
    data class JobDef(
        val id: String,
        val name: String,
        val kind: String,
        val places: Set<String>,
        val days: Set<Int>,
        val slots: Set<String>,
        val pay: Long,
        val effects: Map<String, Long>,
        val condition: String?,
        val minAge: Int,
        val way: String,
        val difficulty: Int,
        val referrer: String?,
        val text: String,
        val icon: String,
        val desc: String,
        val documents: List<String>,
        val hasTest: Boolean,
        /** Sekolah: ujian setiap N hari (0 = tidak ada). */
        val examEvery: Int,
    ) {
        /** Sekolah & pekerjaan tetap wajib dihadiri; bolos/absen tercatat. */
        val mandatory: Boolean get() = kind == "sekolah" || kind == "tetap"
        val kindLabel: String
            get() = when (kind) {
                "sekolah" -> "Sekolah"
                "tetap" -> "Pekerjaan tetap"
                "paruh_waktu" -> "Paruh waktu"
                else -> "Pekerjaan harian"
            }
    }

    data class KosDef(
        val id: String,
        val name: String,
        val price: Long,
        val places: Set<String>,
        val sleepEnergy: Int,
        val sleepSanity: Int,
        val text: String,
    )

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
            val jobs = readSections(ctx, "data/pekerjaan.txt").associate { (id, m) ->
                id to JobDef(
                    id = id,
                    name = m["nama"] ?: id,
                    kind = (m["jenis"] ?: "harian").lowercase(),
                    places = list(m["tempat"]).ifEmpty { setOf("semua") },
                    days = days(m["hari"]),
                    slots = list(m["waktu"]).ifEmpty { setOf("semua") },
                    pay = m["gaji"]?.filter { it.isDigit() }?.toLongOrNull() ?: 0L,
                    effects = effects(m["efek"]),
                    condition = m["syarat"]?.takeIf { it.isNotBlank() },
                    minAge = m["usia_min"]?.trim()?.toIntOrNull() ?: 0,
                    way = (m["cara"] ?: "cerita").lowercase(),
                    difficulty = m["sulit"]?.trim()?.toIntOrNull() ?: 30,
                    referrer = m["orang_dalam"]?.trim()?.lowercase()?.takeIf { it.isNotEmpty() },
                    text = m["teks"] ?: "",
                    icon = m["ikon"] ?: "💼",
                    desc = m["keterangan"] ?: "",
                    documents = list(m["berkas"]).toList(),
                    hasTest = (m["tes"] ?: "tidak").trim().lowercase() == "ya",
                    examEvery = m["ujian_tiap"]?.trim()?.toIntOrNull() ?: 0,
                )
            }
            val kos = readSections(ctx, "data/kos.txt").associate { (id, m) ->
                id to KosDef(
                    id = id,
                    name = m["nama"] ?: id,
                    price = m["harga"]?.filter { it.isDigit() }?.toLongOrNull() ?: 0L,
                    places = list(m["tempat"]).ifEmpty { setOf("semua") },
                    sleepEnergy = m["tidur_energi"]?.trim()?.removePrefix("+")?.toIntOrNull() ?: 0,
                    sleepSanity = m["tidur_kewarasan"]?.trim()?.removePrefix("+")?.toIntOrNull() ?: 0,
                    text = m["keterangan"] ?: "",
                )
            }
            val docs = readSections(ctx, "data/dokumen.txt").associate { (id, m) ->
                id to DocDef(
                    id = id,
                    name = m["nama"] ?: id,
                    cost = m["biaya"]?.filter { it.isDigit() }?.toLongOrNull() ?: 0L,
                    days = m["lama"]?.trim()?.toIntOrNull() ?: 0,
                    minAge = m["usia_min"]?.trim()?.toIntOrNull() ?: 0,
                    initial = (m["awal"] ?: "").trim().lowercase() == "ya",
                    desc = m["keterangan"] ?: "",
                )
            }
            val questions = readSections(ctx, "data/wawancara.txt").mapNotNull { (id, m) ->
                val q = m["tanya"] ?: return@mapNotNull null
                val answers = listOf("a", "b", "c", "d").mapNotNull { k ->
                    val parts = m[k]?.split('|')?.map { it.trim() } ?: return@mapNotNull null
                    InterviewAnswer(
                        parts[0],
                        parts.getOrNull(1)?.removePrefix("+")?.toIntOrNull() ?: 0,
                        parts.getOrNull(2)?.lowercase() == "bohong",
                    )
                }
                if (answers.isEmpty()) null else InterviewQ(id, q, answers)
            }
            val events = readSections(ctx, "data/kejadian.txt").map { (id, m) ->
                val opts = listOf("pilih_a", "pilih_b", "pilih_c").mapNotNull { k ->
                    val parts = m[k]?.split('|')?.map { it.trim() } ?: return@mapNotNull null
                    EventOption(parts[0], effects(parts.getOrNull(1)), parts.getOrNull(2) ?: "")
                }
                EventDef(
                    id = id,
                    title = m["judul"] ?: id,
                    icon = m["ikon"] ?: "⚡",
                    places = list(m["tempat"]).ifEmpty { setOf("semua") },
                    chance = m["peluang"]?.trim()?.toIntOrNull() ?: 0,
                    condition = m["syarat"]?.takeIf { it.isNotBlank() },
                    text = m["teks"] ?: "",
                    effects = effects(m["efek"]),
                    options = opts,
                    onceOnly = (m["sekali"] ?: "tidak").trim().lowercase() == "ya",
                    note = m["catatan"]?.takeIf { it.isNotBlank() },
                )
            }
            return GameData(acts, items, chars, rels, topics, jobs, kos, docs, questions, events)
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

        private val DAY_NAMES = listOf("senin", "selasa", "rabu", "kamis", "jumat", "sabtu", "minggu")

        /** "senin-sabtu", "senin, rabu, jumat", "setiap hari" → indeks hari (0 = Senin). */
        private fun days(v: String?): Set<Int> {
            val t = v?.trim()?.lowercase() ?: return (0..6).toSet()
            if (t.isEmpty() || t == "setiap hari" || t == "semua") return (0..6).toSet()
            val out = mutableSetOf<Int>()
            t.split(',').map { it.trim() }.forEach { part ->
                if (part.contains('-')) {
                    val a = DAY_NAMES.indexOf(part.substringBefore('-').trim())
                    val b = DAY_NAMES.indexOf(part.substringAfter('-').trim())
                    if (a >= 0 && b >= 0) {
                        var i = a
                        while (true) {
                            out += i
                            if (i == b) break
                            i = (i + 1) % 7
                        }
                    }
                } else {
                    DAY_NAMES.indexOf(part).takeIf { it >= 0 }?.let { out += it }
                }
            }
            return out.ifEmpty { (0..6).toSet() }
        }

        private fun list(v: String?): Set<String> =
            v?.split(',')?.map { it.trim().lowercase() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

        /** "energi -20, uang +15000" → {energi=-20, uang=15000} */
        private fun effects(v: String?): Map<String, Long> {
            if (v.isNullOrBlank()) return emptyMap()
            return v.split(',').mapNotNull { part ->
                val m = Regex("^\\s*(\\w+)\\s*([+-]?\\d+)\\s*(%)?\\s*$").find(part.replace(".", "")) ?: return@mapNotNull null
                val key = m.groupValues[1].lowercase() + (if (m.groupValues[3] == "%") "_persen" else "")
                key to m.groupValues[2].removePrefix("+").toLong()
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

    /** Kegiatan yang dijadwalkan pada hari & waktu tertentu. */
    fun scheduled(job: JobDef, weekdayIndex: Int, slot: LifeState.Slot): Boolean =
        weekdayIndex in job.days && ("semua" in job.slots || slot.label.lowercase() in job.slots)
}
