# Alur Cerita Desa — Tanjung Lentera

> Dokumen induk cerita **jalur desa** (dan bagian prolog yang dimainkan semua pemain).
> Semua naskah, jadwal karakter, aktivitas, dan gambar desa harus mengikuti dokumen ini.
> Kalau ada yang diubah di game, ubah juga di sini supaya tidak bertentangan.
>
> Terkait: [RANCANGAN_GAME.md](RANCANGAN_GAME.md) (aturan game) · [NASKAH.md](NASKAH.md) (cara menulis naskah) ·
> [DATA.md](DATA.md) (format data) · [PROMPT_GAMBAR.md](PROMPT_GAMBAR.md) (prompt gambar)

---

## 1. Prinsip

1. **Masuk akal & konsisten.** Di mana seseorang berada dan jam berapa harus sama di cerita, di jadwal, dan di gambar.
   Ayah melaut malam → pagi ia tidur, siang baru bangun. Laras sekolah pagi → baru ada di rumah pukul 13:00.
2. **Ada pemicunya.** Karakter baru muncul (di layar, di daftar, di HP) setelah dikenalkan cerita (`@kenal`).
3. **Hidup terasa berjalan.** Saat waktu bebas, setiap tempat punya orang dan kegiatan sesuai jamnya.
   Cerita kecil (adegan) muncul sendiri saat pemain datang ke tempat & jam yang pas, atau saat menemui orangnya.
4. **Pilihan berakibat.** Banyak percabangan kecil (obrolan, bantuan, kejujuran) yang terkumpul menjadi akibat besar
   di akhir bab (hasil lomba, keadaan keluarga, hubungan dengan teman, petunjuk tentang sahabat).
5. **Tanpa romansa** di jalur desa (semua tokoh muda masih 15 tahun). Hubungan = keluarga, persahabatan, kepercayaan.
6. **Gambar:** latar + karakter dibuat langsung oleh pemilik game (satu gambar utuh). Tokoh setengah badan di kotak
   dialog tetap memakai gambar ekspresi. Tidak ada lagi gambar tokoh yang ditempel di atas latar.

---

## 2. Peta desa

```
                    [Kamarmu]
                        │ (pintu)
[Ruang makan] ─── [Ruang tengah] ─── (keluar rumah) ─── [Depan rumah & warung]
                                                          │        │        │
                                                   5 mnt  │  5 mnt │ 15 mnt │
                                                  [Lapangan] [Dermaga]  [Gudang ikan]
                                                             mercusuar
```

| Lokasi (id) | Nama di game | Isi | Gambar dasar |
|---|---|---|---|
| `kamar_pagi` | Kamarmu | kasur, meja belajar, lemari & kotak kenangan | ✅ kamar_pagi.jpg |
| `ruang_tengah` | Ruang tengah | TV tabung, tikar, sofa tua, radio Ayah | ✅ ruang_tengah.jpg |
| `ruang_makan` | Ruang makan | meja kayu, dapur di belakang | ✅ ruang_makan.jpg |
| `rumah_warung` | Depan rumah & warung | warung gorengan Ibu, bale-bale bambu, jalan desa | ✅ rumah_warung.jpg |
| `gudang_ikan` | Gudang ikan Juragan Darto | tampah jemur ikan, gudang, kandang perkutut | ✅ gudang_ikan.jpg |
| `lapangan` **(baru)** | Lapangan desa | lapangan bola tanah, gawang bambu, pohon ketapang, anak-anak main layangan | ❌ perlu dibuat (sementara memakai gambar lain) |
| `dermaga` **(baru)** | Dermaga mercusuar | dermaga kayu, perahu nelayan, mercusuar tua | ⚠️ baru ada versi malam (dermaga_malam.jpg) |

---

## 3. Jadwal harian karakter (final)

Hari kerja = **Senin–Sabtu**. Di luar jam di tabel, karakter **tidak ada di lokasi yang bisa dikunjungi**
(tidur, sekolah, di laut, di rumahnya sendiri).

### Ayah — nelayan, melaut malam
| Jam | Di mana | Sedang apa |
|---|---|---|
| 06:15–07:15 | Ruang makan | sarapan bersama keluarga (baru pulang melaut ± 05:45) |
| 07:15–12:00 | — | tidur |
| 12:00–13:00 | Ruang makan | makan siang |
| 13:00–16:00 | Depan rumah & warung | memperbaiki jaring di bale-bale |
| 16:00–17:45 | Ruang tengah | minum kopi, mendengarkan radio dangdut |
| 17:45–18:30 | Ruang makan | makan malam bersama keluarga |
| 18:30–19:15 | Dermaga | menyiapkan perahu, lalu berangkat |
| 19:15–05:45 | — | di laut |

### Ibu — pemilik warung gorengan
| Jam | Di mana | Sedang apa |
|---|---|---|
| 05:00–06:15 | Ruang makan | memasak sarapan (dapur di belakang ruang makan) |
| 06:15–07:15 | Ruang makan | sarapan bersama keluarga |
| 07:15–17:15 | Depan rumah & warung | berjualan |
| 17:15–17:45 | Ruang makan | memasak makan malam |
| 17:45–18:30 | Ruang makan | makan malam |
| 18:30–21:00 | Ruang tengah | menonton sinetron sambil melipat pakaian |
| 21:00–05:00 | — | tidur |

### Laras — adik, kelas 5 SD
| Hari | Jam | Di mana | Sedang apa |
|---|---|---|---|
| Setiap hari | 06:15–07:15 | Ruang makan | sarapan |
| Senin–Sabtu | 07:15–13:00 | — | sekolah |
| Senin–Sabtu | 13:00–14:00 | Ruang makan | makan siang |
| Senin–Sabtu | 14:00–16:00 | Ruang tengah | mengerjakan PR / menggambar |
| Minggu | 07:15–12:00 | Depan rumah & warung | membantu Ibu |
| Minggu | 13:00–14:00 | Ruang makan | makan siang |
| Minggu | 14:00–16:00 | Lapangan | main layangan |
| Setiap hari | 16:00–17:15 | Depan rumah & warung | main dengan kucing warung, membantu Ibu |
| Setiap hari | 17:45–18:30 | Ruang makan | makan malam |
| Setiap hari | 18:30–20:30 | Ruang tengah | menonton TV bersama Ibu |

