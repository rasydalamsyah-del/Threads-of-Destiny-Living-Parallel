package com.valeriusgames.threadsofdestiny

import com.valeriusgames.threadsofdestiny.life.GameData
import com.valeriusgames.threadsofdestiny.life.LifeState
import com.valeriusgames.threadsofdestiny.life.LifeState.Slot
import com.valeriusgames.threadsofdestiny.life.LifeState.Stat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Uji sistem jam sungguhan (LifeState & GameData). Dijalankan GitHub Actions setiap build. */
class TimeSystemTest {

    /** 13 Juli 2026 = Senin. Tanggal dipilih supaya cocok dengan nama harinya. */
    private fun life(weekday: String = "Senin", time: String = "07:00", date: String? = null): LifeState =
        LifeState().apply {
            active = true
            val d = date ?: "${13 + LifeState.WEEKDAYS.indexOf(weekday)} Juli 2026"
            startEpochDay = LifeState.parseDate(d)!! - day
            setCalendar(weekday, time)
        }

    private fun school(startDay: Int = 0) = GameData.JobDef(
        id = "sma", name = "SMA", kind = "sekolah", places = setOf("kota"), days = (0..5).toSet(),
        slots = setOf("pagi", "siang"), pay = 0, effects = emptyMap(), condition = null, minAge = 0,
        way = "cerita", difficulty = 0, referrer = null, text = "", icon = "", desc = "", documents = emptyList(),
        hasTest = false, examEvery = 0, startMin = 7 * 60, endMin = 14 * 60,
    )

    // ---------------- format jam ----------------

    @Test fun parseAndFormatClock() {
        assertEquals(450, LifeState.parseClock("07:30"))
        assertEquals(450, LifeState.parseClock("7.30"))
        assertEquals(1440, LifeState.parseClock("24:00"))
        assertNull(LifeState.parseClock("25:00"))
        assertNull(LifeState.parseClock("pagi"))
        assertEquals("07:30", LifeState.clock(450))
        assertEquals("00:15", LifeState.clock(1455))
        assertEquals("45 menit", LifeState.duration(45))
        assertEquals("2 jam", LifeState.duration(120))
        assertEquals("1 jam 30 menit", LifeState.duration(90))
    }

    @Test fun parseDurationsFromData() {
        assertEquals(45, GameData.minutes("45 menit"))
        assertEquals(120, GameData.minutes("2 jam"))
        assertEquals(90, GameData.minutes("1,5 jam"))
        assertEquals(90, GameData.minutes("1 jam 30 menit"))
        assertEquals(90, GameData.minutes("90"))
        assertNull(GameData.minutes(null))
        assertNull(GameData.minutes(""))
    }

    @Test fun parseJobHours() {
        assertEquals(420 to 840, GameData.hours("07:00-14:00", emptySet()))
        assertEquals(1140 to 1410, GameData.hours("19:00-23:30", emptySet()))
        assertEquals(420 to 900, GameData.hours(null, setOf("pagi", "siang")))
        assertEquals(900 to 1320, GameData.hours(null, setOf("sore", "malam")))
        // jam salah → pakai bagian hari
        assertEquals(420 to 660, GameData.hours("14:00-07:00", setOf("pagi")))
    }

    /** Semua "durasi" & "jam" di file data harus terbaca. */
    @Test fun dataFilesTimeFieldsAreValid() {
        val dir = File("src/main/assets/data")
        File(dir, "aktivitas.txt").readLines().map { it.trim() }.filter { it.startsWith("durasi") }.forEach { l ->
            val v = l.substringAfter('=').trim()
            val m = GameData.minutes(v)
            assertTrue("durasi tidak terbaca: $l", m != null && m in 5..12 * 60)
        }
        File(dir, "pekerjaan.txt").readLines().map { it.trim() }.filter { it.startsWith("jam") }.forEach { l ->
            val v = l.substringAfter('=').trim()
            val h = GameData.hours(v, emptySet())
            assertTrue("jam tidak terbaca: $l", h != null && h.second > h.first)
        }
    }

    // ---------------- jam mulai aktivitas ----------------

    private fun act(name: String, places: Set<String>, slots: Set<String>, window: String? = null) = GameData.ActivityDef(
        id = name, name = name, places = places, slots = slots, effects = emptyMap(), condition = null,
        text = "", icon = "", minutes = 60,
        startFrom = GameData.window(window)?.first, startUntil = GameData.window(window)?.second,
    )

