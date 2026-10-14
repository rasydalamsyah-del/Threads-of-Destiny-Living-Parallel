# Rancangan Game — Threads of Destiny: Living Parallel

> Dokumen induk semua aturan & sistem game. Setiap keputusan baru dicatat di sini.
> Status: ✅ sudah dibuat · 🔨 sedang dikerjakan · 📝 direncanakan · ❓ perlu keputusan
>
> Catatan progres harian & daftar tugas ada di **[PROGRES.md](PROGRES.md)**.
> Cara menulis naskah ada di **[NASKAH.md](NASKAH.md)**.

---

## 1. Visi

Visual novel **+ simulasi kehidupan nyata**, portrait, Android.
Pemain menjalani hidup sebagai tokoh utama di kota besar: keluarga, pertemanan, cinta, pekerjaan,
uang, dan rintangan hidup. Setiap pilihan punya akibat, dan **orang-orang di dunia game saling terhubung**:
perlakuan pemain ke satu orang bisa berdampak ke orang lain.

**Pilar desain**
1. **Hidup terasa nyata.** Ada kebutuhan, waktu terbatas, proses yang realistis (lamaran kerja, berkas, wawancara).
2. **Pilihan berakibat.** Bohong, menghindar, ingkar janji tercatat dan bisa kembali menghantui.
3. **Dunia saling terhubung.** Karakter punya hubungan satu sama lain dan bisa ikut campur.
4. **Waktu adalah sumber daya.** Memilih satu hal berarti melewatkan hal lain.
5. **Bisa dikembangkan dari HP.** Cerita, karakter, barang, pekerjaan ditulis sebagai file teks/data, bukan kode.

---

## 2. Alur pembuka ✅

1. Splash (gambar, 3 detik) → video intro → menu utama. ✅
2. START GAME → **isi nama** ✅ → **pilih jenis kelamin** 📝
3. **Prolog masa lalu** (narator, efek kenangan, background berganti per adegan). ✅
4. Masa kini: layar gelap → teks waktu → **efek bangun tidur** (kedip, buram) → plafon → duduk di ranjang. ✅
5. HP bergetar: pesan dari "Nomor Tidak Dikenal". ✅ (cerita lanjutan ❓)

---

## 3. Tokoh utama

| Aspek | Rancangan | Status |
|---|---|---|
| Nama | Diisi pemain, dipakai lewat `{nama}` | ✅ |
| Jenis kelamin | Laki-laki / Perempuan, dipilih di awal | 📝 |
| Aturan pasangan | Laki-laki → pasangan hanya perempuan. Perempuan → pasangan hanya laki-laki | 📝 (keputusan final) |
| Teks menyesuaikan | Panggilan & kalimat bisa beda per jenis kelamin (mis. "Mas"/"Mbak") | 📝 |
| Usia, status awal, tempat tinggal | ❓ | ❓ |

### Status pemain 📝
| Status | Fungsi |
|---|---|
| **Uang** | Bayar kebutuhan, beli barang/hadiah |
| **Energi** | Habis karena kerja/aktivitas, pulih dengan tidur/istirahat. Energi rendah → kerja buruk, pilihan terbatas |
| **Suasana hati (Mood)** | Dipengaruhi kejadian & hubungan. Mood buruk → wawancara/obrolan lebih sulit |
| **Kesehatan** | Bisa turun (kurang tidur, makan tidak teratur) → sakit (rintangan) |
| **Kemampuan** | Komunikasi, Keahlian kerja (per bidang), Pengetahuan umum… naik lewat aktivitas; dipakai di tes & wawancara |

---

## 4. Sistem hubungan 📝

### 4.1 Dua meter per karakter
| Meter | Arti | Naik karena | Turun karena |
|---|---|---|---|
| **Kedekatan** (0–100) | Seberapa suka/akrab | Ngobrol, jawaban yang cocok, hadiah disukai, menepati janji, membantu | Jawaban tidak disukai, hadiah dibenci, diabaikan lama |
| **Kepercayaan** (0–100) | Seberapa yakin pada pemain | Jujur, menepati janji, konsisten | **Bohong (kalau ketahuan)**, menghindar, ingkar janji, mengabaikan chat, membocorkan rahasia |

### 4.2 Tingkat hubungan
`Asing → Kenalan → Teman → Teman Dekat → Sahabat / Spesial → Pasangan`
- **Pasangan** hanya untuk karakter yang boleh dipacari (sesuai aturan jenis kelamin), dan hanya lewat **momen cerita khusus** (mis. pengakuan), butuh Kedekatan **dan** Kepercayaan tinggi.
- Karakter sesama jenis bisa sampai **Sahabat** dengan alur persahabatannya sendiri.
- **Keluarga** punya meter sendiri, alurnya tidak ke arah romansa.