### Bayu — teman sekelas, anak nelayan (dikenal sejak pesan di prolog)
| Hari | Jam | Di mana | Sedang apa |
|---|---|---|---|
| Senin–Sabtu | 04:00–10:00 | — | melaut bersama bapaknya (Pak Karta) |
| Senin–Sabtu | 15:00–17:30 | Lapangan | main bola |
| Senin–Sabtu | 17:30–18:30 | Dermaga | memancing |
| Minggu | 08:00–11:00 | Lapangan | main bola |
| Minggu | 16:00–18:00 | Dermaga | memancing |

Bisa juga diajak janjian lewat Chat → menunggu di **dermaga**.

### Sekar — teman sekelas, juara kelas (dikenal di adegan Bab 2)
| Hari | Jam | Di mana | Sedang apa |
|---|---|---|---|
| Senin–Sabtu | 15:30–17:15 | Dermaga | membaca di bawah mercusuar |

Di luar itu ia membantu ibunya menjahit di rumah. Bisa diajak janjian lewat Chat → menunggu di **dermaga**.
Kalau di Bab 3 Sekar memilih **menerima beasiswa ke Sakala**, mulai Sabtu 11 Juli ia tidak ada lagi di desa (hanya lewat Chat).

### Juragan Darto — juragan ikan asin (dikenal di Bab 2)
| Hari | Jam | Di mana |
|---|---|---|
| Senin–Sabtu | 07:00–16:00 | Gudang ikan |

### Tokoh tambahan tanpa gambar (hanya nama di dialog)
Pak Karta (bapak Bayu, nelayan) · Bu Wati (ibu Sekar, penjahit) · Pak RT Sobari · Pak Kades · Mbak Yati & Pak Usman
(buruh gudang) · Mantri Joko · Rani & Dodi (teman sekelas yang berangkat ke Sakala).

---

## 4. Siapa ada di mana, jam berapa → gambar yang perlu dibuat

Gambar latar berganti **otomatis** sesuai siapa yang sedang ada di lokasi (hanya karakter yang sudah dikenal).
Nama file: `latar/<lokasi>__<id karakter, urut abjad, dipisah _>.jpg`. Contoh: `ruang_makan__ayah_ibu_laras.jpg`.
Pewarnaan pagi/siang/sore/malam tetap otomatis (atau buat versi khusus `..._malam.jpg`).
Selama gambarnya belum ada, game memakai gambar kosong dan menampilkan tanda nama orang yang ada di sana.

**Posisi orang harus sama di semua gambar lokasi itu** (supaya titik ketuk di game tepat):

| File | Kapan tampil | Isi gambar (posisi) |
|---|---|---|
| `ruang_makan__ibu.jpg` | 05:00–06:15, 17:15–17:45 | Ibu memasak/menata piring, berdiri dekat dapur **kanan** |
| `ruang_makan__ayah_ibu_laras.jpg` | 06:15–07:15, 17:45–18:30 | keluarga makan: Ayah **kiri**, Laras **tengah-kanan**, Ibu **kanan** (boleh memakai gambar sarapan_keluarga) |
| `ruang_makan__ayah.jpg` | 12:00–13:00 | Ayah makan siang sendiri, duduk **kiri** meja |
| `ruang_makan__laras.jpg` | 13:00–14:00 | Laras makan siang, seragam SD, duduk **tengah-kanan** |
| `ruang_tengah__laras.jpg` | Sen–Sab 14:00–16:00 | Laras tengkurap di tikar mengerjakan PR/menggambar, **tengah** |
| `ruang_tengah__ayah.jpg` | 16:00–17:45 | Ayah duduk di sofa tua, kopi & radio, **kiri** |
| `ruang_tengah__ibu_laras.jpg` | 18:30–20:30 | Ibu melipat pakaian di sofa **kiri**, Laras di tikar depan TV **tengah** |
| `ruang_tengah__ibu.jpg` | 20:30–21:00 | Ibu sendiri melipat pakaian di sofa **kiri** |
| `rumah_warung__ibu.jpg` | 07:15–13:00 (dan Minggu 12:00–13:00) | Ibu menggoreng di warung, **kiri** |
| `rumah_warung__ayah_ibu.jpg` | 13:00–16:00 | Ibu di warung **kiri** + Ayah memperbaiki jaring di bale-bale **kanan** |
| `rumah_warung__ibu_laras.jpg` | 16:00–17:15 (Minggu juga 07:15–12:00) | Ibu di warung **kiri** + Laras jongkok memberi makan kucing **tengah** |
| `gudang_ikan__darto.jpg` | Sen–Sab 07:00–16:00 | Juragan Darto berdiri mengawasi tampah ikan, **kiri** |
| `lapangan.jpg` | dasar | lapangan desa kosong |
| `lapangan__bayu.jpg` | Sen–Sab 15:00–17:30, Minggu 08:00–11:00 | Bayu menggiring bola, **kanan** |
| `lapangan__laras.jpg` | Minggu 14:00–16:00 | Laras menerbangkan layangan, **kiri** |
| `dermaga.jpg` | dasar (siang) | dermaga kosong, mercusuar tua (versi malam sudah ada) |
| `dermaga__sekar.jpg` | Sen–Sab 15:30–17:15 | Sekar duduk membaca di ujung dermaga, **kiri** |
| `dermaga__bayu.jpg` | Sen–Sab 17:30–18:30, Minggu 16:00–18:00 | Bayu memancing, duduk di tepi dermaga, **kanan** |
| `dermaga__ayah.jpg` | 18:30–19:15 | Ayah menyiapkan perahu, **tengah** |

Total **19 gambar** (1 bisa memakai gambar sarapan_keluarga). Urutan prioritas: rumah_warung (3), ruang_makan (3),
dermaga (4), lapangan (3), ruang_tengah (4), gudang (1).

