# Panduan File Data

File data menentukan isi dunia game: aktivitas waktu bebas, barang/makanan, dan (nanti) karakter, pekerjaan, tempat.
Semua bisa diedit dari HP lewat GitHub, lalu otomatis masuk ke APK berikutnya.

Lokasi: `app/src/main/assets/data/`

## Format umum

```
# komentar (tidak dibaca)

[id_unik]
kunci = nilai
kunci = nilai
```
- `id_unik`: huruf kecil, angka, garis bawah, tanpa spasi. Dipakai di naskah (mis. `@barang air_mineral`).
- Daftar dipisahkan **koma**: `tempat = desa, kota`.
- Efek ditulis `nama_bar +angka` atau `-angka`, dipisah koma: `efek = energi -20, uang +15000`.

### Bar & efek yang dikenali
| Kunci | Arti |
|---|---|
| `kesehatan` | ❤️ Kesehatan (0 = meninggal) |
| `kenyang` | 🍚 Kenyang (0 = kelaparan, kesehatan turun) |
| `hidrasi` | 💧 Hidrasi (0 = dehidrasi, kesehatan turun cepat) |
| `kewarasan` | 🧠 Kewarasan |
| `energi` | ⚡ Energi |
| `uang` | 💰 Uang (rupiah) |
| lainnya | Disimpan sebagai **variabel cerita**, mis. `akademik +2` → bisa dicek dengan `@jika akademik >= 10 -> ...` |

## barang.txt

```
[nasi_bungkus]
nama   = Nasi bungkus lauk ikan
ikon   = 🍱
harga  = 12000
tahan  = 0
tempat = desa, kota
efek   = kenyang +50, hidrasi +5, kewarasan +3
```
- `tahan`: berapa hari sampai **basi/kedaluwarsa**. `0` = harus habis hari itu juga.
  Makanan basi: 70% kemungkinan sakit perut (kesehatan & kewarasan turun).
- `tempat`: di mana bisa dibeli (`desa` = warung desa, `kota` = minimarket).
- Barang tanpa `kenyang`/`hidrasi` (mis. obat) tampil dengan tombol **Pakai**.

## aktivitas.txt

```
[bantu_warung]
nama   = Bantu Ibu di warung
ikon   = 🍳
tempat = desa
waktu  = pagi, siang, sore
efek   = energi -20, uang +15000, kenyang -5, hidrasi -5, kewarasan -2
syarat = jalur != kerja        (opsional)
teks   = Kamu menggoreng pisang dan melayani pembeli seharian.
```
- `tempat`: `desa`, `kota`, atau `semua`. Tempat pemain diatur di naskah dengan `@lokasi desa`.
- `waktu`: `pagi`, `siang`, `sore`, `malam`, atau `semua`.
- `syarat`: aktivitas hanya muncul bila syarat benar (format seperti `@jika`).
- Bila `energi` yang dibutuhkan lebih besar dari energi pemain → tampil **"Terlalu lelah"** dan tidak bisa dipilih.
- Setiap aktivitas memajukan jam sesuai `durasi`-nya. Waktu bebas berakhir di jam yang ditentukan naskah (`@bebas sampai ...`).

### Jam mulai (`mulai`)
```
mulai  = 04:00-06:00     aktivitas hanya bisa DIMULAI di jam ini (boleh 19:00-02:00, melewati tengah malam)
mulai  = 05:00-06:15, 17:15-17:45     boleh beberapa jendela jam (mis. membantu Ibu memasak pagi & sore)
```
Di luar jam itu tetap tampil redup dengan alasannya. Tanpa `mulai`, yang dipakai `waktu` (bagian hari).

### Kegiatan bersama seseorang (`bersama`) & efek hubungan
```
bersama = ibu                          hanya bisa saat Ibu ada di lokasi yang sama (tandanya di layar baru muncul saat Ibu ada)
efek    = kewarasan +4, cinta_ibu +2   cinta_<id> / percaya_<id> mengubah hubungan dengan karakter itu
```
Contoh: melipat pakaian bersama Ibu (ruang tengah 18:30–21:00), membantu PR Laras, menemani Ayah mendengarkan radio,
membantu Ayah memperbaiki jaring, main bola bersama Bayu, membayar cicilan perahu ke Juragan Darto.

