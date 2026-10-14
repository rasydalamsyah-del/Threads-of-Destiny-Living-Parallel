# Catatan Progres — Threads of Destiny: Living Parallel

> Catatan semua yang sudah dikerjakan, yang sedang/akan dikerjakan, dan keputusan yang sudah diambil.
> Aturan & sistem lengkap: **[RANCANGAN_GAME.md](RANCANGAN_GAME.md)**.
> Diperbarui setiap ada perubahan.

---

## ✅ Sudah dikerjakan

| Tanggal | Build | Yang dikerjakan |
|---|---|---|
| 15 Okt 2026 | 31 | **Masukan pemilik (alur & obrolan):** Laras digambar lebih kecil (kolom `skala` per karakter). **Pagi di prolog ditulis ulang** pelan & runtut: bangun → Ibu memanggil sarapan → sarapan di **ruang makan** (latar baru, sementara) bersama Ayah, Ibu, Laras membicarakan ekonomi keluarga (harga ikan, solar, warung sepi) tanpa pilihan, memakan waktu → Ibu mengambil bekal, Laras pakai sepatu, salam, berangkat sekolah naik sepeda → Ayah tidur setelah melaut → Ibu minta bantu buka warung → obrolan masa depan → pilihan. **Sekar tidak lagi muncul** tanpa perkenalan. **Jadwal karakter** (`ditemui`, `jadwal`): "Temui seseorang" hanya berisi orang yang memang ada (Ayah dari siang, Laras setelah sekolah, Bayu/Sekar/Dimas lewat janji). **Obrolan baru per karakter** (`data/obrolan.txt`, 68 obrolan): 😊 santai sesuai kesukaannya, 🔍 kenali lebih dalam (membuka hobi/cita-cita/kisah; menolak bila belum akrab), ❓ pertanyaan khusus sesuai cerita (lowongan, hutang, sahabat masa kecil). Menu bertemu dinamis (hadiah hanya bila tas berisi), maks. 2 obrolan per pertemuan. **HP:** notifikasi hilang setelah pesannya dibaca; ketuk notifikasi membuka chatnya. Tutorial disesuaikan (contoh: temui Ibu). Tes baru `MeetingTest` |
| 15 Okt 2026 | 30 | **Perbaikan dari tangkapan layar pemilik:** tombol pilihan menimpa kotak dialog (pertanyaan & nama pembicara tertutup) dan teks dialog tampak pudar. Penyebab: posisi pilihan dihitung saat kotak dialog masih beranimasi muncul (transparan) → dianggap tidak tampil. Kini posisi pilihan selalu dihitung dari tinggi kotak dialog yang sebenarnya, diukur ulang setiap kali ukuran dialog berubah, dan dialog dipastikan tampil penuh saat ada pilihan |
| 15 Okt 2026 | 29 | **Jam mulai aktivitas** (`mulai` di aktivitas.txt, boleh melewati tengah malam): melaut 04:00–06:00, lari pagi 05:00–09:00, bantu warung 05:30–17:00, main dengan Laras 13:00–20:00, perpustakaan 08:00–16:00, jelajah/cari jejak 08:00–17:00, mal 10:00–21:00, kafe 08:00–22:00, begadang game 19:00–02:00. Aktivitas di tempat yang sama yang belum/sudah lewat waktunya tampil **redup** di bagian "🕐 Belum bisa sekarang" dengan keterangan (mis. "Sudah terlalu siang untuk melaut bersama Ayah (paling lambat 06:00)"). Aktivitas tempat lain (desa saat di kota) tetap tidak tampil. Tes baru: jendela jam mulai, data `mulai`, penyaringan tempat |
| 15 Okt 2026 | 28 | **Tata letak layar ditetapkan** (tidak saling menimpa walau teks memanjang). ATAS: kiri = panel karakter yang bicara (tinggi tetap 56dp, foto 44dp, nama/tingkat/meter satu baris dengan "…", lebar dibatasi agar tidak menabrak kanan); kanan baris 1 = **ikon HP (pindah dari kanan bawah)** + tombol ☰; baris 2 = jam versi pendek ("Sen, 13 Jul · 07:30 ☀️", satu baris); baris 3 = petunjuk "Ketuk HP"; tombol 📊 laci status di sisi kiri di bawah panel. BAWAH: kotak dialog menempel di bawah; tombol pilihan selalu di atas kotak dialog (posisi diukur ulang setelah teks ditata); layar tanpa latar → pilihan di tengah. Perbaikan tutorial 7: ikon HP tertutup kotak penjelasan. **Meter rasa sayang**: 💛 Akrab untuk sesama jenis/keluarga/anak/belum dewasa, 💗 Cinta hanya untuk lawan jenis yang bisa dipacari setelah sama-sama 18+, ❤️ Cinta saat berpacaran (sebelumnya semua tertulis "Cinta"). Tes baru: format jam pendek & aturan romansa |
| 15 Okt 2026 | 27 | **Pemeriksaan & pengujian jam sungguhan.** Unit test otomatis di GitHub Actions (24 tes, semua lulus; build gagal bila ada tes gagal): format jam & durasi, data `durasi`/`jam`, batas bagian hari, lewat tengah malam, berhari-hari, laju lapar/haus/energi, tidur, begadang, mati kelaparan/dehidrasi, `@kalender`, `@maju`, jendela hadir & bolos (hari pertama tidak dihitung), simpan/muat (termasuk simpanan lama), dan **simulasi naskah asli** jalur kota & desa (hari/jam setiap adegan dicocokkan dengan teks cerita). Perbaikan dari pemeriksaan: kartu "Senin, malam" di prolog, "Malam pertama" & hari Minggu di Bab 2 kini sesuai jam; `@bebas 9/10` ("satu minggu") diganti `@bebas sampai Sabtu 15:00` / `@bebas 6 hari` (tidur tidak mengakhiri waktu bebas beberapa hari); `@kalender` ke jam lebih sore di hari yang sama kini menjalankan waktu (lapar/haus ikut turun); hari pertama kerja/sekolah tidak dihitung bolos; makan dari tas saat waktu bebas memakan 15 menit (minum/obat 5 menit); teks Juragan Darto & pengingat sekolah memakai jam |
| 15 Okt 2026 | 26 | **Jam sungguhan:** jam berjalan per menit (contoh "Senin, 13 Jul · 07:30 ☀️"). Aktivitas punya durasi (`durasi` di aktivitas.txt), sekolah/kerja punya jam masuk–pulang (`jam` di pekerjaan.txt; gaji harian disesuaikan per shift), terlambat > 15 menit = kinerja turun, bolos bila tidak datang sampai 1 jam sebelum pulang. Tidur: pilih jam bangun (kurang tidur = mengantuk), begadang menguras energi & kewarasan. Kebutuhan turun per jam. Bagian hari (Pagi/Siang/Sore/Malam) tetap dipakai untuk ketersediaan aktivitas, karakter, janji. Waktu yang dimajukan cerita tidak dihitung bolos/ingkar janji. `@kalender Senin 07:30`, `@maju 2 jam`. **Perbaikan:** teks kejadian (mis. flu di tutorial) sempat langsung hilang sebelum pilihan muncul — kini tampil dulu |
| 14 Okt 2026 | 25 | **Masukan pemilik:** tombol **‹ Menu Utama** di layar isi nama. Tombol Tutorial/Lewati muncul jauh lebih cepat (transisi dipercepat; kalimat tepat sebelum `@pilihan` kini tampil bersama tombolnya, berlaku di seluruh game). Teks judul di tengah layar diberi kotak semi-gelap + bayangan. **Bug panel status tidak muncul** diperbaiki (animasi muncul terbatalkan → panel transparan; juga terjadi di permainan biasa). **Profil karakter**: hobi, kegemaran, pelajaran/keahlian, cita-cita, 3 kisah hidup untuk semua karakter; terbuka bertahap lewat obrolan (Cinta, jumlah obrolan, Kepercayaan), diceritakan orang dekatnya, atau `@info` di naskah. Menu ☰ → **Karakter**: daftar (yang belum dikenal 🔒 ???), halaman detail dengan gambar, bar Cinta & Kepercayaan, sifat, suka/tidak suka, info yang terbuka. Tutorial ikut menampilkan halaman Karakter |
| 14 Okt 2026 | 24 | **Tutorial (versi final, interaktif):** setelah isi nama → pilihan **📖 Tutorial** / **⏭️ Lewati** di tengah layar (tombol pilihan kini rata tengah, huruf tebal cokelat tua, latar krem bergradasi berbingkai emas). Tutorial bisa dicoba langsung (pilihan ke Bayu, makan dari tas, waktu bebas 2 kegiatan, buka pesan HP, kejadian flu) dan setiap penjelasan **disertai contoh & menampilkan bagian layarnya** lewat perintah baru `@tunjuk`: menu ☰, panel karakter, panel status, Status & Tas, Belanja, jam, layar waktu bebas, ikon HP, layar utama HP & setiap aplikasi (Chat, Kontak, Kalender, Lowongan, Dompet, Catatan, Sosmed), Cari kerja. Akhir tutorial: `@mulai_ulang` menghapus hasil latihan → prolog |
| 14 Okt 2026 | 21 | **Masukan pemilik (sinkron & jalur):** panel status jadi **laci geser dari kiri** (nama + angka tiap bar + uang, tombol 📊 pemicu, ⚠️ bila ada yang kritis). **Pilihan awal jadi 2**: sekolah di kota / hidup di desa. Jalur kota: orang tua cemas soal uang → 3 jawaban (cukup kiriman / kerja diam-diam = kebohongan yang bisa terbongkar / bilang terus terang = kiriman lebih kecil, kepercayaan naik). Kota bebas: aktivitas hidup baik (perpustakaan, lari pagi) vs foya-foya (mal, kafe, begadang game) dengan akibat (uang menipis → minta ortu, Ibu curiga, ditegur wali kelas). Dimas bisa jadi orang dalam kurir. **Aplikasi Sosmed**: beranda unggahan teman (suka = Cinta +1) & cari sahabat 1×/hari (3 temuan bertahap → petunjuk). Perbaikan sinkron: tidak bisa mengajak ketemu karakter di tempat lain; janji dengan teman desa batal baik-baik saat pindah ke kota |
| 14 Okt 2026 | 1 | Proyek Android native (Kotlin), portrait. Splash 3 detik → video intro → menu utama (judul emas, 4 tombol). GitHub Actions membuat APK otomatis + rilis "latest-build" |
| 14 Okt 2026 | 4 | START GAME: isi nama → prolog masa lalu (efek kenangan, gerak kamera) → layar gelap + teks waktu → efek bangun tidur (kedip, buram) → duduk di ranjang → pesan HP masuk + layar HP. Mesin naskah teks (`assets/naskah/prolog.txt`), background sementara |
| 14 Okt 2026 | 5 | Tombol burger di masa kini (Lanjutkan, Simpan, Muat, Riwayat, Pengaturan, Menu Utama). Simpan/Muat 1 slot otomatis + 6 manual. CONTINUE & LOAD DATA berfungsi. Halaman Pengaturan (tema, ukuran & gaya huruf, kepekatan kotak, kecepatan teks, volume musik/efek, getar, lewati intro, hapus data). Perintah naskah `@musik`, `@suara` |
| 14 Okt 2026 | — | Dokumen rancangan game & catatan progres ini dibuat |
| 14 Okt 2026 | 12 | **Rintangan & cerita:** kejadian acak dari data (sakit, kehujanan, panas, dompet hilang, kena tipu, dipalak, banjir, nemu uang, keluarga/teman minta tolong) dengan pilihan & akibat. Ujian sekolah berkala, naik/tinggal kelas, lulus SMA. Sahabat masa kecil (Arka/Alya, menyesuaikan pemain), gelang benang merah. **Bab 3** jalur kota (album Nadia, petunjuk #2–#3, Stasiun Besar) & desa (Ibu sakit, hutang ke Juragan Darto). Pemeriksa naskah otomatis di GitHub Actions |
| 14 Okt 2026 | 11 | **HP lengkap:** layar utama dengan ikon aplikasi. **Chat** (karakter mengirim pesan, balas/abaikan, ajak ketemu → janji temu, ingkar/menepati janji), **Kontak** (hubungan & kesukaan yang diketahui), **Kalender** (janji, sewa, kiriman, gajian, batas lamaran), **Lowongan** (dokumen diurus dengan biaya & waktu, kirim lamaran, seleksi berkas, tes tertulis, wawancara dengan opsi bohong, orang dalam), **Dompet** (riwayat uang), **Catatan** (petunjuk cerita) |
| 14 Okt 2026 | 10 | **Sekolah & pekerjaan:** kalender bertanggal, usia bertambah, kos (sewa 30 hari, tunggakan, diusir), kiriman keluarga, sekolah & pekerjaan dari data (jadwal wajib, bolos/absen, surat peringatan, dikeluarkan/dipecat, kinerja, gaji), Cari kerja & lamar (orang dalam), Cari kos. **Bab 2**: jalur kota (stasiun, pilih kos, minggu pertama di Sakala, hari pertama SMA, Dimas & Nadia, petunjuk pertama tentang sahabat) dan jalur desa (kerja di Juragan Darto lewat Ayah) |
| 14 Okt 2026 | 9 | **Sistem hubungan:** data karakter (keluarga + Bayu & Sekar), hubungan antar karakter, topik obrolan. Meter Cinta (teman/pasangan) & Kepercayaan, tingkat hubungan, kabar menyebar, karakter bisa dilarang bertemu oleh sahabatnya, perilaku buruk & kebohongan tercatat, pengakuan (hanya 18+). Gambar karakter (sementara) + panel nama & meter. Waktu bebas: Temui seseorang → ngobrol/hadiah |
| 14 Okt 2026 | 8 | **Sistem kehidupan:** kalender Pagi/Siang/Sore/Malam, bar Kesehatan/Kenyang/Hidrasi/Kewarasan/Energi, uang, tas dengan kedaluwarsa, waktu bebas (aktivitas), Status & Tas, Belanja, game over saat meninggal. Data `aktivitas.txt` & `barang.txt`. Bab 1: siang setelah lulus memakai sistem baru |
| 14 Okt 2026 | 7 | Prolog ditulis ulang (Bab 1: masa kecil di Tanjung Lentera, sahabat pergi, lulus SMP, sarapan bersama keluarga, pesan misterius). Pilih jenis kelamin di layar isi nama. Pilihan jawaban (`@pilihan`), label & lompat, variabel (`@atur`, `@tambah`), syarat (`@jika`), teks sesuai jenis kelamin. Pilihan besar pertama: SMA / kerja / keduanya |
| 14 Okt 2026 | — | Jawaban gambaran besar pemilik dimasukkan ke rancangan; usulan dunia (Desa Tanjung Lentera → Kota Sakala), cerita utama ("hidup paralel" lewat mimpi), dan fase hidup usia 15–25 |

---

## 🔨 Rencana kerja (urutan)

### Tahap 0 — Menyesuaikan awal cerita
- [x] Tulis ulang prolog: masa kecil di Tanjung Lentera, sahabat pergi naik kereta, kini lulus SMP
- [x] Usia tokoh utama tersimpan dan bertambah setiap 365 hari
- [x] Pilihan besar pertama: SMA di kota / kerja / SMA sambil kerja (isi tiap jalur menyusul)

### Tahap A — Fondasi hubungan
- [x] Pilih jenis kelamin di layar isi nama (aturan pasangan menyusul bersama sistem hubungan)
- [x] File data karakter (`assets/data/karakter.txt`) + hubungan antar karakter + topik obrolan
- [x] Perintah `@pilihan` (pilihan jawaban), label, `@lompat`, `@atur`, `@tambah`, `@jika`
- [x] Meter **Cinta (teman)**, **Cinta (pasangan)**, **Kepercayaan** + tahapan hubungan
- [x] Romansa terkunci sampai tokoh & karakter berusia 18; pengakuan bisa ditolak
- [~] Sifat karakter (setia/terbuka) sudah ada di data; deteksi "ketahuan punya pasangan lain" menyusul (Tahap B/E)
- [x] Tampilan karakter di layar (gambar + ekspresi) dan panel nama + meter di pojok kiri atas
- [x] Pencatatan perilaku: bohong, menghindar, ingkar janji, abaikan chat
- [x] Data simpanan ikut menyimpan semua hal di atas

### Tahap B — Waktu & waktu bebas
- [x] Kalender + slot Pagi/Siang/Sore/Malam (tanggal, gajian, tahun ajaran menyusul)
- [x] Layar pilih aktivitas waktu bebas (ajak ketemu karakter menyusul bersama sistem hubungan)
- [x] Jadwal karakter (tempat & waktu bisa ditemui)
- [x] Efek jaringan: kabar menyebar & karakter ikut campur (melarang bertemu). Menjelekkan/memaksa memilih lewat adegan cerita

### Tahap C — Bertahan hidup, ekonomi & barang
- [x] Bar Kesehatan, Kenyang (lapar), Hidrasi (haus), Kewarasan, Energi + game over saat meninggal
- [x] Makanan & minuman dengan kedaluwarsa
- [ ] Status: Uang, Kemampuan, Reputasi
- [x] Tas/inventori & toko, beri hadiah (terima hadiah dari karakter lewat `@barang` di naskah)
- [x] Kebutuhan hidup: kos + tagihan + diusir, makan dari belanja (transport menyusul)

### Tahap D — Sekolah & pekerjaan
- [~] Sekolah (SMA): jadwal wajib, bolos, nilai, teman sekelas (ujian & kenaikan kelas menyusul)
- [ ] Jalur karier yang terbentuk dari pilihan (contoh: dunia kopi)
- [x] Pekerjaan paruh waktu & harian (per kerja)
- [x] Lowongan → berkas → tes → wawancara → pengumuman (HP → Lowongan)
- [x] Jalur orang dalam (rekomendasi menaikkan peluang); risiko merusak hubungan bila kinerja buruk menyusul
- [~] Pekerjaan tetap: gaji per 30 hari, kinerja, absen, surat peringatan, dipecat (naik jabatan menyusul)

### Tahap E — HP lengkap
- [x] Chat, Kontak, Kalender, Lowongan, Dompet, Catatan (Peta & Belanja online menyusul; belanja lewat waktu bebas)

### Tahap F — Rintangan, cerita, ending
- [x] Kejadian rintangan (acak dari data & lewat naskah)
- [~] Alur keluarga, alur tiap karakter, cerita utama (Bab 1–3 selesai; Bab 4 dst. menyusul)
- [x] Ujian & kenaikan kelas, lulus SMA
- [ ] Ending

### Uji coba pertama ("potongan kecil yang lengkap")
1 minggu dalam game · ±4 karakter (2 bersahabat, untuk menguji efek ikut campur) · keluarga ·
1 pekerjaan paruh waktu · 1 proses lamaran dengan wawancara. Setelah semua sistem jalan bersama, baru diperbanyak.

---

## 🗂️ Keputusan yang sudah diambil

| Tanggal | Keputusan |
|---|---|
| 14 Okt 2026 | Aplikasi Android native (Kotlin), bukan web. Compile lewat GitHub Actions (pemilik hanya pakai HP) |
| 14 Okt 2026 | Portrait. Alur: splash 3 detik → video → menu utama |
| 14 Okt 2026 | Game visual novel + simulasi kehidupan nyata |
| 14 Okt 2026 | Nama pemain diisi setelah START GAME, sebelum prolog |
| 14 Okt 2026 | Cerita & data ditulis di file teks agar bisa diedit dari HP |
| 14 Okt 2026 | Tokoh utama punya jenis kelamin; pasangan hanya lawan jenis |
| 14 Okt 2026 | Dua meter hubungan: Kedekatan & Kepercayaan. Bohong/menghindar/ingkar janji tercatat |
| 14 Okt 2026 | Karakter saling terhubung dan bisa ikut campur (mis. melarang bertemu) |
| 14 Okt 2026 | Pekerjaan tetap & panggilan; jalur resmi (berkas, tes, wawancara) atau orang dalam |
| 14 Okt 2026 | Ada rintangan hidup (semua jenis, bertahap) |
| 14 Okt 2026 | Kota & desa fiktif. Tokoh mulai sebagai lulusan SMP; pilih SMA, kerja, atau keduanya |
| 14 Okt 2026 | Keluarga tokoh utama + karakter lain juga punya keluarga |
| 14 Okt 2026 | Boleh lebih dari satu pasangan; akibat bergantung sifat karakter (cemburu vs terbuka) |
| 14 Okt 2026 | Hubungan bertahap sampai menikah; pengakuan bisa ditolak bila cinta kurang |
| 14 Okt 2026 | Romansa hanya antar orang dewasa: terbuka saat tokoh berusia 18 (ditetapkan Claude) |
| 14 Okt 2026 | Karier terbentuk dari pilihan pemain |
| 14 Okt 2026 | Bar Kesehatan, Lapar, Haus, Kewarasan; makanan bisa kedaluwarsa; game over saat meninggal |
| 14 Okt 2026 | Disetujui: Desa Tanjung Lentera → Kota Sakala; cerita "hidup paralel" lewat mimpi & sahabat masa kecil; usia 15–25 dalam 4 bab + bermain bebas; keluarga: Ayah (nelayan), Ibu (warung), adik Laras |
| 14 Okt 2026 | Ending = gabungan sahabat masa kecil × hubungan cinta × kondisi hidup, ditutup mimpi terakhir (menyesal/berdamai). Sahabat tidak dijamin ditemukan |
| 14 Okt 2026 | Gambar karakter & background **dipisah** (karakter PNG transparan, background tanpa orang); adegan spesial (CG) boleh gabungan |
| 14 Okt 2026 | Bahasa Indonesia dulu. Gambar dibuat pemilik lewat AI, Claude bantu prompt. Dimainkan sendiri dulu |

---

## ❓ Menunggu
- (kosong) — usulan dunia, cerita utama, durasi, dan keluarga **disetujui pemilik 14 Okt 2026**

---

## 🔜 Langkah berikutnya (urutan)
1. **Perkenalan Sekar** lewat cerita (Bab 4), baru setelah itu ia bisa diajak janjian.
2. **Bab 4**: bertemu langsung dengan sahabat masa kecil, rahasia keluarganya; jalur desa menuju Sakala (atau sahabat yang pulang).
3. **Loncatan waktu antar bab** (ringkasan bulan-bulan rutin) agar cerita bisa sampai usia 18+.
4. Hutang & cicilan (potong upah), naik jabatan & risiko orang dalam, kebohongan wawancara yang terbongkar, transportasi.
5. Deteksi "ketahuan punya pasangan lain" (untuk masa dewasa), sistem ending & mimpi terakhir.
6. ~~Prompt gambar AI~~ ✅ dibuat: [PROMPT_GAMBAR.md](PROMPT_GAMBAR.md). Pemilik sedang membuat gambar asli.
   - Foto **tokoh utama** (`pemain_l` / `pemain_p`): tampil di halaman isi nama, Status, dan Kontak → Saya (tersembunyi bila gambar belum ada).
   - **Perbaikan tampilan karakter** (masukan pemilik setelah mencoba build 19):
     - Sisa putih di rambut: penghapus latar baru `tools/hapus_latar.py` (alpha halus di tepi, buang campuran putih,
       deteksi celah putih di sela rambut yang dikelilingi warna gelap; kilau baju/handuk/jilbab tetap utuh). Semua 13 gambar diproses ulang.
     - Karakter bertumpuk: penataan otomatis — 1 orang di tengah, 2 orang kiri–kanan, 3+ dibagi rata (urutan mengikuti
       kiri/tengah/kanan di naskah), makin banyak makin kecil. Yang **sedang bicara** di depan & terang, yang lain sedikit redup.
       Karakter yang disembunyikan lalu langsung dimunculkan lagi tidak lagi hilang/tersangkut.
   - Gambar asli: **ruang_makan** (latar sarapan keluarga di prolog), menggantikan gambar sementara.
   - Gambar asli gelombang 3: **pemain_l, pemain_p** (foto tokoh utama) + background **toko_buku, taman_kota** (untuk Bab 4). Semua gambar karakter & background dasar kini sudah asli.
   - Gambar asli terpasang (gelombang 2): **darto, bayu, sekar, dimas, nadia, sahabat_l (Arka), sahabat_p (Alya), sosok** + 12 background: kamar_pagi, plafon, rumah_warung, gudang_ikan, kota_kecil, sekolah, stasiun_hujan, stasiun_desa, kos_kamar, kelas_sma, stasiun_besar (dipakai di Bab 3), dermaga_malam (untuk Bab 4).
   - Gambar asli terpasang: **ayah** (versi ke-2), **ibu**, **laras** (latar putih dihapus otomatis + foto profil `_foto.jpg` 512×512).

## 🐞 Catatan teknis
- Bar "Lapar" & "Haus" ditampilkan sebagai **Kenyang** & **Hidrasi** (bar penuh = baik), agar semua bar searah.
- Angka penurunan bar per slot masih perkiraan awal; perlu diuji dan disesuaikan (lihat DATA.md).
- Kejadian yang menunggu tampil (belum muncul) tidak ikut tersimpan bila game ditutup.
- `hutang_darto` baru tercatat sebagai variabel; cicilan/potong upah belum ada.
- Tampilan belum pernah dicek langsung di HP oleh Claude (tidak bisa menjalankan Android di lingkungan kerjanya). Pemilik mengecek & mengirim screenshot bila ada yang tidak pas.
- Data simpanan mengingat nomor baris naskah; menambah baris sebelum posisi simpanan bisa menggeser posisi muat.
- Volume musik/efek baru berpengaruh ke video intro sampai ada file audio.
