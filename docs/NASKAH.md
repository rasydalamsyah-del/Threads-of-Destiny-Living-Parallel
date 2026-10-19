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

### Sistem kehidupan
```
@hidup mulai              nyalakan panel status, uang, jam otomatis
@lokasi desa              tempat pemain (menentukan aktivitas & toko): desa / kota
@kalender Senin siang     atur hari & jam (pagi=06:00, siang=11:00, sore=15:00, malam=18:30) atau jam tepat: @kalender Senin 07:30
@uang +50000              tambah / kurangi uang
@stat energi -10          ubah bar: kesehatan, kenyang, hidrasi, kewarasan, energi
@barang air_mineral 2     masukkan barang ke tas (id dari data/barang.txt)
@maju 1                   loncat ke bagian hari berikutnya (Malam → tidur sampai 06:00)
@maju 2 jam               majukan jam (juga: @maju 30 menit). Waktu yang dimajukan cerita tidak dihitung bolos
@bebas 2                  (lama) waktu bebas 2 kegiatan — sebaiknya tidak dipakai lagi; pakai "sampai"
@bebas 5 hari             waktu bebas selama 5 hari (pemain makan, sekolah/kerja, tidur sendiri)
@bebas sampai Sabtu 15:00 waktu bebas sampai hari & jam tertentu (DISARANKAN). Pemain bebas berkegiatan selama
@jalan ruang_makan | petunjuk   pemain berjalan sendiri (mengetuk pintu) sampai lokasi itu, lalu naskah lanjut.
                          Lokasi dari data/lokasi.txt. Selama berjalan hanya pintu yang bisa disentuh.
@atur latar_ruang_tengah = ruang_tengah_sarapan   ganti sementara gambar sebuah lokasi (mis. keluarga sedang makan);
                          kembalikan dengan @atur latar_ruang_tengah = -
                          tenaganya cukup; tombol "⏩ Santai sampai ..." untuk langsung lanjut; Tidur hanya muncul
                          bila waktu bebas masih ≥ 8 jam (tidak melompati kelanjutan cerita)
```
- Saat `@bebas`, pemain memilih aktivitas dari `data/aktivitas.txt`, bisa membuka **Status & Tas** dan **Belanja**.
  Saat Malam ada tombol **Tidur sampai pagi**.
- Variabel otomatis: `{uang}`, `{hari}`, `{waktu}` (Pagi/Siang/Sore/Malam), `{jam}` (mis. 07:30), `jam_menit` (menit sejak tengah malam, untuk `@jika`), `{hari_ke}`.
- Kalau Kesehatan habis, tokoh **meninggal** (game over).
- Cara menambah barang & aktivitas: **[DATA.md](DATA.md)**.

### Karakter & hubungan
```
@kenal bayu                      pemain mengenal karakter (muncul di panel & "Temui seseorang")
@tampil bayu senyum kiri         tampilkan gambar karakter (posisi: kiri / tengah / kanan)
@sembunyi bayu                   sembunyikan satu karakter   (@sembunyi semua = semua)
@cinta bayu +5                   ubah Cinta (bila sudah pacaran → Cinta pasangan)
@percaya bayu -10                ubah Kepercayaan
@perilaku bayu menghindar        catat perilaku (lihat tabel di bawah)
@bohong kode_bohong bayu sekar   catat kebohongan ke karakter tsb (belum ketahuan)
@terbongkar kode_bohong          kebohongan terbongkar → Cinta −10, Kepercayaan −25 ke yang dibohongi
@ungkapkan bayu -> diterima, ditolak   ungkapkan perasaan, lompat ke label sesuai hasil
```
| `@perilaku` | Cinta | Kepercayaan |
|---|---|---|
| `menghindar` | −3 | −5 |
| `ingkar_janji` | −6 | −15 |
| `abaikan_chat` | −2 | −3 |
| `kasar` | −8 | −4 |
| `selingkuh_ketahuan` | −40 | −50 |
| `jujur` / `membantu` / `menepati_janji` | +2 / +5 / +3 | +5 / +4 / +6 |

- Penurunan ikut **menyebar** ke orang dekat karakter (lihat `data/hubungan.txt`).
- Variabel otomatis untuk `@jika`: `cinta_bayu`, `percaya_bayu`, `tingkat_bayu` (kenalan/teman/teman_dekat/sahabat/pacaran/…/musuh), `kenal_bayu`, `bohong_kode`, `terbongkar_kode`.
- `@ungkapkan` **selalu ditolak** bila pemain atau karakter belum 18 tahun, sesama jenis, atau karakter tidak bisa dipacari.
  Selain itu: Cinta < 50 hampir pasti ditolak; makin tinggi Cinta & Kepercayaan, makin besar peluang diterima.

### Bab, tanggal, tempat tinggal, sekolah & pekerjaan
```
@naskah bab2.txt                   pindah ke file naskah berikutnya (folder assets/naskah)
@tanggal 11 Juli 2026              atur tanggal hari ini (hari dalam seminggu ikut tanggal)
@kalender Senin pagi               loncat ke hari Senin berikutnya (bila tanggal sudah diatur) & atur waktu
@kos kos_cendana                   pindah ke kos (bayar 30 hari pertama)   ·   @kos kosong = tanpa kos
@kiriman 600000                    kiriman keluarga setiap 30 hari   ·   @kiriman 0 = berhenti
@kerja_mulai sma_sakala            mulai sekolah / bekerja (id dari data/pekerjaan.txt)
@kerja_berhenti kurir_paket        berhenti
@lamar buruh_ikan -> diterima, ditolak   melamar, lompat sesuai hasil
```
- Variabel otomatis: `usia` (bertambah tiap 365 hari), `kerja_<id>` (1/0), `dipecat_<id>`, `dikeluarkan_<id>`,
  `kos`, `diusir`, `lewat_orang_dalam`, plus kemampuan dari efek kerja/aktivitas (mis. `akademik`, `komunikasi`).
