# Rancangan Game — Threads of Destiny: Living Parallel

> Dokumen induk semua aturan & sistem game. Setiap keputusan baru dicatat di sini.
> Status: ✅ sudah dibuat · 🔨 sedang dikerjakan · 📝 direncanakan · 💡 usulan Claude (menunggu persetujuan) · ❓ perlu keputusan
>
> Catatan progres & daftar tugas: **[PROGRES.md](PROGRES.md)** · Cara menulis naskah: **[NASKAH.md](NASKAH.md)**
>
> Terakhir diperbarui: 14 Okt 2026 (jawaban gambaran besar dari pemilik game sudah dimasukkan)

---

## 1. Visi

Visual novel **+ simulasi kehidupan nyata**, portrait, Android, **bahasa Indonesia**.
Pemain menjalani hidup tokoh utama sejak **lulus SMP**: memilih sekolah atau kerja, merantau,
berteman, berkeluarga, jatuh cinta, berkarier, mengatur uang, makan & minum, dan menghadapi rintangan hidup.
Setiap pilihan punya akibat, dan **orang-orang di dunia game saling terhubung**.

**Pilar desain**
1. **Hidup terasa nyata.** Kebutuhan dasar, waktu terbatas, proses realistis (sekolah, lamaran kerja, berkas, wawancara), semuanya bertahap.
2. **Pilihan berakibat.** Bohong, menghindar, ingkar janji, selingkuh tercatat dan bisa kembali menghantui.
3. **Dunia saling terhubung.** Karakter punya keluarga, sahabat, saingan, dan bisa ikut campur.
4. **Waktu adalah sumber daya.** Memilih satu hal berarti melewatkan hal lain.
5. **Bisa dikembangkan dari HP.** Cerita, karakter, barang, pekerjaan ditulis sebagai file teks/data.

**Target:** dimainkan sendiri oleh pemilik dulu (belum rilis Play Store).

---

## 2. Dunia 💡

Usulan: **dua latar dengan transisi** (desa → kota). Pindah ke kota adalah keputusan besar pertama pemain,
dan desa tetap bisa dikunjungi (pulang kampung).

### Desa Tanjung Lentera — kampung halaman
- Desa nelayan kecil di pesisir, dengan **mercusuar tua** di ujung tanjung dan dermaga kayu (latar prolog).
- Tenang, semua orang saling kenal, gosip cepat menyebar.
- Ekonomi: nelayan, warung, kebun kelapa, pengolahan ikan asin. Uang sedikit, biaya hidup murah.
- Fasilitas terbatas: hanya ada SMP; **SMA terdekat ada di kota**. Sinyal HP kadang hilang.
- Keluarga tokoh utama tinggal di sini.

### Kota Sakala — kota besar (± 4 jam naik kereta)
- Nama dari kata *sakala* (waktu/seketika): kota yang tak pernah berhenti bergerak.
- Metropolitan: gedung tinggi, jalan layang, kereta komuter, kafe di setiap sudut, macet di jam sibuk.
- **Kesenjangan nyata:** kawasan elit bersebelahan dengan kampung padat di tepi rel.
- Biaya hidup mahal: kos, makan, transportasi. Lowongan banyak, persaingan ketat.
- Kawasan (usulan):
  - **Pusat Kota Sakala:** perkantoran, mal, kafe mahal.
  - **Kampung Rel Utara:** kos murah, padat, ramah tapi keras.
  - **Kawasan Pendidikan Cendana:** SMA, kampus, toko buku, warung murah.
  - **Pasar Lama Sakala:** pasar tradisional, kuliner, pekerjaan harian.
  - **Pelabuhan Sakala:** gudang, pekerjaan kasar, kehidupan malam.
  - **Taman Kota & Stasiun Besar:** tempat bertemu, adegan penting.

---

## 3. Cerita utama 💡

### Arti "Living Parallel"
Setiap keputusan besar membelah hidup menjadi dua jalur: yang dipilih, dan yang ditinggalkan.
Tokoh utama sesekali **bermimpi tentang hidup paralelnya**, yaitu dirinya di jalur yang tidak ia pilih
(mis. kalau dulu memilih kerja, ia memimpikan dirinya yang bersekolah). Mimpi ini:
- menjadi penghubung cerita (adegan pendek bergaya kenangan/mimpi),
- memberi petunjuk samar tentang akibat pilihan,
- di akhir cerita menjadi pertanyaan besar: *apakah ia menyesali jalannya, atau berdamai dengannya?*