### Batas kegiatan berulang (`batas`, `jeda`, `teks_ulang`)
Supaya kegiatan tidak bisa diulang terus (makan terus, istirahat terus sampai energi penuh):
| Kegiatan | Aturan |
|---|---|
| Makan bersama keluarga | jeda 4 jam (dari jam mulai), maks. 3×/hari; makan lagi → Ibu menggoda "Lapar lagi?" |
| Istirahat sebentar (rumah / kos) | maks. 2×/hari, jeda 3 jam; di rumah, kedua kalinya Ibu menegur |
| Belajar | maks. 2×/hari, jeda 2 jam |
| Taman kota | maks. 2×/hari, jeda 3 jam |
| Bantu warung, melaut, dermaga, main dengan Laras, jelajah kota, cari jejak, perpustakaan, lari pagi, mal, kafe, warnet | 1×/hari |
Yang terkena batas tampil redup dengan alasan ("Sudah dilakukan hari ini — besok lagi", "Baru saja dilakukan — bisa lagi pukul 11:00").
Saat waktu bebas berakhir di jam tertentu, kegiatan yang selesainya melewati jam itu juga redup ("Waktunya tidak cukup — cerita berlanjut pukul 20:00").

## lokasi.txt (menjelajah saat waktu bebas)
Saat waktu bebas, halaman waktu bebas bisa **ditutup** (✕ / ketuk di luar) untuk menjelajah, dan dibuka lagi lewat tombol **📋** di kanan bawah.
Di layar jelajah: **pintu** (pindah tempat), **orang** (ketuk → mengobrol), **benda** (ketuk → kegiatan), dan **😴 Tidur** di kasur saat malam.
```
[ruang_makan]                       (id = nama file latar)
nama   = Ruang makan
tempat = desa
pintu  = kamar_pagi | 🚪 Ke kamarmu | 0.84, 0.36 | 0 ; rumah_warung | ⬅ Keluar ke depan rumah | 0.16, 0.90 | 0
benda  = makan_keluarga | 🍲 Makan | 0.52, 0.56
orang  = ayah 0.30, 0.66, 0.40 ; laras 0.62, 0.58, 0.30      (x, y kaki, tinggi orang itu di gambar latar)
tidur  = ya                                                    (hanya kamar / kos)
cadangan = kota_kecil                                          (gambar pengganti selama latar/<id>.jpg belum ada)
```
- x, y = posisi di gambar (0–1). Layar HP memotong ±10% kiri-kanan → taruh tanda di x 0.12–0.88.
- Pintu: menit perjalanan (di dalam rumah 0, depan rumah ↔ lapangan / dermaga 5, ke gudang ikan 15, kos ↔ pusat kota 10, pusat kota ↔ tempat lain 15). Tujuan `kos` = kos yang sedang disewa.
- **Peta sekarang** — desa: kamar ↔ ruang tengah ↔ ruang makan; ruang tengah ↔ depan rumah & warung ↔ lapangan / dermaga / gudang ikan; lapangan ↔ dermaga. Kota: kamar kos ↔ pusat kota ↔ sekolah / toko buku / taman / Stasiun Besar.
- **Kegiatan terikat lokasi** (`lokasi` di aktivitas.txt). Daftar lengkap desa: [CERITA.md](CERITA.md) bagian 5. Di tempat lain tampil redup "Pergi ke Ruang makan".
- **Di mana orang berada** (`lokasi` di karakter.txt). Jadwal lengkap desa: [CERITA.md](CERITA.md) bagian 3. Yang janjian menunggu di `tempat_janji` (Bayu & Sekar di dermaga, Dimas di taman kota).
- "👥 Temui seseorang" di halaman waktu bebas hanya **keterangan** ("Ayah · 📍 Ruang makan"). Untuk bertemu, datangi tempatnya lalu ketuk orangnya.
- **Latar + orang (gambar buatan pemilik):** saat waktu bebas, latar berganti otomatis ke `latar/<lokasi>__<id orang urut abjad>.jpg`
  sesuai siapa yang sedang ada di situ (hanya yang sudah dikenal), mis. `ruang_makan__ayah_ibu_laras.jpg`, `rumah_warung__ayah_ibu.jpg`.
  Orang di gambar disentuh lewat area tak terlihat di titik `orang` (jadi posisi orang di gambar harus sama dengan titik itu).
  Bila gambar gabungannya belum ada, latar kosong yang tampil dan setiap orang ditandai **👤 nama** (selalu terlihat).
  Gambar yang perlu dibuat & posisinya: [CERITA.md](CERITA.md) bagian 4. Tokoh tidak lagi ditempel di atas latar.
