# Prompt Gambar AI — Threads of Destiny: Living Parallel

Semua prompt ditulis dalam **bahasa Inggris** (AI gambar umumnya lebih akurat dengan bahasa Inggris).
Penjelasan dalam bahasa Indonesia. Salin bagian di dalam kotak kode.

---

## 1. Cara kerja yang disarankan

**Karakter dan background DIPISAH.**
- Karakter → PNG **transparan** (tanpa latar), lalu game yang menaruhnya di atas background.
- Background → **tanpa orang**.
- Adegan spesial (CG) → boleh gabungan, dipasang sebagai `@latar`.

**Supaya karakter konsisten di semua ekspresi:**
1. Buat dulu gambar **utama** tiap karakter (ekspresi biasa) sampai benar-benar pas.
2. Untuk ekspresi & variasi lain, **lampirkan gambar utama** lalu pakai **prompt lanjutan** di bagian **3b**.
   Prompt lanjutan hanya mengubah ekspresi/baju dan mengunci semua bagian fisik.
3. Jangan tulis ulang deskripsi fisik di prompt lanjutan.

**Latar transparan:** kebanyakan AI tidak bisa membuat PNG transparan. Minta latar **putih polos**
(sudah ada di prompt), lalu hapus latarnya dengan aplikasi penghapus background (banyak yang gratis di HP),
simpan sebagai **PNG**.

**Ukuran:**
| Jenis | Rasio | Ukuran ideal |
|---|---|---|
| Background | 9:16 (portrait) | 1080 × 1920 |
| Karakter | 3:5 atau 9:16 (portrait) | tinggi ± 1600–2000 px, badan kepala sampai paha/lutut |
| Foto profil | 1:1 | 512 × 512 (potong bagian wajah dari gambar utama) |

---

## 2. Blok gaya (tempel di SETIAP prompt)

Gaya mengikuti gambar menu utama & gambar Hana: anime lembut, cahaya hangat.

```
Style: modern Japanese anime visual novel art, clean thin lineart, soft cel shading,
warm cinematic lighting, gentle pastel colors, detailed but clean, high quality, consistent character design.
```

**Tambahan untuk karakter:**
```
Single character, waist-up to thigh-up portrait, facing slightly toward the viewer, centered,
plain pure white background, no shadow on background, no text, no watermark, full head visible with space above the head.
```

**Tambahan untuk background:**
```
Background art only, no people, no characters, no text, vertical 9:16 composition, visual novel background.
```

**Negative prompt** (kalau AI-nya punya kolom negative prompt):
```
text, watermark, logo, signature, extra fingers, deformed hands, extra limbs, blurry, cropped head,
multiple characters, busy background, realistic photo, 3d render
```

---

## 3. Karakter

Format nama file → simpan di `app/src/main/assets/karakter/`.

| File | Isi |
|---|---|
| `id.png` | Ekspresi biasa (wajib) |
| `id_senyum.png` | Senang / tersenyum |
| `id_sedih.png` | Sedih |
| `id_kesal.png` | Kesal / marah |
| `id_kaget.png` | Kaget |
| `id_foto.jpg` | Foto wajah persegi untuk panel & kontak |

**Prompt ekspresi** (tambahkan di akhir prompt karakter, ganti ekspresinya):
```
Expression: neutral calm face.                    → id.png
Expression: warm happy smile, eyes slightly closed. → id_senyum.png
Expression: sad, eyes looking down, slight frown.   → id_sedih.png
Expression: annoyed, frowning, slightly puffed cheeks. → id_kesal.png
Expression: surprised, eyes wide, mouth slightly open. → id_kaget.png
```

---

### Ayah — `ayah`
```
Indonesian man, 44 years old, fisherman from a small coastal village.
Tanned sun-weathered skin, short black hair with a few grey strands, light stubble, lean and strong build,
quiet and serious face with kind eyes.
Wearing a faded blue button-up work shirt with rolled-up sleeves, a small towel over one shoulder.
```

### Ibu — `ibu`
```
Indonesian woman, 41 years old, runs a small fried-snack food stall in a coastal village.
Warm motherly face, black hair tied in a low bun, small gold earrings, light tan skin.
Wearing a simple floral blouse in soft pink with a cream-colored apron.
```

