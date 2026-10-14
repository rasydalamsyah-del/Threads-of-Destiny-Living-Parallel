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

## Aturan waktu & kebutuhan (sementara, bisa disesuaikan)
| Kejadian | Akibat |
|---|---|
| Ganti slot (terjaga) | Kenyang −10, Hidrasi −14, Energi −5 |
| Tidur (Malam → Pagi) | Kenyang −15, Hidrasi −15, Energi +80 (hanya +45 bila lapar/haus), Kewarasan +5 |
| Kenyang 0 / Hidrasi 0 / Energi 0 | Kesehatan −10 / −15 / −5 setiap slot |
| Kenyang ≥50, Hidrasi ≥50, Energi ≥30 | Kesehatan pulih +2 per slot |
| Kenyang/Hidrasi <25 atau Energi <20 | Kewarasan −3 per slot |
| Kesehatan 0 | **Meninggal (game over)** |
