# Threads of Destiny: Living Parallel

Game Android (portrait) oleh **Valerius Games**. Aplikasi Android native (Kotlin), tanpa web.

## Alur pembuka

1. **Splash** – gambar pembuka tampil 3 detik.
2. **Video intro** – transisi fade, video logo studio langsung diputar (bisa dilewati dengan mengetuk layar).
3. **Halaman utama** – setelah video selesai: judul, START GAME, CONTINUE, LOAD DATA, SETTINGS.
   CONTINUE tampil redup bertuliskan *(No Save Data Found)* selama belum ada data simpanan.

## Alur permainan (Start Game)

1. **Isi nama** pemain (dipakai di seluruh cerita lewat `{nama}`).
2. **Prolog masa lalu** – narator bercerita, background berganti per adegan dengan efek kenangan.
3. **Masa kini** – layar gelap, teks waktu, lalu **efek bangun tidur** (mata berkedip, menatap plafon).
4. **Duduk di ranjang** – kamera seolah bangkit, pemandangan kamar.
5. **HP** – pesan masuk, ikon HP bergetar; ketuk untuk membuka layar HP.

**Menu dalam game (☰ pojok kanan atas, muncul di masa kini):** Lanjutkan, Simpan, Muat,
Riwayat dialog, Pengaturan, Menu Utama. Progres juga **disimpan otomatis** saat keluar,
jadi tombol **CONTINUE** di menu utama melanjutkan dari simpanan terbaru.

**Pengaturan:** tema kotak dialog, ukuran & gaya huruf, kepekatan kotak dialog, kecepatan teks,
volume musik & efek suara, getar, lewati video intro, hapus data simpanan.

Cerita ditulis di **`app/src/main/assets/naskah/prolog.txt`**, background di
**`app/src/main/assets/latar/`**. Cara menulisnya: **[docs/NASKAH.md](docs/NASKAH.md)**.

Gambar background yang ada sekarang masih **sementara**: ganti dengan file bernama sama.

## Cara mendapatkan APK (dari HP)

Setiap ada perubahan di branch `main`, GitHub Actions otomatis membuat APK.

- **Paling mudah:** buka halaman **Releases** repo ini → **Build terbaru** → ketuk file `.apk` → install.
- **Alternatif:** tab **Actions** → pilih build terbaru → bagian **Artifacts** (berupa file zip).
- Build manual: tab **Actions** → **Build APK** → **Run workflow**.

APK selalu ditandatangani dengan kunci yang sama (`keystore/debug.keystore`),
jadi versi baru bisa langsung di-install menimpa versi lama.

## Mengganti aset

| Aset | Lokasi file |
|---|---|
| Gambar splash | `app/src/main/res/drawable-nodpi/splash.jpg` |
| Video intro | `app/src/main/res/raw/intro.mp4` |
| Background menu | `app/src/main/res/drawable-nodpi/menu_bg.jpg` |
| Ikon aplikasi | `app/src/main/res/mipmap-*/` |
| Teks tombol & versi | `app/src/main/res/values/strings.xml` |

Ganti file dengan **nama yang sama persis** (huruf kecil semua), lalu commit. Build berikutnya otomatis memakai aset baru.

## Pengaturan cepat

Di `app/src/main/java/com/valeriusgames/threadsofdestiny/MainActivity.kt`:

- `SPLASH_DURATION_MS` – lama splash (default 3000 = 3 detik)
- `SPLASH_TO_VIDEO_FADE_MS`, `VIDEO_TO_MENU_FADE_MS` – lama transisi
- `ALLOW_SKIP_VIDEO` – `true`/`false` untuk mengizinkan video dilewati

Versi aplikasi diatur di `app/build.gradle.kts` (`versionName`).

## Struktur kode

```
app/src/main/java/com/valeriusgames/threadsofdestiny/
├── MainActivity.kt   alur splash → video → menu
├── MainMenu.kt       logika halaman utama & tombol
├── FantasyButton.kt  tombol bergaya bingkai emas
├── GoldTitleView.kt  teks judul bergradasi emas
└── SparkleView.kt    partikel cahaya berkelip di langit
```

Font: Cinzel & Cinzel Decorative (SIL Open Font License, lihat `docs/FONT_LICENSE_OFL.txt`).
Desain acuan menu: `docs/menu_mockup.jpg`.