### Laras — `laras`
(adik perempuan, 11 tahun)
```
Indonesian girl, 11 years old, cheerful and playful little sister.
Black hair in two short pigtails with straight bangs, bright lively eyes, light tan skin.
Wearing a plain orange t-shirt and simple knee-length shorts, no accessories on her wrists.
Child-appropriate, innocent, simple casual clothes.
```
> Catatan: Laras **tidak** memakai gelang benang merah (gelang itu khusus tokoh utama & sahabatnya).

### Bayu — `bayu`
(teman sekelas di desa, 15 tahun)
```
Indonesian teenage boy, 15 years old, fisherman's son, energetic and loyal friend.
Short spiky black hair, tanned skin, big friendly grin, slightly messy look.
Wearing a green t-shirt with a faded print and dark shorts, a cheap rubber wristwatch.
```

### Sekar — `sekar`
(teman sekelas di desa, juara kelas, 15 tahun)
```
Indonesian teenage girl, 15 years old, top student, serious and thoughtful.
Long straight black hair with a simple dark purple headband, thin round glasses, calm eyes, light tan skin.
Wearing a lavender cardigan over a white t-shirt, holding a book against her chest.
```

### Juragan Darto — `darto`
(juragan pengolahan ikan asin, 52 tahun)
```
Indonesian man, 52 years old, owner of a dried-fish business in a coastal village, strict and calculating but fair.
Stocky build, thick black mustache, greying hair combed back, tanned skin, sharp eyes.
Wearing a brown batik short-sleeve shirt, a gold wristwatch, a pen in his shirt pocket.
```

### Dimas — `dimas`
(teman sebangku SMA di Kota Sakala, 15 tahun)
```
Indonesian teenage boy, 15 years old, humorous and laid-back, from a crowded city neighborhood.
Messy medium black hair, playful smirk, light brown skin, earphones hanging around his neck.
Wearing an Indonesian public high school uniform: white short-sleeve shirt untucked, grey trousers, school badge on the pocket.
```

### Nadia — `nadia`
(ketua kelas SMA, anak pemilik toko buku, 15 tahun)
```
Indonesian teenage girl, 15 years old, class president, neat, strict, curious, a little lonely.
Wearing a neat white hijab, light skin, sharp intelligent eyes, a subtle confident expression.
Wearing an Indonesian public high school uniform: white long-sleeve shirt, grey long skirt, school badge on the pocket, a class president armband.
```
> Kalau Anda ingin Nadia tanpa hijab: ganti baris hijab dengan `neat shoulder-length black hair with a side part`.

### Sahabat masa kecil — `sahabat_l` (Arka) dan `sahabat_p` (Alya)
Buat **dua versi**: `sahabat_l.png` (dipakai kalau pemain perempuan) dan `sahabat_p.png` (kalau pemain laki-laki),
beserta ekspresinya: `sahabat_p_senyum.png`, `sahabat_l_sedih.png`, dst., dan `sahabat_l_foto.jpg` / `sahabat_p_foto.jpg`.

**Alya (perempuan):**
```
Indonesian teenage girl, 15 years old, gentle, quiet and melancholic, carrying a heavy family secret.
Long dark brown-black hair loose with soft bangs, slightly tired gentle eyes, light tan skin.
Wearing a simple light blue oversized cardigan over a white t-shirt.
IMPORTANT DETAIL: a thin bracelet made of red thread tied on her left wrist, clearly visible.
```

**Arka (laki-laki):**
```
Indonesian teenage boy, 15 years old, gentle, quiet and melancholic, carrying a heavy family secret.
Slightly long dark hair with bangs partly covering his eyes, soft sad eyes, light tan skin.
Wearing a simple dark navy hoodie over a white t-shirt.
IMPORTANT DETAIL: a thin bracelet made of red thread tied on his left wrist, clearly visible.
```

### Tokoh utama — `pemain_l` (laki-laki) dan `pemain_p` (perempuan)
Tidak tampil di adegan (layar = mata tokoh). Fotonya dipakai di **halaman isi nama** (saat memilih jenis kelamin),
**Status**, dan **Kontak → Saya**. Cukup kirim gambar badannya; foto `pemain_l_foto.jpg` / `pemain_p_foto.jpg` dipotong otomatis.
Lampirkan gambar Ayah sebagai acuan gaya.

