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
        /** Pilihan jawaban: setiap opsi melompat ke sebuah label. */
        data class Choice(val options: List<Option>) : Cmd()
        /** Lompat ke label. */
        data class Jump(val label: String) : Cmd()
        /** Lompat ke label bila syarat terpenuhi, contoh: @jika usia >= 18 -> dewasa */
        data class JumpIf(val variable: String, val op: String, val value: String, val label: String) : Cmd()
        /** Isi variabel, contoh: @atur jalur = sma */
        data class SetVar(val variable: String, val value: String) : Cmd()
        /** Tambah/kurangi variabel angka, contoh: @tambah uang -5000 */
        data class AddVar(val variable: String, val amount: Long) : Cmd()
        /** Nyalakan/matikan sistem kehidupan (bar, uang, jam otomatis). */
        data class Life(val active: Boolean) : Cmd()
        /** Atur hari & waktu, contoh: @kalender Senin pagi */
        data class Calendar(val weekday: String, val slot: String) : Cmd()
        /** Waktu bebas sebanyak n kesempatan. */
        data class Free(val count: Int) : Cmd()
        /** Majukan waktu n kali (Pagi → Siang → Sore → Malam → tidur → Pagi). */
        data class Advance(val count: Int) : Cmd()
        /** Ubah uang. */
        data class Money(val delta: Long) : Cmd()
        /** Ubah bar: kesehatan, kenyang, hidrasi, kewarasan, energi. */
        data class Stat(val stat: String, val delta: Int) : Cmd()
        /** Tambah barang ke tas. */
        data class GiveItem(val id: String, val count: Int) : Cmd()
        /** Tempat pemain sekarang (menentukan aktivitas & toko): desa, kota, ... */
        data class Location(val place: String) : Cmd()
        /** Akhir naskah. */
        data class End(val text: String) : Cmd()
    }

    data class Option(val text: String, val label: String)

    /** Hasil baca naskah: daftar perintah + posisi setiap label. */
    data class Script(val cmds: List<Cmd>, val labels: Map<String, Int>)

    class ParseError(val lineNumber: Int, message: String) :
        Exception("Naskah baris $lineNumber: $message")

    fun parse(lines: List<String>): Script {
        val out = mutableListOf<Cmd>()
        val labels = mutableMapOf<String, Int>()
        val jumps = mutableListOf<Pair<String, Int>>()   // (label, nomor baris) untuk dicek
        var openChoice: MutableList<Option>? = null

        fun closeChoice(n: Int) {
            val opts = openChoice ?: return
            if (opts.isEmpty()) throw ParseError(n, "@pilihan harus diikuti minimal satu baris > pilihan -> label")
            out += Cmd.Choice(opts.toList())
            openChoice = null
        }

        lines.forEachIndexed { index, rawLine ->
            val n = index + 1
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEachIndexed

            if (line.startsWith(">")) {
                val opts = openChoice ?: throw ParseError(n, "baris > hanya boleh tepat di bawah @pilihan")
                val body = line.substring(1)
                val arrow = body.lastIndexOf("->")
                if (arrow < 0) throw ParseError(n, "format pilihan: > Teks pilihan -> nama_label")
                val text = body.substring(0, arrow).trim()
                val label = body.substring(arrow + 2).trim()
                if (text.isEmpty() || label.isEmpty()) throw ParseError(n, "format pilihan: > Teks pilihan -> nama_label")
                opts += Option(text, label)
                jumps += label to n
                return@forEachIndexed
            }
            closeChoice(n)

            when {
                line.startsWith("::") -> {
                    val label = line.substring(2).trim()
                    if (label.isEmpty()) throw ParseError(n, "label butuh nama, contoh: :: sma")
                    if (labels.containsKey(label)) throw ParseError(n, "label :: $label sudah ada")
                    labels[label] = out.size
                }
                line.startsWith("@") -> {
                    val body = line.substring(1).trim()
                    if (body.substringBefore(' ').lowercase() in setOf("pilihan", "choice")) {
                        openChoice = mutableListOf()
                    } else {
                        val cmd = parseCommand(body, n)
                        when (cmd) {
                            is Cmd.Jump -> jumps += cmd.label to n
                            is Cmd.JumpIf -> jumps += cmd.label to n
                            else -> Unit
                        }
                        out += cmd
                    }
                }
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
        closeChoice(lines.size)

        jumps.forEach { (label, n) ->
            if (!labels.containsKey(label)) throw ParseError(n, "label :: $label tidak ditemukan")
        }
        return Script(out, labels)
    }

    private fun parseCommand(body: String, n: Int): Cmd {
        val name = body.substringBefore(' ').lowercase()
        val rest = body.substringAfter(' ', "").trim()
        val args = rest.split(Regex("\\s+")).filter { it.isNotEmpty() }.map { it.lowercase() }

        fun ms(default: Long): Long = args.firstOrNull()?.toLongOrNull() ?: default

        return when (name) {
            "latar", "bg" -> {
                val file = rest.split(Regex("\\s+")).firstOrNull()?.takeIf { it.isNotEmpty() }
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
            "lompat", "jump", "goto" -> {
                val label = rest.takeIf { it.isNotEmpty() }
                    ?: throw ParseError(n, "@lompat butuh nama label")
                Cmd.Jump(label)
            }
            "jika", "if" -> {
                val m = Regex("^(\\w+)\\s*(==|!=|>=|<=|>|<)\\s*(.+?)\\s*->\\s*(\\w+)$").find(rest)
                    ?: throw ParseError(n, "format: @jika variabel >= nilai -> nama_label")
                val (v, op, value, label) = m.destructured
                Cmd.JumpIf(v, op, value, label)
            }
            "atur", "set" -> {
                val m = Regex("^(\\w+)\\s*=\\s*(.*)$").find(rest)
                    ?: throw ParseError(n, "format: @atur variabel = nilai")
                Cmd.SetVar(m.groupValues[1], m.groupValues[2].trim())
            }
            "tambah", "add" -> {
                val m = Regex("^(\\w+)\\s+([+-]?\\d+)$").find(rest)
                    ?: throw ParseError(n, "format: @tambah variabel +5  (atau -5)")
                Cmd.AddVar(m.groupValues[1], m.groupValues[2].removePrefix("+").toLong())
            }
            "hidup", "life" -> Cmd.Life(args.firstOrNull() !in setOf("berhenti", "stop", "mati"))
            "kalender", "calendar" -> {
                if (args.size < 2) throw ParseError(n, "format: @kalender Senin pagi")
                Cmd.Calendar(args[0], args[1])
            }
            "bebas", "free" -> Cmd.Free(args.firstOrNull()?.toIntOrNull()?.coerceAtLeast(1) ?: 1)
            "maju", "advance" -> Cmd.Advance(args.firstOrNull()?.toIntOrNull()?.coerceAtLeast(1) ?: 1)
            "uang", "money" -> {
                val v = rest.replace(".", "").replace(" ", "").removePrefix("+").toLongOrNull()
                    ?: throw ParseError(n, "format: @uang +50000  (atau -5000)")
                Cmd.Money(v)
            }
            "stat" -> {
                val m = Regex("^(\\w+)\\s+([+-]?\\d+)$").find(rest)
                    ?: throw ParseError(n, "format: @stat energi -10")
                Cmd.Stat(m.groupValues[1].lowercase(), m.groupValues[2].removePrefix("+").toInt())
            }
            "barang", "item" -> {
                val id = args.firstOrNull() ?: throw ParseError(n, "format: @barang id_barang 2")
                Cmd.GiveItem(id, args.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(1) ?: 1)
            }
            "lokasi", "location" -> Cmd.Location(args.firstOrNull() ?: throw ParseError(n, "format: @lokasi desa"))
            "tamat", "end" -> Cmd.End(rest.ifEmpty { "Bersambung…" })
            else -> throw ParseError(n, "perintah @$name tidak dikenal")
        }
    }
}
