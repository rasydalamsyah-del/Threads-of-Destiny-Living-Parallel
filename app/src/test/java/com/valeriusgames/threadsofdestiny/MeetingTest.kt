package com.valeriusgames.threadsofdestiny

import com.valeriusgames.threadsofdestiny.life.GameData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Jadwal kapan karakter bisa ditemui & data obrolan (data/obrolan.txt). */
class MeetingTest {

    private val dataDir = File("src/main/assets/data")

    /** Baca file data berformat [id] / kunci = nilai (sama seperti GameData.readSections). */
    private fun sections(name: String): List<Pair<String, Map<String, String>>> {
        val out = mutableListOf<Pair<String, MutableMap<String, String>>>()
        File(dataDir, name).readLines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.forEach { line ->
            if (line.startsWith("[") && line.endsWith("]")) out += line.substring(1, line.length - 1).trim() to mutableMapOf()
            else if (line.contains('=') && out.isNotEmpty())
                out.last().second[line.substringBefore('=').trim().lowercase()] = line.substringAfter('=').trim()
        }
        return out
    }

    private val chars by lazy { sections("karakter.txt").toMap() }

    private fun canMeet(id: String, weekday: Int, clock: String): Boolean {
        val m = chars.getValue(id)
        val slots = m["waktu"].orEmpty().split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
        val minute = clock.substringBefore(':').toInt() * 60 + clock.substringAfter(':').toInt()
        // sama seperti GameData.meetableNow: kolom "lokasi" menggantikan "jadwal"
        if (!m["lokasi"].isNullOrBlank())
            return (m["ditemui"] ?: "ya").trim() != "tidak" && GameData.locationAt(m["lokasi"], weekday, minute) != null
        return GameData.meetable(m["ditemui"], m["jadwal"], slots, weekday, minute)
    }

    private val SENIN = 0
    private val MINGGU = 6

    @Test fun morningAtHomeOnlyIbu() {
        // Senin pagi sesudah sarapan: Ayah tidur (habis melaut), Laras sekolah, Bayu melaut bersama bapaknya
        assertTrue(canMeet("ibu", SENIN, "08:00"))
        assertFalse(canMeet("ayah", SENIN, "08:00"))
        assertFalse(canMeet("laras", SENIN, "08:00"))
        assertFalse(canMeet("bayu", SENIN, "08:00"))
        assertFalse(canMeet("sekar", SENIN, "08:00"))
    }

    @Test fun afternoonFamilyIsHome() {
        assertTrue(canMeet("ayah", SENIN, "13:00"))
        assertTrue(canMeet("laras", SENIN, "14:00"))
        assertFalse(canMeet("laras", SENIN, "12:00"))
        assertTrue(canMeet("laras", MINGGU, "09:00"))   // Minggu tidak sekolah
        assertFalse(canMeet("ayah", SENIN, "20:00"))    // berangkat melaut / tidur
    }

    @Test fun appointmentOnlyAndNeverMeetable() {
        for (h in 0 until 24) {
            val c = "%02d:00".format(h)
            assertFalse(canMeet("dimas", SENIN, c))
            assertFalse(canMeet("sahabat", SENIN, c))
        }
        // Bayu & Sekar: punya tempat main sendiri (lapangan / dermaga), juga bisa diajak janjian
        assertTrue(canMeet("bayu", SENIN, "16:00"))
        assertTrue(canMeet("sekar", SENIN, "16:00"))
        assertFalse(canMeet("sekar", MINGGU, "16:00"))
        assertEquals("dermaga", chars.getValue("bayu")["tempat_janji"])
    }

    @Test fun overnightSchedule() {
        val s = setOf("malam")
        assertTrue(GameData.meetable("ya", "setiap hari 19:00-02:00", s, SENIN, 23 * 60))
        assertTrue(GameData.meetable("ya", "senin 19:00-02:00", s, 1, 60))      // Selasa 01:00 = lanjutan Senin malam
        assertFalse(GameData.meetable("ya", "senin 19:00-02:00", s, 2, 60))
    }

