# Panduan Menulis Naskah

Cerita game ditulis di file teks biasa, jadi bisa diedit langsung dari HP lewat GitHub
tanpa menyentuh kode.

- Naskah: `app/src/main/assets/naskah/prolog.txt`
- Gambar background: `app/src/main/assets/latar/` (format `.jpg`, `.png`, atau `.webp`, portrait, idealnya 1080×1920)

Setelah file diubah dan di-commit, GitHub Actions otomatis membuat APK baru.

## Dasar

| Tulis seperti ini | Hasilnya |
|---|---|
| `* Dulu, di sebuah kota kecil...` | **Narator** (tanpa nama, huruf miring) |
| `Hana: Latte di sini enak banget!` | **Dialog** dengan nama pembicara |
| `{nama}: ...Mimpi itu lagi.` | Dialog oleh **pemain** (otomatis memakai nama yang diisi) |
| `# catatan` | Komentar, tidak tampil di game |

`{nama}` bisa dipakai di mana saja (narasi, dialog, pesan HP) dan otomatis diganti nama pemain.
Satu baris = satu kotak dialog. Pemain mengetuk layar untuk lanjut.

### Teks sesuai jenis kelamin pemain
Tulis `{teks untuk laki-laki|teks untuk perempuan}`:
```
Ayah: Nah, ini dia {jagoan|putri} Ayah yang baru lulus.
```

### Variabel di dalam teks
`{nama_variabel}` diganti nilainya, misalnya `{usia}` atau `{jalur}` (lihat `@atur` di bawah).
Variabel bawaan: `{nama}`, `{kelamin}` (`laki-laki` / `perempuan`).

## Perintah

Perintah diawali `@`.

### Background
```
@latar nama_gambar
@latar nama_gambar kenangan
@latar nama_gambar kenangan gerak
@latar nama_gambar naik
```
- `nama_gambar` = nama file di folder `latar` **tanpa** `.jpg`
- `kenangan` → warna pudar kecokelatan + tepi gelap (efek masa lalu), kotak dialog jadi gelap
- `gerak` → kamera zoom & geser pelan
- `naik` → kamera seperti bangkit/duduk (gambar lama naik keluar, gambar baru masuk dari bawah)

### Layar gelap & transisi
```
@gelap          layar perlahan gelap total (1,5 detik)
@gelap 2500     sama, tapi 2,5 detik
@terang         layar perlahan terang kembali
@waktu Sabtu, 07:12     teks waktu di tengah layar gelap
@kedip          efek bangun tidur: mata berkedip, pandangan buram lalu jelas
@jeda 1000      diam sebentar (1 detik)
```
`@waktu` dan `@kedip` dipakai setelah `@gelap`. Ganti background saat layar gelap
(`@latar ...`) terjadi langsung tanpa transisi, lalu munculkan dengan `@terang` atau `@kedip`.

### Jam di pojok kanan atas
```
@jam Sabtu, 07:12 ☀️
@jam sembunyi
```
Jam ini juga dipakai di layar kunci HP.

### HP
```
@hp tampil
@hp sembunyi
@hp pesan Nama Pengirim | Isi pesannya
@tunggu_hp
```
- `@hp pesan` → pesan masuk, ikon HP bergetar dan bertitik merah
- `@tunggu_hp` → cerita berhenti sampai pemain membuka HP lalu menutupnya

### Musik & efek suara
```
@musik nama_file      putar musik latar berulang (dari folder assets/musik)
@musik stop           hentikan musik
@suara nama_file      putar efek suara sekali (dari folder assets/suara)
```
- Format: `.mp3`, `.ogg`, `.m4a`, atau `.wav`. Tulis nama file **tanpa** ekstensi.
- File `assets/suara/notifikasi.mp3` (kalau ada) otomatis diputar saat pesan HP masuk.
- Volume mengikuti halaman **Pengaturan**. Kalau file tidak ada, game tetap jalan tanpa suara.

### Pilihan jawaban
```
@pilihan
> Lanjut SMA di Kota Sakala -> sma
> Tetap di desa dan bekerja -> kerja
> SMA sambil bekerja -> keduanya

:: sma
{nama}: Aku mau lanjut SMA.
@lompat malam

:: kerja
{nama}: Aku mau kerja dulu.
@lompat malam

:: keduanya
{nama}: Aku mau sekolah sambil kerja.

:: malam
* Malam itu...
```
- Kalimat **sebelum** `@pilihan` tetap tampil sebagai pertanyaannya.
- Setiap baris `> teks -> nama_label` adalah satu tombol pilihan (maksimal ±4 agar muat di layar).
- `:: nama_label` menandai tempat tujuan. `@lompat nama_label` pindah ke sana.
- Tanpa `@lompat`, cerita **lanjut ke baris berikutnya** (termasuk masuk ke label di bawahnya).
- Nama label: huruf, angka, garis bawah, tanpa spasi. Label yang tidak ada → pesan error.

### Variabel & syarat
```
@atur jalur = sma          isi variabel
@atur usia = 15
@tambah uang +50000        tambah / kurangi angka (pakai - untuk mengurangi)
@jika jalur == kerja -> malam_kerja
@jika usia >= 18 -> dewasa
```
- `@jika` melompat ke label hanya bila syaratnya benar; kalau tidak, lanjut ke baris berikutnya.
- Pembanding: `==` `!=` `>=` `<=` `>` `<`. Angka dibandingkan sebagai angka, teks tidak membedakan huruf besar/kecil.
- Variabel ikut tersimpan di data simpanan.

### Akhir
```
@tamat Bersambung…
```

## Contoh adegan

```
@gelap
@waktu Senin, 16:40
@latar kafe
@jam Senin, 16:40 ☀️
@terang
* Kafe kecil di sudut jalan itu selalu ramai di sore hari.
Hana: Latte di sini enak banget! Untung kita jadi ke sini.
{nama}: Iya, pemandangannya juga bagus.
```

## Simpan & muat

Data simpanan mengingat **nomor baris** perintah di naskah. Kalau Anda menambah atau menghapus
baris di bagian **sebelum** posisi simpanan, data simpanan lama bisa melompat ke kalimat lain.
Saat masih tahap pengembangan, ini wajar: cukup mulai ulang dengan START GAME.

## Kalau ada yang salah

Kalau naskah salah tulis (misalnya perintah tidak dikenal), game menampilkan pesan
**"Naskah bermasalah: Naskah baris X: ..."** saat Start Game. Cek baris tersebut.
Kalau gambar tidak ditemukan, muncul pesan *"Gambar latar/… tidak ditemukan"* dan layar hitam.