### "Benang takdir" & sosok dari masa lalu
- Sahabat masa kecil di Tanjung Lentera yang pergi tiba-tiba naik kereta (prolog), tanpa pamit.
- **Pengirim pesan misterius** = sahabat itu, yang kini tinggal di Kota Sakala dan menyimpan alasan kepergiannya.
- Alur besar: menemukan kembali sahabat itu, mengungkap kenapa ia pergi (rahasia keluarganya),
  dan memilih: memperbaiki hubungan, melepaskan, atau (setelah dewasa) menjadikannya pasangan.
- Jenis kelamin sahabat ini menyesuaikan pilihan pemain, agar tetap bisa menjadi calon pasangan setelah dewasa.

> Catatan: prolog yang sekarang (sudah tiba di kota besar, kuliah & kerja paruh waktu) **perlu ditulis ulang**
> agar sesuai dengan awal baru: lulus SMP di Tanjung Lentera.

---

## 4. Fase hidup & durasi 💡

Usulan: cerita utama mencakup **± 10 tahun dalam game (usia 15 → 25)**, dibagi bab.
Masa yang penting dimainkan **hari per hari**; masa yang rutin dilompati (**loncatan waktu**) dengan ringkasan.

| Bab | Usia | Isi |
|---|---|---|
| 1. Lulus SMP | 15 | Desa, keluarga, pilihan besar: SMA di kota / kerja / keduanya |
| 2. Masa SMA atau masa kerja muda | 15–18 | Sekolah, kerja sampingan, pertemanan, mulai merantau. **Tanpa romansa** |
| 3. Dewasa muda | 18–21 | Lulus SMA → kuliah/kerja; **romansa mulai terbuka**; pencarian sahabat masa kecil |
| 4. Mapan | 21–25 | Karier, pacaran → tunangan → menikah, keluarga sendiri, puncak cerita utama |
| Setelah ending | 25+ | **Bermain bebas** (hidup terus berjalan tanpa cerita utama) |

Setiap hari dalam game dimainkan ± 3–8 menit.

---

## 5. Alur pembuka ✅/📝
1. Splash 3 detik → video intro → menu utama. ✅
2. START GAME → isi nama ✅ → **pilih jenis kelamin** 📝
3. Prolog masa lalu ✅ (isi perlu ditulis ulang sesuai Bab 1 💡)
4. Bangun tidur (kedip, buram), duduk di ranjang, pesan HP. ✅

---

## 6. Tokoh utama 📝

| Aspek | Rancangan |
|---|---|
| Nama | Diisi pemain (`{nama}`) ✅ |
| Jenis kelamin | Laki-laki / Perempuan, dipilih di awal |
| Awal cerita | **Baru lulus SMP (15 tahun)** di Desa Tanjung Lentera |
| Pilihan besar pertama | **Lanjut SMA** (di kota, perlu biaya & kos) · **Kerja** · **SMA sambil kerja** |
| Usia | Bertambah seiring waktu dalam game; memengaruhi apa yang bisa dilakukan |

### Kebutuhan dasar (bertahan hidup) 📝
| Bar | Turun karena | Dipulihkan dengan | Bila habis |
|---|---|---|---|
| **Kesehatan** | Lapar/haus parah, kurang tidur, sakit, makanan basi | Makan layak, istirahat, obat, berobat | **Meninggal → game over** |
| **Lapar** | Waktu berjalan, kerja berat | Makan | Kesehatan terus turun |
| **Haus** | Waktu berjalan, cuaca panas, aktivitas | Minum | Kesehatan turun lebih cepat |
| **Kewarasan** | Stres, konflik, kerja berlebihan, kesepian, kegagalan | Tidur cukup, hobi, ngobrol dengan orang dekat, liburan, konseling | Sulit fokus, kerja & obrolan memburuk, menolak ajakan; tidak menyebabkan kematian |
| **Energi** | Aktivitas & kerja | Tidur, istirahat | Tidak bisa beraktivitas berat |

- **Makanan & minuman** dibeli (warung, pasar, minimarket) atau dimasak. Punya **tanggal kedaluwarsa**;
  yang basi/tidak layak bisa membuat sakit.
- Game over = **tokoh meninggal** (mis. kelaparan berhari-hari tanpa uang). Hidup keras tapi bertahap:
  sebelum fatal selalu ada tanda bahaya & kesempatan (minta tolong keluarga/teman, kerja harian, utang).