Gambar khusus adegan (CG) dipakai naskah lewat `@latar`, misalnya `sarapan_keluarga`, `sarapan_tanpa_laras`,
`ruang_tengah_sarapan`. Daftar CG yang **diinginkan** (boleh menyusul; sebelum ada, naskah memakai latar biasa):
`stasiun_desa_pamit` (teman-teman berangkat), `dermaga_badai` (malam badai), `lapangan_lomba` (17 Agustus),
`mercusuar_menyala` (malam 17 Agustus), `ibu_sakit` (Ibu berbaring di tikar ruang tengah).

---

## 5. Aktivitas pemain per lokasi

Aktivitas `bersama = X` hanya bisa dilakukan saat X sedang ada di lokasi itu.

| Lokasi | Aktivitas | Jam | Catatan |
|---|---|---|---|
| Kamarmu | 🛏️ Istirahat / 😴 Tidur | kapan saja / malam | |
| | 📚 Belajar persiapan SMA | siang, malam | hanya jalur sekolah |
| | 📖 Belajar mandiri (Paket C) | siang, malam | jalur desa, terbuka setelah Sekar menyarankan |
| | ✍️ Menulis buku harian | malam | kewarasan |
| | 🧶 Melihat gelang & foto lama | kapan saja, 1×/hari | kenangan sahabat |
| Ruang tengah | 📺 Menonton TV | siang, sore, malam | lebih seru bila ada Ibu/Laras |
| | 👕 Membantu Ibu melipat pakaian | 18:30–21:00, bersama Ibu | Cinta Ibu |
| | ✏️ Membantu PR Laras | 14:00–16:00, bersama Laras | Cinta Laras |
| | 📻 Menemani Ayah mendengarkan radio | 16:00–17:45, bersama Ayah | Cinta Ayah |
| Ruang makan | 🍲 Makan bersama keluarga | jam makan, bersama Ibu | kenyang besar |
| | 🍚 Makan sisa lauk | kapan saja (jeda 4 jam) | kenyang sedang |
| | 🍳 Membantu Ibu memasak | 05:00–06:15, 17:15–17:45, bersama Ibu | keahlian masak |
| | 🧽 Mencuci piring | setelah makan | Cinta Ibu |
| Depan rumah & warung | 🍳 Bantu Ibu di warung | 05:30–17:00, bersama Ibu | uang kecil |
| | 🪢 Membantu Ayah memperbaiki jaring | 13:00–16:00, bersama Ayah | keahlian laut, Cinta Ayah |
| | 🧹 Menyapu halaman | pagi | |
| | 🐈 Memberi makan kucing warung | kapan saja, 1×/hari | |
| | 🪁 Mengajak Laras main layangan | 16:00–17:15, bersama Laras | |
| Lapangan | ⚽ Main bola bersama Bayu | 15:00–17:30, bersama Bayu | Cinta Bayu, latihan lomba |
| | 🏃 Lari sore keliling lapangan | sore | kesehatan |
| | 🌳 Duduk di bawah pohon ketapang | siang, sore | kewarasan |
| Dermaga | 🌅 Duduk di dermaga mercusuar | sore, malam | kewarasan |
| | 🎣 Memancing | sore | keahlian laut |
| | ⛵ Membantu Ayah menyiapkan perahu | 18:30–19:15, bersama Ayah | Cinta Ayah |
| | 🚣 Ikut perahu Pak Karta melaut subuh | mulai 04:00–05:00 | uang, perlu kenal Bayu |
| Gudang ikan | (kerja buruh ikan lewat jadwal kerja) | Sen–Sab 07:00–14:00 | |

### Pekerjaan di desa
| Pekerjaan | Jam | Cara dapat |
|---|---|---|
| Buruh ikan asin (Juragan Darto) | Sen–Sab 07:00–14:00, Rp65.000/hari | Bab 2: dilamar bersama Ayah |
| Ikut melaut perahu Pak Karta (bagi hasil) | setiap hari 04:30–10:00, ± Rp45.000 | ditawari Bayu |
| Antar jahitan Bu Wati (paruh waktu) | Sen–Sab 15:00–17:00, Rp20.000 | cerita Sekar (Bab 3) |
| Lembur Sabtu sore di gudang | Sabtu 14:00–17:00, Rp30.000 | cerita hutang perahu (Bab 3) |

---

## 6. Garis waktu jalur desa

| Bab | Tanggal | Judul | Inti |
|---|---|---|---|
| 1 (prolog, semua pemain) | Senin 22 Juni 2026 | Lulus SMP | sarapan, warung, pilihan besar, hari pertama bebas |
| 2 Desa | Selasa 23 Juni – Minggu 5 Juli | Mencari Pegangan | melamar ke Darto, kenal Sekar, hari pertama kerja, rahasia hutang perahu |
| 3 Desa | Senin 6 Juli – Minggu 19 Juli | Benang Merah | teman-teman berangkat ke Sakala, pilihan Sekar, Ibu jatuh sakit, petunjuk sahabat |
| 4 Desa | Senin 20 Juli – Senin 17 Agustus | Tujuh Belasan | lomba & bazar, malam badai, perahu Ayah, mercusuar menyala |

Jalur kota tetap: prolog → Sabtu 11 Juli berangkat ke Sakala.

---

## 6a. RANCANGAN BARU (disetujui 19 Okt 2026, ✅ dibuat di game build 56: prolog.txt, bab1.txt) — Bab 1 jadi 3 hari, keputusan kota/desa mundur

> Dibuat bersama Bab 2 setelah Bab 2 selesai dibahas, karena saling menyambung.

**Inti:** pertanyaan Ibu tetap di hari pertama, tapi **keputusan final dimundurkan ke Jumat 3 Juli** (batas pendaftaran SMA Sakala).
Semua pemain menjalani desa dulu. Jalur kota: seminggu persiapan & pamitan, berangkat **Sabtu 11 Juli** (sama dengan cerita kota
yang sudah ada). Jalur desa: Sabtu 11 Juli mengantar teman-teman di stasiun (momen paralel).

