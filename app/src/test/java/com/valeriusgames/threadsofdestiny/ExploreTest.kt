package com.valeriusgames.threadsofdestiny

import com.valeriusgames.threadsofdestiny.life.GameData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
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
        assertNull("Ayah tidur pagi", at("ayah", SENIN, "09:00"))
        assertEquals("ruang_makan", at("ayah", SENIN, "12:30"))
        assertEquals("rumah_warung", at("ayah", SENIN, "15:00"))
        assertEquals("rumah_warung", at("ibu", SENIN, "11:00"))
        assertEquals("ruang_makan", at("ibu", SENIN, "19:00"))
        assertNull("Laras sekolah", at("laras", SENIN, "10:00"))
        assertEquals("ruang_makan", at("laras", SENIN, "14:00"))
        assertEquals("rumah_warung", at("laras", MINGGU, "09:00"))
        assertEquals("gudang_ikan", at("darto", SENIN, "08:00"))
        assertNull("gudang tutup Minggu", at("darto", MINGGU, "08:00"))
        assertEquals("toko_buku", at("nadia", SENIN, "15:00"))
    }

    @Test fun locationMatchesPresenceSchedule() {
        // "lokasi" dan "jadwal" (kapan bisa ditemui) harus sama persis tiap 15 menit sepanjang minggu
        chars.filter { it.value["lokasi"] != null }.forEach { (id, m) ->
            val slots = m["waktu"].orEmpty().split(',').map { it.trim() }.toSet()
            for (wd in 0..6) for (min in 0 until 1440 step 15) {
                val here = GameData.locationAt(m["lokasi"], wd, min) != null
                val meet = GameData.meetable(m["ditemui"], m["jadwal"], slots, wd, min)
                assertEquals("$id hari $wd menit $min", meet, here)
            }
        }
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
        // di dalam rumah tidak memakan waktu, ke gudang ikan 15 menit
        assertEquals(0, locs.getValue("ruang_makan").doors.first { it.target == "kamar_pagi" }.minutes)
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
}
