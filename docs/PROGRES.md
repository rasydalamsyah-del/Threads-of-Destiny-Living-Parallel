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
| 14 Okt 2026 | 7 | Prolog ditulis ulang (Bab 1: masa kecil di Tanjung Lentera, sahabat pergi, lulus SMP, sarapan bersama keluarga, pesan misterius). Pilih jenis kelamin di layar isi nama. Pilihan jawaban (`@pilihan`), label & lompat, variabel (`@atur`, `@tambah`), syarat (`@jika`), teks sesuai jenis kelamin. Pilihan besar pertama: SMA / kerja / keduanya |
| 14 Okt 2026 | — | Jawaban gambaran besar pemilik dimasukkan ke rancangan; usulan dunia (Desa Tanjung Lentera → Kota Sakala), cerita utama ("hidup paralel" lewat mimpi), dan fase hidup usia 15–25 |

---

## 🔨 Rencana kerja (urutan)

### Tahap 0 — Menyesuaikan awal cerita
- [x] Tulis ulang prolog: masa kecil di Tanjung Lentera, sahabat pergi naik kereta, kini lulus SMP
- [~] Usia tokoh utama tersimpan (variabel `usia`); bertambah otomatis menyusul bersama sistem kalender
- [x] Pilihan besar pertama: SMA di kota / kerja / SMA sambil kerja (isi tiap jalur menyusul)

### Tahap A — Fondasi hubungan
- [x] Pilih jenis kelamin di layar isi nama (aturan pasangan menyusul bersama sistem hubungan)
- [ ] File data karakter (`assets/data/karakter.txt`) + hubungan antar karakter
- [x] Perintah `@pilihan` (pilihan jawaban), label, `@lompat`, `@atur`, `@tambah`, `@jika`
- [ ] Meter **Cinta (teman)**, **Cinta (pasangan)**, **Kepercayaan** + tahapan hubungan
- [ ] Romansa terkunci sampai tokoh berusia 18; pengakuan bisa ditolak
- [ ] Sifat karakter (setia/cemburuan vs terbuka) untuk akibat banyak pasangan
- [ ] Tampilan karakter di layar (gambar + ekspresi) dan panel nama + meter di pojok kiri atas
- [ ] Pencatatan perilaku: bohong, menghindar, ingkar janji, abaikan chat
- [ ] Data simpanan ikut menyimpan semua hal di atas

### Tahap B — Waktu & waktu bebas
- [ ] Kalender + slot Pagi/Siang/Sore/Malam
- [ ] Layar pilih aktivitas waktu bebas (ajak ketemu, tempat, kerja, istirahat, keluarga)
- [ ] Jadwal karakter (kapan bisa ditemui)
- [ ] Efek jaringan: kabar menyebar & karakter ikut campur (melarang, menjelekkan)

### Tahap C — Bertahan hidup, ekonomi & barang
- [ ] Bar Kesehatan, Lapar, Haus, Kewarasan, Energi + game over saat meninggal
- [ ] Makanan & minuman dengan kedaluwarsa
- [ ] Status: Uang, Kemampuan, Reputasi
- [ ] Tas/inventori, toko, beri & terima hadiah
- [ ] Kebutuhan hidup (kos, makan, transport) + tagihan

### Tahap D — Sekolah & pekerjaan
- [ ] Sekolah (SMA): jadwal, ujian, teman sekelas
- [ ] Jalur karier yang terbentuk dari pilihan (contoh: dunia kopi)
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
| 14 Okt 2026 | Bahasa Indonesia dulu. Gambar dibuat pemilik lewat AI, Claude bantu prompt. Dimainkan sendiri dulu |

---

## ❓ Menunggu
- (kosong) — usulan dunia, cerita utama, durasi, dan keluarga **disetujui pemilik 14 Okt 2026**

---

## 🐞 Catatan teknis
- Tampilan belum pernah dicek langsung di HP oleh Claude (tidak bisa menjalankan Android di lingkungan kerjanya). Pemilik mengecek & mengirim screenshot bila ada yang tidak pas.
- Data simpanan mengingat nomor baris naskah; menambah baris sebelum posisi simpanan bisa menggeser posisi muat.
- Volume musik/efek baru berpengaruh ke video intro sampai ada file audio.