    @Test fun activityStartWindow() {
        val gd = GameDataStub.empty()
        val melaut = act("Melaut bersama Ayah", setOf("desa"), setOf("pagi"), "04:00-06:00")
        assertNull(gd.activityBlockedReason(melaut, 5 * 60))
        assertNull(gd.activityBlockedReason(melaut, 6 * 60))
        assertEquals("Sudah terlalu siang untuk melaut bersama Ayah (paling lambat 06:00)",
            gd.activityBlockedReason(melaut, 10 * 60))
        assertEquals("Sudah terlalu malam untuk melaut bersama Ayah (paling lambat 06:00)",
            gd.activityBlockedReason(melaut, 20 * 60))
        assertEquals("Belum bisa. Mulai pukul 04:00", gd.activityBlockedReason(melaut, 3 * 60))
        // lewat tengah malam
        val game = act("Begadang", setOf("kota"), setOf("malam"), "19:00-02:00")
        assertNull(gd.activityBlockedReason(game, 23 * 60))
        assertNull(gd.activityBlockedReason(game, 60))
        assertEquals("Hanya bisa dimulai pukul 19:00–02:00", gd.activityBlockedReason(game, 10 * 60))
        // tanpa jam mulai: pakai bagian hari
        val dermaga = act("Dermaga", setOf("desa"), setOf("sore", "malam"))
        assertNull(gd.activityBlockedReason(dermaga, 16 * 60))
        assertTrue(gd.activityBlockedReason(dermaga, 9 * 60)!!.startsWith("Hanya bisa saat Sore"))
        assertNull(gd.activityBlockedReason(act("Istirahat", setOf("semua"), setOf("semua")), 3 * 60))
    }

    /** Semua "mulai" di aktivitas.txt terbaca. */
    @Test fun dataStartWindowsValid() {
        File("src/main/assets/data/aktivitas.txt").readLines().map { it.trim() }.filter { it.startsWith("mulai") }.forEach { l ->
            val v = l.substringAfter('=').trim()
            assertEquals("mulai tidak terbaca: $l", v.split(',').size, GameData.windows(v).size)
        }
    }

    /** Aktivitas desa tidak muncul di kota (dan sebaliknya). */
    @Test fun activitiesFollowLocation() {
        val melaut = act("melaut", setOf("desa"), setOf("pagi"))
        val mal = act("mal", setOf("kota"), setOf("siang"))
        val rest = act("istirahat", setOf("semua"), setOf("semua"))
        val gd = GameDataStub.with(listOf(melaut, mal, rest))
        assertEquals(listOf("mal", "istirahat"), gd.activitiesAt("kota").map { it.id })
        assertEquals(listOf("melaut", "istirahat"), gd.activitiesAt("desa").map { it.id })
    }

    // ---------------- bagian hari ----------------

    @Test fun slotBoundaries() {
        mapOf(
            "03:59" to Slot.MALAM, "04:00" to Slot.PAGI, "10:59" to Slot.PAGI, "11:00" to Slot.SIANG,
            "14:59" to Slot.SIANG, "15:00" to Slot.SORE, "17:59" to Slot.SORE, "18:00" to Slot.MALAM, "23:59" to Slot.MALAM,
        ).forEach { (t, s) -> assertEquals("jam $t", s, Slot.of(LifeState.parseClock(t)!!)) }
    }

    @Test fun shortClockForTopBar() {
        val l = life("Senin", "07:30")
        assertEquals("Sen, 13 Jul · 07:30 ☀️", l.shortClockText)
        l.setCalendar("Senin", "22:05")
        assertEquals("Sen, 13 Jul · 22:05 🌙", l.shortClockText)
    }

    /** Meter 💗 Cinta hanya untuk lawan jenis yang bisa dipacari & keduanya 18+; selain itu 💛 Akrab. */
    @Test fun romanceOnlyOppositeSexAdults() {
        fun ch(gender: String, age: Int, romanceable: Boolean) = GameData.CharacterDef(
            id = "x", name = "X", gender = gender, baseAge = age, role = "", loyal = true, romanceable = romanceable,
            places = setOf("desa"), slots = setOf("semua"), meetable = true, loveStart = 20, trustStart = 40,
            likeTopics = emptySet(), hateTopics = emptySet(), likeItems = emptySet(), hateItems = emptySet(),
            text = emptyMap(), color = 0,
        )
        val social = com.valeriusgames.threadsofdestiny.life.SocialState()
        val male = "laki-laki"; val female = "perempuan"
        assertFalse("sesama jenis", social.canRomance(ch(male, 18, true), 18, male))
        assertFalse("pemain belum 18", social.canRomance(ch(female, 15, true), 15, male))
        assertFalse("karakter tidak bisa dipacari (mis. keluarga)", social.canRomance(ch(female, 41, false), 18, male))
        assertTrue("lawan jenis, sama-sama 18+", social.canRomance(ch(female, 15, true), 18, male))  // usia ikut bertambah
        assertTrue(social.canRomance(ch(male, 15, true), 18, female))
    }

