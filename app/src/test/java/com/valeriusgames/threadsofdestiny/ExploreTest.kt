package com.valeriusgames.threadsofdestiny

import com.valeriusgames.threadsofdestiny.life.GameData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.valeriusgames.threadsofdestiny.story.StoryScript
import java.io.File

/** Lokasi jelajah saat waktu bebas (data/lokasi.txt) & di mana karakter berada. */
class ExploreTest {

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

    private val locs by lazy { sections("lokasi.txt").associate { (id, m) -> id to GameData.location(id, m) } }
    private val chars by lazy { sections("karakter.txt").toMap() }
    private val SENIN = 0
    private val MINGGU = 6

    private fun at(id: String, wd: Int, clock: String): String? {
        val min = clock.substringBefore(':').toInt() * 60 + clock.substringAfter(':').toInt()
        return GameData.locationAt(chars.getValue(id)["lokasi"], wd, min)
    }

    @Test fun familyIsWhereTheStorySays() {
        // jadwal lengkap: docs/CERITA.md bagian 3
        assertEquals("sarapan bersama", "ruang_makan", at("ayah", SENIN, "06:30"))
        assertNull("Ayah tidur pagi", at("ayah", SENIN, "09:00"))
        assertEquals("ruang_makan", at("ayah", SENIN, "12:30"))
        assertEquals("jaring di depan rumah", "rumah_warung", at("ayah", SENIN, "15:00"))
        assertEquals("radio sore", "ruang_tengah", at("ayah", SENIN, "16:30"))
        assertEquals("berangkat melaut", "dermaga", at("ayah", SENIN, "18:45"))
        assertNull("Ayah di laut", at("ayah", SENIN, "23:00"))
        assertEquals("Ibu memasak sarapan di dapur", "dapur", at("ibu", SENIN, "05:30"))
        assertEquals("sarapan", "ruang_makan", at("ibu", SENIN, "06:30"))
        assertEquals("Ayah membasuh muka di sumur", "halaman_belakang", at("ayah", SENIN, "05:45"))
        assertEquals("rumah_warung", at("ibu", SENIN, "11:00"))
        assertEquals("sinetron", "ruang_tengah", at("ibu", SENIN, "19:30"))
        assertNull("Ibu tidur", at("ibu", SENIN, "22:00"))
        assertNull("Laras sekolah", at("laras", SENIN, "10:00"))
        assertEquals("makan siang", "ruang_makan", at("laras", SENIN, "13:30"))
        assertEquals("PR di kamarnya", "kamar_laras", at("laras", SENIN, "15:00"))
        assertEquals("TV malam", "ruang_tengah", at("laras", SENIN, "19:00"))
        assertEquals("menggambar sebelum tidur", "kamar_laras", at("laras", SENIN, "20:45"))
        // tidur: tampil di kamarnya, bukan jadwal bangun
        assertNull("tidur bukan jadwal bangun", at("laras", SENIN, "22:00"))
        assertEquals("kamar_laras", GameData.locationAt(chars.getValue("laras")["tidur"], 0, 22 * 60))
        assertEquals("kamar_laras", GameData.locationAt(chars.getValue("laras")["tidur"], 1, 5 * 60))
        assertNull(GameData.locationAt(chars.getValue("laras")["tidur"], 1, 7 * 60))
        assertEquals("rumah_warung", at("laras", MINGGU, "09:00"))
        assertEquals("layangan Minggu", "lapangan", at("laras", MINGGU, "15:00"))
        assertEquals("lapangan", at("bayu", SENIN, "16:00"))
        assertEquals("dermaga", at("bayu", SENIN, "18:00"))
        assertNull("Bayu melaut subuh", at("bayu", SENIN, "06:00"))
        assertEquals("dermaga", at("sekar", SENIN, "16:00"))
        assertEquals("gudang_ikan", at("darto", SENIN, "08:00"))
        assertNull("gudang tutup Minggu", at("darto", MINGGU, "08:00"))
        assertEquals("toko_buku", at("nadia", SENIN, "15:00"))
    }

    @Test fun scheduleHasNoGapsOrDoubles() {
        // satu orang tidak boleh berada di dua tempat sekaligus; "lokasi" menggantikan "jadwal"
        chars.filter { it.value["lokasi"] != null }.forEach { (id, m) ->
            assertNull("$id: hapus kolom jadwal (sudah diwakili lokasi)", m["jadwal"])
            val parts = m["lokasi"]!!.split(',').map { it.trim() }
            for (wd in 0..6) for (min in 0 until 1440 step 15) {
                val hits = parts.count { GameData.locationAt(it, wd, min) != null }
                assertTrue("$id hari $wd menit $min ada di $hits tempat", hits <= 1)
            }
        }
    }

