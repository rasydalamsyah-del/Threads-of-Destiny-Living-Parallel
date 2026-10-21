package com.valeriusgames.threadsofdestiny

import com.valeriusgames.threadsofdestiny.life.LifeState
import com.valeriusgames.threadsofdestiny.life.LifeState.Stat
import com.valeriusgames.threadsofdestiny.story.StoryScript
import com.valeriusgames.threadsofdestiny.story.StoryScript.Cmd
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.time.LocalDate

/**
 * Menjalankan naskah ASLI (prolog → bab1 → bab2 → bab3/bab4 kota atau desa) dengan sistem waktu yang sama dengan game,
 * lalu memeriksa hari & jam di setiap adegan cocok dengan yang tertulis di cerita.
 */
class StoryTimelineTest {

    private val dir = File("src/main/assets/naskah")

    data class Mark(val text: String, val weekday: String, val clock: String, val date: String?, val slot: String)

    private fun load(name: String) = StoryScript.parse(File(dir, name).readLines())

    /** Jalankan naskah. [prefer] = label pilihan yang diambil (selain itu: pilihan pertama). */
    private fun run(prefer: Set<String>, startVars: Map<String, String> = emptyMap(), rejectJobs: Boolean = false): List<Mark> {
        val life = LifeState()
        val vars = startVars.toMutableMap()
        var script = load("prolog.txt")
        var pc = 0
        val marks = mutableListOf<Mark>()
        var pendingCard: String? = null
        var steps = 0

        fun mark(text: String) {
            val d = life.date()
            marks += Mark(text, life.weekday, life.timeText,
                d?.let { "${it.dayOfMonth}/${it.monthValue}" }, life.slot.label)
        }
        fun feed() { // anggap pemain makan & minum cukup
            life.stats[Stat.KENYANG] = 80; life.stats[Stat.HIDRASI] = 80; life.stats[Stat.ENERGI] = 80
        }
        fun pass(min: Int, sleeping: Boolean = false) { feed(); life.advance(min, sleeping) }
        fun value(v: String) = vars[v] ?: ""
        fun check(c: Cmd.JumpIf): Boolean {
            val a = value(c.variable); val b = c.value
            val an = a.toDoubleOrNull() ?: if (a.isEmpty()) 0.0 else null
            val bn = b.toDoubleOrNull()
            return if (an != null && bn != null) when (c.op) {
                "==" -> an == bn; "!=" -> an != bn; ">=" -> an >= bn; "<=" -> an <= bn; ">" -> an > bn; else -> an < bn
            } else when (c.op) { "==" -> a.equals(b, true); "!=" -> !a.equals(b, true); else -> false }
        }

        while (pc < script.cmds.size) {
            if (steps++ > 20000) fail("naskah berputar tanpa akhir")
            when (val c = script.cmds[pc++]) {
                // kartu waktu sebelum sistem kehidupan aktif (prolog pagi) memakai @jam manual
                is Cmd.TimeCard -> pendingCard = if (life.active) c.text else null
                is Cmd.Line -> {
                    pendingCard?.let { mark(it) }
                    pendingCard = null
                    if (c.text.contains("Malam pertama") || c.text.contains("Sore pertama") ||
                        c.text.contains("minggu pertama") || c.text.contains("Satu minggu berlalu")) mark(c.text)
                }
                is Cmd.Choice -> {
                    val pick = c.options.firstOrNull { it.label in prefer } ?: c.options.first()
                    pc = script.labels[pick.label] ?: fail("label ${pick.label}").let { 0 }
                }
                is Cmd.Jump -> pc = script.labels[c.label]!!
                is Cmd.JumpIf -> if (check(c)) pc = script.labels[c.label]!!
                is Cmd.SetVar -> vars[c.variable] = c.value
                is Cmd.AddVar -> vars[c.variable] = ((vars[c.variable]?.toLongOrNull() ?: 0L) + c.amount).toString()
                is Cmd.Life -> life.active = c.active
                is Cmd.SetDate -> life.startEpochDay = LifeState.parseDate(c.text)!! - life.day
                is Cmd.Calendar -> life.calendarJump(c.weekday, c.slot, onNewDay = {}, passForward = { pass(it) })
                is Cmd.Advance -> if (c.minutes > 0) pass(c.minutes) else repeat(c.count) {
                    val (m, s) = life.nextPeriodStep(); pass(m, s)
                }
                is Cmd.Free -> {
                    if (c.days > 0 || c.untilWeekday != null) {
                        val until = if (c.days > 0) life.absMinute + c.days * 1440L
                            else life.absNext(c.untilWeekday!!, LifeState.parseClock(c.untilClock ?: "18:00")!!)
                        // pemain menjalani hari-hari itu (termasuk tidur) sampai waktunya habis
                        while (life.absMinute < until) pass(minOf(240L, until - life.absMinute).toInt())
                    } else {
                        repeat(c.count) { pass(90) } // rata-rata satu kegiatan
                    }
                }
                is Cmd.Apply -> pc = script.labels[if (rejectJobs) c.noLabel else c.okLabel]!!
                is Cmd.Confess -> pc = script.labels[c.noLabel]!!
                is Cmd.LoadScript -> { script = load(c.file); pc = 0 }
                is Cmd.End -> break
                else -> {}
            }
            assertTrue("tokoh tidak boleh mati di simulasi", !life.dead)
        }
        return marks
    }

