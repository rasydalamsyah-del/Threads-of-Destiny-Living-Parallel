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
        /**
         * Waktu bebas. count = jumlah kesempatan (tidur mengakhiri waktu bebas),
         * days = selama N hari, atau untilWeekday/untilClock = sampai hari & jam tertentu (tidur tidak mengakhiri).
         */
        data class Free(val count: Int, val days: Int = 0, val untilWeekday: String? = null, val untilClock: String? = null) : Cmd()
        /** Majukan waktu n kali (Pagi → Siang → Sore → Malam → tidur → Pagi). */
        data class Advance(val count: Int, val minutes: Int = 0) : Cmd()
        /** Ubah uang. */
        data class Money(val delta: Long) : Cmd()
        /** Ubah bar: kesehatan, kenyang, hidrasi, kewarasan, energi. */
        data class Stat(val stat: String, val delta: Int) : Cmd()
        /** Tambah barang ke tas. */
        data class GiveItem(val id: String, val count: Int) : Cmd()
        /** Tempat pemain sekarang (menentukan aktivitas & toko): desa, kota, ... */
        data class Location(val place: String) : Cmd()
        /** Tampilkan gambar karakter: @tampil bayu senyum kiri */
        data class ShowChar(val id: String, val expression: String, val position: String) : Cmd()
        /** Sembunyikan karakter (null = semua). */
        data class HideChar(val id: String?) : Cmd()
        /** Pemain mengenal karakter ini (muncul di daftar & panel). */
        data class Meet(val id: String) : Cmd()
        /** Ubah Cinta karakter. */
        data class Love(val id: String, val delta: Int) : Cmd()
        /** Ubah Kepercayaan karakter. */
        data class Trust(val id: String, val delta: Int) : Cmd()
        /** Catat perilaku: menghindar, ingkar_janji, abaikan_chat, kasar, jujur, membantu, menepati_janji */
        data class Behave(val id: String, val kind: String) : Cmd()
        /** Catat kebohongan (belum ketahuan). */
        data class Lie(val code: String, val ids: List<String>) : Cmd()
        /** Kebohongan terbongkar. */
        data class Reveal(val code: String) : Cmd()
        /** Ungkapkan perasaan → lompat ke label diterima / ditolak. */
        data class Confess(val id: String, val okLabel: String, val noLabel: String) : Cmd()
        /** Pindah ke file naskah lain (bab berikutnya). */
        data class LoadScript(val file: String) : Cmd()
        /** Atur tanggal hari ini: @tanggal 6 Juli 2026 */
        data class SetDate(val text: String) : Cmd()
        /** Pindah ke kos (bayar bulan pertama). id kosong = tidak punya tempat tinggal. */
        data class Kos(val id: String) : Cmd()
        /** Kiriman uang bulanan dari keluarga (0 = berhenti). */
        data class Allowance(val amount: Long) : Cmd()
        /** Mulai / berhenti sekolah atau pekerjaan. */
        data class JobStart(val id: String) : Cmd()
        data class JobStop(val id: String) : Cmd()
        /** Melamar → lompat ke label diterima / ditolak. */
        data class Apply(val id: String, val okLabel: String, val noLabel: String) : Cmd()
        /** Tambah catatan di aplikasi Catatan HP. */
        data class Note(val text: String) : Cmd()
        /** Pemain mendapat dokumen (mis. ijazah_sma). */
        data class Doc(val id: String) : Cmd()
        /** Jalankan kejadian dari data/kejadian.txt sekarang juga. */
        data class Event(val id: String) : Cmd()
        /** Akhir naskah. */
        data class End(val text: String) : Cmd()
        /** Hapus semua hasil latihan (tutorial): hidup, hubungan, HP, variabel. Nama & kelamin tetap. */
        object ResetState : Cmd()
        /** Akhir adegan waktu bebas (data/adegan.txt): kembali ke waktu bebas. */
        object Return : Cmd()
        /** Pemain mengetahui info profil karakter: @info bayu hobi  (atau: semua) */
        data class Info(val id: String, val key: String) : Cmd()
        /** Tutorial: tampilkan bagian layar (hp, status, tas, bebas, ...) beserta penjelasan. */
        data class Show(val target: String, val sub: String?, val top: Boolean, val text: String) : Cmd()
        /** @jalan ruang_makan | petunjuk — pemain berjalan sendiri (ketuk pintu) sampai lokasi itu, lalu cerita lanjut. */
        data class Walk(val target: String, val hint: String) : Cmd()
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
                            is Cmd.Confess -> {
                                jumps += cmd.okLabel to n
                                jumps += cmd.noLabel to n
                            }
                            is Cmd.Apply -> {
                                jumps += cmd.okLabel to n
                                jumps += cmd.noLabel to n
                            }
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
            "bebas", "free" -> when {
                args.getOrNull(1) == "hari" ->
                    Cmd.Free(1, days = args[0].toIntOrNull()?.coerceAtLeast(1) ?: throw ParseError(n, "format: @bebas 5 hari"))
                args.firstOrNull() == "sampai" -> {
                    val wd = args.getOrNull(1) ?: throw ParseError(n, "format: @bebas sampai Sabtu 15:00")
                    Cmd.Free(1, untilWeekday = wd, untilClock = args.getOrNull(2) ?: "18:00")
                }
                else -> Cmd.Free(args.firstOrNull()?.toIntOrNull()?.coerceAtLeast(1) ?: 1)
            }
            "maju", "advance" -> {
                val nArg = args.firstOrNull()?.replace(',', '.')
                val unit = args.getOrNull(1)
                when (unit) {
                    "jam" -> Cmd.Advance(1, ((nArg?.toFloatOrNull() ?: 1f) * 60).toInt())
                    "menit" -> Cmd.Advance(1, nArg?.toIntOrNull() ?: 30)
                    else -> Cmd.Advance(nArg?.toIntOrNull()?.coerceAtLeast(1) ?: 1)
                }
            }
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
            "tampil", "show" -> {
                val id = args.firstOrNull() ?: throw ParseError(n, "format: @tampil id_karakter ekspresi [kiri/tengah/kanan]")
                val pos = args.drop(1).firstOrNull { it in setOf("kiri", "tengah", "kanan") } ?: "tengah"
                val expr = args.drop(1).firstOrNull { it !in setOf("kiri", "tengah", "kanan") } ?: "biasa"
                Cmd.ShowChar(id, expr, pos)
            }
            "sembunyi", "hide" -> Cmd.HideChar(args.firstOrNull()?.takeIf { it != "semua" && it != "all" })
            "kenal", "meet" -> Cmd.Meet(args.firstOrNull() ?: throw ParseError(n, "format: @kenal id_karakter"))
            "cinta", "love", "percaya", "trust" -> {
                val m = Regex("^(\\w+)\\s+([+-]?\\d+)$").find(rest)
                    ?: throw ParseError(n, "format: @$name id_karakter +5  (atau -5)")
                val id = m.groupValues[1].lowercase()
                val d = m.groupValues[2].removePrefix("+").toInt()
                if (name == "cinta" || name == "love") Cmd.Love(id, d) else Cmd.Trust(id, d)
            }
            "perilaku", "behave" -> {
                if (args.size < 2) throw ParseError(n, "format: @perilaku id_karakter menghindar")
                Cmd.Behave(args[0], args[1])
            }
            "bohong", "lie" -> {
                if (args.size < 2) throw ParseError(n, "format: @bohong kode_bohong id_karakter [id_lain ...]")
                Cmd.Lie(args[0], args.drop(1))
            }
            "terbongkar", "reveal" -> Cmd.Reveal(args.firstOrNull() ?: throw ParseError(n, "format: @terbongkar kode_bohong"))
            "ungkapkan", "confess" -> {
                val m = Regex("^(\\w+)\\s*->\\s*(\\w+)\\s*,\\s*(\\w+)$").find(rest)
                    ?: throw ParseError(n, "format: @ungkapkan id_karakter -> label_diterima, label_ditolak")
                Cmd.Confess(m.groupValues[1].lowercase(), m.groupValues[2], m.groupValues[3])
            }
            "naskah", "script" -> {
                val f = rest.trim().takeIf { it.isNotEmpty() } ?: throw ParseError(n, "format: @naskah bab2.txt")
                Cmd.LoadScript(if (f.contains('.')) f else "$f.txt")
            }
            "tanggal", "date" -> {
                if (rest.split(Regex("\\s+")).size < 3) throw ParseError(n, "format: @tanggal 6 Juli 2026")
                Cmd.SetDate(rest)
            }
            "kos" -> Cmd.Kos(args.firstOrNull()?.takeIf { it != "kosong" && it != "tidak_ada" } ?: "")
            "kiriman", "allowance" -> {
                val v = rest.replace(".", "").replace(" ", "").removePrefix("+").toLongOrNull()
                    ?: throw ParseError(n, "format: @kiriman 500000  (0 = berhenti)")
                Cmd.Allowance(v)
            }
            "kerja_mulai", "sekolah_mulai", "job_start" ->
                Cmd.JobStart(args.firstOrNull() ?: throw ParseError(n, "format: @kerja_mulai id_pekerjaan"))
            "kerja_berhenti", "sekolah_berhenti", "job_stop" ->
                Cmd.JobStop(args.firstOrNull() ?: throw ParseError(n, "format: @kerja_berhenti id_pekerjaan"))
            "lamar", "apply" -> {
                val m = Regex("^(\\w+)\\s*->\\s*(\\w+)\\s*,\\s*(\\w+)$").find(rest)
                    ?: throw ParseError(n, "format: @lamar id_pekerjaan -> label_diterima, label_ditolak")
                Cmd.Apply(m.groupValues[1].lowercase(), m.groupValues[2], m.groupValues[3])
            }
            "catatan", "note" -> Cmd.Note(rest.ifEmpty { throw ParseError(n, "format: @catatan isi catatan") })
            "dokumen", "doc" -> Cmd.Doc(args.firstOrNull() ?: throw ParseError(n, "format: @dokumen id_dokumen"))
            "kejadian", "event" -> Cmd.Event(args.firstOrNull() ?: throw ParseError(n, "format: @kejadian id_kejadian"))
            "tamat", "end" -> Cmd.End(rest.ifEmpty { "Bersambung…" })
            "mulai_ulang", "reset" -> Cmd.ResetState
            "kembali", "return" -> Cmd.Return
            "info" -> {
                if (args.size < 2) throw ParseError(n, "format: @info id_karakter hobi  (hobi, kegemaran, pelajaran, keahlian, cita_cita, kisah_1..3, semua)")
                Cmd.Info(args[0].lowercase(), args[1].lowercase())
            }
            "jalan", "walk" -> {
                val target = rest.substringBefore('|').trim().lowercase()
                if (target.isEmpty()) throw ParseError(n, "format: @jalan ruang_makan | petunjuk")
                Cmd.Walk(target, rest.substringAfter('|', "").trim())
            }
            "tunjuk", "show_ui" -> {
                val head = rest.substringBefore('|').trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                if (head.isEmpty()) throw ParseError(n, "format: @tunjuk hp kalender | penjelasan")
                val flags = head.drop(1)
                Cmd.Show(
                    target = head[0].lowercase(),
                    sub = flags.firstOrNull { it != "atas" && it != "bawah" }?.lowercase(),
                    top = "bawah" !in flags,
                    text = rest.substringAfter('|', "").trim(),
                )
            }
            else -> throw ParseError(n, "perintah @$name tidak dikenal")
        }
    }
}
