package com.valeriusgames.threadsofdestiny

import com.valeriusgames.threadsofdestiny.life.GameData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Balasan chat (data/pesan.txt) dan batas kegiatan berulang (data/aktivitas.txt). */
class PhoneAndActivityTest {

    private val dataDir = File("src/main/assets/data")

    private fun sections(name: String): List<Pair<String, Map<String, String>>> {
        val out = mutableListOf<Pair<String, MutableMap<String, String>>>()
        File(dataDir, name).readLines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.forEach { line ->
            if (line.startsWith("[") && line.endsWith("]")) out += line.substring(1, line.length - 1).trim() to mutableMapOf()
            else if (line.contains('=') && out.isNotEmpty())
                out.last().second[line.substringBefore('=').trim().lowercase()] = line.substringAfter('=').trim()
        }
        return out
    }

    private val messages by lazy { sections("pesan.txt").map { (id, m) -> GameData.message(id, m) } }
    private val chars by lazy { sections("karakter.txt").toMap() }

    // ---------------- pesan ----------------

    @Test fun everyMessageHasMatchingReplies() {
        assertTrue(messages.size >= 20)
        messages.forEach { m ->
            assertTrue("${m.id}: dari", m.from == "*" || m.from in chars)
            assertTrue("${m.id}: jenis", m.kind in setOf("cerita", "acak", "sistem"))
            listOf(m.text, m.warm, m.warmReply, m.short, m.shortReply).forEach { assertTrue("${m.id}: kolom kosong", it.isNotBlank()) }
            // balasan pemain tidak boleh sama persis dengan pesan masuk / jawaban
            assertTrue("${m.id}: balasan hangat & singkat sama", m.warm != m.short)
        }
        assertEquals("id pesan dobel", messages.size, messages.map { it.id }.toSet().size)
    }

    @Test fun storyMessagesInScriptsHaveReplies() {
        val nameToId = chars.flatMap { (id, m) -> listOfNotNull(m["nama"], m["nama_l"], m["nama_p"]).map { it.lowercase() to id } }.toMap()
        var checked = 0
        File("src/main/assets/naskah").listFiles { f -> f.name.endsWith(".txt") }!!.forEach { f ->
            f.readLines().map { it.trim() }.filter { it.startsWith("@hp pesan ") && it.contains('|') }.forEach { l ->
                val sender = l.removePrefix("@hp pesan ").substringBefore('|').trim()
                val text = l.substringAfter('|').trim()
                val id = nameToId[sender.lowercase()] ?: return@forEach
                checked++
                assertTrue("${f.name}: pesan $sender tanpa balasan: $text",
                    messages.any { (it.from == id || it.from == "*") && it.text.trim() == text })
            }
        }
        assertTrue(checked >= 4)
    }

    @Test fun villageOnlyMessagesNotSentInCity() {
        // pesan yang menyebut desa/dermaga hanya terkirim di desa, rindu rumah hanya saat di kota
        messages.filter { it.kind == "acak" }.forEach { m ->
            if (m.text.contains("dermaga") && m.from != "sahabat") assertEquals(m.id, "lokasi == desa", m.condition)
            if (m.text.contains("Kapan pulang") || m.text.contains("pulang bawain")) assertEquals(m.id, "lokasi == kota", m.condition)
        }
    }

    // ---------------- kegiatan berulang ----------------

    @Test fun repeatLimits() {
        // batas 1×/hari
        assertNull(GameData.repeatBlock(1, null, 0, null, 600))
        assertNotNull(GameData.repeatBlock(1, null, 1, 500, 900))
        // makan: jeda 4 jam dari jam mulai
        val eatAt = 7 * 60L
        assertNotNull(GameData.repeatBlock(3, 240, 1, eatAt, eatAt + 120))
        assertTrue(GameData.repeatBlock(3, 240, 1, eatAt, eatAt + 120)!!.contains("11:00"))
        assertNull(GameData.repeatBlock(3, 240, 1, eatAt, eatAt + 240))
        assertNotNull(GameData.repeatBlock(3, 240, 3, eatAt, eatAt + 600))
        // jeda melewati tengah malam (hari berganti, hitungan per hari mulai lagi dari 0)
        val late = 1439L - 30
        assertNotNull(GameData.repeatBlock(2, 180, 0, late, late + 60))
        assertNull(GameData.repeatBlock(2, 180, 0, late, late + 180))
    }

    @Test fun activitiesCannotBeSpammed() {
        val acts = sections("aktivitas.txt").toMap()
        // kegiatan yang menambah energi/kenyang atau memberi uang/petunjuk wajib dibatasi
        acts.forEach { (id, m) ->
            val ef = m["efek"].orEmpty()
            val restoring = Regex("(energi|kenyang|kewarasan) \\+").containsMatchIn(ef)
            val reward = Regex("(uang \\+|petunjuk_sahabat|akademik)").containsMatchIn(ef)
            if (restoring || reward) assertTrue("$id perlu batas/jeda", m["batas"] != null || m["jeda"] != null)
            m["batas"]?.let { assertTrue("$id batas", it.toIntOrNull() != null) }
            m["jeda"]?.let { assertNotNull("$id jeda", GameData.minutes(it)) }
        }
        assertEquals(240, GameData.minutes(acts.getValue("makan_keluarga")["jeda"]))
        assertEquals("1", acts.getValue("bantu_warung")["batas"])
        assertNotNull("Ibu menegur bila tidur lagi", acts.getValue("istirahat")["teks_ulang"])
    }
}