    private fun List<Mark>.at(prefix: String): Mark =
        firstOrNull { it.text.startsWith(prefix) } ?: run { fail("adegan '$prefix' tidak ditemukan. Ada: ${map { it.text }}"); first() }

    /** Kata waktu di kartu/teks harus cocok dengan jam sebenarnya. */
    private fun assertConsistent(marks: List<Mark>) {
        marks.forEach { m ->
            LifeState.WEEKDAYS.firstOrNull { m.text.contains(Regex("\\b$it\\b")) && !m.text.contains("Minggu kedua") && !m.text.contains("minggu") }
                ?.let { assertEquals("hari di '${m.text}'", it, m.weekday) }
            Regex("\\b(\\d{2}:\\d{2})\\b").find(m.text)?.let { assertEquals("jam di '${m.text}'", it.groupValues[1], m.clock) }
            listOf("pagi", "siang", "sore", "malam").firstOrNull { Regex("\\b$it\\b", RegexOption.IGNORE_CASE).containsMatchIn(m.text) }
                ?.let { assertEquals("bagian hari di '${m.text}'", it, m.slot.lowercase()) }
        }
    }

    @Test fun allScriptsParse() {
        val files = dir.listFiles { f -> f.name.endsWith(".txt") }!!
        assertTrue(files.size >= 5)
        files.forEach { f ->
            try { StoryScript.parse(f.readLines()) } catch (e: Exception) { fail("${f.name}: ${e.message}") }
        }
    }

    @Test fun breakfastHasExpressions() {
        // adegan sarapan memakai gambar keluarga, tapi ekspresi di kotak dialog tetap berganti
        val l = File(dir, "prolog.txt").readLines().map { it.trim() }
        val start = l.indexOf("@latar ruang_makan__ayah_ibu_laras_pagi")
        val end = l.indexOf("@latar ruang_makan")
        assertTrue(start in 0 until end)
        val scene = l.subList(start, end)
        listOf("@tampil ayah sedih", "@tampil ibu sedih", "@tampil laras kesal").forEach { e ->
            assertTrue("sarapan tanpa $e", scene.any { it.startsWith(e) })
        }
    }

    @Test fun newTimeCommandsParse() {
        val s = StoryScript.parse(listOf("@bebas 5 hari", "@bebas sampai Sabtu 15:00", "@bebas 3", "@maju 2 jam", "@maju 30 menit", "@maju 2",
            "@kalender Senin 07:30", "@tunjuk hp kalender atas | contoh", "@info bayu hobi"))
        assertEquals(Cmd.Free(1, days = 5), s.cmds[0])
        assertEquals(Cmd.Free(1, untilWeekday = "sabtu", untilClock = "15:00"), s.cmds[1])
        assertEquals(Cmd.Free(3), s.cmds[2])
        assertEquals(Cmd.Advance(1, 120), s.cmds[3])
        assertEquals(Cmd.Advance(1, 30), s.cmds[4])
        assertEquals(Cmd.Advance(2), s.cmds[5])
        assertEquals(Cmd.Calendar("senin", "07:30"), s.cmds[6])
        assertEquals(Cmd.Show("hp", "kalender", true, "contoh"), s.cmds[7])
        assertEquals(Cmd.Info("bayu", "hobi"), s.cmds[8])
    }