- Untuk simulasi **wawancara**, tulis adegan dengan `@pilihan` lalu `@tambah skor_wawancara +10` (atau −) sebelum `@lamar`.

### HP: catatan & dokumen
```
@catatan Petunjuk #1 — ...        tambah catatan di aplikasi Catatan (untuk petunjuk cerita, tugas)
@dokumen ijazah_sma               pemain mendapat dokumen (id dari data/dokumen.txt)
@hp pesan Bayu | ...              pesan dari karakter masuk ke aplikasi Chat (bisa dibalas)
```
- Pesan dari karakter yang tidak dibalas **2 hari** otomatis dihitung `abaikan_chat`.
- `@tunggu_hp`: cerita baru lanjut setelah pesan dari cerita **dibuka di Chat** (menutup HP tanpa membaca → diingatkan). Setiap `@hp pesan` dari karakter wajib punya pilihan balasan di `data/pesan.txt`.
- Pemain bisa **mengajak ketemu** lewat Chat. Datang = `menepati_janji`, tidak datang = `ingkar_janji` (+ pesan kecewa).
- Variabel otomatis: `misterius_dibalas` (pemain membalas Nomor Tidak Dikenal), `bohong_wawancara_<id_pekerjaan>`.

### Profil karakter
```
@info bayu hobi                   pemain mengetahui info profil (hobi, kegemaran, pelajaran, keahlian, cita_cita, kisah_1..3, semua)
```

### Tutorial & mulai ulang
```
@mulai_ulang                      hapus semua hasil latihan (hidup, hubungan, HP, variabel); nama & kelamin tetap
```
- Urutan file: `mulai.txt` (pilih Tutorial / Lewati) → `tutorial.txt` (diakhiri `@mulai_ulang` + `@naskah prolog.txt`) → `prolog.txt`.

```
@tunjuk hp kalender | Penjelasan...<br>baris baru      tampilkan bagian layar + kotak penjelasan (ketuk untuk lanjut)
@tunjuk status bawah | ...                             kotak penjelasan di bawah (default di atas)
```
Bagian yang bisa ditunjuk: `menu`, `panel`, `karakter`, `karakter_detail <id>`, `status`, `tas`, `belanja`, `jam`, `bebas`, `ikon_hp`, `hp` (+ nama aplikasi:
`chat`, `kontak`, `kalender`, `lowongan`, `dompet`, `catatan`, `sosmed`), `kerja`, `kos`.

### Kejadian & sekolah
```
@kejadian flu                     jalankan kejadian dari data/kejadian.txt sekarang
@atur kelas = 10                  kelas awal SMA (naik otomatis setiap 365 hari)
```
- Variabel otomatis: `kejadian_terakhir`, `kejadian_<id>` (bila `sekali = ya`), `nilai_ujian_terakhir`, `kelas`,
  `tinggal_kelas`, `lulus_sma`, `musim`, `bulan`, `nama_<id>` (nama tampil karakter, mis. `{nama_sahabat}`).

### Pemeriksaan otomatis
Setiap build, GitHub Actions menjalankan **Cek naskah & data** (`tools/cek_naskah.py`). Kalau ada perintah salah tulis,
label hilang, atau id karakter/barang/pekerjaan/latar yang tidak ada, build gagal dan **baris yang salah** ditampilkan
di tab Actions → build tersebut → bagian Annotations.

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


## Tampilan karakter saat dialog
- Semua dialog memakai satu kotak di bawah: pembicara tampil **setengah badan di sisi kiri kotak**, teks di kanan.
- `@tampil ayah sedih` menentukan **ekspresi** Ayah saat ia bicara. Tidak ada lagi gambar tokoh besar di tengah layar
  (sejak build 48), posisi kiri/tengah/kanan boleh tetap ditulis tapi tidak berpengaruh.
- Kalimat `{nama}:` memakai gambar tokoh utama (pemain_l / pemain_p). Narasi `*` tanpa gambar. Adegan kenangan tanpa gambar.
- Orang yang terlihat di layar adalah bagian dari **gambar latar** (dibuat pemilik), mis. `@latar sarapan_keluarga`.

## Adegan waktu bebas (`data/adegan.txt` + `@kembali`)
Cerita kecil yang muncul saat waktu bebas, ketika pemain datang ke suatu tempat atau menemui seseorang. Ditulis di file
naskah tersendiri (mis. `adegan_keluarga.txt`), mulai dari sebuah label, dan **diakhiri `@kembali`**:
```
:: b2_ayah_jaring
@tampil ayah biasa
Ayah: Sini. Pegang ujungnya.
@pilihan
> Coba lagi dengan sabar -> b2aj_sabar
> "Susah, Yah." -> b2aj_nyerah
:: b2aj_sabar
@cinta ayah +3
@lompat b2aj_akhir
:: b2aj_nyerah
Ayah: Nanti itu kapan?
:: b2aj_akhir
@kembali
```
- Pemicu, jam, dan syaratnya di `data/adegan.txt` (lihat [DATA.md](DATA.md)).
- Di dalam adegan boleh `@maju`, `@latar` (gambar khusus), `@kerja_mulai`, `@hp pesan`, dll. Jangan memakai `@bebas`, `@naskah`, `@tamat`.
- Adegan bisa memindahkan pemain: `@atur _tempat = dermaga` (setelah `@kembali`, pemain berada di dermaga).
- Alur & daftar adegan desa: [CERITA.md](CERITA.md).