### Status lain 📝
**Uang**, **Kemampuan** (Komunikasi, Akademik, Keahlian per bidang, dll.), **Reputasi** (di desa, sekolah, tempat kerja).

---

## 7. Keluarga 📝
- **Keluarga tokoh utama** (di desa): susunan anggota ❓ (usulan: ayah nelayan, ibu pemilik warung, satu adik).
- **Karakter lain juga punya keluarga**: orang tua bisa merestui/menolak hubungan, saudara bisa ikut campur,
  keluarga bisa jadi jalur "orang dalam" pekerjaan.

---

## 8. Hubungan 📝

### 8.1 Meter per karakter
| Meter | Arti |
|---|---|
| **Cinta (kategori teman)** 0–100 | Rasa sayang & akrab sebagai teman |
| **Cinta (kategori pasangan)** 0–100 | Rasa sayang romantis, terbuka setelah resmi berpacaran |
| **Kepercayaan** 0–100 | Turun karena bohong (bila ketahuan), menghindar, ingkar janji, selingkuh |

Naik lewat: ngobrol, jawaban yang disukai, hadiah, menepati janji, membantu.
Turun lewat: jawaban yang tidak disukai, bohong, menghindar, mengabaikan chat, hadiah yang dibenci.

### 8.2 Tahapan hubungan romantis (hanya antar orang dewasa)
`Pendekatan → (ungkapkan perasaan) → Pacaran → Tunangan → Menikah (suami-istri)`
- **Romansa baru terbuka saat tokoh utama berusia 18 tahun**, dan hanya dengan karakter dewasa.
  Sebelum itu, hubungan dengan karakter lain berupa pertemanan, persahabatan, dan keluarga.
- Menikah mengikuti batas usia nikah di Indonesia (19 tahun) dan butuh proses: restu keluarga, biaya, persiapan.
- **Mengungkapkan perasaan** bisa **ditolak**. Peluang diterima bergantung pada Cinta & Kepercayaan:
  di bawah ± 50 hampir pasti ditolak; makin penuh, makin besar peluangnya. Ditolak juga berakibat (canggung, Kepercayaan turun sedikit).
- Setiap tahap butuh waktu & momen khusus; tidak bisa dilompati.
- Aturan jenis kelamin: tokoh laki-laki → pasangan perempuan; tokoh perempuan → pasangan laki-laki.

### 8.3 Lebih dari satu pasangan
- Pemain **boleh menjalin lebih dari satu hubungan**. Akibatnya bergantung pada **sifat tiap karakter**:
  - **Setia/cemburuan:** bila tahu ada pasangan lain → cemburu, sakit hati, marah, putus, membalas, menyebarkan ke teman/keluarga.
  - **Terbuka:** tidak keberatan → tidak ada efek buruk.
- **Selingkuh yang ketahuan** dari pasangan bersifat setia → efek & tindakan negatif.
- Ketahuan bisa terjadi lewat: bertemu di tempat umum, chat terlihat, gosip, teman yang melapor.

### 8.4 Jaringan antar karakter
- Karakter punya hubungan: keluarga, sahabat, saingan, mantan, rekan kerja, atasan.
- **Kabar menyebar** ke orang-orang dekat; **ikut campur** (melarang bertemu, menjelekkan, memaksa memilih).
- **Memperbaiki** butuh waktu: minta maaf, hadiah, bukti tindakan, penengah.

---

## 9. Waktu 📝
- Hari: **Pagi · Siang · Sore · Malam** + tidur. Kalender (hari, akhir pekan, libur, gajian, bayar kos, tahun ajaran sekolah).
- Waktu **maju saat pemain memilih aktivitas**.
- Karakter punya jadwal sendiri.
- Loncatan waktu antar bab (lihat §4).

---

## 10. Obrolan & pilihan 📝
- Pilihan jawaban 2–3 opsi, termasuk opsi bohong & menghindar.
- Karakter punya sifat, topik favorit & tidak disukai.
- Panel karakter saat bicara: foto + nama + meter (seperti gambar Hana).

---

## 11. Barang, makanan, uang 📝
- Tas/inventori. Barang dari toko, pemberian karakter, hadiah acara, hasil kerja.
- Hadiah: disukai/biasa/tidak disukai, 1 per karakter per hari.
- **Makanan & minuman** dengan kedaluwarsa (lihat §6).
- Kebutuhan hidup: kos, makan, transport, kuota, biaya sekolah.