    /** Bab 1–2 (sama untuk semua pemain): 1 Juni → keputusan Jumat 26 Juni. docs/CERITA.md 6a–6b */
    private fun assertSharedChapters(m: List<Mark>, onBoat: Boolean = false) {
        with(m.at("Senin, 1 Juni 2026 · malam")) { assertEquals("1/6", date); assertEquals("20:00", clock) }
        with(m.at("Selasa, 2 Juni 2026")) { assertEquals("2/6", date); assertEquals("06:00", clock) }
        with(m.at("Rabu, 3 Juni 2026")) { assertEquals("3/6", date); assertEquals("06:30", clock) }
        with(m.at("Kamis, 4 Juni 2026")) { assertEquals("4/6", date); assertEquals(if (onBoat) "04:15" else "06:50", clock) }
        with(m.at("Kamis, 18 Juni 2026")) { assertEquals("18/6", date); assertEquals("20:00", clock) }
        with(m.at("Minggu, 21 Juni 2026")) { assertEquals("21/6", date); assertEquals("19:30", clock) }
        with(m.at("Jumat, 26 Juni 2026")) { assertEquals("26/6", date); assertEquals("Jumat", weekday); assertEquals("06:30", clock) }
        assertTrue(LocalDate.of(2026, 6, 1).dayOfWeek.value == 1)  // 1 Juni 2026 memang Senin
        assertTrue(LocalDate.of(2026, 6, 26).dayOfWeek.value == 5) // 26 Juni 2026 memang Jumat
    }

    @Test fun cityPathTimeline() {
        val m = run(setOf("putus_kota", "uang_ortu", "kos_sedang", "dimas_canda", "cerita_nadia"))
        assertConsistent(m)
        assertSharedChapters(m)
        // Bab 3 Kota "Pamit"
        with(m.at("Sabtu, 27 Juni 2026")) { assertEquals("27/6", date); assertEquals("06:00", clock) }
        with(m.at("Senin, 29 Juni 2026")) { assertEquals("29/6", date); assertEquals("07:30", clock) }
        with(m.at("Sabtu, 4 Juli 2026")) { assertEquals("4/7", date); assertEquals("19:00", clock) }
        with(m.at("Senin, 6 Juli 2026")) { assertEquals("6/7", date); assertEquals("06:00", clock) }
        with(m.at("Jumat, 10 Juli 2026")) { assertEquals("10/7", date); assertEquals("19:30", clock) }
        with(m.at("Sabtu, 11 Juli 2026")) { assertEquals("11/7", date); assertEquals("07:00", clock) }
        // Bab 4 Kota
        with(m.at("Kota Sakala")) { assertEquals("11/7", date); assertEquals("Sabtu", weekday); assertEquals("11:00", clock) }
        with(m.at("Malam pertama")) { assertEquals("Sabtu", weekday); assertEquals("20:00", clock) }
        with(m.at("Minggu, 12 Juli 2026")) { assertEquals("12/7", date); assertEquals("06:00", clock) }
        with(m.at("Senin, 13 Juli 2026")) { assertEquals("13/7", date); assertEquals("06:00", clock) }
        with(m.at("Sore pertama")) { assertEquals("Senin", weekday); assertEquals("15:00", clock) }
        with(m.at("Satu minggu pertama")) { assertEquals("18/7", date); assertEquals("Sabtu", weekday); assertEquals("15:00", clock) }
        with(m.at("Minggu kedua di Kota Sakala")) { assertEquals("20/7", date); assertEquals("Senin", weekday) }
        with(m.at("Sabtu sore")) { assertEquals("25/7", date); assertEquals("15:00", clock) }
        with(m.at("Minggu, 19 Juli 2026")) { assertEquals("19/7", date); assertEquals("20:00", clock) }
        with(m.at("Senin, 3 Agustus 2026")) { assertEquals("3/8", date); assertEquals("20:00", clock) }
        with(m.at("Jumat, 7 Agustus 2026")) { assertEquals("7/8", date); assertEquals("23:00", clock) }
        with(m.at("Sabtu, 8 Agustus 2026")) { assertEquals("8/8", date) }
        with(m.at("Jumat, 14 Agustus 2026")) { assertEquals("14/8", date); assertEquals("20:00", clock) }
        with(m.at("Senin, 17 Agustus 2026")) { assertEquals("17/8", date); assertEquals("07:00", clock) }
    }