- **Pengaturan → Nama lokasi & benda**: bila mati, tidak ada tulisan apa pun (nama tempat, pintu, benda, orang); pintu & benda tetap bisa disentuh langsung di gambar (area sentuh diperlebar).

## adegan.txt (cerita kecil saat waktu bebas)
Adegan muncul sendiri saat pemain **datang ke suatu lokasi** (`pemicu = masuk`) atau **menemui seseorang** (`pemicu = orang`),
pada hari & jam tertentu, bila syaratnya terpenuhi. Isinya ditulis di file naskah biasa dan **wajib diakhiri `@kembali`**
(lalu waktu bebas berlanjut).
```
[b2_ayah_jaring]
naskah = adegan_keluarga.txt         file naskah berisi adegannya
label  = b2_ayah_jaring              (opsional, bawaan = id) label awal adegan
judul  = Belajar simpul jaring dari Ayah
orang  = ayah                        pemicu orang: muncul saat Ayah diketuk (menggantikan sapaan biasa sekali itu)
lokasi = rumah_warung                (opsional untuk orang) hanya bila ditemui di sini
waktu  = senin-sabtu 15:00-17:30     (opsional) hari & jam
syarat = desa_bab == 2               (opsional) gabungkan dengan && (semua) atau || (salah satu)
sekali = ya                          (bawaan ya)
```
- Adegan yang sudah dimainkan tercatat `adegan_<id> = 1` → dipakai sebagai syarat tahap berikutnya.
- Variabel tanggal: `tgl` = bulan×100 + tanggal (627 = 27 Juni, 817 = 17 Agustus), `hari_ke`, `jam_menit`.
  Contoh "minimal sehari setelah tahap sebelumnya": di tahap 1 `@atur tahap1_hari = {hari_ke}`, di tahap 2 `syarat = hari_ke > {tahap1_hari}`.
- Variabel yang belum pernah diisi dianggap 0 bila dibandingkan dengan angka.
- Urutan di file = prioritas. Pemeriksa naskah & tes `SceneTest` mengecek setiap cabang adegan berakhir di `@kembali`.
- Daftar adegan desa per bab: [CERITA.md](CERITA.md) bagian 7–10.

## latar.txt (latar mengikuti jam)
Gambar latar hanya satu per tempat, padahal jam berjalan. Supaya tidak aneh (langit terang padahal jam 21:00),
game **mewarnai latar otomatis** sesuai bagian hari: pagi hangat, siang terang, sore jingga, malam gelap kebiruan.
Perubahan terjadi perlahan saat waktu berganti; karakter ikut menyesuaikan tapi lebih lembut (45%).
```
[rumah_warung]
waktu = pagi      (suasana di gambar aslinya: pagi / siang / sore / malam, atau "tetap" = tidak diubah)
```
- `tetap`: kenangan & adegan khusus (plafon, sekolah SMP, stasiun hujan, dermaga malam). Adegan kenangan juga tidak pernah diwarnai.
- Gambar asli malam tidak dibuat terang (hasilnya pucat).
- **Versi khusus** lebih bagus dari pewarnaan: simpan `latar/<nama>_<waktu>.jpg` (mis. `rumah_warung_malam.jpg`). Bila ada, game memakainya otomatis (berganti halus saat jamnya tiba) dan tidak mewarnai ulang.

## pesan.txt (chat di HP & pilihan balasannya)
```
[bayu_lulus]
dari          = bayu
jenis         = cerita        (cerita = dari naskah · acak = dikirim sendiri oleh karakter · sistem = dari game)
isi           = Woy {nama}! Selamat lulus, ...    (untuk "cerita": harus sama persis dengan teks @hp pesan di naskah)
hangat        = Makasih, Bay! Selamat juga buat kamu. Nanti aku ajak ketemu, ya.
balas_hangat  = Sip! Pencet aja "Ajak ketemu", terus pilih jamnya.
singkat       = Oke.
balas_singkat = Yaelah, singkat amat 😂 Ya udah, kabari aja kalau mau ketemu.
syarat        = lokasi == kota      (opsional, untuk "acak")
```
- Tombol balasan di Chat menampilkan kalimat balasannya langsung (bukan "Balas hangat/singkat"). Hangat = Cinta +3, singkat = +1 (sekali sehari).
- Pesan acak memakai `syarat` agar sesuai keadaan: soal dermaga hanya di desa, "Kapan pulang?" hanya di kota, Juragan Darto hanya bila kerja di gudang (`kerja_buruh_ikan == 1`), PR hanya bila sekolah (`kerja_sma_sakala == 1`). Pesan terakhir tidak diulang, dan tidak dikirim bila pesan sebelumnya belum dibalas.
- `tools/cek_naskah.py` memastikan setiap `@hp pesan` dari karakter di naskah punya entri di sini.
- Ajak ketemu: pemain menulis "Sore ini ada waktu? Aku mau ketemu." (pilihan: "Sore ini", "Besok siang", ...).