    @Test fun calendarSetsWeekdayAndTime() {
        val l = life("Senin", "pagi")
        assertEquals("Senin", l.weekday)
        assertEquals(6 * 60, l.minute)
        l.setCalendar("Senin", "07:30")
        assertEquals(450, l.minute)
        assertTrue(l.clockText, l.clockText.startsWith("Senin, 13 Jul · 07:30"))
    }

    // ---------------- waktu berjalan ----------------

    @Test fun passingMidnightRollsDayOnce() {
        val l = life(time = "22:00")
        val ended = mutableListOf<Int>()
        l.advance(180) { ended += it }
        assertEquals(1, l.day)
        assertEquals(60, l.minute)
        assertEquals(listOf(0), ended)
        assertEquals("Selasa", l.weekday)
    }

    @Test fun threeDaysPassThreeMidnights() {
        val l = life(time = "07:00")
        l.stats[Stat.KENYANG] = 100; l.stats[Stat.HIDRASI] = 100
        val ended = mutableListOf<Int>()
        // dipecah per 10 jam supaya tidak mati kelaparan dalam tes
        repeat(7) { l.advance(600, sleeping = it % 2 == 1) { d -> ended += d } ; l.stats[Stat.KENYANG] = 100; l.stats[Stat.HIDRASI] = 100 }
        // 07:00 + 70 jam = hari ke-3 pukul 05:00
        assertEquals(listOf(0, 1, 2), ended)
        assertEquals(3, l.day)
        assertEquals(5 * 60, l.minute)
    }

    @Test fun awakeDecayPerHour() {
        val l = life(time = "07:00")
        l.stats[Stat.KENYANG] = 70; l.stats[Stat.HIDRASI] = 70; l.stats[Stat.ENERGI] = 90
        l.advance(10 * 60)
        assertEquals(17 * 60, l.minute)
        assertEquals(50, l[Stat.KENYANG])           // -2 / jam
        assertTrue("hidrasi ${l[Stat.HIDRASI]}", l[Stat.HIDRASI] in 42..43)   // -2,8 / jam
        assertEquals(80, l[Stat.ENERGI])            // -1 / jam
    }

    @Test fun sleepingRestoresEnergy() {
        val l = life(time = "22:00")
        l.stats[Stat.KENYANG] = 80; l.stats[Stat.HIDRASI] = 80; l.stats[Stat.ENERGI] = 20
        l.advance(8 * 60, sleeping = true)
        assertEquals(6 * 60, l.minute)
        assertEquals(1, l.day)
        assertEquals(100, l[Stat.ENERGI])
        assertEquals(72, l[Stat.KENYANG])           // tidur: setengah lajunya
        assertTrue(l[Stat.HIDRASI] in 69..71)
    }

    @Test fun stayingUpLateCostsMore() {
        val normal = life(time = "20:00").apply { stats[Stat.ENERGI] = 80; stats[Stat.KEWARASAN] = 80 }
        normal.advance(120)
        val late = life(time = "00:00").apply { stats[Stat.ENERGI] = 80; stats[Stat.KEWARASAN] = 80 }
        late.advance(120)
        assertEquals(78, normal[Stat.ENERGI])
        assertEquals(74, late[Stat.ENERGI])          // ×3
        assertEquals(78, late[Stat.KEWARASAN])       // -1 / jam
        assertEquals(80, normal[Stat.KEWARASAN])
    }

    @Test fun starvationEventuallyKills() {
        val l = life(time = "07:00")
        l.stats[Stat.KENYANG] = 0; l.stats[Stat.HIDRASI] = 0
        var hours = 0
        while (!l.dead && hours < 100) { l.advance(60); hours++ }
        assertTrue(l.dead)
        assertEquals("dehidrasi", l.deathReason)
        assertTrue("mati dalam $hours jam", hours in 15..30)
    }