**Bab 1 — Lulus SMP (Senin 22 – Rabu 24 Juni)**
- **Senin:** kenangan → bangun → sarapan → buka warung → Ibu bertanya. 🔀 Jawaban **sementara**: *kota / desa / belum tahu*
  (`condong`); Ibu: "Pendaftaran tutup Jumat 3 Juli, pikirkan dulu." Pesan misterius. Waktu bebas: bicara dengan Ayah
  (tanggapan sesuai `condong`; cabang kota menangkap Ayah menyembunyikan sesuatu = benih hutang; cabang desa membuka Paket C;
  semua cabang: Rabu ke gudang Darto), 🔀 seribu untuk Laras, Bayu (sesuai `condong`, menyebut beasiswa Sekar),
  makan malam (🔀 "Kakak belum memutuskan" / "Kakak nggak ke mana-mana" → bisa jadi kebohongan ke Laras), Ayah di dermaga
  (🔀 "Menurut Ayah aku harus bagaimana?" / "Ayah mau aku di sini, kan?").
- **Selasa:** mengenal desa — kenal Sekar (juga bimbang soal beasiswa), Bayu di lapangan/dermaga, cerita sampingan kecil
  (mis. Si Belang hilang dicari bersama Laras; Pak Haji minta halamannya dibersihkan lalu bercerita soal mercusuar).
- **Rabu:** gudang Juragan Darto. 🔀 Wawancara (jujur / sok tahu / diam) → diterima (gudang) / ditolak (perahu Pak Karta).
  Pemain condong kota bisa bilang "kerja sementara selama liburan". Bab 1 selesai.
- Percabangan Bab 1: jawaban ke Ibu, janji seribu ke Laras, wawancara Darto (+ pilihan kecil di adegan waktu bebas).

**Keputusan lain yang sudah disetujui**
- Pilihan uang jalur kota (hemat / kerja diam-diam / terus terang) pindah ke keputusan final 3 Juli.
- Sekar: bila pemain & Sekar sama-sama memilih kota → **berangkat bersama** Sabtu 11 Juli. Tidak ada versi Sekar duluan/belakangan.
- Kerja di gudang lalu ke kota: adegan pamit ke Darto (kecewa karena Raka / menitip foto Raka; bila dulu bilang "kerja tetap" →
  merasa dibohongi, Kepercayaan turun).
- **Kerja serabutan** sesekali (Rp10.000–25.000): bersihkan halaman Pak Haji, angkat karung kios beras, cat pagar balai desa,
  jaga anak tetangga, jemur jaring nelayan lain — kadang dengan cerita kecil.
- Bab 1 fokus membangun hubungan + 1–2 cerita sampingan kecil; cerita besar (Laras diejek, hutang Ayah) tetap di Bab 2.

## 6b. RANCANGAN BARU lanjutan (disetujui 19 Okt 2026, ✅ dibuat build 56: bab2.txt, adegan_bab2.txt) — timeline mulai 1 Juni, Bab 2 tiga minggu

**Timeline baru** (menggantikan tanggal di 6a): cerita dimajukan supaya pemain jalur kota juga lama di desa (± 6 minggu).
| Bagian | Tanggal |
|---|---|
| Bab 1 — Lulus SMP (isi sama seperti 6a; Ibu menyebut batas pendaftaran **Jumat 26 Juni**) | Senin 1 – Rabu 3 Juni |
| Bab 2 — Mencari Pegangan (semua pemain) | Kamis 4 – Jumat 26 Juni |
| **Keputusan final kota/desa** | Jumat 26 Juni |
| Jalur kota: persiapan & pamitan | 27 Juni – Sabtu 11 Juli (berangkat) |
| Jalur desa: Bab 3 & 4 (dirancang ulang) | 27 Juni – 17 Agustus |

**Bab 2 per minggu**
- **Minggu 1 (4–7 Juni) — beradaptasi:** hari pertama kerja 🔀 teliti/cepat/beralasan · warung sepi 🔀 ide untuk Ibu ·
  kerja serabutan pertama (halaman Pak Haji → cerita mercusuar) · Minggu malam gaji pertama 🔀 untuk Ibu.
- **Minggu 2 (8–14 Juni) — mengenal lebih dalam:** Laras diejek 🔀 hibur/adukan/datangi · simpul jaring & Ayah putus sekolah ·
  Sekar: beasiswa & mata ibunya · perkutut Darto · Sabtu malam bakar ikan 🔀 rahasia Bayu (bantu/ledek/simpan).
- **Minggu 3 (15–21 Juni) — rahasia keluarga:** bisik-bisik hutang perahu 🔀 tanya/pura-pura/janji · bicara dengan Darto 🔀
  bayar sebagian/lembur/minta waktu · utang bapak Bayu · Ibu & kampung halamannya (+ alasan keluarga sahabat pergi bila percaya tinggi) ·
  Si Belang hilang.
- **Minggu 4 (22–26 Juni) — memilih:** tiap orang membicarakan pilihan pemain (Laras merengek, Ayah di dermaga, Bayu, Darto & kisah Raka
  sebagai peringatan) · berkas pendaftaran (bila condong kota) · Sekar memutuskan 🔀 pergi/tinggal/tanya ibumu ·
  **Jumat 26 Juni: keputusan final** 🔀 kota (+ hemat / kerja diam-diam / terus terang) / desa.
- Prinsip: tiap karakter desa minimal 2–4 adegan di Bab 2 (ada yang sore/malam supaya pemain yang bekerja bisa menemui);
  kebiasaan keluarga malam hari memunculkan obrolan kecil.
- Isi Bab 3 desa lama (hutang, Sekar, utang Bayu, cerita Ibu) pindah ke Bab 2 → **Bab 3 & 4 desa dirancang ulang**.

**Desa tetap hidup walau pemain di kota** (pemain kota mengalaminya dari jauh):
Ibu sakit (telepon Laras; kirim uang / pulang dengan risiko bolos) · Darto menagih (chat Ibu; kirim uang) · malam badai 7 Agustus
(telepon tengah malam, menunggu kabar) · lomba layangan Laras (video call; janji layangan sebelum berangkat) · mercusuar menyala
(foto di Sosmed + pesan misterius) · libur semester & Lebaran: pulang kampung dengan adegan khusus.