**Laki-laki (`pemain_l.png`):**
```
Match the exact art style, lineart, coloring and shading of the attached image, but draw a completely different person as described below.
Indonesian teenage boy, 15 years old, just graduated from junior high school, from a small coastal fishing village.
Short messy black hair, determined but kind eyes with a hint of longing, light tan sun-kissed skin, slim build.
Wearing a plain white t-shirt under an open short-sleeve dark green shirt, simple dark jeans.
IMPORTANT DETAIL: a thin bracelet made of red thread tied on his left wrist, clearly visible.
Expression: neutral calm face with a slight hopeful smile.
Single character, waist-up to thigh-up portrait, facing slightly toward the viewer, centered, plain pure white background, no text, full head visible with space above the head.
```

**Perempuan (`pemain_p.png`):**
```
Match the exact art style, lineart, coloring and shading of the attached image, but draw a completely different person as described below.
Indonesian teenage girl, 15 years old, just graduated from junior high school, from a small coastal fishing village.
Shoulder-length black hair tied in a low half ponytail, determined but kind eyes with a hint of longing, light tan sun-kissed skin.
Wearing a plain white t-shirt under an open short-sleeve dark green shirt, simple dark jeans, modest casual clothes.
IMPORTANT DETAIL: a thin bracelet made of red thread tied on her left wrist, clearly visible.
Expression: neutral calm face with a slight hopeful smile.
Single character, waist-up to thigh-up portrait, facing slightly toward the viewer, centered, plain pure white background, no text, full head visible with space above the head.
```

### Sosok misterius — `sosok`
(dipakai di adegan Stasiun Besar, terlihat dari belakang)
```
A teenage figure seen from behind in a crowded train station, face not visible, wearing a hood,
left wrist with a thin red thread bracelet clearly visible, slightly blurred, mysterious mood,
plain pure white background.
```

---

## 3b. Prompt lanjutan DENGAN GAMBAR REFERENSI (untuk ekspresi & variasi)

**Alurnya:**
1. Buat **gambar utama** dengan prompt karakter di atas (+ blok gaya + tambahan karakter + `Expression: neutral calm face.`).
2. Kalau sudah pas, **simpan** gambar itu. Inilah "gambar referensi" karakter tersebut.
3. Untuk setiap ekspresi / variasi: **lampirkan gambar referensi**, lalu kirim **salah satu prompt di bawah**.
   Jangan tulis ulang deskripsi fisik karakter (supaya AI tidak "menafsirkan ulang" wajahnya).
4. Kalau hasilnya tetap berubah, ulangi dengan menambahkan kalimat penguat (bagian **Penguat** di bawah).

### Kalimat pengunci (selalu ada di depan prompt lanjutan)
```
Use the attached image as the exact reference for this character.
Keep EVERYTHING identical: same face shape, same eyes and eye color, same nose and mouth shape,
same hairstyle, hair length and hair color, same skin tone, same body proportions and height,
same outfit, same colors, same accessories, same art style, same lineart and shading,
same framing, same camera angle, same plain pure white background.
Change ONLY what is described below.
```

### Ekspresi
Tempel kalimat pengunci, lalu SATU baris berikut:

| Simpan sebagai | Prompt |
|---|---|
| `id_senyum.png` | `Change only the facial expression to a warm happy smile, eyes slightly closed, cheeks lightly raised.` |
| `id_sedih.png` | `Change only the facial expression to sad: eyebrows tilted up in the middle, eyes looking down, small frown.` |
| `id_kesal.png` | `Change only the facial expression to annoyed: eyebrows lowered, slight frown, lips pressed together.` |
| `id_kaget.png` | `Change only the facial expression to surprised: eyes wide open, eyebrows raised, mouth slightly open.` |
| `id_malu.png` *(opsional)* | `Change only the facial expression to shy: soft blush on cheeks, eyes looking away to the side, small smile.` |
| `id_menangis.png` *(opsional)* | `Change only the facial expression to crying quietly: teary eyes, a single tear on one cheek, trembling lips.` |