    @Test fun cityPathGoesHomeForStormAndFinal() {
        // uang cukup: pulang saat badai (tiba Sabtu 08:00) dan pulang untuk final Bayu & tujuh belasan desa
        val m = run(setOf("putus_kota", "uang_ortu", "kos_sedang", "tagih_kiriman", "badai_pulang", "final_pulang"),
            startVars = mapOf("uang" to "5000000"))
        assertConsistent(m)
        with(m.at("Sabtu, 8 Agustus 2026")) { assertEquals("8/8", date); assertEquals("08:00", clock) }
        with(m.at("Minggu, 16 Agustus 2026")) { assertEquals("16/8", date); assertEquals("15:00", clock) }
        with(m.at("Senin, 17 Agustus 2026")) { assertEquals("17/8", date); assertEquals("19:30", clock) }
    }

    @Test fun cityPathStaysForIndependenceDay() {
        // tetap di kota saat final Bayu: upacara 17 Agustus di sekolah, malam di kota
        val m = run(setOf("putus_kota", "uang_ortu", "kos_murah", "tagih_kiriman", "final_kota", "badai_tunggu"))
        assertConsistent(m)
        with(m.at("Jumat, 7 Agustus 2026")) { assertEquals("23:00", clock) }
        with(m.at("Sabtu, 8 Agustus 2026")) { assertEquals("06:00", clock) }
        with(m.at("Senin, 17 Agustus 2026")) { assertEquals("17/8", date); assertEquals("07:00", clock) }
        with(m.at("Malam tujuh belasan")) { assertEquals("17/8", date); assertEquals("19:30", clock) }
    }

    @Test fun cityPathFromBoat() {
        // ditolak di gudang (ikut perahu), lalu memilih kota: tetap sampai di Sakala tepat waktu
        val m = run(setOf("putus_kota", "uang_ortu", "kos_sedang"), rejectJobs = true)
        assertConsistent(m)
        assertSharedChapters(m, onBoat = true)
        with(m.at("Senin, 29 Juni 2026")) { assertEquals("07:30", clock) }
        with(m.at("Kota Sakala")) { assertEquals("11/7", date); assertEquals("11:00", clock) }
    }

    @Test fun villagePathTimeline() {
        // jalur desa dimainkan hari demi hari: docs/CERITA.md bagian 6c & ALUR_BAB4.md
        val m = run(setOf("putus_desa", "bayar_sendiri"))
        assertConsistent(m)
        assertSharedChapters(m)
        // Bab 3 Desa "Musim Ombak Tinggi"
        with(m.at("Sabtu, 27 Juni 2026")) { assertEquals("27/6", date); assertEquals("06:00", clock) }
        with(m.at("Sabtu, 4 Juli 2026")) { assertEquals("4/7", date); assertEquals("19:00", clock) }
        with(m.at("Senin, 6 Juli 2026")) { assertEquals("6/7", date); assertEquals("Senin", weekday); assertEquals("06:00", clock) }
        with(m.at("Sabtu, 11 Juli 2026")) { assertEquals("11/7", date); assertEquals("07:00", clock) }
        with(m.at("Senin, 13 Juli 2026")) { assertEquals("13/7", date); assertEquals("05:45", clock) }
        with(m.at("Dua hari kemudian")) { assertEquals("15/7", date); assertEquals("Rabu", weekday) }
        with(m.at("Minggu, 19 Juli 2026")) { assertEquals("19/7", date); assertEquals("20:00", clock) }
        // Bab 4 Desa "Tujuh Belasan"
        with(m.at("Senin, 20 Juli 2026")) { assertEquals("20/7", date) }
        with(m.at("Senin, 3 Agustus 2026")) { assertEquals("3/8", date); assertEquals("Senin", weekday) }
        with(m.at("Jumat, 7 Agustus 2026")) { assertEquals("7/8", date); assertEquals("17:30", clock) }
        with(m.at("Sabtu, 8 Agustus 2026")) { assertEquals("8/8", date) }
        with(m.at("Minggu, 16 Agustus 2026")) { assertEquals("16/8", date); assertEquals("15:00", clock) }
        with(m.at("Senin, 17 Agustus 2026")) { assertEquals("17/8", date); assertEquals("07:00", clock) }
        assertTrue(LocalDate.of(2026, 8, 17).dayOfWeek.value == 1) // 17 Agustus 2026 memang Senin
    }

    @Test fun villagePathOnBoatWhenRejected() {
        // ditolak di gudang → ikut perahu Pak Karta subuh Kamis 4 Juni
        val m = run(setOf("putus_desa", "bayar_sendiri"), rejectJobs = true)
        assertConsistent(m)
        assertSharedChapters(m, onBoat = true)
        with(m.at("Senin, 17 Agustus 2026")) { assertEquals("17/8", date) }
    }
}