    @Test fun everyScheduleParses() {
        chars.forEach { (id, m) ->
            val j = m["jadwal"] ?: return@forEach
            assertEquals("jadwal $id: $j", j.split(',').size, GameData.schedule(j).size)
            assertTrue("ditemui $id", (m["ditemui"] ?: "ya") in setOf("ya", "janji", "tidak"))
        }
    }

    // ---------------- obrolan ----------------

    private val chats by lazy { sections("obrolan.txt").map { (id, m) -> GameData.chat(id, m) } }

    @Test fun chatsParseAndBelongToCharacters() {
        assertTrue(chats.isNotEmpty())
        assertEquals("id obrolan dobel", chats.size, chats.map { it.id }.toSet().size)
        chats.forEach { c ->
            assertTrue("${c.id}: karakter ${c.charId}", c.charId in chars)
            assertTrue("${c.id}: jenis", c.kind in setOf("santai", "kenal", "khusus"))
            assertTrue("${c.id}: kalimat pemain", c.say.isNotBlank())
            assertTrue("${c.id}: jawaban", c.lines.isNotEmpty())
            if (c.kind == "kenal") {
                assertTrue("${c.id}: kenal harus membuka info", c.reveal != null)
                assertTrue("${c.id}: kenal hanya sekali", c.once)
            }
            if (c.kind == "santai") assertFalse("${c.id}: santai boleh diulang", c.once)
            if (c.needLove > 0 || c.needTrust > 0) assertTrue("${c.id}: perlu kalimat tolak", c.refuse != null)
        }
    }

    @Test fun everyMeetableCharacterHasOwnConversation() {
        // Tiap orang yang bisa ditemui punya obrolan santai sendiri (tidak ada topik umum yang sama untuk semua)
        chars.filter { (_, m) -> (m["ditemui"] ?: "ya") != "tidak" }.keys.forEach { id ->
            assertTrue("$id belum punya obrolan santai", chats.any { it.charId == id && it.kind == "santai" })
        }
        val says = chats.map { it.say }
        assertEquals("kalimat pemain dobel antar-obrolan", says.size, says.toSet().size)
    }

    @Test fun revealedFactsExistInProfile() {
        chats.forEach { c ->
            c.reveal?.let { key -> assertTrue("${c.id}: ${c.charId} tidak punya $key", chars.getValue(c.charId)[key] != null) }
            c.revealOther?.let { (o, key) -> assertTrue("${c.id}: $o tidak punya $key", chars[o]?.get(key) != null) }
            // pertanyaan "kenal" untuk info yang sudah diketahui dari awal tidak akan pernah muncul
            val known = chars.getValue(c.charId)["tahu_awal"].orEmpty().split(',').map { it.trim() }
            if (c.kind == "kenal") assertFalse("${c.id}: ${c.reveal} sudah diketahui dari awal", c.reveal in known)
        }
    }

    @Test fun topicReactionMatchesLikes() {
        // obrolan bertopik yang ia sukai → cinta naik; yang ia benci → turun (sesuai profil)
        chats.filter { it.topic != null }.forEach { c ->
            val m = chars.getValue(c.charId)
            fun list(k: String) = m[k].orEmpty().split(',').map { it.trim() }
            val love = c.effects["cinta"] ?: 0L
            if (c.topic in list("suka_topik")) assertTrue("${c.id} disukai tapi cinta $love", love > 0)
            if (c.topic in list("benci_topik")) assertTrue("${c.id} dibenci tapi cinta $love", love < 0)
        }
    }

    @Test fun chatEffectsAndTopicsParse() {
        val ayah = chats.first { it.id == "ayah_kisah1" }
        assertEquals(3L, ayah.effects["cinta"])
        assertEquals(2L, ayah.effects["percaya"])
        val sahabat = chats.first { it.id == "ayah_sahabat" }
        assertEquals(1L, sahabat.effects["petunjuk_sahabat"])
        assertEquals("sahabat" to "kisah_2", sahabat.revealOther)
        val topics = sections("topik.txt").map { it.first }.toSet()
        chats.mapNotNull { it.topic }.forEach { assertTrue("topik $it", it in topics) }
        assertEquals(3, chats.first { it.id == "ayah_kisah2" }.lines.size)
    }
}