**Contoh lengkap (Bayu senyum):**
```
[lampirkan bayu.png]

Use the attached image as the exact reference for this character.
Keep EVERYTHING identical: same face shape, same eyes and eye color, same nose and mouth shape,
same hairstyle, hair length and hair color, same skin tone, same body proportions and height,
same outfit, same colors, same accessories, same art style, same lineart and shading,
same framing, same camera angle, same plain pure white background.
Change ONLY what is described below.

Change only the facial expression to a warm happy smile, eyes slightly closed, cheeks lightly raised.
```

### Foto profil (`id_foto.jpg`)
Cara termudah: **potong (crop)** wajah dari gambar utama menjadi persegi, tanpa AI.
Kalau ingin dibuat AI:
```
[kalimat pengunci]
Create a square close-up portrait of the same character: head and shoulders only, centered,
same neutral expression, soft light pastel background.
```

### Ganti baju (mis. Bayu pakai seragam SMA, atau baju tidur), fisik tetap sama
```
[kalimat pengunci — tapi hapus "same outfit, same colors, same accessories"]
Keep the face, hair, skin tone and body exactly the same.
Change only the clothes to: <tulis bajunya, mis. Indonesian public high school uniform, white short-sleeve shirt, grey trousers>.
```
Simpan dengan nama ekspresi khusus, mis. `bayu_seragam.png`, lalu pakai di naskah: `@tampil bayu seragam`.

### Karakter tumbuh dewasa (untuk bab usia 18+)
```
[lampirkan gambar utama]
Use the attached image as the reference for this character's identity.
Show the SAME person 3 years older (18 years old): keep the same face identity, eye shape, eye color,
hair color, skin tone and overall look so they are clearly recognizable as the same person,
only slightly more mature facial features and taller proportions.
Outfit: <tulis bajunya>. Same art style, plain pure white background.
```
Simpan sebagai gambar baru (mis. `sahabat_p_dewasa.png`) — gambar versi dewasa akan dipakai lewat naskah nanti.

### Penguat (kalau hasilnya masih berubah)
Tambahkan di akhir:
```
This is an edit of the attached image, not a new character. Do not redesign the character.
If unsure, keep the original pixels as much as possible and only modify the face.
```
Tips tambahan:
- Kalau AI-nya punya pilihan **"edit area" / "inpaint"**, tandai **hanya bagian wajah** lalu tulis baris ekspresinya saja → paling konsisten.
- Kalau ada pengaturan **kekuatan referensi** / *image strength*, pasang tinggi (± 0,7–0,85).
- Selalu bandingkan berdampingan dengan gambar utama sebelum disimpan.

---

## 4. Background

Simpan di `app/src/main/assets/latar/` dengan **nama file persis** seperti di tabel (format `.jpg`).
Tempel **blok gaya** + **tambahan background** di setiap prompt.