## karakter.txt

Setiap karakter punya data seperti ini (contoh lengkap ada di file-nya):
```
[bayu]
nama        = Bayu
kelamin     = laki-laki
usia        = 15
peran       = Teman sekelas, anak nelayan
pandangan   = setia            (setia / terbuka — sikap bila pasangannya punya pasangan lain)
bisa_dipacari = tidak          (ya / tidak)
tempat      = desa
waktu       = siang, sore      (bagian hari; dipakai untuk pilihan jam "Ajak ketemu")
ditemui     = janji            (ya = langsung, sesuai jadwal · janji = hanya lewat janji di Chat · tidak = tidak bisa)
jadwal      = senin-sabtu 13:00-20:00, minggu 07:00-20:00   (kapan ada di sekitarmu; tanpa jadwal → pakai "waktu")
skala       = 0.95             (ukuran gambar dibanding orang dewasa; Laras 0.78 karena masih anak-anak)
cinta_awal  = 45
percaya_awal = 55
suka_topik  = laut, sepak_bola, kota
benci_topik = pelajaran
suka_barang = gorengan, kopi_sachet
benci_barang = obat_flu
sapa        = Woy! Akhirnya muncul juga kamu.
respon_suka / respon_biasa / respon_benci = jawaban saat diajak ngobrol
hadiah_suka / hadiah_biasa / hadiah_benci = jawaban saat diberi hadiah
larangan    = kalimat saat ia dilarang bertemu oleh orang dekatnya
warna       = #2E8B57
```
- Tambahan untuk HP: `ajak_ya` / `ajak_tidak` (jawaban saat diajak ketemu). Pesan chat yang ia kirim & pilihan balasannya ada di **pesan.txt**.
  Karakter yang sudah dikenal dan Cintanya ≥ 25 kadang mengirim pesan (maks. 2 pesan per hari dari semua karakter).
- `kelamin = lawan` + `nama_l` / `nama_p`: karakter yang jenis kelamin & namanya menyesuaikan pemain (contoh: sahabat masa kecil Arka/Alya).
  Gambarnya: `id_l.png` / `id_p.png` (dan `id_l_foto.jpg` / `id_p_foto.jpg`). Di naskah pakai `{nama_<id>}`, mis. `{nama_sahabat}`.
- `nama` juga dipakai sebagai **nama pembicara** di naskah (`Bayu: ...`). Saat karakter bicara, panel foto + nama + meter muncul di pojok kiri atas (setelah dikenal lewat `@kenal`).
- `usia` bertambah bersama usia pemain. **Romansa hanya terbuka bila pemain dan karakter sama-sama 18 tahun ke atas**, lawan jenis, dan `bisa_dipacari = ya`.

### Profil karakter (☰ Menu → Karakter)
```
hobi        = Memancing di ujung dermaga
kegemaran   = Gorengan & kopi sachet
pelajaran   = Olahraga            (anak sekolah)   ·   keahlian = ...   (orang dewasa)
cita_cita   = Punya kapal ikan sendiri
kisah_1 / kisah_2 / kisah_3 = cerita hidupnya, makin dalam makin sulit dibuka
tahu_awal   = hobi, kegemaran     (sudah diketahui sejak kenal, mis. keluarga sendiri)
tampil_awal = ya                  (tampil di daftar walau belum bertemu, mis. sahabat masa kecil)
daftar      = tidak               (tidak tampil di daftar)
```
Cara info terbuka:
| Info | Lewat obrolan langsung (syarat) | Bisa diceritakan orang lain? |
|---|---|---|
| Hobi | Cinta ≥ 20, sudah 1× ngobrol | ✅ |
| Kegemaran / Pelajaran / Keahlian | Cinta ≥ 30, 2× ngobrol | ✅ |
| Cita-cita | Cinta ≥ 45, 3× ngobrol | ✅ |
| Kisah 1 | Cinta ≥ 40, 3× ngobrol | ✅ |
| Kisah 2 | Cinta ≥ 60, 6× ngobrol, Kepercayaan ≥ 55 | ❌ |
| Kisah 3 | Cinta ≥ 80, 10× ngobrol, Kepercayaan ≥ 75 | ❌ |

