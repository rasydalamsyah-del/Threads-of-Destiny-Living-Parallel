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
    val posts: List<PostDef>,
    val chats: List<ChatDef> = emptyList(),
    val messages: List<MessageDef> = emptyList(),
    /** Waktu asli tiap gambar latar (data/latar.txt): pagi / siang / sore / malam / tetap. */
    val backgroundTimes: Map<String, String> = emptyMap(),
    /** Lokasi yang bisa dijelajahi saat waktu bebas (data/lokasi.txt), kunci = nama file latar. */
    val locations: Map<String, LocationDef> = emptyMap(),
    /** Adegan cerita yang muncul saat waktu bebas (data/adegan.txt). */
    val scenes: List<SceneDef> = emptyList(),
) {

    /**
     * Adegan waktu bebas: dipicu saat pemain datang ke [location] ("masuk") atau menemui [person] ("orang"),
     * pada [schedule] (hari & jam) dan bila [condition] terpenuhi. Isinya ada di naskah/[file] mulai label [label],
     * diakhiri @kembali.
     */
    data class SceneDef(
        val id: String,
        val file: String,
        val label: String,
        val title: String,
        val trigger: String,
        val location: String?,
        val person: String?,
        val schedule: List<Triple<Set<Int>, Int, Int>>,
        val condition: String?,
        val once: Boolean,
    )

    data class Spot(val target: String, val label: String, val x: Float, val y: Float, val minutes: Int = 0)
    data class PersonSpot(val charId: String, val x: Float, val y: Float, val height: Float)
    data class LocationDef(
        val id: String,
        val name: String,
        val area: String,
        val doors: List<Spot>,
        val objects: List<Spot>,
        val people: List<PersonSpot>,
        val bed: Boolean,
        /** Gambar pengganti selama latar/<id>.jpg belum ada (mis. lapangan memakai gambar lain dulu). */
        val fallback: String? = null,
    )

    /**
     * Di lokasi mana karakter berada sekarang (kolom "lokasi" di karakter.txt), atau null.
     * Saat tanggal [tgl] (bulan×100 + tanggal) termasuk kolom "libur", dipakai "lokasi_libur" (mis. libur sekolah Laras).
     */
    fun characterLocation(c: CharacterDef, weekdayIndex: Int, minute: Int, tgl: Int = 0): String? {
        val alt = c.text["lokasi_libur"]
        if (!alt.isNullOrBlank() && tgl > 0 && inDateRange(c.text["libur"], tgl)) return locationAt(alt, weekdayIndex, minute)
        return locationAt(c.text["lokasi"], weekdayIndex, minute)
    }

    /**
     * Tempat karakter TIDUR sekarang (kolom "tidur", saat libur "tidur_libur"), atau null.
     * Orang yang tidur tampil di latar (gambar latar/<lokasi>__<id>_tidur.jpg) tapi tidak bisa diajak bicara.
     */
    fun sleepLocation(c: CharacterDef, weekdayIndex: Int, minute: Int, tgl: Int = 0): String? {
        val alt = c.text["tidur_libur"]
        if (!alt.isNullOrBlank() && tgl > 0 && inDateRange(c.text["libur"], tgl)) return locationAt(alt, weekdayIndex, minute)
        return locationAt(c.text["tidur"], weekdayIndex, minute)
    }

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

    /**
     * Obrolan saat menemui seseorang (data/obrolan.txt).
     * kind: santai / kenal / khusus. lines: jawaban (diawali "*" = narasi).
     */
    data class ChatDef(
        val id: String,
        val charId: String,
        val kind: String,
        val title: String,
        val say: String,
        val lines: List<String>,
        val effects: Map<String, Long>,
        val topic: String?,
        val needLove: Int,
        val needTrust: Int,
        val refuse: String?,
        val reveal: String?,
        val revealOther: Pair<String, String>?,
        val condition: String?,
        val once: Boolean,
    )

    /** Pesan chat & pilihan balasannya (data/pesan.txt). */
    data class MessageDef(
        val id: String,
        val from: String,
        val kind: String,
        val text: String,
        val warm: String,
        val warmReply: String,
        val short: String,
        val shortReply: String,
        val condition: String?,
    )

    /** Pesan dari naskah: dicocokkan lewat pengirim + isi (teks mentah sebelum {nama} diisi). */
    fun storyMessage(charId: String, rawText: String): MessageDef? =
        messages.firstOrNull { (it.from == charId || it.from == "*") && it.text.trim() == rawText.trim() }

    fun message(id: String?): MessageDef? = id?.let { i -> messages.firstOrNull { it.id == i } }

    fun randomMessages(charId: String): List<MessageDef> = messages.filter { it.kind == "acak" && it.from == charId }

    fun chatsOf(charId: String, kind: String): List<ChatDef> = chats.filter { it.charId == charId && it.kind == kind }

    /** Pilihan dalam kejadian: teks tombol, efek, teks hasil. */
    data class EventOption(val label: String, val effects: Map<String, Long>, val result: String)

    /** Kejadian acak / rintangan (data/kejadian.txt). */
    /** Unggahan teman di Sosmed (jenis = unggahan) atau hasil pencarian sahabat (jenis = cari). */
    data class PostDef(
        val id: String,
        val search: Boolean,
        val author: String?,
        val text: String,
        val minDay: Int,
        val need: Int,
        val condition: String?,
        val note: String?,
        val clue: Int,
    )

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
        /** Jam mulai & selesai (menit sejak tengah malam). */
        val startMin: Int,
        val endMin: Int,
    ) {
        val hoursText: String get() = LifeState.clock(startMin) + "–" + (if (endMin >= 1440) "24:00" else LifeState.clock(endMin))
        val lengthMin: Int get() = endMin - startMin
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
        /** Lama kegiatan dalam menit. */
        val minutes: Int,
        /** Jam boleh MULAI (menit). null = mengikuti bagian hari. Bisa melewati tengah malam (19:00-02:00). */
        val startFrom: Int? = null,
        val startUntil: Int? = null,
        /** Paling banyak berapa kali per hari (null = tanpa batas). */
        val maxPerDay: Int? = null,
        /** Jeda minimal sebelum boleh diulang, dalam menit. */
        val cooldown: Int? = null,
        /** Teks bila diulang di hari yang sama (mis. Ibu menegur karena tidur lagi). */
        val repeatText: String? = null,
        /** Lokasi (lokasi.txt) tempat aktivitas dilakukan; kosong = di mana saja. "kos" = kamar kos. */
        val locations: Set<String> = emptySet(),
        /** Semua jendela jam boleh mulai (kolom "mulai" boleh berisi beberapa: 05:00-06:15, 17:15-17:45). */
        val windows: List<Pair<Int, Int>> = emptyList(),
        /** Hanya bisa dilakukan saat karakter ini ada di lokasi yang sama (kolom "bersama"). */
        val with: String? = null,
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
        /** Untuk unit test: data kosong. */
        internal fun forTest(activities: List<ActivityDef> = emptyList()): GameData = GameData(activities, emptyMap(), emptyMap(), emptyList(), emptyMap(),
            emptyMap(), emptyMap(), emptyMap(), emptyList(), emptyList(), emptyList())

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
                    minutes = minutes(m["durasi"]) ?: 60,
                    startFrom = windows(m["mulai"]).firstOrNull()?.first,
                    startUntil = windows(m["mulai"]).firstOrNull()?.second,
                    windows = windows(m["mulai"]),
                    with = m["bersama"]?.trim()?.lowercase()?.takeIf { it.isNotEmpty() },
                    maxPerDay = m["batas"]?.filter { it.isDigit() }?.toIntOrNull(),
                    cooldown = minutes(m["jeda"]),
                    repeatText = m["teks_ulang"]?.takeIf { it.isNotBlank() },
                    locations = list(m["lokasi"]),
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
                    startMin = hours(m["jam"], list(m["waktu"]))?.first ?: 7 * 60,
                    endMin = hours(m["jam"], list(m["waktu"]))?.second ?: 11 * 60,
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
            val posts = readSections(ctx, "data/sosmed.txt").map { (id, m) ->
                PostDef(
                    id = id,
                    search = (m["jenis"] ?: "").trim().lowercase() == "cari",
                    author = m["penulis"]?.trim()?.takeIf { it.isNotEmpty() },
                    text = m["isi"] ?: "",
                    minDay = m["hari"]?.trim()?.toIntOrNull() ?: 0,
                    need = m["butuh"]?.trim()?.toIntOrNull() ?: 1,
                    condition = m["syarat"]?.takeIf { it.isNotBlank() },
                    note = m["catatan"]?.takeIf { it.isNotBlank() },
                    clue = m["petunjuk"]?.trim()?.toIntOrNull() ?: 0,
                )
            }
            val chats = readSections(ctx, "data/obrolan.txt").map { (id, m) -> chat(id, m) }
            val msgs = readSections(ctx, "data/pesan.txt").map { (id, m) -> message(id, m) }
            val bgTimes = readSections(ctx, "data/latar.txt").associate { (id, m) -> id to (m["waktu"] ?: "tetap").trim().lowercase() }
            val locs = readSections(ctx, "data/lokasi.txt").associate { (id, m) -> id to location(id, m) }
            val scenes = readSections(ctx, "data/adegan.txt").map { (id, m) -> scene(id, m) }
            return GameData(acts, items, chars, rels, topics, jobs, kos, docs, questions, events, posts, chats, msgs, bgTimes, locs, scenes)
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

        /** "45 menit", "2 jam", "1,5 jam", "1 jam 30 menit", "90" → menit. */
        internal fun minutes(v: String?): Int? {
            val t = v?.trim()?.lowercase()?.replace(',', '.') ?: return null
            if (t.isEmpty()) return null
            t.toIntOrNull()?.let { return it }
            var total = 0f
            Regex("([\\d.]+)\\s*(jam|menit)").findAll(t).forEach { m ->
                val n = m.groupValues[1].toFloatOrNull() ?: 0f
                total += if (m.groupValues[2] == "jam") n * 60 else n
            }
            return if (total > 0) total.toInt() else null
        }

        /** "senin-sabtu 13:00-20:00, minggu 07:00-20:00" → daftar (hari, mulai, selesai). */
        /** Inti [meetableNow] (internal agar bisa diuji tanpa CharacterDef). */
        internal fun meetable(ditemui: String?, jadwal: String?, slots: Set<String>, weekdayIndex: Int, minute: Int): Boolean {
            val mode = (ditemui ?: "ya").trim().lowercase()
            if (mode == "tidak" || mode == "janji") return false
            val sched = schedule(jadwal)
            if (sched.isEmpty()) return "semua" in slots || LifeState.Slot.of(minute).label.lowercase() in slots
            return sched.any { (days, from, to) ->
                if (to < from) (weekdayIndex in days && minute >= from) || (((weekdayIndex + 6) % 7) in days && minute < to)
                else weekdayIndex in days && minute in from until to
            }
        }

        internal fun schedule(v: String?): List<Triple<Set<Int>, Int, Int>> {
            if (v.isNullOrBlank()) return emptyList()
            return v.split(',').mapNotNull { part ->
                val m = Regex("^(.*?)\\s*(\\d{1,2}[:.]\\d{2}\\s*[-–]\\s*\\d{1,2}[:.]\\d{2})\\s*$").find(part.trim()) ?: return@mapNotNull null
                val w = window(m.groupValues[2].replace(" ", "")) ?: return@mapNotNull null
                Triple(days(m.groupValues[1].trim().ifEmpty { null }), w.first, w.second)
            }
        }

        /** "04:00-06:00" → (240, 360). Boleh melewati tengah malam: "19:00-02:00" → (1140, 120). */
        internal fun window(v: String?): Pair<Int, Int>? {
            val p = v?.split('-', '–')?.map { it.trim() }?.takeIf { it.size == 2 } ?: return null
            val a = LifeState.parseClock(p[0]) ?: return null
            val b = LifeState.parseClock(p[1]) ?: return null
            return (a % 1440) to (b % 1440)
        }

        /** "05:00-06:15, 17:15-17:45" → beberapa jendela jam. */
        internal fun windows(v: String?): List<Pair<Int, Int>> =
            v.orEmpty().split(',').mapNotNull { window(it.trim()) }

        /** "07:00-14:00" → (420, 840). Tanpa jam: dari bagian hari (pagi, siang, sore, malam). */
        internal fun hours(v: String?, slots: Set<String>): Pair<Int, Int>? {
            v?.split('-', '–')?.map { it.trim() }?.takeIf { it.size == 2 }?.let { (a, b) ->
                val s = LifeState.parseClock(a); val e = LifeState.parseClock(b)
                if (s != null && e != null && e > s) return s to e
            }
            val ranges = mapOf("pagi" to (7 * 60 to 11 * 60), "siang" to (11 * 60 to 15 * 60),
                "sore" to (15 * 60 to 18 * 60), "malam" to (18 * 60 to 22 * 60))
            val picked = slots.mapNotNull { ranges[it] }
            if (picked.isEmpty()) return null
            return picked.minOf { it.first } to picked.maxOf { it.second }
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

        /** Satu entri lokasi.txt → [LocationDef]. */
        internal fun location(id: String, m: Map<String, String>): LocationDef {
            fun xy(v: String): Pair<Float, Float>? {
                val p = v.split(',').map { it.trim().toFloatOrNull() }
                return if (p.size >= 2 && p[0] != null && p[1] != null) p[0]!! to p[1]!! else null
            }
            fun spots(v: String?, withMinutes: Boolean) = v.orEmpty().split(';').mapNotNull { part ->
                val f = part.split('|').map { it.trim() }
                if (f.size < 3) return@mapNotNull null
                val (x, y) = xy(f[2]) ?: return@mapNotNull null
                Spot(f[0], f[1], x, y, if (withMinutes) f.getOrNull(3)?.toIntOrNull() ?: 0 else 0)
            }
            val people = m["orang"].orEmpty().split(';').mapNotNull { part ->
                val t = part.trim()
                val idc = t.substringBefore(' ').trim().lowercase()
                val n = t.substringAfter(' ', "").split(',').map { it.trim().toFloatOrNull() }
                if (idc.isEmpty() || n.size < 3 || n.any { it == null }) null else PersonSpot(idc, n[0]!!, n[1]!!, n[2]!!)
            }
            return LocationDef(
                id = id,
                name = m["nama"] ?: id,
                area = (m["tempat"] ?: "desa").trim().lowercase(),
                doors = spots(m["pintu"], true),
                objects = spots(m["benda"], false),
                people = people,
                bed = (m["tidur"] ?: "").trim().lowercase() == "ya",
                fallback = m["cadangan"]?.trim()?.takeIf { it.isNotEmpty() },
            )
        }

        /**
         * "setiap hari 12:00-13:30 ruang_makan, senin-sabtu 07:00-16:00 gudang_ikan" → lokasi pada hari & jam itu.
         * Jendela boleh melewati tengah malam.
         */
        internal fun locationAt(spec: String?, weekdayIndex: Int, minute: Int): String? {
            if (spec.isNullOrBlank()) return null
            spec.split(',').forEach { part ->
                val m = Regex("^(.*?)\\s*(\\d{1,2}[:.]\\d{2}\\s*[-–]\\s*\\d{1,2}[:.]\\d{2})\\s+(\\w+)\\s*$").find(part.trim()) ?: return@forEach
                val w = window(m.groupValues[2].replace(" ", "")) ?: return@forEach
                val days = days(m.groupValues[1].trim().ifEmpty { null })
                val (from, to) = w
                val inside = if (to < from) (weekdayIndex in days && minute >= from) || (((weekdayIndex + 6) % 7) in days && minute < to)
                    else weekdayIndex in days && minute in from until to
                if (inside) return m.groupValues[3]
            }
            return null
        }

        /** "627-712, 1221-104" (bulan×100+tanggal, boleh melewati tahun baru) → apakah [tgl] termasuk. */
        internal fun inDateRange(spec: String?, tgl: Int): Boolean {
            if (spec.isNullOrBlank()) return false
            return spec.split(',').any { part ->
                val p = part.trim().split('-').map { it.trim().toIntOrNull() }
                if (p.size != 2 || p[0] == null || p[1] == null) return@any false
                val a = p[0]!!; val b = p[1]!!
                if (a <= b) tgl in a..b else tgl >= a || tgl <= b
            }
        }

        /** Satu entri adegan.txt → [SceneDef]. */
        internal fun scene(id: String, m: Map<String, String>) = SceneDef(
            id = id,
            file = (m["naskah"] ?: "").trim(),
            label = (m["label"] ?: id).trim(),
            title = m["judul"] ?: id,
            trigger = (m["pemicu"] ?: if (m["orang"] != null) "orang" else "masuk").trim().lowercase(),
            location = m["lokasi"]?.trim()?.takeIf { it.isNotEmpty() },
            person = m["orang"]?.trim()?.lowercase()?.takeIf { it.isNotEmpty() },
            schedule = schedule(m["waktu"]),
            condition = m["syarat"]?.takeIf { it.isNotBlank() },
            once = (m["sekali"] ?: "ya").trim().lowercase() != "tidak",
        )

        /** Apakah sekarang termasuk jadwal (kosong = kapan saja). */
        internal fun inSchedule(sched: List<Triple<Set<Int>, Int, Int>>, weekdayIndex: Int, minute: Int): Boolean =
            sched.isEmpty() || sched.any { (days, from, to) ->
                if (to < from) (weekdayIndex in days && minute >= from) || (((weekdayIndex + 6) % 7) in days && minute < to)
                else weekdayIndex in days && minute in from until to
            }

        /** Satu entri pesan.txt → [MessageDef]. */
        internal fun message(id: String, m: Map<String, String>) = MessageDef(
            id = id,
            from = (m["dari"] ?: "*").trim().lowercase(),
            kind = (m["jenis"] ?: "acak").trim().lowercase(),
            text = m["isi"] ?: "",
            warm = m["hangat"] ?: "",
            warmReply = m["balas_hangat"] ?: "",
            short = m["singkat"] ?: "",
            shortReply = m["balas_singkat"] ?: "",
            condition = m["syarat"]?.takeIf { it.isNotBlank() },
        )

        /**
         * Kegiatan yang sedang tidak boleh diulang: sudah mencapai batas hari ini, atau jedanya belum lewat.
         * Hasil: alasan (untuk ditampilkan redup) atau null bila boleh.
         */
        internal fun repeatBlock(maxPerDay: Int?, cooldown: Int?, usedToday: Int, lastAbs: Long?, nowAbs: Long): String? {
            if (maxPerDay != null && usedToday >= maxPerDay)
                return if (maxPerDay == 1) "Sudah dilakukan hari ini — besok lagi" else "Sudah ${usedToday}× hari ini (paling banyak ${maxPerDay}×) — besok lagi"
            if (cooldown != null && lastAbs != null && nowAbs - lastAbs < cooldown) {
                val at = ((lastAbs + cooldown) % 1440).toInt()
                return "Baru saja dilakukan — bisa lagi pukul ${LifeState.clock(at)}"
            }
            return null
        }

        /** Satu entri obrolan.txt → [ChatDef]. internal agar bisa diuji. */
        internal fun chat(id: String, m: Map<String, String>): ChatDef {
            val kind = (m["jenis"] ?: "santai").trim().lowercase()
            val other = m["ungkap_lain"]?.trim()?.split(Regex("\\s+"))?.takeIf { it.size == 2 }
            return ChatDef(
                id = id,
                charId = (m["karakter"] ?: "").trim().lowercase(),
                kind = kind,
                title = m["judul"] ?: id,
                say = m["kamu"] ?: "",
                lines = (m["jawab"] ?: "...").split("//").map { it.trim() }.filter { it.isNotEmpty() },
                effects = effects(m["efek"]),
                topic = m["topik"]?.trim()?.lowercase()?.takeIf { it.isNotEmpty() },
                needLove = m["akrab"]?.trim()?.toIntOrNull() ?: 0,
                needTrust = m["percaya"]?.trim()?.toIntOrNull() ?: 0,
                refuse = m["tolak"]?.takeIf { it.isNotBlank() },
                reveal = m["ungkap"]?.trim()?.takeIf { it.isNotEmpty() },
                revealOther = other?.let { it[0].lowercase() to it[1] },
                condition = m["syarat"]?.takeIf { it.isNotBlank() },
                once = when ((m["sekali"] ?: "").trim().lowercase()) {
                    "ya" -> true
                    "tidak" -> false
                    else -> kind != "santai"
                },
            )
        }

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

    /**
     * Bisa ditemui langsung sekarang? (daftar "Temui seseorang")
     * ditemui = tidak → tidak pernah · ditemui = janji → hanya lewat janji di Chat ·
     * jadwal = "senin-sabtu 13:00-20:00, minggu 07:00-20:00" → sesuai jadwal · tanpa jadwal → bagian hari ("waktu").
     */
    fun meetableNow(c: CharacterDef, weekdayIndex: Int, minute: Int, tgl: Int = 0): Boolean {
        // karakter dengan kolom "lokasi": bisa ditemui tepat saat ia berada di salah satu lokasinya
        if (!c.text["lokasi"].isNullOrBlank()) {
            val mode = (c.text["ditemui"] ?: "ya").trim().lowercase()
            return mode != "tidak" && characterLocation(c, weekdayIndex, minute, tgl) != null
        }
        return meetable(c.text["ditemui"], c.text["jadwal"], c.slots, weekdayIndex, minute)
    }

    /** Hanya bisa ditemui lewat janji (Chat → Ajak ketemu). */
    fun meetByAppointmentOnly(c: CharacterDef): Boolean = (c.text["ditemui"] ?: "").trim().lowercase() == "janji"

    /** Semua aktivitas di tempat ini (dipakai untuk menampilkan yang belum bisa, redup). */
    fun activitiesAt(place: String): List<ActivityDef> =
        activities.filter { a -> "semua" in a.places || place.lowercase() in a.places }

    /**
     * Bisakah aktivitas dimulai sekarang? null = bisa. Selain itu: alasan untuk ditampilkan (redup).
     * Bila ada jam "mulai", itu yang dipakai; kalau tidak, bagian hari ("waktu").
     */
    fun activityBlockedReason(a: ActivityDef, minute: Int): String? {
        if (a.windows.size > 1) {
            if (a.windows.any { (f, u) -> if (u < f) minute >= f || minute <= u else minute in f..u }) return null
            val next = a.windows.map { it.first }.filter { it > minute }.minOrNull()
            val all = a.windows.joinToString(" / ") { LifeState.clock(it.first) + "–" + LifeState.clock(it.second) }
            return if (next != null) "Belum bisa. Mulai pukul ${LifeState.clock(next)} (jamnya: $all)" else "Hanya bisa dimulai pukul $all"
        }
        val from = a.startFrom
        val until = a.startUntil
        if (from != null && until != null) {
            val wraps = until < from
            val inWindow = if (wraps) minute >= from || minute <= until else minute in from..until
            if (inWindow) return null
            val range = LifeState.clock(from) + "–" + LifeState.clock(until)
            return when {
                wraps -> "Hanya bisa dimulai pukul $range"
                minute < from && minute >= 4 * 60 -> "Belum bisa. Mulai pukul ${LifeState.clock(from)}"
                minute < 4 * 60 && from >= 4 * 60 -> "Belum bisa. Mulai pukul ${LifeState.clock(from)}"
                else -> "Sudah terlalu ${if (minute in 4 * 60 until 18 * 60) "siang" else "malam"} untuk " +
                    a.name.replaceFirstChar { it.lowercase() } + " (paling lambat ${LifeState.clock(until)})"
            }
        }
        if ("semua" in a.slots || LifeState.Slot.of(minute).label.lowercase() in a.slots) return null
        val labels = LifeState.Slot.values().filter { it.label.lowercase() in a.slots }
            .joinToString(" / ") { "${it.label} (${LifeState.clock(it.start)}–${LifeState.clock(it.end % 1440)})" }
        return "Hanya bisa saat $labels"
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
    /** Pekerjaan dijadwalkan pada hari itu. */
    fun scheduledOn(job: JobDef, weekdayIndex: Int): Boolean = weekdayIndex in job.days

    /** Bisa berangkat sekarang: mulai 1 jam sebelum jam masuk, paling lambat 1 jam sebelum selesai. */
    fun canAttendNow(job: JobDef, weekdayIndex: Int, minute: Int): Boolean =
        scheduledOn(job, weekdayIndex) && minute >= job.startMin - 60 && minute <= job.endMin - 60
}