### 4.3 Perilaku buruk (tercatat) 📝
| Perilaku | Akibat |
|---|---|
| **Bohong** | Dicatat sebagai "rahasia". Bisa tidak ketahuan, tapi bila terbongkar → Kepercayaan anjlok, bisa menyebar |
| **Menghindar** (menolak ketemu, kabur dari topik) | Kepercayaan & Kedekatan turun sedikit; berulang → karakter berhenti mengajak |
| **Ingkar janji** (tidak datang ke janji temu) | Kepercayaan turun besar |
| **Mengabaikan chat** | Turun pelan; karakter bisa kirim chat "kamu kenapa?" |
| **Jawaban yang menyakiti** | Kedekatan turun; bisa memicu konflik |

### 4.4 Jaringan antar karakter 📝
- Setiap karakter punya hubungan dengan karakter lain: **sahabat, keluarga, saingan, mantan, rekan kerja, atasan**.
- **Kabar menyebar:** perbuatan ke A ikut memengaruhi penilaian orang-orang dekat A (sebagian, mis. 30–50%).
- **Ikut campur:** bila A tidak suka pemain dan A dekat dengan B:
  - A melarang B bertemu pemain → ajakan ke B ditolak dengan alasan.
  - A menjelekkan pemain → Kepercayaan B turun.
  - A memaksa B memilih (adegan konflik).
- **Memperbaiki:** minta maaf (adegan), hadiah, membuktikan lewat tindakan, atau minta karakter lain jadi penengah. Butuh waktu, tidak instan.
- **Saingan cinta:** karakter lain bisa juga menyukai karakter yang sama.

---

## 5. Waktu 📝
- Hari dibagi: **Pagi · Siang · Sore · Malam** (+ tidur).
- Kalender: hari dalam seminggu, akhir pekan, hari libur, tanggal gajian, tanggal bayar kos.
- **Adegan cerita utama** mengisi slot tertentu; sisanya **waktu bebas**.
- Waktu **maju saat pemain memilih aktivitas** (bukan berjalan sendiri). ❓ (perlu konfirmasi)
- Durasi total game: ❓

### Waktu bebas
Pilihan aktivitas per slot: ajak ketemu karakter · datangi tempat · kerja/shift · belajar/latihan kemampuan · istirahat · bersama keluarga · belanja.
Karakter punya **jadwal sendiri** (kerja, kuliah, kebiasaan) → tidak selalu bisa ditemui.

---

## 6. Obrolan & pilihan 📝
- Pilihan jawaban 2–3 opsi (seperti gambar Hana).
- Setiap karakter punya **sifat & topik favorit/tidak disukai** → jawaban dinilai sesuai sifatnya.
- Ada opsi **bohong** dan **menghindar** yang terlihat menguntungkan sesaat, tapi tercatat.
- Tampilan saat bicara dengan karakter: foto + nama + meter (pojok kiri atas, seperti gambar Hana).

---

## 7. Barang, hadiah, uang 📝
- **Tas (inventori)**.
- Barang didapat dari: **toko** (beli), **diberi karakter**, hadiah acara, hasil kerja.
- Setiap karakter: barang **disukai / biasa / tidak disukai** (+ barang favorit spesial).
- Batas **1 hadiah per karakter per hari**.
- **Kebutuhan hidup:** kos (bulanan), makan, transportasi, pulsa/kuota. Telat bayar → rintangan.

---

## 8. Pekerjaan 📝
| Jenis | Ciri |
|---|---|
| **Tetap** | Jadwal tetap, gaji bulanan, atasan & rekan kerja (karakter), kinerja dinilai, bisa naik jabatan / ditegur / dipecat |
| **Panggilan / paruh waktu** | Pilih shift sendiri, bayar per shift, tidak stabil |

### Jalur resmi
`Lowongan (aplikasi di HP) → Siapkan berkas → Kirim lamaran → Tes → Wawancara → Pengumuman → Kontrak`
- **Berkas:** CV, surat lamaran, KTP, ijazah/transkrip, SKCK, surat sehat, pas foto (sesuai pekerjaan). Mengurus berkas butuh waktu/biaya (mis. SKCK ke kantor polisi).
- **Tes:** soal sesuai bidang + psikotes sederhana; dipengaruhi Kemampuan.
- **Wawancara:** simulasi dialog; menilai jawaban, kejujuran, mood & persiapan. Bisa ditanya hal yang pernah pemain bohongi.

### Jalur orang dalam
- Bila dekat & **dipercaya** oleh karakter yang bekerja/berkuasa di sana → **rekomendasi** (lewati sebagian tahap / peluang lebih besar).
- Risiko: kinerja buruk / berhenti mendadak → **hubungan dengan pemberi rekomendasi rusak**, reputasinya di kantor ikut turun.

---