**Romansa Sekar–Bayu:** di desa (15 th) hanya persahabatan; Bayu bisa cemburu biasa bila pemain akrab dengan Sekar.
Setelah 18+: pemain boleh mendekati Sekar diam-diam, tapi **tidak dijamin aman** — bisa ketahuan (dilihat Bayu, gosip, chat, Sekar mengaku).
Ending: *Rahasia yang Terjaga* / *Persahabatan yang Retak*; atau jujur ke Bayu sejak awal / membantu Bayu.

**Perbaikan yang harus ikut dibuat:** Sekar di naskah ditulis "berkerudung biru" — SALAH. Sekar: rambut hitam panjang, bando ungu,
kacamata bulat, kardigan lavender. Perbaiki naskah & prompt `dermaga__sekar.jpg`.

**Tokoh tambahan bergambar (prompt di PROMPT_GAMBAR.md):** Pak Karta (`karta`, bapak Bayu) ★ · Pak Usman (`usman`) ★ · Mbak Yati (`yati`) ★ ·
Pak Haji Somad (`haji`) ☆ · Bu Wati (`wati`, ibu Sekar) ○. Dino (pengejek Laras) tanpa gambar.

## 6c. RANCANGAN BARU — Bab 3 (disetujui 19 Okt 2026, ✅ dibuat build 56: kota_bab3.txt + adegan_pamit.txt, desa_bab3.txt + adegan_desa.txt)

**Bab 3 Kota — "Pamit" (Sabtu 27 Juni – Sabtu 11 Juli)**
- Minggu 1, urusan berangkat: daftar ulang & berkas di kecamatan bersama Ibu · info 3 kos lewat HP (pilihan kos tetap saat tiba,
  tidak dobel) · berhenti kerja & pamit ke Darto (hasil sesuai "kerja sementara"/"kerja tetap") · bekal: kerja serabutan terakhir,
  🔀 terima/tolak celengan bambu Ibu.
- Bila Sekar ikut: adegan berdua (urus berkas bersama, menghibur Bu Wati).
- **Sabtu 4 Juli: pasar malam di lapangan desa** (adegan ringan untuk kedua jalur: komidi putar, Laras, Bayu & Sekar).
- Minggu 2, pamitan dalam: Laras (kotak sepatu "Buat Kakak"; 🔀 janji layangan 17 Agustus / janji telepon tiap malam) ·
  Ayah di dermaga (pisau lipat nelayan; bila tahu hutang 🔀 "aku kirim uang" / "jangan melaut kalau langit merah" → kunci malam badai) ·
  Ibu (bekal, menangis di dapur) · Bayu (titip untuk Sekar / mengaku iri lalu berdamai) · Darto (foto Raka, bila percaya cukup) ·
  malam terakhir di dermaga, pesan misterius "Aku dengar kamu akan datang."
- Sabtu 11 Juli berangkat (sendiri / bersama Sekar). *(Dibuat: tiba di Sakala & survei kos ada di awal Bab 4 Kota, sesuai ALUR_BAB4.)* **Tiba di Sakala: menjelajah kota & survei 3 kos langsung** (kondisi asli
  baru ketahuan), 🔀 menawar, memilih sebelum malam. Sekar ke asrama beasiswa, menemani berkeliling dulu.

**Bab 3 Desa — "Musim Ombak Tinggi" (27 Juni – 19 Juli)** (bukan "angin barat": Juli = musim angin timur, kemarau, ombak tinggi)
- Tangkapan sepi → penghasilan Ayah turun → tekanan hutang naik (benih badai Bab 4).
- Tahun ajaran baru Laras (kelas 6) 🔀 bantu beli seragam & buku / serahkan ke orang tua.
- Daftar Paket C di kecamatan (bila benih ada), teman belajar baru.
- Bayu masuk gudang (bila utang bapaknya) → cerita kecil sebagai rekan kerja.
- Sekar tinggal: tawaran kerja antar jahitan Bu Wati & teman belajar Paket C.
- Darto: lembur Sabtu bersama, mulai melihat pemain seperti anaknya sendiri.
- Sabtu 4 Juli pasar malam · Sabtu 11 Juli stasiun (mengantar teman/Sekar) + mimpi "hidup paralel" (hari yang sama pemain kota berangkat).
- Senin 13 Juli Ibu jatuh sakit 🔀 bayar sendiri / pinjam Darto / serahkan Ayah, lalu 🔀 siapa menjaga warung.
- Sahabat: Laras & gelang 🔀 jujur/mengelak · akun lama "A." di Sosmed · Minggu 19 Juli pesan misterius.

**Desa dari jauh (pemain di kota)** — berlaku Bab 3 dst.
| Cara | Kegunaan |
|---|---|
| 💬 Chat (ada) | kabar sehari-hari Ibu, Laras, Bayu |
| 📞 Telepon / video call (fitur baru) | momen penting: Ibu sakit, malam badai, lomba layangan |
| 💸 Kirim uang lewat Dompet (fitur baru) | obat Ibu, cicilan perahu, sekolah Laras |
| 🚆 Pulang kampung (4 jam kereta) | akhir pekan (risiko lelah/bolos), libur semester, Lebaran — adegan khusus |
| 📷 Sosmed (ada) | foto desa: mercusuar, piala Bayu |
- Ayah: telepon singkat; percaya naik bila ikut mencicil / mengingatkan soal langit merah. Ibu: chat & kiriman. Laras: video call,
  janji ditagih. Bayu: chat, curhat Sekar, bisa berkunjung ke Sakala. Sekar: langsung (kota) / chat (desa).
- **Darto: misi mencari Raka di Sakala** — petunjuk dikirim lewat Ayah menaikkan Kepercayaan; Raka bisa ditemukan (cerita besar).
- Ibu sakit 13 Juli untuk pemain kota: telepon Laras menangis 🔀 kirim uang / izin pulang di minggu pertama sekolah.
- **Hubungan bisa merenggang** bila lama tidak dihubungi; telepon di momen penting / pulang kampung memulihkan.

**Gambar:** semua tokoh di game dibuatkan gambar (prompt di PROMPT_GAMBAR.md), termasuk Pak RT, Pak Kades, Mantri Joko, Rani, Dodi,
Dino, Raka muda (sesuai foto).

## 6d. STRUKTUR BAB (disetujui 19 Okt 2026) — mulai Bab 3 jalur DIPISAH

