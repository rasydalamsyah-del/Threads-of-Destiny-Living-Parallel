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
- Setiap aktivitas memakan **satu** kesempatan dan memajukan waktu satu slot.

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
waktu       = siang, sore      (kapan bisa ditemui di waktu bebas)
ditemui     = ya
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
- `nama` juga dipakai sebagai **nama pembicara** di naskah (`Bayu: ...`). Saat karakter bicara, panel foto + nama + meter muncul di pojok kiri atas (setelah dikenal lewat `@kenal`).
- `usia` bertambah bersama usia pemain. **Romansa hanya terbuka bila pemain dan karakter sama-sama 18 tahun ke atas**, lawan jenis, dan `bisa_dipacari = ya`.

### Ngobrol & hadiah (waktu bebas → Temui seseorang)
| Aksi | Akibat ke Cinta |
|---|---|
| Topik disukai | +6 (+3 bila sudah ngobrol hari itu) |
| Topik biasa | +2 (+1) |
| Topik dibenci | −5 |
| Hadiah disukai / biasa / dibenci | +10 / +3 / −6 (maks. 1 hadiah per karakter per hari) |
| Hadiah basi | Cinta −8, Kepercayaan −6 |
| Pamit | Memakai 1 kesempatan waktu bebas, Energi −5, Kewarasan +2 sampai +5 |

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
Saat ngobrol, pemain memilih 1 dari 3 topik acak.

## Gambar karakter
Folder `app/src/main/assets/karakter/`:
| File | Isi |
|---|---|
| `bayu.png` | Gambar utama (PNG transparan, badan dari kepala sampai pinggang/lutut, portrait) |
| `bayu_senyum.png`, `bayu_sedih.png`, `bayu_kesal.png`, `bayu_kaget.png` | Ekspresi (opsional; bila tidak ada, dipakai `bayu.png`) |
| `bayu_foto.jpg` | Foto wajah persegi untuk panel & daftar kontak |

Ekspresi yang dipakai otomatis saat ngobrol/memberi hadiah: `senyum`, `kesal`, `biasa`.

## Aturan waktu & kebutuhan (sementara, bisa disesuaikan)
| Kejadian | Akibat |
|---|---|
| Ganti slot (terjaga) | Kenyang −10, Hidrasi −14, Energi −5 |
| Tidur (Malam → Pagi) | Kenyang −15, Hidrasi −15, Energi +80 (hanya +45 bila lapar/haus), Kewarasan +5 |
| Kenyang 0 / Hidrasi 0 / Energi 0 | Kesehatan −10 / −15 / −5 setiap slot |
| Kenyang ≥50, Hidrasi ≥50, Energi ≥30 | Kesehatan pulih +2 per slot |
| Kenyang/Hidrasi <25 atau Energi <20 | Kewarasan −3 per slot |
| Kesehatan 0 | **Meninggal (game over)** |
