# Catatan Progres — Threads of Destiny: Living Parallel

> Catatan semua yang sudah dikerjakan, yang sedang/akan dikerjakan, dan keputusan yang sudah diambil.
> Aturan & sistem lengkap: **[RANCANGAN_GAME.md](RANCANGAN_GAME.md)**.
> Diperbarui setiap ada perubahan.

---

## ✅ Sudah dikerjakan

| Tanggal | Build | Yang dikerjakan |
|---|---|---|
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
1. **Bab 4**: bertemu langsung dengan sahabat masa kecil, rahasia keluarganya; jalur desa menuju Sakala (atau sahabat yang pulang).
2. **Loncatan waktu antar bab** (ringkasan bulan-bulan rutin) agar cerita bisa sampai usia 18+.
3. Hutang & cicilan (potong upah), naik jabatan & risiko orang dalam, kebohongan wawancara yang terbongkar, transportasi.
4. Deteksi "ketahuan punya pasangan lain" (untuk masa dewasa), sistem ending & mimpi terakhir.
5. ~~Prompt gambar AI~~ ✅ dibuat: [PROMPT_GAMBAR.md](PROMPT_GAMBAR.md). Pemilik sedang membuat gambar asli.
   - Foto **tokoh utama** (`pemain_l` / `pemain_p`): tampil di halaman isi nama, Status, dan Kontak → Saya (tersembunyi bila gambar belum ada).
   - **Perbaikan tampilan karakter** (masukan pemilik setelah mencoba build 19):
     - Sisa putih di rambut: penghapus latar baru `tools/hapus_latar.py` (alpha halus di tepi, buang campuran putih,
       deteksi celah putih di sela rambut yang dikelilingi warna gelap; kilau baju/handuk/jilbab tetap utuh). Semua 13 gambar diproses ulang.
     - Karakter bertumpuk: penataan otomatis — 1 orang di tengah, 2 orang kiri–kanan, 3+ dibagi rata (urutan mengikuti
       kiri/tengah/kanan di naskah), makin banyak makin kecil. Yang **sedang bicara** di depan & terang, yang lain sedikit redup.
       Karakter yang disembunyikan lalu langsung dimunculkan lagi tidak lagi hilang/tersangkut.
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
