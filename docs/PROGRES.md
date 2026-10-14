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

---

## 🔨 Rencana kerja (urutan)

### Tahap A — Fondasi hubungan
- [ ] Pilih jenis kelamin di layar isi nama (aturan pasangan lawan jenis)
- [ ] File data karakter (`assets/data/karakter.txt`) + hubungan antar karakter
- [ ] Perintah `@pilihan` (pilihan jawaban) dan `@jika` (syarat)
- [ ] Meter **Kedekatan** & **Kepercayaan** + tingkat hubungan
- [ ] Tampilan karakter di layar (gambar + ekspresi) dan panel nama + meter di pojok kiri atas
- [ ] Pencatatan perilaku: bohong, menghindar, ingkar janji, abaikan chat
- [ ] Data simpanan ikut menyimpan semua hal di atas

### Tahap B — Waktu & waktu bebas
- [ ] Kalender + slot Pagi/Siang/Sore/Malam
- [ ] Layar pilih aktivitas waktu bebas (ajak ketemu, tempat, kerja, istirahat, keluarga)
- [ ] Jadwal karakter (kapan bisa ditemui)
- [ ] Efek jaringan: kabar menyebar & karakter ikut campur (melarang, menjelekkan)

### Tahap C — Ekonomi & barang
- [ ] Status pemain: Uang, Energi, Mood, Kesehatan, Kemampuan
- [ ] Tas/inventori, toko, beri & terima hadiah
- [ ] Kebutuhan hidup (kos, makan, transport) + tagihan

### Tahap D — Pekerjaan
- [ ] Pekerjaan paruh waktu (shift)
- [ ] Lowongan → berkas → tes → wawancara → pengumuman
- [ ] Jalur orang dalam (rekomendasi + risikonya)
- [ ] Pekerjaan tetap: gaji bulanan, kinerja, teguran, naik jabatan

### Tahap E — HP lengkap
- [ ] Chat, Kontak, Kalender, Peta, Belanja, Lowongan, Dompet

### Tahap F — Rintangan, cerita, ending
- [ ] Kejadian rintangan (acak & terjadwal)
- [ ] Alur keluarga, alur tiap karakter, cerita utama
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
| 14 Okt 2026 | Ada rintangan hidup |

---

## ❓ Menunggu jawaban
Lihat **Pertanyaan terbuka** di [RANCANGAN_GAME.md](RANCANGAN_GAME.md#14-pertanyaan-terbuka-jawaban-gambaran-besar-dari-pemilik-game).

---

## 🐞 Catatan teknis
- Tampilan belum pernah dicek langsung di HP oleh Claude (tidak bisa menjalankan Android di lingkungan kerjanya). Pemilik mengecek & mengirim screenshot bila ada yang tidak pas.
- Data simpanan mengingat nomor baris naskah; menambah baris sebelum posisi simpanan bisa menggeser posisi muat.
- Volume musik/efek baru berpengaruh ke video intro sampai ada file audio.
