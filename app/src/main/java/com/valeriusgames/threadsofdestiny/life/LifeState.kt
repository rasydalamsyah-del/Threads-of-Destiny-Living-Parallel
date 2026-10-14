package com.valeriusgames.threadsofdestiny.life

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import kotlin.random.Random

/**
 * Keadaan hidup tokoh utama: waktu, kebutuhan dasar, uang, dan isi tas.
 * Semua bar 0–100, makin penuh makin baik.
 */
class LifeState {

    enum class Slot(val label: String, val icon: String) {
        PAGI("Pagi", "☀️"), SIANG("Siang", "🌤️"), SORE("Sore", "🌇"), MALAM("Malam", "🌙")
    }

    enum class Stat(val key: String, val label: String, val icon: String) {
        KESEHATAN("kesehatan", "Kesehatan", "❤️"),
        KENYANG("kenyang", "Kenyang", "🍚"),
        HIDRASI("hidrasi", "Hidrasi", "💧"),
        KEWARASAN("kewarasan", "Kewarasan", "🧠"),
        ENERGI("energi", "Energi", "⚡");

        companion object {
            fun of(key: String): Stat? = values().firstOrNull { it.key == key.lowercase() }
        }
    }

    data class Item(val id: String, val boughtDay: Int, val expiresDay: Int)

    /** Kegiatan yang sedang dijalani pemain (sekolah / pekerjaan). */
    data class Job(
        val id: String,
        val startDay: Int,
        var performance: Int = 50,
        var absences: Int = 0,
        var shifts: Int = 0,
    )

    companion object {
        val WEEKDAYS = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
        val MONTHS = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember",
        )

        /** "6 Juli 2026" → epoch day, atau null bila tidak terbaca. */
        fun parseDate(text: String): Long? {
            val p = text.trim().split(Regex("\\s+"))
            if (p.size < 3) return null
            val d = p[0].toIntOrNull() ?: return null
            val m = MONTHS.indexOfFirst { it.equals(p[1], true) || it.take(3).equals(p[1].take(3), true) } + 1
            val y = p[2].toIntOrNull() ?: return null
            if (m <= 0) return null
            return runCatching { LocalDate.of(y, m, d).toEpochDay() }.getOrNull()
        }

        /** Penurunan tiap pergantian waktu (saat terjaga). */
        private const val DECAY_KENYANG = 10
        private const val DECAY_HIDRASI = 14
        private const val DECAY_ENERGI = 5

