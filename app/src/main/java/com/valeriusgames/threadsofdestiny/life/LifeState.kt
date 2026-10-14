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

    /**
     * Bagian hari (untuk ketersediaan aktivitas, karakter, janji temu).
     * start = menit saat bagian ini dimulai, set = jam yang dipakai bila naskah memakai "@kalender Senin pagi".
     */
    enum class Slot(val label: String, val icon: String, val start: Int, val set: Int, val end: Int) {
        PAGI("Pagi", "☀️", 4 * 60, 6 * 60, 11 * 60),
        SIANG("Siang", "🌤️", 11 * 60, 11 * 60, 15 * 60),
        SORE("Sore", "🌇", 15 * 60, 15 * 60, 18 * 60),
        MALAM("Malam", "🌙", 18 * 60, 18 * 60 + 30, 24 * 60);

        companion object {
            fun of(minute: Int): Slot = when (minute) {
                in 4 * 60 until 11 * 60 -> PAGI
                in 11 * 60 until 15 * 60 -> SIANG
                in 15 * 60 until 18 * 60 -> SORE
                else -> MALAM
            }
        }
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

        /** Perubahan per jam saat terjaga / tidur. */
        private const val KENYANG_PER_JAM = -2.0f
        private const val HIDRASI_PER_JAM = -2.8f
        private const val ENERGI_PER_JAM = -1.0f
        private const val TIDUR_ENERGI_PER_JAM = 11f

        /** "07:30" → 450 menit. "24:00" → 1440. */
        fun parseClock(t: String): Int? {
            val m = Regex("^(\\d{1,2})[:.](\\d{2})$").find(t.trim()) ?: return null
            val h = m.groupValues[1].toInt(); val mi = m.groupValues[2].toInt()
            if (h > 24 || mi > 59) return null
            return h * 60 + mi
        }

        /** 450 → "07:30". */
        fun clock(min: Int): String {
            val m = ((min % 1440) + 1440) % 1440
            return "%02d:%02d".format(m / 60, m % 60)
        }

        /** 90 → "1 jam 30 menit". */
        fun duration(min: Int): String = when {
            min < 60 -> "$min menit"
            min % 60 == 0 -> "${min / 60} jam"
            else -> "${min / 60} jam ${min % 60} menit"
        }

        fun fromJson(s: String?): LifeState {
            val st = LifeState()
            if (s.isNullOrEmpty()) return st
            try {
                val o = JSONObject(s)
                st.active = o.optBoolean("active")
                st.day = o.optInt("day", 0)
                st.startWeekday = o.optInt("startWeekday", 0)
                st.minute = if (o.has("minute")) o.optInt("minute")
                    else runCatching { Slot.valueOf(o.optString("slot")).set }.getOrDefault(Slot.PAGI.set)
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
    /** Jam sekarang dalam menit sejak tengah malam (0–1439). */
    var minute = 7 * 60
    /** Bagian hari dari jam sekarang. Mengisi nilai = loncat ke awal bagian itu. */
    var slot: Slot
        get() = Slot.of(minute)
        set(v) { minute = v.set }
    private val frac = mutableMapOf<Stat, Float>()
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

    /** Hari dalam seminggu untuk hari ke-d (0 = Senin). */
    fun weekdayOf(d: Int): Int = startEpochDay.takeIf { it != 0L }
        ?.let { LocalDate.ofEpochDay(it + d).dayOfWeek.value - 1 } ?: ((startWeekday + d) % 7)

    /** Penanda per hari: "hari:id" = hadir, "hari:x:id" = sudah dicatat absen. */
    fun hasAttended(jobId: String, d: Int = day) = attended.split(';').contains("$d:$jobId")
    fun markAttended(jobId: String, d: Int = day) = mark("$d:$jobId")
    fun isMarkedAbsent(jobId: String, d: Int) = attended.split(';').contains("$d:x:$jobId")
    fun markAbsent(jobId: String, d: Int) = mark("$d:x:$jobId")
    private fun mark(key: String) {
        // simpan penanda 2 hari terakhir saja
        attended = (attended.split(';').filter { k ->
            k.substringBefore(':').toIntOrNull()?.let { it >= day - 1 } == true
        } + key).joinToString(";")
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
    val timeText: String get() = clock(minute)
    val clockText: String
        get() = date()?.let { "$weekday, ${it.dayOfMonth} ${MONTHS[it.monthValue - 1].take(3)} · $timeText ${slot.icon}" }
            ?: "$weekday · $timeText ${slot.icon}"

    /** "@kalender Senin pagi" atau "@kalender Senin 07:30". */
    fun setCalendar(weekdayName: String, slotName: String) {
        val wd = WEEKDAYS.indexOfFirst { it.equals(weekdayName, true) }
        if (wd >= 0 && startEpochDay == 0L) startWeekday = ((wd - day) % 7 + 7) % 7
        val t = parseClock(slotName)
        if (t != null) minute = t.coerceIn(0, 1439)
        else Slot.values().firstOrNull { it.label.equals(slotName, true) }?.let { minute = it.set }
    }

    /** Menit sampai tengah malam berikutnya. */
    val minutesToMidnight: Int get() = 1440 - minute

    /** Waktu mutlak dalam menit sejak hari pertama (untuk membandingkan "sampai kapan"). */
    val absMinute: Long get() = day * 1440L + minute

    /** Waktu mutlak untuk hari [weekdayName] berikutnya pukul [clockMin] (hari ini bila jamnya belum lewat). */
    fun absNext(weekdayName: String, clockMin: Int): Long {
        val wd = WEEKDAYS.indexOfFirst { it.equals(weekdayName, true) }.takeIf { it >= 0 } ?: weekdayIndex
        var ahead = (wd - weekdayIndex + 7) % 7
        if (ahead == 0 && clockMin <= minute) ahead = 7
        return (day + ahead) * 1440L + clockMin
    }

    /**
     * "@kalender Senin pagi" / "@kalender Senin 07:30".
     * Bila tanggal sudah diatur dan harinya berbeda: loncat hari demi hari ([onNewDay] tiap hari baru).
     * Bila harinya sama dan jamnya lebih sore: waktu berjalan sungguhan lewat [passForward] (menit).
     * Bila jamnya lebih pagi di hari yang sama: jam langsung diatur.
     */
    fun calendarJump(weekdayName: String, slotName: String, onNewDay: () -> Unit, passForward: (Int) -> Unit) {
        val target = WEEKDAYS.indexOfFirst { it.equals(weekdayName, true) }
        if (startEpochDay != 0L && target >= 0) {
            var guard = 0
            while (weekdayIndex != target && guard++ < 7) {
                day++
                onNewDay()
            }
        }
        val targetMin = parseClock(slotName) ?: Slot.values().firstOrNull { it.label.equals(slotName, true) }?.set
        if (active && (target < 0 || target == weekdayIndex) && targetMin != null && targetMin > minute) {
            passForward(targetMin - minute)
        }
        setCalendar(weekdayName, slotName)
    }

    /** "@maju 1": menit menuju awal bagian hari berikutnya, dan apakah itu tidur (dari Malam → 06:00). */
    fun nextPeriodStep(): Pair<Int, Boolean> {
        val s = slot
        return if (s == Slot.MALAM) {
            val target = 6 * 60
            (if (minute < 4 * 60) target - minute else minutesToMidnight + target) to true
        } else {
            (s.end - minute) to false
        }
    }

    /**
     * Sekolah/kerja wajib di hari ke-[d] yang harus dicatat bolos/absen sekarang.
     * [wholeDay] = hari itu sudah berakhir. Batas datang: 1 jam sebelum pulang. Hari pertama tidak dihitung.
     */
    fun dueAbsences(defs: Map<String, GameData.JobDef>, d: Int, wholeDay: Boolean): List<Pair<Job, GameData.JobDef>> =
        jobs.mapNotNull { job ->
            val def = defs[job.id] ?: return@mapNotNull null
            when {
                !def.mandatory || weekdayOf(d) !in def.days -> null
                d <= job.startDay || hasAttended(def.id, d) || isMarkedAbsent(def.id, d) -> null
                !wholeDay && minute <= def.endMin - 60 -> null
                else -> job to def
            }
        }

    /**
     * Majukan waktu [min] menit, dipecat per tengah malam. [onMidnight] dipanggil dengan nomor hari
     * yang baru saja berakhir (sebelum hal-hal harian hari berikutnya dijalankan).
     */
    fun advance(min: Int, sleeping: Boolean = false, onMidnight: (endedDay: Int) -> Unit = {}): List<String> {
        val out = mutableListOf<String>()
        var left = min
        while (left > 0 && !dead) {
            val chunk = minOf(left, minutesToMidnight)
            val before = day
            out += pass(chunk, sleeping)
            left -= chunk
            if (day != before) onMidnight(before)
        }
        return out.distinct()
    }

    /**
     * Majukan waktu [min] menit (tidak melewati tengah malam — pemanggil memecahnya).
     * Mengembalikan peringatan baru (mis. "Kamu mulai lapar").
     */
    fun pass(min: Int, sleeping: Boolean = false): List<String> {
        if (min <= 0) return emptyList()
        val before = stats.toMap()
        var left = min
        while (left > 0 && !dead) {
            val step = minOf(left, 15)
            val h = step / 60f
            val lateNight = !sleeping && (minute < 4 * 60)
            if (sleeping) {
                addFrac(Stat.KENYANG, KENYANG_PER_JAM * 0.5f * h)
                addFrac(Stat.HIDRASI, HIDRASI_PER_JAM * 0.45f * h)
                val wellFed = this[Stat.KENYANG] >= 20 && this[Stat.HIDRASI] >= 20
                addFrac(Stat.ENERGI, (if (wellFed) TIDUR_ENERGI_PER_JAM else 6f) * h + sleepEnergy / 8f * h)
                addFrac(Stat.KEWARASAN, (if (wellFed) 0.7f else -0.4f) * h + sleepSanity / 8f * h)
            } else {
                addFrac(Stat.KENYANG, KENYANG_PER_JAM * h)
                addFrac(Stat.HIDRASI, HIDRASI_PER_JAM * h)
                addFrac(Stat.ENERGI, ENERGI_PER_JAM * h * (if (lateNight) 3f else 1f))
                if (lateNight) addFrac(Stat.KEWARASAN, -1f * h)
            }
            needsEffect(h)
            minute += step
            left -= step
            if (minute >= 1440) {
                minute -= 1440
                day++
            }
        }
        return warnings(before)
    }

    private fun addFrac(s: Stat, delta: Float) {
        val v = (frac[s] ?: 0f) + delta
        val whole = v.toInt()
        if (whole != 0) change(s, whole)
        frac[s] = v - whole
    }

    /** Akibat kebutuhan dasar terhadap kesehatan & kewarasan, untuk [h] jam. */
    private fun needsEffect(h: Float) {
        val k = this[Stat.KENYANG]
        val hy = this[Stat.HIDRASI]
        val e = this[Stat.ENERGI]
        var dmg = 0f
        if (k == 0) dmg += 2f
        if (hy == 0) dmg += 3f
        if (e == 0) dmg += 1f
        if (dmg > 0f) addFrac(Stat.KESEHATAN, -dmg * h)
        else if (k >= 50 && hy >= 50 && e >= 30) addFrac(Stat.KESEHATAN, 0.4f * h)

        if (k < 25 || hy < 25 || e < 20) addFrac(Stat.KEWARASAN, -0.6f * h)
        val h2 = hy

        if (this[Stat.KESEHATAN] <= 0) {
            dead = true
            deathReason = when {
                h2 == 0 -> "dehidrasi"
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
        put("minute", minute)
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
