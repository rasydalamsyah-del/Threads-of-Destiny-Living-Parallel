package com.valeriusgames.threadsofdestiny

import com.valeriusgames.threadsofdestiny.life.GameData
import com.valeriusgames.threadsofdestiny.story.StoryScript
import com.valeriusgames.threadsofdestiny.story.StoryScript.Cmd
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/** Adegan waktu bebas (data/adegan.txt): pemicu, jadwal, dan setiap jalur cerita adegan berakhir di @kembali. */
class SceneTest {

    private val dataDir = File("src/main/assets/data")
    private val naskahDir = File("src/main/assets/naskah")

    private fun sections(name: String): List<Pair<String, Map<String, String>>> {
        val out = mutableListOf<Pair<String, MutableMap<String, String>>>()
        File(dataDir, name).readLines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.forEach { line ->
            if (line.startsWith("[") && line.endsWith("]")) out += line.substring(1, line.length - 1).trim() to mutableMapOf()
            else if (line.contains('=') && out.isNotEmpty())
                out.last().second[line.substringBefore('=').trim().lowercase()] = line.substringAfter('=').trim()
        }
        return out
    }

    private val scenes by lazy { sections("adegan.txt").map { (id, m) -> GameData.scene(id, m) } }
    private val chars by lazy { sections("karakter.txt").map { it.first }.toSet() }
    private val locs by lazy { sections("lokasi.txt").map { it.first }.toSet() }

    @Test fun manyScenesForTheVillage() {
        assertTrue("adegan desa harus banyak (sekarang ${scenes.size})", scenes.size >= 40)
        assertEquals("id adegan dobel", scenes.size, scenes.map { it.id }.toSet().size)
        // setiap karakter desa punya beberapa adegan
        listOf("ayah", "ibu", "laras", "bayu", "sekar", "darto").forEach { c ->
            val n = scenes.count { it.person == c } + scenes.count { s -> s.trigger == "masuk" && File(naskahDir, s.file).readText().contains("@tampil $c") && s.person == null }
            assertTrue("$c hanya punya $n adegan", n >= 4)
        }
    }

    @Test fun triggersPointToRealThings() {
        scenes.forEach { s ->
            assertTrue("${s.id}: pemicu", s.trigger in setOf("masuk", "orang"))
            if (s.trigger == "orang") assertTrue("${s.id}: orang ${s.person}", s.person in chars)
            if (s.trigger == "masuk") assertTrue("${s.id}: lokasi ${s.location}", s.location in locs)
            s.location?.let { assertTrue("${s.id}: lokasi $it", it in locs) }
            s.condition?.split("&&", "||")?.forEach { part ->
                assertTrue("${s.id}: syarat '$part'", Regex("^\\s*(\\w+)\\s*(==|!=|>=|<=|>|<)\\s*(.+?)\\s*$").matches(part))
            }
        }
    }

    /** Adegan "temui orang di lokasi X" hanya bisa terjadi bila jadwal orang itu memang membawanya ke X pada jam adegan. */
    @Test fun personScenesMatchSchedules() {
        val charText = sections("karakter.txt").toMap()
        scenes.filter { it.trigger == "orang" && it.location != null }.forEach { s ->
            val t = charText.getValue(s.person!!)
            if (t["lokasi"].isNullOrBlank()) return@forEach
            val ok = (0..6).any { wd -> (0 until 24 * 60 step 15).any { m ->
                GameData.inSchedule(s.schedule, wd, m) &&
                    (GameData.locationAt(t["lokasi"], wd, m) == s.location || GameData.locationAt(t["lokasi_libur"], wd, m) == s.location)
            } }
            assertTrue("${s.id}: ${s.person} tidak pernah di ${s.location} pada jam adegan", ok)
        }
    }

    @Test fun leaveCommandAndScheduleBlock() {
        val s = StoryScript.parse(listOf("@pergi ayah ibu laras"))
        assertEquals(Cmd.Leave(listOf("ayah", "ibu", "laras")), s.cmds[0])
        val spec = "setiap hari 06:15-07:15 ruang_makan, setiap hari 12:00-13:00 ruang_makan"
        assertEquals(7 * 60 + 15 to "ruang_makan", GameData.blockAt(spec, 0, 6 * 60 + 40))
        assertEquals(13 * 60 to "ruang_makan", GameData.blockAt(spec, 3, 12 * 60 + 5))
        assertEquals(null, GameData.blockAt(spec, 3, 9 * 60))
    }