- Satu info baru per obrolan (yang tidak membuatnya kesal). Muncul sebagai kalimat "📖 Bayu bercerita — Hobi: ...".
- Kadang (30%, bila Cinta ≥ 35) ia bercerita tentang orang dekatnya (dari `hubungan.txt`). Karakter yang belum pernah ditemui pun jadi muncul di daftar.
- Kesukaan/ketidaksukaan topik & barang terbuka saat dicoba (ngobrol/hadiah).
- Dari naskah: `@info bayu hobi` (atau `@info bayu semua`).

### Siapa yang bisa ditemui (waktu bebas → Temui seseorang)
Hanya orang yang **sudah dikenal**, ada di **tempat yang sama** (desa/kota), dan **memang ada sekarang** menurut kolom `lokasi`
(karakter tanpa `lokasi` memakai `jadwal`). Jadwal desa lengkap: [CERITA.md](CERITA.md) bagian 3.
| Karakter | ditemui | kapan & di mana (ringkas) |
|---|---|---|
| Ayah | ya | sarapan 06:15, makan siang 12:00, jaring di depan rumah 13:00–16:00, radio 16:00–17:45, makan malam, dermaga 18:30–19:15 |
| Ibu | ya | memasak 05:00, warung 07:15–17:15, memasak & makan malam, ruang tengah 18:30–21:00 |
| Laras | ya | sarapan, (sekolah), makan siang 13:00, PR 14:00–16:00, depan rumah 16:00–17:15, TV 18:30–20:30; Minggu warung & lapangan |
| Bayu | ya (+ janji) | lapangan 15:00–17:30, dermaga 17:30–18:30; Minggu lapangan pagi & dermaga sore |
| Sekar | ya (+ janji) | dermaga Senin–Sabtu 15:30–17:15 (`ada_bila = sekar_pergi != 1 \|\| tgl <= 710` — bila memilih beasiswa, pergi 11 Juli) |
| Juragan Darto | ya | gudang Senin–Sabtu 07:00–16:00 |
| Dimas | janji | Chat → 📅 Ajak ketemu |
| Nadia | ya | toko buku 14:00–20:00 |
| Sahabat | tidak | hanya lewat cerita |
`ada_bila` (opsional) = syarat karakter masih ada di tempatnya; mis. Sekar tidak ada lagi di desa setelah berangkat beasiswa ke Sakala.
Boleh memakai `&&` (semua harus benar) dan `||` (salah satu cukup), juga di `syarat` adegan, aktivitas, pekerjaan, Sosmed.
`tidur` / `tidur_libur` (opsional) = tempat & jam tidur, mis. Laras `tidur = setiap hari 21:00-06:15 kamar_laras`. Orang yang tidur tampil di latar
(gambar `latar/<lokasi>__<id>_tidur.jpg`, posisi `<id>_tidur` di lokasi.txt) tapi tidak bisa diajak bicara; diketuk = merapikan selimut (`tidur_teks`).
`libur` + `lokasi_libur` (opsional) = jadwal pengganti di rentang tanggal tertentu, mis. Laras `libur = 622-712` (libur kenaikan kelas,
tanggal = bulan×100 + hari) dengan `lokasi_libur` berisi jadwal seharian di rumah/warung/lapangan.

### Saat bertemu
Menu menyesuaikan keadaan: **💬 Ngobrol** (maks. 2× per pertemuan) · **🎁 Beri hadiah** (hanya bila tas berisi & belum memberi hari itu) · **👋 Pamit**.
Isi "Ngobrol" diambil dari **obrolan.txt** (milik masing-masing karakter, tidak acak).