```
Bab 1 — Lulus SMP            (desa, semua pemain)
Bab 2 — Mencari Pegangan     (desa, semua pemain)
            │
   Keputusan Jumat 26 Juni
      ┌─────┴─────┐
   KOTA          DESA
Bab 3 Kota     Bab 3 Desa
"Pamit"        "Musim Ombak Tinggi"
Bab 4 Kota     Bab 4 Desa
(hidup di      "Tujuh Belasan"
 Sakala)
```
- Satu permainan = satu jalur. Bab 3 Kota = proses pindah (masih di desa: berkas, berhenti kerja, pamitan → kereta → tiba di Sakala,
  survei & pilih kos). Bab 4 Kota = hidup di kota (cerita kota yang sudah ada, disesuaikan; nomor bab bergeser).
- Bab 3 & 4 Desa = pemain tetap di desa.
- "Desa dari jauh" BUKAN bab gabungan: di Bab 4 Kota kabar desa hanya datang lewat chat / telepon / kirim uang / pulang kampung.
- Setiap bab 3 & 4 (desa maupun kota) harus punya arah cerita jelas, banyak adegan, banyak percabangan.
- ✅ Bab 4 Kota dirancang (ALUR_BAB4.md) dan dibuat di game build 56 (kota_bab4.txt + adegan_kota.txt).

**Bab 4 Desa "Tujuh Belasan" (20 Juli – 17 Agustus)** — isi seperti bagian 10 dengan penyesuaian: Darto datang 3 Agustus (cicilan dari
Bab 2–3), Sekar memimpin mercusuar bila tinggal (Pak Kades bila pergi), hadiah Bayu untuk Sekar, malam 17 Agustus Darto menatap
mercusuar berharap Raka melihat. Kejadian yang sama dialami pemain kota dari jauh (tabel 6c).

## 7. Bab 1 — Lulus SMP (Senin 22 Juni)

Alur utama (sudah ada): kenangan → bangun → jalan ke ruang makan → sarapan → Laras berangkat → buka warung →
pertanyaan Ibu + pesan misterius → **pilihan besar** (sekolah di kota / tetap di desa) → uang hadiah → waktu bebas
sampai 20:00 → malam.

Adegan di waktu bebas hari itu:

| Adegan | Tempat & jam | Isi & cabang |
|---|---|---|
| **Bicara dengan Ayah** | Ruang makan 12:00–13:00 (otomatis) | Ibu menyuruh bilang sendiri ke Ayah. Jalur desa: Ayah bangga tapi diam-diam sedih (ia putus sekolah). Pilihan: *"Aku tetap belajar sendiri, Yah"* / *"Uang lebih penting sekarang"* / *"Aku belum tahu, Yah"*. Jalur kota: Ayah merestui, "jangan jadi seperti Ayah". |
| **Seribu untuk Laras** | Ruang makan 13:00–14:00 (otomatis) | Laras menagih janji seribu rupiah dari sarapan. Beri / pura-pura lupa / bercanda. Tercatat: menepati janji ke Laras. |
| **Bayu di lapangan** | Lapangan 15:00–17:30 atau dermaga 17:30–18:30 (temui Bayu) | Ngobrol soal masa depan, iri pada yang ke kota. Bayu menyebut Sekar, juara kelas. |
| **Makan malam pertama** | Ruang makan 17:45–18:30 (otomatis) | Keputusan diumumkan ke Laras. Jalur kota: Laras ngambek. Jalur desa: Laras senang kakaknya tidak pergi. |
| **Ayah berangkat** | Dermaga 18:30–19:15 (temui Ayah) | Ayah bercerita tentang badai tahun pemain lahir (kisah_3). |

---

## 8. Bab 2 Desa — Mencari Pegangan (23 Juni – 5 Juli)

**Selasa 23 Juni (utama, sarapan):** Ayah: "Besok pagi ikut Ayah ke gudang Juragan Darto." Waktu bebas sampai Rabu 06:30.
- Adegan *Kenalan dengan Sekar* (Selasa–Kamis 09:00–11:30, depan rumah & warung, otomatis): Sekar mengantar seragam
  jahitan ibunya untuk Laras. Ia diam-diam menolak beasiswa ke Sakala. Pilihan sikap: menyemangati / bertanya kenapa / bercanda.

**Rabu 24 Juni 07:00 (utama): Gudang Juragan Darto.** Darto menguji dengan pertanyaan ("Kenapa tidak sekolah?",
"Tahan bau dan panas?"): jujur / sok kuat / diam → skor wawancara → `@lamar buruh_ikan`.
- Diterima → mulai **Senin 29 Juni**. Ditolak → Ayah menyarankan ikut perahu Pak Karta (bapak Bayu).
- Pulang bersama Ayah: "Juragan Darto keras... tapi Ayah berutang budi padanya." (benih rahasia hutang perahu)

**Rabu–Minggu (waktu bebas sampai Senin 29 Juni 06:00), adegan:**
| Adegan | Tempat & jam | Isi & cabang |
|---|---|---|
| Tawaran melaut | Temui Bayu | Pak Karta butuh tenaga subuh → membuka "Ikut perahu Pak Karta". Bayu cerita bapaknya berutang ke Darto. |
| Warung sepi | Temui Ibu di warung | Ibu menghitung uang. Usulan: menu baru (cireng isi ikan asin) / pesan antar lewat grup WA desa / tidak usah. → `warung_ide` |
| Simpul jaring | Temui Ayah 13:00–16:00 | Ayah mengajari simpul jaring; cerita putus sekolah (kisah_1). |
| Laras diejek | Ruang tengah 14:00–16:00 (otomatis, Kamis/Jumat) | Laras menangis, diejek "bau ikan". Hibur & ajari menjawab / adukan ke Ibu / datangi anak-anak itu besok. → `laras_diejek` |
| Bakar ikan malam Minggu | Dermaga Sabtu 19:00–21:00 (otomatis) | Bayu, Sekar, pemain. Ketahuan Bayu suka Sekar. Bantu Bayu / ledek / simpan rahasia. |