---

## 12. Sekolah 📝
- SMA di Kota Sakala (perlu kos & biaya) atau sekolah sambil kerja.
- Jadwal sekolah, PR/ujian (dipengaruhi Akademik), teman sekelas, guru.
- Nilai & kelulusan memengaruhi pilihan setelahnya (kuliah/beasiswa/pekerjaan).

---

## 13. Pekerjaan 📝
- **Karier terbentuk dari pilihan pemain** (mis. sering ke kafe & memilih belajar kopi → jalur dunia kopi: barista → kepala barista → buka kedai sendiri). Tidak ada daftar karier tetap; jalurnya muncul dari minat & pilihan.
- **Tetap** (gaji bulanan, kinerja, naik jabatan/PHK) & **panggilan/paruh waktu** (per shift).
- **Jalur resmi:** lowongan → berkas (CV, surat lamaran, KTP, ijazah, SKCK, surat sehat, pas foto) → tes → wawancara → pengumuman.
- **Jalur orang dalam:** rekomendasi karakter yang dekat & percaya; risiko merusak hubungan bila kinerja buruk.
- Pekerjaan untuk usia di bawah 18: hanya pekerjaan ringan (bantu warung, nelayan keluarga, jaga toko, kurir).

---

## 14. Rintangan 📝
Semua jenis, bertahap: ekonomi (uang habis, telat bayar, kena tipu), pekerjaan (gagal tes, ditegur, PHK),
kesehatan (sakit, makanan basi), hubungan (cemburu, gosip, kebohongan terbongkar, saingan),
keluarga (masalah orang tua, saudara butuh bantuan), sekolah (nilai jatuh, perundungan), kejadian kota (banjir, macet, HP rusak).

---

## 15. HP
| Aplikasi | Status |
|---|---|
| Layar kunci + notifikasi | ✅ |
| Chat · Kontak · Kalender · Peta · Belanja · Lowongan · Dompet · Catatan kesehatan | 📝 |

---

## 16. Ending 📝
Ending cerita utama ditentukan oleh: hubungan dengan sahabat masa kecil, pasangan, keluarga, karier, keuangan, dan sikap terhadap "hidup paralel".
Setelah ending → bermain bebas.

---

## 17. Data (file teks, bisa diedit dari HP) 📝
`assets/data/karakter.txt` · `hubungan.txt` · `barang.txt` · `makanan.txt` · `pekerjaan.txt` · `tempat.txt` · `sekolah.txt`
`assets/karakter/` (gambar karakter) · `assets/latar/` (background) · `assets/naskah/` (cerita)

Perintah naskah baru (rencana): `@pilihan`, `@jika`, `@cinta hana +5`, `@percaya hana -10`, `@bohong`, `@terbongkar`,
`@uang`, `@beri`, `@terima`, `@tampil hana senyum`, `@makan`, `@waktu_maju`, `@usia`.

---

## 18. Aset & gambar
- Gambar dibuat pemilik dengan AI lain; **Claude membantu menulis prompt** (gaya anime konsisten, PNG transparan, beberapa ekspresi).
- Background sementara: placeholder ✅.

---

## 19. Jawaban pemilik (14 Okt 2026)
1. Kota fiktif dengan nama & kondisi → usulan §2 (desa → kota).
2. Mulai lulus SMP; pilih SMA, kerja, atau keduanya.
3. Minta penjelasan konsep cerita → usulan §3.
4. Minta saran durasi → usulan §4.
5. Keluarga tokoh utama + keluarga karakter lain.
6. Boleh banyak pasangan; akibatnya bergantung sifat karakter (cemburu vs terbuka).
7. Bisa sampai menikah, bertahap; pengakuan bisa ditolak bila cinta kurang.
8. Selingkuh ketahuan → efek negatif, kecuali pasangan bersifat terbuka.
9. Karier terbentuk dari pilihan (contoh: dunia kopi).
10. Semua rintangan, bertahap. Game over saat meninggal. Bar Kesehatan, Lapar, Haus, Kewarasan. Makanan bisa kedaluwarsa.
11. Rintangan: semuanya.
12. Bahasa Indonesia dulu.
13. Gambar dibuat sendiri lewat AI; Claude bantu prompt.
14. Dimainkan sendiri dulu.
