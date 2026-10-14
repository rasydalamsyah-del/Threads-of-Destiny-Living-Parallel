package com.valeriusgames.threadsofdestiny.story

/**
 * Pembaca file naskah (folder assets/naskah).
 * Panduan lengkap cara menulis naskah: docs/NASKAH.md
 */
object StoryScript {

    enum class Motion { NONE, PAN, RISE }

    sealed class Cmd {
        /** Ganti background. */
        data class Background(val name: String, val memory: Boolean, val motion: Motion) : Cmd()
        /** Layar perlahan menjadi gelap total. */
        data class FadeToBlack(val ms: Long) : Cmd()
        /** Layar perlahan terang kembali dari gelap. */
        data class FadeFromBlack(val ms: Long) : Cmd()
        /** Teks penanda waktu di tengah layar gelap. */
        data class TimeCard(val text: String) : Cmd()
        /** Efek mata berkedip saat bangun tidur. */
        object Blink : Cmd()
        /** Jam di pojok kanan atas (null = sembunyikan). */
        data class Clock(val text: String?) : Cmd()
        /** Jeda tanpa apa-apa. */
        data class Pause(val ms: Long) : Cmd()
        /** Kalimat. speaker == null berarti narator. */
        data class Line(val speaker: String?, val text: String) : Cmd()
        /** Tampilkan / sembunyikan ikon HP. */
        data class PhoneIcon(val visible: Boolean) : Cmd()
        /** Pesan masuk ke HP. */
        data class PhoneMessage(val sender: String, val text: String) : Cmd()
        /** Tunggu sampai pemain membuka (lalu menutup) HP. */
        object WaitForPhone : Cmd()
        /** Musik latar (null = berhenti). */
        data class Music(val name: String?) : Cmd()
        /** Efek suara sekali. */
        data class Sound(val name: String) : Cmd()
        /** Akhir naskah. */
        data class End(val text: String) : Cmd()
    }

    class ParseError(val lineNumber: Int, message: String) :
        Exception("Naskah baris $lineNumber: $message")

    fun parse(lines: List<String>): List<Cmd> {
        val out = mutableListOf<Cmd>()
        lines.forEachIndexed { index, rawLine ->
            val n = index + 1
            val line = rawLine.trim()
            when {
                line.isEmpty() || line.startsWith("#") -> Unit
                line.startsWith("@") -> out += parseCommand(line.substring(1).trim(), n)
                line.startsWith("*") -> out += Cmd.Line(null, line.substring(1).trim())
                else -> {
                    val colon = line.indexOf(':')
                    out += if (colon in 1..30) {
                        Cmd.Line(line.substring(0, colon).trim(), line.substring(colon + 1).trim())
                    } else {
                        Cmd.Line(null, line)
                    }
                }
            }
        }
        return out
    }

    private fun parseCommand(body: String, n: Int): Cmd {
        val name = body.substringBefore(' ').lowercase()
        val rest = body.substringAfter(' ', "").trim()
        val args = rest.split(Regex("\\s+")).filter { it.isNotEmpty() }.map { it.lowercase() }

        fun ms(default: Long): Long = args.firstOrNull()?.toLongOrNull() ?: default

        return when (name) {
            "latar", "bg" -> {
                val file = rest.split(Regex("\\s+")).firstOrNull()
                    ?: throw ParseError(n, "@latar butuh nama gambar, contoh: @latar plafon")
                val flags = args.drop(1)
                val memory = flags.any { it == "kenangan" || it == "memory" }
                val motion = when {
                    flags.any { it == "naik" || it == "rise" } -> Motion.RISE
                    flags.any { it == "gerak" || it == "pan" } -> Motion.PAN
                    else -> Motion.NONE
                }
                Cmd.Background(file, memory, motion)
            }
            "gelap", "black" -> Cmd.FadeToBlack(ms(1500))
            "terang", "lift" -> Cmd.FadeFromBlack(ms(1500))
            "waktu", "timecard" -> Cmd.TimeCard(rest)
            "kedip", "blink" -> Cmd.Blink
            "jam", "clock" ->
                Cmd.Clock(if (rest.isEmpty() || rest.equals("sembunyi", true)) null else rest)
            "jeda", "wait" -> Cmd.Pause(ms(1000))
            "hp", "phone" -> {
                val action = args.firstOrNull()
                when (action) {
                    "tampil", "show" -> Cmd.PhoneIcon(true)
                    "sembunyi", "hide" -> Cmd.PhoneIcon(false)
                    "pesan", "message" -> {
                        val content = rest.substringAfter(' ', "")
                        val sender = content.substringBefore('|').trim()
                        val text = content.substringAfter('|', "").trim()
                        if (sender.isEmpty() || text.isEmpty()) {
                            throw ParseError(n, "format: @hp pesan Pengirim | isi pesan")
                        }
                        Cmd.PhoneMessage(sender, text)
                    }
                    else -> throw ParseError(n, "@hp harus diikuti tampil, sembunyi, atau pesan")
                }
            }
            "tunggu_hp", "wait_phone" -> Cmd.WaitForPhone
            "musik", "music" -> {
                val file = rest.split(Regex("\\s+")).firstOrNull()?.takeIf { it.isNotEmpty() }
                    ?: throw ParseError(n, "@musik butuh nama file, atau: @musik stop")
                Cmd.Music(if (file.equals("stop", true) || file.equals("berhenti", true)) null else file)
            }
            "suara", "sound", "sfx" -> {
                val file = rest.split(Regex("\\s+")).firstOrNull()?.takeIf { it.isNotEmpty() }
                    ?: throw ParseError(n, "@suara butuh nama file")
                Cmd.Sound(file)
            }
            "tamat", "end" -> Cmd.End(rest.ifEmpty { "Bersambung…" })
            else -> throw ParseError(n, "perintah @$name tidak dikenal")
        }
    }
}