| Aksi | Akibat |
|---|---|
| Obrolan santai | sesuai `efek` (topik yang ia sukai +4/+5, dibenci −2/−3). Diulang di hari yang sama: paling banyak +1 |
| Kenali lebih dalam | membuka info profil; belum cukup akrab/percaya → ia mengelak (tanpa efek, bisa ditanya lagi nanti) |
| Pertanyaan khusus | muncul bila syarat cerita terpenuhi; bisa membuka petunjuk (`petunjuk_sahabat +1`) |
| Hadiah disukai / biasa / dibenci | +10 / +3 / −6 (maks. 1 hadiah per karakter per hari) |
| Hadiah basi | Cinta −8, Kepercayaan −6 |
| Pamit | Memakai 1 kesempatan waktu bebas, Energi −5, Kewarasan +2 sampai +5. Lama pertemuan: 15 menit + 20 menit per obrolan + 10 menit bila memberi hadiah |

### Nama meter di layar
- 💛 **Akrab**: rasa sayang sebagai teman/keluarga (sesama jenis, keluarga, anak-anak, atau belum 18 tahun).
- 💗 **Cinta**: hanya untuk lawan jenis dengan `bisa_dipacari = ya`, setelah pemain & karakter sama-sama 18+.
- ❤️ **Cinta (pasangan)**: setelah resmi berpacaran.
Nilainya sama (kolom Cinta di data/naskah, mis. `@cinta bayu +5`); yang berubah hanya namanya.

### Tingkat hubungan
`Belum kenal → Kenalan (<30) → Teman (30+) → Teman dekat (60+) → Sahabat (85+ & percaya 60+)`
Cinta di bawah 15 atau Kepercayaan di bawah 10 → **Bermusuhan**.
Tingkat romantis (`Pacar → Tunangan → Pasangan hidup`) hanya lewat cerita (`@ungkapkan`).

## hubungan.txt
```
bayu - sekar = sahabat 75
ayah - ibu   = pasangan 90
```
- Jenis: `keluarga`, `sahabat`, `teman`, `pasangan`, `saingan`, `mantan`, `rekan`. Kekuatan 0–100.
- **Kabar menyebar:** setiap penurunan Cinta/Kepercayaan ke A ikut menurunkan orang dekat A sebesar ±45% × kekuatan (tidak untuk saingan/mantan).
- **Ikut campur:** bila A sudah dikenal, Cinta A < 25, dan hubungan A–B ≥ 60, maka B **dilarang** bertemu pemain (muncul kalimat `larangan` B).

## topik.txt
```
[laut]
nama    = Laut & melaut
ikon    = 🌊
kalimat = Kemarin aku lihat kapal besar lewat dekat mercusuar.
```
Dipakai untuk mencatat "Suka / Tidak suka" di halaman Karakter (kolom `topik` di obrolan.txt).

## obrolan.txt (ngobrol saat bertemu)
```
[ayah_kisah2]
karakter = ayah
jenis    = kenal                 (santai / kenal / khusus)
judul    = Tanya apa yang Ayah sembunyikan
akrab    = 60                    (Cinta/Keakraban minimal)
percaya  = 55                    (Kepercayaan minimal)
ungkap   = kisah_2               (info profil yang terbuka)
kamu     = Yah... ada yang Ayah sembunyikan, ya?
jawab    = * Ayah menghela napas panjang. // Perahu itu... Ayah beli dengan uang pinjaman Juragan Darto.
tolak    = Tidak ada apa-apa. Jangan dipikirkan.
efek     = percaya +5
```
- `jawab`: beberapa kalimat dipisah `//`; diawali `*` = narasi.
- `santai` boleh diulang; `kenal` & `khusus` hanya sekali (kecuali `sekali = tidak`). Pertanyaan `kenal` untuk info yang sudah diketahui tidak muncul.
- `topik` (santai) → id dari topik.txt; `ungkap_lain = sahabat kisah_2` membuka info karakter lain; `syarat` sama seperti di aktivitas (mis. `jalur == kerja && kerja_aktif == 0`).
- Diperiksa otomatis oleh `tools/cek_naskah.py` dan unit test `MeetingTest`.