## 9. Rintangan 📝 (daftar awal, akan ditambah)
- **Ekonomi:** uang habis, telat bayar kos, kehilangan dompet, tagihan mendadak, kena tipu.
- **Pekerjaan:** gagal tes/wawancara, ditegur atasan, kena PHK, rekan kerja menjatuhkan.
- **Kesehatan:** sakit karena kurang tidur/makan, kelelahan.
- **Hubungan:** saingan cinta, gosip, ikut campur karakter lain, kebohongan terbongkar, salah paham.
- **Keluarga:** masalah orang tua, adik/kakak butuh bantuan, tekanan keluarga.
- **Acak/kejadian kota:** hujan/banjir, macet, HP rusak.
- Apakah ada **game over** (mis. terusir/bangkrut)? ❓

---

## 10. HP (pusat permainan)
| Aplikasi | Fungsi | Status |
|---|---|---|
| Layar kunci + notifikasi | Pesan masuk | ✅ |
| **Chat** | Pesan dari karakter, membalas / mengabaikan / mengajak ketemu | 📝 |
| **Kontak** | Foto, tingkat hubungan, meter (setelah kenal) | 📝 |
| **Kalender** | Janji temu, shift, tanggal penting | 📝 |
| **Peta** | Pilih tempat di waktu bebas | 📝 |
| **Belanja** | Beli barang/hadiah | 📝 |
| **Lowongan** | Cari kerja, status lamaran | 📝 |
| **Dompet** | Uang, tagihan | 📝 |

---

## 11. Ending 📝
Ditentukan oleh: pasangan (atau tidak), hubungan keluarga, karier, keuangan, dan keputusan cerita utama. Jenis ending ❓

---

## 12. Data (semua berbasis file teks, bisa diedit dari HP) 📝
| File | Isi |
|---|---|
| `assets/naskah/*.txt` | Naskah cerita ✅ |
| `assets/data/karakter.txt` | Nama, jenis kelamin, bisa dipacari, sifat, topik favorit, barang disukai, jadwal |
| `assets/data/hubungan.txt` | Hubungan antar karakter (sahabat, saingan, …) dan kekuatannya |
| `assets/data/barang.txt` | Nama, harga, kategori, deskripsi |
| `assets/data/pekerjaan.txt` | Jenis, gaji, jadwal, syarat berkas, tes, kontak orang dalam |
| `assets/data/tempat.txt` | Tempat di peta, jam buka, siapa yang sering di sana |
| `assets/latar/`, `assets/karakter/` | Gambar background, gambar karakter (PNG transparan, beberapa ekspresi) |

### Perintah naskah baru yang direncanakan
`@pilihan` (pilihan jawaban) · `@kedekatan hana +5` · `@percaya hana -10` · `@jika …` (syarat) ·
`@bohong id` / `@terbongkar id` · `@uang +50000` · `@beri barang` · `@terima barang` · `@tampil hana senyum` · `@waktu_maju`

---

## 13. Aset yang dibutuhkan
- Background: kamar, rumah keluarga, kampus/kantor, kafe, jalan, stasiun, dll. (sementara: placeholder ✅)
- Karakter: PNG transparan, minimal 3 ekspresi (biasa, senang, sedih/marah) + foto profil.
- Musik & efek suara (folder sudah siap ✅).

---

## 14. Pertanyaan terbuka (jawaban gambaran besar dari pemilik game)
Jawaban dicatat di bagian **Keputusan** di PROGRES.md.

**Dunia & cerita**
1. Latar kota: kota nyata atau fiktif di Indonesia? Era sekarang?
2. Tokoh utama di awal: usia, status (kuliah/baru lulus/kerja), tinggal di mana (kos sendiri / rumah keluarga)?
3. Cerita utama: siapa sosok dari masa lalu / pengirim pesan misterius? "Living Parallel" itu kiasan, atau ada unsur dunia paralel sungguhan?
4. Durasi game: berapa lama waktu di dalam game (mis. 1 tahun)? Ada akhir, atau bisa terus main setelah ending?

**Karakter & hubungan**
5. Keluarga: siapa saja, dan tinggal bersama atau di kampung halaman?
6. Berapa karakter yang bisa dipacari & berapa teman di awal? Sudah ada nama/sifat?
7. Hubungan romantis sampai mana: pacaran, tunangan, menikah?
8. Boleh punya lebih dari satu hubungan sekaligus (dengan konsekuensi kalau ketahuan), atau dibatasi satu?

**Kehidupan & rintangan**
9. Pekerjaan bidang apa saja yang diinginkan?
10. Seberapa keras hidupnya: bisa bangkrut / terusir / game over, atau selalu ada jalan keluar?
11. Rintangan apa yang paling penting menurut Anda?

**Teknis & visual**
12. Bahasa: Indonesia saja, atau bisa pilih Inggris juga?
13. Gambar karakter & background dibuat sendiri (mis. pakai AI) dengan gaya anime seperti gambar Hana?
14. Target: untuk dimainkan sendiri/teman, atau rilis di Play Store?
