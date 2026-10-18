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
 * Menjalankan naskah ASLI (prolog → bab2 → bab3) dengan sistem waktu yang sama dengan game,
 * lalu memeriksa hari & jam di setiap adegan cocok dengan yang tertulis di cerita.
 */
class StoryTimelineTest {

    private val dir = File("src/main/assets/naskah")

    data class Mark(val text: String, val weekday: String, val clock: String, val date: String?, val slot: String)

    private fun load(name: String) = StoryScript.parse(File(dir, name).readLines())

    /** Jalankan naskah. [prefer] = label pilihan yang diambil (selain itu: pilihan pertama). */
    private fun run(prefer: Set<String>, startVars: Map<String, String> = emptyMap()): List<Mark> {
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
                is Cmd.Apply -> pc = script.labels[c.okLabel]!!
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
        val start = l.indexOf("@latar sarapan_keluarga")
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

    @Test fun cityPathTimeline() {
        val m = run(setOf("sma", "uang_ortu", "kos_sedang", "dimas_canda", "cerita_nadia"))
        assertConsistent(m)
        with(m.at("Senin, 20:00")) { assertEquals("Senin", weekday); assertEquals("20:00", clock) }
        with(m.at("Sabtu, 11 Juli 2026")) { assertEquals("11/7", date); assertEquals("06:00", clock) }
        with(m.at("Kota Sakala")) { assertEquals("Sabtu", weekday); assertEquals("11:00", clock) }
        with(m.at("Malam pertama")) { assertEquals("Sabtu", weekday); assertEquals("20:00", clock) }
        with(m.at("Minggu, 12 Juli 2026")) { assertEquals("12/7", date); assertEquals("06:00", clock) }
        with(m.at("Senin, 13 Juli 2026")) { assertEquals("13/7", date); assertEquals("06:00", clock) }
        with(m.at("Sore pertama")) { assertEquals("Senin", weekday); assertEquals("15:00", clock) }
        with(m.at("Satu minggu pertama")) { assertEquals("18/7", date); assertEquals("Sabtu", weekday); assertEquals("15:00", clock) }
        with(m.at("Minggu kedua di Kota Sakala")) { assertEquals("20/7", date); assertEquals("Senin", weekday) }
        with(m.at("Sabtu sore")) { assertEquals("25/7", date); assertEquals("15:00", clock) }
    }

    @Test fun villagePathTimeline() {
        val m = run(setOf("kerja", "diterima", "bayar_sendiri"))
        assertConsistent(m)
        with(m.at("Senin, 20:00")) { assertEquals("20:00", clock) }
        with(m.at("Senin, 6 Juli 2026")) { assertEquals("6/7", date); assertEquals("Senin", weekday); assertEquals("06:00", clock) }
        with(m.at("Satu minggu berlalu")) { assertEquals("12/7", date); assertEquals("Minggu", weekday) }
        with(m.at("Minggu kedua di Tanjung Lentera")) { assertEquals("13/7", date); assertEquals("Senin", weekday) }
        assertTrue(LocalDate.of(2026, 7, 6).dayOfWeek.value == 1) // 6 Juli 2026 memang Senin
    }
}