## kejadian.txt (rintangan & kejadian acak)
```
[dompet_hilang]
judul   = Dompet hilang!
ikon    = 👛
tempat  = kota
peluang = 3                      (persen per hari; 0 = hanya lewat naskah @kejadian)
syarat  = uang >= 30000          (gabungkan dengan &&, mis. musim == hujan && kos == kos_rel_utara)
sekali  = ya                     (opsional: hanya sekali seumur permainan)
teks    = Cerita kejadiannya
efek    = uang -40%, kewarasan -12
pilih_a = Teks tombol | efek | teks hasil
pilih_b = ...
catatan = (opsional) masuk ke aplikasi Catatan
```
- Setiap ganti hari, **paling banyak satu** kejadian terjadi; tampil di waktu bebas berikutnya.
- Efek bisa: bar (`kesehatan`, `kenyang`, …), `uang` (angka atau **persen**: `uang -40%`), `cinta_<id>` / `percaya_<id>` karakter, `terbongkar_<kode> 1` (kebohongan dari `@bohong` terbongkar), atau variabel lain (mis. `foya +1`).
- Variabel berguna untuk `syarat`: `uang`, `lokasi`, `kos`, `musim` (`hujan` Nov–Mar / `kemarau`), `bulan`, `usia`, `jalur` (`sma` / `kerja`), `rencana_kerja` (`tidak` / `rahasia` / `terbuka` / `desa`), `kerja_aktif` (jumlah pekerjaan selain sekolah), `foya`, `hidup_baik`, `minta_ortu`, `kenal_<id>`, `cinta_<id>`, `kerja_<id>`.

## sosmed.txt (aplikasi Sosmed di HP)
```
[bayu_dermaga]            unggahan teman (beranda)
penulis = bayu            id karakter — hanya tampil bila pemain sudah kenal
hari    = 6               mulai tampil pada hari ke-6
syarat  = jalur == kerja  (opsional)
isi     = Sore di dermaga. Makin sepi aja 😔

[cari_1]                  hasil pencarian sahabat
jenis    = cari
butuh    = 2              ditemukan pada pencarian ke-2 (pemain mencari 1x sehari)
isi      = Yang ditemukan ({nama_sahabat} = nama sahabat)
catatan  = Masuk ke aplikasi Catatan
petunjuk = 1              tambahan petunjuk_sahabat
```
- Menyukai unggahan (sekali per unggahan) → Cinta penulis +1.
- Variabel: `sosmed_cari` (jumlah pencarian), `sosmed_<id>` = 1 bila temuan sudah didapat.

## pekerjaan.txt (sekolah & pekerjaan)
```
[kurir_paket]
nama        = Kurir antar paket (sepeda)
ikon        = 📦
jenis       = paruh_waktu        (sekolah / tetap / paruh_waktu / harian)
tempat      = kota
hari        = setiap hari        (senin-sabtu / senin, rabu, jumat / setiap hari)
waktu       = sore, malam
gaji        = 30000              (paruh_waktu & harian: per kerja · tetap: per 30 hari)
efek        = energi -25, hidrasi -15, komunikasi +1
usia_min    = 15
cara        = langsung           (langsung / lamar / cerita)
sulit       = 25                 (0-100)
orang_dalam = dimas              (opsional: id karakter yang bisa merekomendasikan)
keterangan  = Penjelasan singkat
teks        = Narasi setiap kali kerja
```
| Jenis | Aturan |
|---|---|
| `sekolah`, `tetap` | **Wajib hadir** sesuai hari & waktu. Tidak hadir saat waktu bebas = bolos/absen (kinerja −8). 3× → surat peringatan, 6× → **dikeluarkan/dipecat** |
| `paruh_waktu`, `harian` | Datang kapan saja sesuai jadwal. Dibayar setiap kali kerja |
| `tetap` | Digaji setiap 30 hari sejak mulai, besarnya tergantung kinerja |
| `sekolah` + `ujian_tiap = 45` | Ujian setiap 45 hari (4 soal; nilai naik/turun). Setiap 365 hari: **naik kelas** (nilai ≥ 40 & bolos < 6), **tinggal kelas**, atau **lulus** setelah kelas 12 (dapat `ijazah_sma`) |

- Kinerja: +3 tiap kerja (−4 bila energi < 20). Kinerja < 15 → diberhentikan (bukan sekolah).
- `cara = langsung` / `lamar` muncul di **🔎 Cari kerja** saat waktu bebas; `cara = cerita` hanya lewat naskah.
- **Peluang diterima** = (100 − sulit) + 2 × (akademik + komunikasi, maks. +30) + `skor_wawancara`
  − 15 bila Kewarasan < 30, **+45 lewat orang dalam** (bila Cinta & Kepercayaan orang itu ≥ 60). Usia di bawah `usia_min` selalu ditolak.