    @Test fun parentsRoomLockedAtNightAndActivityImages() {
        val lok = sections("lokasi.txt").toMap()
        val room = GameData.location("kamar_ayah_ibu", lok.getValue("kamar_ayah_ibu"))
        assertTrue(room.lockedText(22 * 60)!!.contains("Ibu"))
        assertTrue(room.lockedText(3 * 60) != null)
        assertTrue(room.lockedText(9 * 60)!!.contains("Ayah"))
        assertEquals(null, room.lockedText(14 * 60))
        val akt = sections("aktivitas.txt").toMap()
        val belajar = GameData.activity("belajar", akt.getValue("belajar"))
        assertEquals("meja_belajar", belajar.sceneFor("kamar_pagi", false))
        assertEquals(null, belajar.sceneFor("kos_cendana", true))
        assertEquals("buku_harian", GameData.activity("buku_harian", akt.getValue("buku_harian")).sceneFor("kamar_pagi", false))
    }

    @Test fun scheduleParsing() {
        val sc = GameData.scene("x", mapOf("naskah" to "a.txt", "lokasi" to "lapangan", "waktu" to "senin-sabtu 15:00-17:00, minggu 08:00-10:30"))
        assertEquals("masuk", sc.trigger)
        assertTrue(GameData.inSchedule(sc.schedule, 0, 16 * 60))       // Senin 16:00
        assertFalse(GameData.inSchedule(sc.schedule, 0, 17 * 60 + 30)) // Senin 17:30
        assertTrue(GameData.inSchedule(sc.schedule, 6, 9 * 60))        // Minggu 09:00
        assertFalse(GameData.inSchedule(sc.schedule, 6, 16 * 60))      // Minggu 16:00
        assertTrue(GameData.inSchedule(emptyList(), 3, 3))             // tanpa jadwal = kapan saja
        assertEquals("orang", GameData.scene("y", mapOf("orang" to "bayu")).trigger)
    }

    /** Telusuri semua cabang (pilihan, @jika, @lompat) dari label adegan: semuanya harus berakhir di @kembali. */
    @Test fun everyBranchReturns() {
        val parsed = HashMap<String, StoryScript.Script>()
        scenes.forEach { s ->
            val script = parsed.getOrPut(s.file) { StoryScript.parse(File(naskahDir, s.file).readLines()) }
            val start = script.labels[s.label] ?: run { fail("${s.id}: label ${s.label} tidak ada di ${s.file}"); 0 }
            val otherStarts = scenes.filter { it.file == s.file && it.id != s.id }.mapNotNull { script.labels[it.label] }.toSet()
            val seen = HashSet<Int>()
            val todo = ArrayDeque(listOf(start))
            var returns = 0
            while (todo.isNotEmpty()) {
                var pc = todo.removeFirst()
                while (true) {
                    if (pc in otherStarts) fail("${s.id}: masuk ke adegan lain tanpa @kembali (baris perintah $pc di ${s.file})")
                    if (!seen.add(pc)) break
                    if (pc >= script.cmds.size) fail("${s.id}: naskah habis tanpa @kembali")
                    when (val c = script.cmds[pc]) {
                        is Cmd.Return -> { returns++; break }
                        is Cmd.Free, is Cmd.LoadScript, is Cmd.End -> fail("${s.id}: adegan tidak boleh berisi @bebas / @naskah / @tamat")
                        is Cmd.Jump -> pc = script.labels.getValue(c.label)
                        is Cmd.JumpIf -> { todo.add(script.labels.getValue(c.label)); pc++ }
                        is Cmd.Choice -> { c.options.forEach { todo.add(script.labels.getValue(it.label)) }; break }
                        is Cmd.Apply -> { todo.add(script.labels.getValue(c.okLabel)); todo.add(script.labels.getValue(c.noLabel)); break }
                        else -> pc++
                    }
                }
            }
            assertTrue("${s.id}: tidak ada @kembali", returns > 0)
        }
    }

    @Test fun storyFilesHaveScenesForEveryChapter() {
        val ids = scenes.map { it.id }
        assertTrue(ids.count { it.startsWith("p_") } >= 5)
        assertTrue(ids.count { it.startsWith("b2_") } >= 10)
        assertTrue(ids.count { it.startsWith("b3_") } >= 9)
        assertTrue(ids.count { it.startsWith("b4_") } >= 18)
        assertTrue(ids.count { it.startsWith("k3_") } >= 10)
        assertTrue(ids.count { it.startsWith("k4_") } >= 20)
    }
}
