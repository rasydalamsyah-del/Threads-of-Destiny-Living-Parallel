package com.valeriusgames.threadsofdestiny.game

/**
 * Pewarnaan latar sesuai jam (pagi / siang / sore / malam).
 *
 * Setiap waktu punya "tampilan" (skala & geser warna per kanal + saturasi) relatif terhadap siang.
 * Untuk gambar yang aslinya pagi lalu ditampilkan malam: kembalikan dulu ke "siang", lalu beri tampilan malam.
 * Hasilnya matriks 4×5 (format android.graphics.ColorMatrix) tanpa bergantung pada Android, agar bisa diuji.
 */
object TimeLook {

    private class Look(val scale: FloatArray, val offset: FloatArray, val sat: Float)

    private val looks = mapOf(
        "pagi" to Look(floatArrayOf(1.02f, 1.0f, 0.96f), floatArrayOf(6f, 4f, 0f), 1.0f),
        "siang" to Look(floatArrayOf(1f, 1f, 1f), floatArrayOf(0f, 0f, 0f), 1.0f),
        "sore" to Look(floatArrayOf(1.08f, 0.9f, 0.72f), floatArrayOf(10f, 0f, -6f), 0.95f),
        "malam" to Look(floatArrayOf(0.42f, 0.48f, 0.70f), floatArrayOf(0f, 4f, 22f), 0.65f),
    )

    val IDENTITY: FloatArray get() = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )

    /**
     * Matriks untuk menampilkan gambar berwaktu [base] pada waktu [target].
     * null = tidak perlu diubah (sama, "tetap", atau tidak dikenal).
     * Gambar malam tidak dibuat terang (hasilnya akan pucat) — hanya malam→malam yang dipakai apa adanya.
     */
    fun matrix(base: String?, target: String?): FloatArray? {
        if (base == null || target == null || base == target || base == "tetap") return null
        val b = looks[base] ?: return null
        val t = looks[target] ?: return null
        if (base == "malam") return null
        var m = IDENTITY
        // kembalikan ke siang: (c - off) / scale, lalu saturasi 1/sat
        m = concat(diag(FloatArray(3) { 1f / b.scale[it] }, FloatArray(3) { -b.offset[it] / b.scale[it] }), m)
        m = concat(saturation(1f / b.sat), m)
        // tampilan waktu tujuan
        m = concat(saturation(t.sat), m)
        m = concat(diag(t.scale, t.offset), m)
        return m
    }

    /** Campuran identitas ↔ [m] (untuk karakter: ikut menyesuaikan, tapi lebih lembut). */
    fun blend(m: FloatArray?, amount: Float): FloatArray? {
        m ?: return null
        val id = IDENTITY
        return FloatArray(20) { id[it] + (m[it] - id[it]) * amount }
    }

    /** Interpolasi dua matriks (untuk animasi perubahan waktu). */
    fun lerp(a: FloatArray?, b: FloatArray?, t: Float): FloatArray {
        val x = a ?: IDENTITY
        val y = b ?: IDENTITY
        return FloatArray(20) { x[it] + (y[it] - x[it]) * t }
    }

    /** Terapkan matriks ke satu warna RGB 0..255 (untuk uji). */
    fun apply(m: FloatArray, rgb: IntArray): IntArray = IntArray(3) { r ->
        val v = m[r * 5] * rgb[0] + m[r * 5 + 1] * rgb[1] + m[r * 5 + 2] * rgb[2] + m[r * 5 + 4]
        v.coerceIn(0f, 255f).toInt()
    }

    private fun diag(scale: FloatArray, offset: FloatArray) = floatArrayOf(
        scale[0], 0f, 0f, 0f, offset[0],
        0f, scale[1], 0f, 0f, offset[1],
        0f, 0f, scale[2], 0f, offset[2],
        0f, 0f, 0f, 1f, 0f,
    )

    /** Sama dengan ColorMatrix.setSaturation. */
    private fun saturation(s: Float): FloatArray {
        val inv = 1f - s
        val r = 0.213f * inv
        val g = 0.715f * inv
        val b = 0.072f * inv
        return floatArrayOf(
            r + s, g, b, 0f, 0f,
            r, g + s, b, 0f, 0f,
            r, g, b + s, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
    }

    /** Hasil = [second] setelah [first] (sama dengan first.postConcat(second)). */
    private fun concat(second: FloatArray, first: FloatArray): FloatArray {
        val out = FloatArray(20)
        for (i in 0 until 4) {
            for (j in 0 until 5) {
                var v = 0f
                for (k in 0 until 4) v += second[i * 5 + k] * first[k * 5 + j]
                if (j == 4) v += second[i * 5 + 4]
                out[i * 5 + j] = v
            }
        }
        return out
    }
}