Tambahan untuk `cara = lamar` (dilamar lewat **HP → Lowongan**):
```
berkas = cv, pas_foto, ijazah_smp     (id dari dokumen.txt — wajib dimiliki)
tes    = ya                           (ada tes tertulis sebelum wawancara)
```
Alur lamaran di HP: **Kirim lamaran → seleksi berkas (2 hari) → tes (bila ada, 3 hari) → wawancara (3 hari) → diterima/ditolak**.
Lewat orang dalam: langsung ke wawancara. Lewat batas waktu = gugur. Kabar dikirim lewat Chat dari "HRD · nama pekerjaan".

## dokumen.txt
```
[skck]
nama       = SKCK
biaya      = 30000
lama       = 3          (hari sampai jadi; 0 = langsung)
usia_min   = 17
awal       = ya         (opsional: sudah dimiliki sejak awal)
keterangan = Surat dari kantor polisi.
```
Diurus di **HP → Lowongan → Dokumen saya**. `usia_min = 99` berarti hanya didapat lewat cerita (`@dokumen`).

## wawancara.txt
```
[motivasi]
tanya = Kenapa kamu ingin bekerja di sini?
a = Saya ingin belajar dan membantu keluarga. | 15
b = Ya... butuh uang saja. | -5
c = Saya sudah sangat berpengalaman! | 20 | bohong
```
3 pertanyaan diacak setiap wawancara. Jawaban `bohong`: 40% ketahuan (skor −30).
**Peluang diterima** = 40 − sulit/3 + skor wawancara + ½ skor tes + 2×(akademik+komunikasi) (+30 orang dalam, −15 bila Kewarasan < 30).

## kos.txt
```
[kos_cendana]
nama            = Kos Melati (Kawasan Cendana)
harga           = 650000          (per 30 hari)
tempat          = kota
tidur_energi    = 0               (bising = minus)
tidur_kewarasan = 1
keterangan      = Penjelasan
```
- Pindah = bayar 30 hari pertama. Selanjutnya otomatis ditagih setiap 30 hari.
- Uang kurang = **tunggakan**. Tunggakan 2× → **diusir**: tidur di emperan (energi −40, kewarasan −10 setiap tidur) sampai pindah ke kos baru lewat **🏠 Cari kos**.

## Gambar karakter
Folder `app/src/main/assets/karakter/`:
| File | Isi |
|---|---|
| `bayu.png` | Gambar utama (PNG transparan, badan dari kepala sampai pinggang/lutut, portrait) |
| `bayu_senyum.png`, `bayu_sedih.png`, `bayu_kesal.png`, `bayu_kaget.png` | Ekspresi (opsional; bila tidak ada, dipakai `bayu.png`) |
| `bayu_foto.jpg` | Foto wajah persegi untuk panel & daftar kontak |

Ekspresi yang dipakai otomatis saat ngobrol/memberi hadiah: `senyum`, `kesal`, `biasa`.

## Aturan waktu & kebutuhan (sementara, bisa disesuaikan)

**Jam sungguhan** (sejak build 26): jam berjalan per menit. Bagian hari: Pagi 04:00–10:59, Siang 11:00–14:59,
Sore 15:00–17:59, Malam 18:00–03:59. Aktivitas punya `durasi`, pekerjaan punya `jam` (masuk–pulang).
Per jam terjaga: 🍚 −2, 💧 −2,8, ⚡ −1 (begadang 00:00–04:00: ⚡ ×3 & 🧠 −1/jam). Saat tidur: ⚡ +11/jam,
🍚/💧 turun setengahnya. Tidur < 6 jam: 🧠 −4, ⚡ −8. Kenyang/hidrasi/energi 0 → ❤️ turun tiap jam.

| Kejadian | Akibat |
|---|---|
| Ganti slot (terjaga) | Kenyang −10, Hidrasi −14, Energi −5 |
| Tidur (Malam → Pagi) | Kenyang −15, Hidrasi −15, Energi +80 (hanya +45 bila lapar/haus), Kewarasan +5 |
| Kenyang 0 / Hidrasi 0 / Energi 0 | Kesehatan −10 / −15 / −5 setiap slot |
| Kenyang ≥50, Hidrasi ≥50, Energi ≥30 | Kesehatan pulih +2 per slot |
| Kenyang/Hidrasi <25 atau Energi <20 | Kewarasan −3 per slot |
| Kesehatan 0 | **Meninggal (game over)** |