**Kamis 25 Juni (utama): hari pertama kerja.** Gudang (07:00): kenal Mbak Yati & Pak Usman, Darto galak, pilihan teliti / cepat /
beralasan, upah pertama Rp65.000. Jalur perahu (04:15): subuh pertama di perahu Pak Karta bersama Bayu.

**Minggu 28 Juni malam (utama): uang pertama.** Memberi Ibu Rp50.000 / Rp20.000 / menyimpan semuanya; gambar Laras "Kakak pahlawan bersayap".

**Waktu bebas Kamis 25 Juni – Minggu 5 Juli 19:30, adegan:**
| Adegan | Tempat & jam | Isi & cabang |
|---|---|---|
| Perkutut Juragan | Temui Darto (setelah kenal) | Darto & burung perkututnya; sikap sopan → Kepercayaan; isyarat anaknya yang pergi ke kota. |
| Sekar & buku | Temui Sekar di dermaga | Sekar cerita beasiswa (kisah_2). Jalur desa: menyarankan **Paket C** → membuka "Belajar mandiri (Paket C)". |
| PR matematika | Temui Laras 14:00–16:00 | Bantu PR. Jawab sendiri vs ajari pelan-pelan. |
| Sinetron Ibu | Temui Ibu 18:30–21:00 | Ibu bercerita kampung halamannya & keluarganya yang menolak (kisah_1). |

**Minggu 5 Juli malam (utama): bisik-bisik.** Pemain mendengar Ayah & Ibu: cicilan perahu ke Darto menunggak tiga bulan.
Pilihan: masuk dan bertanya / pura-pura tidak dengar / diam-diam berencana membantu. → `tahu_hutang_perahu`

---

## 9. Bab 3 Desa — Benang Merah (6–19 Juli)

**Senin 6 Juli (utama):** rutinitas; kabar teman-teman bersiap ke Sakala Sabtu ini. Waktu bebas sampai Sabtu 11 Juli 07:00.

| Adegan | Tempat & jam | Isi & cabang |
|---|---|---|
| Hutang perahu | Temui Darto (tahu hutang) | Sisa Rp1.200.000. Pilihan: potong upah tiap minggu / lembur Sabtu sore / minta waktu. → `rencana_lunasi` |
| Pilihan Sekar | Temui Sekar (setelah "Sekar & buku") | Batas beasiswa Jumat. Dorong berangkat / dukung tinggal / suruh bicara jujur ke ibunya. → `sekar_pergi` |
| Utang bapak Bayu | Temui Bayu di dermaga | Bayu mungkin berhenti dan jadi buruh Darto. Bisa jadi rekan kerja di gudang. |
| Antar jahitan | Depan rumah 15:00–17:00 (setelah Sekar tinggal) | Bu Wati menawarkan kerja antar jahitan. |

**Sabtu 11 Juli 07:00 (utama): Stasiun desa.** Rani & Dodi (dan Sekar bila memilih pergi) berangkat ke SMA di Sakala.
Pemain menatap kereta yang sama dengan kereta sahabatnya dulu → mimpi singkat "hidup paralel" (dirinya di kereta itu).
Bila Sekar tinggal dan cukup percaya: ia mengaku pernah melihat **{nama_sahabat} menangis di stasiun ini** sehari sebelum pergi (kisah_3, petunjuk).

**Senin 13 Juli 05:45 (utama): Ibu jatuh di dapur.** Laras membangunkan pemain; Ayah (baru pulang) membopong Ibu ke tikar ruang tengah;
Mantri Joko: kelelahan & darah tinggi, obat Rp150.000. Bayar sendiri / pinjam Darto (hutang atas nama pemain) / serahkan Ayah.
Lalu: siapa menjaga warung? Pemain sebelum kerja / Laras sepulang sekolah / tutup warung. Dua hari Ibu berbaring → Rabu 15 Juli
Ibu kembali berjualan pelan-pelan (`ibu_sakit = 1`).

**Waktu bebas sampai Minggu 19 Juli 20:00:**
| Adegan | Tempat & jam | Isi & cabang |
|---|---|---|
| Cerita Ibu | Temui Ibu (setelah sakit) | Bila Kepercayaan Ibu tinggi: keluarga sahabat pergi malam-malam karena ayahnya tertipu di kota dan rumahnya disita (kisah_3, petunjuk). |
| Gelang benang merah | Temui Laras | Laras bertanya soal gelang. Cerita jujur / mengelak. |
| Anak Juragan | Temui Darto (Kepercayaan ≥ 50) | Putra Darto pergi ke Sakala dan tidak pulang. Darto keras karena itu. |
| Gambar untuk Kakak | Ruang tengah malam, Laras | Kotak sepatu "Buat Kakak" (kisah_3 Laras). |

**Minggu 19 Juli malam (utama):** pesan misterius: *"Kalau kamu tidak datang ke Sakala... mungkin suatu hari aku yang akan pulang."*

---

## 10. Bab 4 Desa — Tujuh Belasan (20 Juli – 17 Agustus)

**Senin 20 Juli (utama):** Pak RT di warung mengumumkan lomba 17 Agustus: sepak bola antar-RT, layangan anak, panjat pinang,
bazar UMKM, dan rencana Pak Kades **menyalakan kembali mercusuar** (butuh relawan & dana).

Empat alur berjalan bersamaan selama waktu bebas (adegan bertahap, tiap tahap butuh tahap sebelumnya):
| Alur | Tahap | Hasil di 16–17 Agustus |
|---|---|---|
| ⚽ **Tim bola Bayu** | ajakan → latihan 1–3 di lapangan → strategi | menang / kalah, Cinta Bayu |
| 🪁 **Layangan Laras** | rangka bambu → kertas & lukisan Laras → uji terbang | juara / tidak, Cinta Laras |
| 🍳 **Bazar warung Ibu** | memilih menu (dari `warung_ide`) → belanja bahan → promosi | laku / sepi, uang keluarga |
| 🗼 **Mercusuar** | rapat relawan → mencari lampu & aki → sumbangan (Darto/pemain) | mercusuar menyala / tidak |

