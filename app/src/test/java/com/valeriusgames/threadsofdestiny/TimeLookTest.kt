package com.valeriusgames.threadsofdestiny

import com.valeriusgames.threadsofdestiny.game.TimeLook
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Pewarnaan latar sesuai jam. */
class TimeLookTest {

    private fun bright(rgb: IntArray) = rgb.sum()

    @Test fun nightIsDarkAndBluish() {
        val m = TimeLook.matrix("pagi", "malam")!!
        val sky = TimeLook.apply(m, intArrayOf(200, 210, 230))
        assertTrue("malam lebih gelap", bright(sky) < 0.7 * (200 + 210 + 230))
        assertTrue("malam kebiruan", sky[2] > sky[0])
    }

    @Test fun eveningIsWarm() {
        val c = TimeLook.apply(TimeLook.matrix("siang", "sore")!!, intArrayOf(180, 180, 180))
        assertTrue(c[0] > c[2] + 30)
    }

    @Test fun sameTimeOrFixedNotChanged() {
        assertNull(TimeLook.matrix("pagi", "pagi"))
        assertNull(TimeLook.matrix("tetap", "malam"))
        assertNull(TimeLook.matrix(null, "malam"))
        assertNull(TimeLook.matrix("pagi", null))
        // gambar malam tidak dipaksa jadi terang (hasilnya pucat)
        assertNull(TimeLook.matrix("malam", "siang"))
    }

    @Test fun roundTripBackToDay() {
        // gambar sore yang ditampilkan siang: warna jingga dikurangi
        val m = TimeLook.matrix("sore", "siang")!!
        val c = TimeLook.apply(m, intArrayOf(200, 160, 110))
        assertTrue(c[2] > 110 && c[0] < 200)
    }

    @Test fun charactersTintedSofterThanBackground() {
        val full = TimeLook.apply(TimeLook.matrix("siang", "malam")!!, intArrayOf(220, 200, 180))
        val soft = TimeLook.apply(TimeLook.blend(TimeLook.matrix("siang", "malam"), 0.45f)!!, intArrayOf(220, 200, 180))
        assertTrue(bright(soft) > bright(full))
        assertNotNull(TimeLook.lerp(null, TimeLook.matrix("siang", "sore"), 0.5f))
    }

    @Test fun everyBackgroundHasTime() {
        val listed = File("src/main/assets/data/latar.txt").readLines().map { it.trim() }
            .filter { it.startsWith("[") }.map { it.trim('[', ']') }.toSet()
        val times = File("src/main/assets/data/latar.txt").readLines().map { it.trim() }
            .filter { it.startsWith("waktu") }.map { it.substringAfter('=').trim() }
        times.forEach { assertTrue(it, it in setOf("pagi", "siang", "sore", "malam", "tetap")) }
        val files = File("src/main/assets/latar").listFiles()!!.map { it.nameWithoutExtension }
            .filterNot { n -> listOf("_pagi", "_siang", "_sore", "_malam").any { n.endsWith(it) && n != "kamar_pagi" && n != "dermaga_malam" } }
        files.forEach { assertTrue("latar $it belum ada di data/latar.txt", it in listed) }
    }
}