        fun fromJson(s: String?): LifeState {
            val st = LifeState()
            if (s.isNullOrEmpty()) return st
            try {
                val o = JSONObject(s)
                st.active = o.optBoolean("active")
                st.day = o.optInt("day", 0)
                st.startWeekday = o.optInt("startWeekday", 0)
                st.slot = runCatching { Slot.valueOf(o.optString("slot")) }.getOrDefault(Slot.PAGI)
                st.money = o.optLong("money", 0)
                st.dead = o.optBoolean("dead")
                st.deathReason = o.optString("deathReason")
                st.startEpochDay = o.optLong("startEpochDay", 0L)
                st.kosId = o.optString("kosId")
                st.rent = o.optLong("rent")
                st.rentDueDay = o.optInt("rentDueDay", -1)
                st.arrears = o.optInt("arrears")
                st.sleepEnergy = o.optInt("sleepEnergy")
                st.sleepSanity = o.optInt("sleepSanity")
                st.allowance = o.optLong("allowance")
                st.allowanceNextDay = o.optInt("allowanceNextDay", -1)
                st.attended = o.optString("attended")
                val jobs = o.optJSONArray("jobs") ?: JSONArray()
                for (i in 0 until jobs.length()) {
                    val j = jobs.getJSONObject(i)
                    st.jobs += Job(j.getString("id"), j.optInt("start"), j.optInt("perf", 50), j.optInt("abs"), j.optInt("shifts"))
                }
                val stats = o.optJSONObject("stats")
                Stat.values().forEach { k -> stats?.let { st.stats[k] = it.optInt(k.key, st.stats[k] ?: 0) } }
                val inv = o.optJSONArray("items") ?: JSONArray()
                for (i in 0 until inv.length()) {
                    val it = inv.getJSONObject(i)
                    st.items += Item(it.getString("id"), it.optInt("bought"), it.optInt("expires"))
                }
            } catch (_: Exception) {
            }
            return st
        }
    }

    /** Sistem kehidupan aktif (bar & jam tampil, waktu berjalan). */
    var active = false
    var day = 0
    var startWeekday = 0
    var slot = Slot.PAGI
    var money = 0L
    var dead = false
    var deathReason = ""

    // ---- kalender bertanggal ----
    /** Tanggal hari pertama (epoch day). 0 = belum diatur, pakai hari tanpa tanggal. */
    var startEpochDay = 0L

    // ---- tempat tinggal & keuangan rutin ----
    var kosId = ""
    var rent = 0L
    var rentDueDay = -1
    var arrears = 0
    /** Pengaruh tempat tidur terhadap energi & kewarasan saat tidur. */
    var sleepEnergy = 0
    var sleepSanity = 0
    var allowance = 0L
    var allowanceNextDay = -1

    // ---- sekolah & pekerjaan ----
    val jobs = mutableListOf<Job>()
    /** Penanda kegiatan yang sudah dihadiri: "hari:slot:id;..." */
    var attended = ""

    fun date(): LocalDate? = if (startEpochDay != 0L) LocalDate.ofEpochDay(startEpochDay + day) else null

    /** Indeks hari dalam seminggu, 0 = Senin. */
    val weekdayIndex: Int
        get() = date()?.let { it.dayOfWeek.value - 1 } ?: ((startWeekday + day) % 7)

    val dateText: String
        get() = date()?.let { "${it.dayOfMonth} ${MONTHS[it.monthValue - 1]} ${it.year}" } ?: "Hari ke-${day + 1}"

    fun attendKey(jobId: String) = "$day:${slot.name}:$jobId"
    fun hasAttended(jobId: String) = attended.split(';').contains(attendKey(jobId))
    fun markAttended(jobId: String) {
        attended = (attended.split(';').filter { it.startsWith("$day:") } + attendKey(jobId)).joinToString(";")
    }

    val stats: MutableMap<Stat, Int> = mutableMapOf(
        Stat.KESEHATAN to 100,
        Stat.KENYANG to 70,
        Stat.HIDRASI to 70,
        Stat.KEWARASAN to 80,
        Stat.ENERGI to 90,
    )
    val items = mutableListOf<Item>()

    operator fun get(s: Stat): Int = stats[s] ?: 0

    fun change(s: Stat, delta: Int) {
        stats[s] = ((stats[s] ?: 0) + delta).coerceIn(0, 100)
    }

    val weekday: String get() = WEEKDAYS[weekdayIndex]
    val clockText: String
        get() = date()?.let { "$weekday, ${it.dayOfMonth} ${MONTHS[it.monthValue - 1].take(3)} · ${slot.label} ${slot.icon}" }
            ?: "$weekday · ${slot.label} ${slot.icon}"

    fun setCalendar(weekdayName: String, slotName: String) {
        val wd = WEEKDAYS.indexOfFirst { it.equals(weekdayName, true) }
        if (wd >= 0) startWeekday = ((wd - day) % 7 + 7) % 7
        Slot.values().firstOrNull { it.label.equals(slotName, true) }?.let { slot = it }
    }

    /**
     * Majukan waktu satu slot. Malam → Pagi berarti tidur.
     * Mengembalikan daftar peringatan baru (mis. "Kamu mulai lapar").
     */
    fun advance(): List<String> {
        val before = stats.toMap()
        if (slot == Slot.MALAM) {
            sleep()
        } else {
            slot = Slot.values()[slot.ordinal + 1]
            change(Stat.KENYANG, -DECAY_KENYANG)
            change(Stat.HIDRASI, -DECAY_HIDRASI)
            change(Stat.ENERGI, -DECAY_ENERGI)
            needsEffect()
        }
        return warnings(before)
    }

    private fun sleep() {
        day++
        slot = Slot.PAGI
        change(Stat.KENYANG, -15)
        change(Stat.HIDRASI, -15)
        val wellFed = this[Stat.KENYANG] >= 20 && this[Stat.HIDRASI] >= 20
        change(Stat.ENERGI, (if (wellFed) 80 else 45) + sleepEnergy)
        change(Stat.KEWARASAN, (if (wellFed) 5 else -3) + sleepSanity)
        needsEffect()
    }

    /** Akibat kebutuhan dasar terhadap kesehatan & kewarasan. */
    private fun needsEffect() {
        val k = this[Stat.KENYANG]
        val h = this[Stat.HIDRASI]
        val e = this[Stat.ENERGI]
        var dmg = 0
        if (k == 0) dmg += 10
        if (h == 0) dmg += 15
        if (e == 0) dmg += 5
        if (dmg > 0) change(Stat.KESEHATAN, -dmg)
        else if (k >= 50 && h >= 50 && e >= 30) change(Stat.KESEHATAN, 2)

        if (k < 25 || h < 25 || e < 20) change(Stat.KEWARASAN, -3)

        if (this[Stat.KESEHATAN] <= 0) {
            dead = true
            deathReason = when {
                h == 0 -> "dehidrasi"
                k == 0 -> "kelaparan"
                else -> "kelelahan"
            }
        }
    }

    private fun warnings(before: Map<Stat, Int>): List<String> {
        val out = mutableListOf<String>()
        fun crossed(s: Stat, limit: Int) = (before[s] ?: 0) >= limit && this[s] < limit
        if (crossed(Stat.KENYANG, 25)) out += "Perutmu mulai keroncongan. Kamu lapar."
        if (crossed(Stat.HIDRASI, 25)) out += "Tenggorokanmu kering. Kamu haus."
        if (crossed(Stat.ENERGI, 20)) out += "Badanmu lelah sekali."
        if (crossed(Stat.KEWARASAN, 30)) out += "Pikiranmu terasa berat dan kalut."
        if (crossed(Stat.KESEHATAN, 40)) out += "Tubuhmu mulai lemah. Jaga makan, minum, dan istirahat!"
        if (crossed(Stat.KESEHATAN, 15)) out += "Kondisimu kritis! Kamu bisa meninggal kalau begini terus."
        if (this[Stat.KENYANG] == 0 && (before[Stat.KENYANG] ?: 0) > 0) out += "Kamu kelaparan. Kesehatanmu terus turun."
        if (this[Stat.HIDRASI] == 0 && (before[Stat.HIDRASI] ?: 0) > 0) out += "Kamu dehidrasi. Kesehatanmu turun cepat."
        return out
    }

    /** Terapkan efek (mis. dari aktivitas / makanan). Kunci lain dikembalikan untuk variabel cerita. */
    fun applyEffects(effects: Map<String, Long>): Map<String, Long> {
        val rest = mutableMapOf<String, Long>()
        effects.forEach { (k, v) ->
            val stat = Stat.of(k)
            when {
                stat != null -> change(stat, v.toInt())
                k == "uang" -> money = (money + v).coerceAtLeast(0)
                k == "uang_persen" -> money = (money + money * v / 100).coerceAtLeast(0)
                else -> rest[k] = v
            }
        }
        if (this[Stat.KESEHATAN] <= 0 && !dead) {
            dead = true
            deathReason = "sakit"
        }
        return rest
    }

    fun addItem(id: String, shelfDays: Int) {
        items += Item(id, day, day + shelfDays)
    }

    fun isExpired(item: Item): Boolean = day > item.expiresDay

    /**
     * Makan/minum barang. Makanan basi: 70% kemungkinan sakit.
     * Mengembalikan teks hasil.
     */
    fun consume(item: Item, data: GameData.ItemDef): String {
        items.remove(item)
        if (isExpired(item) && Random.nextInt(100) < 70) {
            applyEffects(mapOf("kesehatan" to -20L, "kewarasan" to -6L, "kenyang" to (data.effects["kenyang"] ?: 0L) / 3))
            return "${data.name} itu sudah basi... Perutmu sakit melilit."
        }
        applyEffects(data.effects)
        return if (data.effects.containsKey("hidrasi") && (data.effects["kenyang"] ?: 0L) <= 0L)
            "Kamu meminum ${data.name}." else "Kamu menyantap ${data.name}."
    }

    fun toJson(): String = JSONObject().apply {
        put("active", active)
        put("day", day)
        put("startWeekday", startWeekday)
        put("slot", slot.name)
        put("money", money)
        put("dead", dead)
        put("deathReason", deathReason)
        put("startEpochDay", startEpochDay)
        put("kosId", kosId)
        put("rent", rent)
        put("rentDueDay", rentDueDay)
        put("arrears", arrears)
        put("sleepEnergy", sleepEnergy)
        put("sleepSanity", sleepSanity)
        put("allowance", allowance)
        put("allowanceNextDay", allowanceNextDay)
        put("attended", attended)
        put("jobs", JSONArray().apply {
            jobs.forEach {
                put(JSONObject().put("id", it.id).put("start", it.startDay).put("perf", it.performance)
                    .put("abs", it.absences).put("shifts", it.shifts))
            }
        })
        put("stats", JSONObject().apply { stats.forEach { (k, v) -> put(k.key, v) } })
        put("items", JSONArray().apply {
            items.forEach { put(JSONObject().put("id", it.id).put("bought", it.boughtDay).put("expires", it.expiresDay)) }
        })
    }.toString()
}

/** Format rupiah: 50000 → "Rp50.000". */
fun rupiah(v: Long): String {
    val s = v.toString().reversed().chunked(3).joinToString(".").reversed()
    return "Rp$s"
}