**Senin 3 Agustus (utama): Juragan datang saat sarapan.** Bila cicilan yang sudah dibayar pemain < Rp400.000: tenggat Sabtu 8 Agustus
(separuh hutang) → `tekanan_hutang = 1`. Bila ≥ Rp400.000: Darto memberi waktu sampai akhir tahun; Ayah bangga (kalau sudah diberi tahu)
atau tersinggung (kalau dicicil diam-diam → kebohongan `cicilan_rahasia` terbongkar).

**Jumat 7 Agustus 17:30 (utama): langit merah tembaga.** Tanpa tekanan hutang, Ayah tidak berangkat (aturannya sendiri) — badai
hanya meretakkan perahu di tambatan (perbaikan Rp300.000). Dengan tekanan hutang, Ayah nekat berangkat: pemain bisa mencegah
(berhasil bila Cinta Ayah ≥ 75), memaksa ikut (ditolak), atau membiarkan. Bila berangkat: malam badai di rumah → subuh Ayah belum pulang →
cari dengan perahu Pak Karta (lebih aman bila keahlian laut ≥ 3) / minta perahu motor Darto (gratis bila Kepercayaan ≥ 45) / menunggu.
Ayah ditemukan berpegangan di tiang bagan; **perahunya tenggelam**.

**Sabtu 8 Agustus siang (utama): rapat keluarga.** Perahu tenggelam: terima tawaran Darto (hutang lunas, Ayah jadi anak buah kapal motornya) /
menabung untuk perahu bekas / minta tolong orang desa (berhasil bila Cinta Bayu ≥ 55 atau Kepercayaan Darto ≥ 55 → gotong royong,
Darto memberi lambung perahu tuanya). Perahu retak: bayar Rp300.000 / dicicil. → `perahu_ayah`

**Minggu 16 Agustus (utama):** final sepak bola antar-RT.
**Senin 17 Agustus (utama):** upacara, lomba layangan, bazar, malamnya mercusuar (menyala bila berhasil). Pesan misterius:
*"Aku lihat fotonya. Mercusuar kita menyala lagi."* → Bersambung.

### File naskah desa
| File | Isi |
|---|---|
| `prolog.txt` | Bab 1 (semua pemain) |
| `adegan_prolog.txt` | 5 adegan waktu bebas hari pertama |
| `desa_bab2.txt`, `desa_bab3.txt`, `desa_bab4.txt` | alur utama Bab 2–4 desa |
| `adegan_keluarga.txt` | adegan Ayah, Ibu, Laras (Bab 2–3) |
| `adegan_teman.txt` | adegan Bayu & Sekar (Bab 2–3) |
| `adegan_darto.txt` | adegan Juragan Darto (Bab 2–3) |
| `adegan_tujuhbelas.txt` | Bab 4: tim bola, layangan, bazar, mercusuar, setelah badai |

Pemicu semua adegan: `data/adegan.txt` (43 adegan). Nama tambahan: Ayah = Pak Darman, Ibu = Bu Ratna.

---

## 11. Benang merah sahabat di desa

| # | Petunjuk | Dari | Syarat |
|---|---|---|---|
| 1 | Pesan "Aku tunggu di Kota Sakala" | prolog | semua |
| 2 | Sosmed: akun lama ber-inisial A. | HP Sosmed | rutin mencari |
| 3 | {nama_sahabat} menangis di stasiun sehari sebelum pergi | Sekar | Sekar tinggal, Kepercayaan ≥ 40 |
| 4 | Keluarganya pergi karena ayahnya tertipu & rumah disita | Ibu | Kepercayaan Ibu ≥ 70 |
| 5 | Putra Darto juga di Sakala — "kota itu menelan anak-anak desa" | Darto | Kepercayaan ≥ 50 |
| 6 | Pesan "mungkin suatu hari aku yang akan pulang" | naskah Bab 3 | semua |
| 7 | Pesan "Mercusuar kita menyala lagi" | naskah Bab 4 | mercusuar menyala |

---

## 12. Variabel cerita penting

`desa_bab` (2/3/4) · `ayah_restu` · `janji_laras_seribu` · `warung_ide` (menu/antar/tidak) · `laras_diejek` (hibur/adu/datangi) ·
`rahasia_bayu` · `paket_c` · `tahu_hutang_perahu` · `rencana_lunasi` (potong/lembur/tunda) · `sekar_pergi` · `ibu_sakit_jaga` ·
`latihan_bola` (0–3) · `layangan_tahap` · `bazar_tahap` · `mercusuar_tahap` · `ayah_selamat_cara` · `perahu_ayah` (diperbaiki/dijual).
Setiap adegan yang sudah terjadi tercatat otomatis sebagai `adegan_<id> = 1`.

---

## 13. Ketidaksesuaian yang diperbaiki (19 Okt 2026)

| Sebelumnya | Diperbaiki menjadi |
|---|---|
| Profil Ayah: "selalu pulang sebelum magrib", padahal di cerita Ayah melaut malam | Ayah tidak pernah melaut saat langit di barat merah tanda badai, dan selalu pulang sebelum matahari terbit |
| Ibu tercatat di warung sejak 05:00, padahal pagi ia memasak & sarapan | Ibu di ruang makan 05:00–07:15, warung mulai 07:15 |
| "Nanti siang bilang sendiri ke Ayahmu" tapi tidak pernah ada adegannya | Adegan *Bicara dengan Ayah* di ruang makan 12:00–13:00 |
| Bab 2 desa langsung loncat "dua minggu setelah kelulusan" | Hari-hari itu dimainkan (Bab 2 Desa, 23 Juni – 5 Juli) |
| Ibu pingsan "dibaringkan di tikar ruang tengah" tapi latarnya ruang makan | Latar ruang tengah |
| Bayu: "nongkrong di dermaga", tapi tempat janjinya warung | Bayu punya jadwal di lapangan & dermaga; janjian di dermaga |
| Melaut "bersama Ayah" jam 04:00 padahal Ayah melaut malam | Melaut subuh = ikut perahu Pak Karta (bapak Bayu) |
| Sekar tidak pernah dikenalkan di cerita | Adegan *Kenalan dengan Sekar* di Bab 2 |
| Tokoh berdiri ditempel di atas latar saat jelajah | Dihapus; latar berganti ke gambar latar+karakter buatan pemilik |