| File | Prompt |
|---|---|
| `kota_kecil.jpg` | `A small Indonesian fishing village on the coast at sunset, wooden houses with clay tile roofs, an old white lighthouse at the tip of a cape, a long wooden pier stretching into calm golden sea, fishing boats, warm orange and pink sky, nostalgic mood.` |
| `sekolah.jpg` | `Inside a modest Indonesian village junior high school classroom, wooden desks and chairs, large windows with afternoon sunlight, view of coconut trees outside, chalkboard, nostalgic warm memory mood.` |
| `stasiun_hujan.jpg` | `A small rural Indonesian train station platform in heavy rain at dusk, a train with lit windows about to depart, puddles reflecting lamp light, melancholic lonely mood.` |
| `stasiun_desa.jpg` | `A small rural Indonesian train station platform in the morning, sign reading "TANJUNG LENTERA", old wooden benches, green hills and a glimpse of the sea in the distance, clear blue sky.` |
| `plafon.jpg` | `View looking straight up at the ceiling of a simple Indonesian village bedroom while lying in bed, wooden ceiling planks, a single round lamp, soft morning light coming from a window, slightly dreamy.` |
| `kamar_pagi.jpg` | `A simple Indonesian village bedroom in the morning, single bed with a thin mattress and floral bedsheet, wooden window with sunlight streaming through thin curtains, small desk with school books, a calendar on the wall.` |
| `rumah_warung.jpg` | `Front of a small Indonesian village house with a tiny fried-snack food stall (warung) in the morning, wooden table with trays of fried bananas and snacks, plastic stools, sea visible down the road, warm cozy atmosphere.` |
| `gudang_ikan.jpg` | `A traditional Indonesian dried-fish processing yard by the sea, rows of bamboo racks with salted fish drying under bright sun, a simple wooden warehouse, baskets and barrels, hot sunny day.` |
| `kota_besar.jpg` | `A huge modern Indonesian metropolis at sunset seen from above, skyscrapers, elevated highways with traffic lights, commuter train line, golden-pink sky, sense of awe and loneliness.` (bisa juga tetap pakai gambar menu utama) |
| `kos_kamar.jpg` | `A tiny cramped rented room (kos) in a dense Indonesian city neighborhood, 2x3 meters, thin mattress on the floor, standing fan, small window with a view of train tracks and wires, a bag still unpacked, evening lamp light.` |
| `kelas_sma.jpg` | `A bright Indonesian public high school classroom, rows of desks, whiteboard with "Selamat datang, kelas X-3" written in marker, ceiling fans, windows with city buildings outside, morning light.` |
| `ruang_makan.jpg` | `Interior of a simple Indonesian fishing-village house dining room in the early morning, eye-level view, a low wooden dining table with a woven mat, plates of rice, fried fish, sambal and a teapot, four simple wooden chairs, plain whitewashed walls with a family photo and a wall calendar, a doorway to a small kitchen on one side, soft golden sunlight from a wooden window, open empty space in the center for characters, warm cozy family mood, no people.` (sertakan `kamar_pagi.jpg` / `rumah_warung.jpg` sebagai referensi gaya) |

**Background yang akan dibutuhkan bab berikutnya** (boleh dibuat dari sekarang):
| File | Prompt |
|---|---|
| `stasiun_besar.jpg` | `A huge busy central train station in an Indonesian metropolis at evening rush hour, crowded platforms (people as soft blurred silhouettes only), big departure boards, warm lights, steel roof structure.` |
| `toko_buku.jpg` | `A cozy small bookstore in front of a big city train station, wooden shelves full of books, warm lamp light, rainy window with station lights outside.` |
| `taman_kota.jpg` | `A city park in Indonesia at dusk, benches, street lamps turning on, trees, distant skyscrapers, couples and joggers only as tiny silhouettes far away.` |
| `minimarket.jpg` | `Interior of a small Indonesian convenience store at night, bright fluorescent lights, shelves of snacks and drinks, cashier counter, glass door showing a dark street.` |
| `dermaga_malam.jpg` | `The old wooden pier of a small fishing village at night, the dark silent lighthouse, starry sky reflected in calm water, a red thread bracelet lying on the wooden planks.` |

---

## 5. Adegan spesial (CG, gambar gabungan) — opsional

Dipasang dengan `@latar nama_file`. Gambar karakter tidak perlu ditampilkan di atasnya.

| File | Prompt |
|---|---|
| `cg_dermaga_janji.jpg` | `Two 9-year-old children sitting side by side on an old wooden pier at sunset, seen from behind, a white lighthouse nearby, their pinky fingers linked in a promise, both wearing thin red thread bracelets, warm nostalgic glow, childhood memory.` |
| `cg_peron_kereta.jpg` | `Crowded central train station platform at evening, a teenager seen from behind stepping into a train as the doors close, a thin red thread bracelet on the left wrist, motion blur of the crowd, emotional cinematic moment.` |

---

## 6. Setelah gambar jadi

1. Ganti nama file persis seperti tabel di atas (huruf kecil semua).
2. Upload ke folder yang sesuai di GitHub (dari HP: buka folder → **Add file → Upload files**).
3. Commit. Build berikutnya otomatis memakai gambar baru. Pemeriksa naskah juga mengecek nama latar yang dipakai naskah.
4. Gambar sementara yang lama otomatis tertimpa bila nama filenya sama.