    // ---------------- kalender & "sampai kapan" ----------------

    @Test fun absNextWeekday() {
        val l = life("Senin", "15:00")
        assertEquals(l.absMinute + 5 * 1440, l.absNext("Sabtu", 15 * 60))
        assertEquals(l.absMinute + 60, l.absNext("Senin", 16 * 60))      // hari ini, belum lewat
        assertEquals(l.absMinute - 60 + 7 * 1440, l.absNext("Senin", 14 * 60)) // sudah lewat → minggu depan
    }

    @Test fun calendarJumpToNextDayAndLaterTime() {
        val l = life("Minggu", "20:00", date = "12 Juli 2026")
        var newDays = 0
        var passed = 0
        l.calendarJump("Senin", "pagi", { newDays++ }, { passed += it })
        assertEquals(1, newDays)
        assertEquals(0, passed)
        assertEquals("Senin", l.weekday)
        assertEquals(6 * 60, l.minute)
        // hari sama, jam lebih sore → waktu berjalan sungguhan
        l.calendarJump("Senin", "20:00", { newDays++ }, { passed += it })
        assertEquals(14 * 60, passed)
        assertEquals(1, newDays)
    }

    @Test fun nextPeriodStep() {
        assertEquals(5 * 60 to false, life(time = "06:00").nextPeriodStep())        // Pagi → 11:00
        assertEquals(4 * 60 to false, life(time = "11:00").nextPeriodStep())        // Siang → 15:00
        assertEquals(11 * 60 + 30 to true, life(time = "18:30").nextPeriodStep())   // Malam → tidur sampai 06:00
        assertEquals(4 * 60 to true, life(time = "02:00").nextPeriodStep())
    }

    // ---------------- sekolah: hadir, terlambat, bolos ----------------

    @Test fun canAttendWindow() {
        val gd = GameDataStub.empty()
        val s = school()
        assertTrue(gd.canAttendNow(s, 0, 6 * 60))
        assertFalse(gd.canAttendNow(s, 0, 5 * 60 + 59))
        assertTrue(gd.canAttendNow(s, 0, 13 * 60))
        assertFalse(gd.canAttendNow(s, 0, 13 * 60 + 1))
        assertFalse("Minggu libur", gd.canAttendNow(s, 6, 8 * 60))
    }

    @Test fun absenceRules() {
        val defs = mapOf("sma" to school())
        val l = life("Senin", "06:00")
        l.jobs += LifeState.Job("sma", startDay = 0)
        assertTrue("hari pertama tidak dihitung", l.dueAbsences(defs, 0, wholeDay = true).isEmpty())
        l.advance(1440) // Selasa 06:00
        l.minute = 12 * 60
        assertTrue("belum lewat batas 13:00", l.dueAbsences(defs, 1, false).isEmpty())
        l.minute = 13 * 60 + 1
        assertEquals(1, l.dueAbsences(defs, 1, false).size)
        l.markAttended("sma", 1)
        assertTrue("sudah hadir", l.dueAbsences(defs, 1, false).isEmpty())
        // Minggu (hari ke-6) libur
        assertTrue(l.dueAbsences(defs, 6, true).isEmpty())
        // Rabu dicatat absen sekali saja
        assertEquals(1, l.dueAbsences(defs, 2, true).size)
        l.markAbsent("sma", 2)
        assertTrue(l.dueAbsences(defs, 2, true).isEmpty())
    }

    @Test fun attendanceMarksPerDay() {
        val l = life()
        l.markAttended("a")
        assertTrue(l.hasAttended("a"))
        assertFalse(l.hasAttended("a", 1))
        l.advance(1440)
        assertFalse(l.hasAttended("a"))
        assertTrue(l.hasAttended("a", 0))
    }

    // ---------------- simpanan ----------------

    @Test fun saveRoundTripKeepsMinute() {
        val l = life("Rabu", "16:45")
        val back = LifeState.fromJson(l.toJson())
        assertEquals(l.minute, back.minute)
        assertEquals(l.day, back.day)
        assertEquals("Rabu", back.weekday)
    }

    @Test fun oldSaveWithoutMinuteUsesSlot() {
        val old = """{"active":true,"day":2,"slot":"SORE","money":5000}"""
        val l = LifeState.fromJson(old)
        assertEquals(15 * 60, l.minute)
        assertEquals(Slot.SORE, l.slot)
    }
}