    @Test fun presenceImagesHaveFixedSpots() {
        // setiap orang yang punya jadwal di sebuah lokasi punya titik berdiri di lokasi itu (untuk area sentuh)
        chars.forEach { (id, m) ->
            m["lokasi"].orEmpty().split(',').mapNotNull { it.trim().split(Regex("\\s+")).lastOrNull() }.filter { it.isNotEmpty() }.forEach { p ->
                assertTrue("$id di $p", locs.getValue(p).people.any { it.charId == id })
            }
        }
        // lokasi baru desa punya gambar pengganti sampai gambarnya dibuat
        assertEquals("kota_kecil", locs.getValue("lapangan").fallback)
        assertEquals("dermaga_malam", locs.getValue("dermaga").fallback)
    }

    @Test fun everyPersonHasAPlaceToStand() {
        chars.forEach { (id, m) ->
            val places = m["lokasi"].orEmpty().split(',').mapNotNull { it.trim().split(Regex("\\s+")).lastOrNull()?.takeIf { s -> s.isNotEmpty() } } +
                listOfNotNull(m["tempat_janji"])
            places.forEach { p ->
                val loc = locs[p]
                assertTrue("$id: lokasi $p tidak ada", loc != null)
                assertTrue("$id: belum ada posisi berdiri di $p", loc!!.people.any { it.charId == id })
            }
        }
    }

    @Test fun locationsConnectedAndOnScreen() {
        // semua lokasi bisa dicapai dari rumah (desa) atau kos (kota), dan bisa kembali
        fun reach(start: String): Set<String> {
            val seen = mutableSetOf(start)
            val queue = ArrayDeque(listOf(start))
            while (queue.isNotEmpty()) {
                val cur = locs.getValue(queue.removeFirst())
                cur.doors.map { if (it.target == "kos") "kos_kamar" else it.target }.forEach { if (seen.add(it)) queue.add(it) }
            }
            return seen
        }
        val desa = locs.values.filter { it.area == "desa" }.map { it.id }.toSet()
        assertTrue(reach("rumah_warung").containsAll(desa))
        val kota = locs.values.filter { it.area == "kota" && !it.id.startsWith("kos_") }.map { it.id }.toSet()
        assertTrue(reach("kos_kamar").containsAll(kota))
        locs.values.forEach { l ->
            (l.doors + l.objects).forEach { s -> assertTrue("${l.id}: ${s.label} terpotong (x ${s.x})", s.x in 0.1f..0.9f && s.y in 0f..1f) }
            l.doors.forEach { assertTrue("${l.id}: menit", it.minutes in 0..60) }
        }
        // di dalam rumah tidak memakan waktu, ke gudang ikan 15 menit, ke lapangan & dermaga 5 menit
        assertEquals(0, locs.getValue("ruang_tengah").doors.first { it.target == "kamar_pagi" }.minutes)
        assertEquals(5, locs.getValue("rumah_warung").doors.first { it.target == "lapangan" }.minutes)
        assertEquals(5, locs.getValue("rumah_warung").doors.first { it.target == "dermaga" }.minutes)
        assertTrue("ruang tengah tersambung ke ruang makan", locs.getValue("ruang_tengah").doors.any { it.target == "ruang_makan" })
        assertEquals(15, locs.getValue("rumah_warung").doors.first { it.target == "gudang_ikan" }.minutes)
    }

    @Test fun activitiesBelongToARealPlace() {
        val acts = sections("aktivitas.txt").toMap()
        acts.forEach { (id, m) ->
            m["lokasi"]?.split(',')?.map { it.trim() }?.forEach { l ->
                assertTrue("$id: lokasi $l", l == "kos" || l in locs)
            }
        }
        // benda di layar menunjuk aktivitas yang memang dilakukan di lokasi itu
        locs.values.forEach { l ->
            l.objects.forEach { o ->
                val where = acts[o.target]?.get("lokasi").orEmpty().split(',').map { it.trim() }
                assertTrue("${l.id}: ${o.target} bukan di sini", l.id in where || ("kos" in where && l.id.startsWith("kos_")))
            }
        }
        assertEquals("ruang_makan", acts.getValue("makan_keluarga")["lokasi"])
    }

    @Test fun walkCommandParses() {
        val c = StoryScript.parse(listOf("@jalan ruang_makan | Ibu memanggil")).cmds[0]
        assertEquals(StoryScript.Cmd.Walk("ruang_makan", "Ibu memanggil"), c)
        // prolog: setelah bangun, pemain berjalan sendiri dari kamar ke ruang makan
        val prolog = File("src/main/assets/naskah/prolog.txt").readLines().map { it.trim() }
        assertTrue(prolog.any { it.startsWith("@jalan ruang_makan") })
        val path = setOf("kamar_pagi", "ruang_tengah", "ruang_makan")
        assertTrue(locs.getValue("kamar_pagi").doors.any { it.target == "ruang_tengah" })
        assertTrue(locs.getValue("ruang_tengah").doors.any { it.target == "ruang_makan" })
        assertTrue(path.all { it in locs })
    }
}
