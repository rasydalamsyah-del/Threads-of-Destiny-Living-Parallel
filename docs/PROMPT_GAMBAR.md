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

### Tokoh tambahan desa (rancangan Bab 1–2 baru) — `karta`, `usman`, `yati`, `haji`, `wati`
Ekspresi: cukup `senyum` (Pak Karta & Bu Wati + `sedih`). Prioritas: karta ★, usman ★, yati ★, haji ☆, wati ○.

Pak Karta — bapak Bayu (`karta.png`)
```
Indonesian fisherman, 46 years old, father of five, hardworking and kind but tired, gentle smile.
Lean wiry build, deeply sun-tanned skin, short black hair with some grey, light stubble, rough hands.
Wearing a faded orange long-sleeve fishing shirt with sleeves rolled up, a checkered towel around his neck.
```
Pak Usman — buruh senior gudang (`usman.png`)
```
Indonesian old laborer, 62 years old, senior worker at a dried-fish warehouse, wise and humorous.
Thin and bony, dark tanned wrinkled skin, short white hair, white stubble, a few missing teeth when smiling, a clove cigarette tucked behind his ear.
Wearing a worn white singlet under an open faded blue short-sleeve shirt, trousers rolled up to the knees.
```
Mbak Yati — buruh gudang (`yati.png`)
```
Indonesian woman, 30 years old, cheerful dried-fish warehouse worker, talkative and playful.
Small slim build, tanned skin, black hair tied in a low bun, bright lively eyes, a wide woven bamboo farmer's hat (caping) hanging on her back.
Wearing a long-sleeve pink floral blouse, an apron stained with salt, rubber gloves tucked in the apron pocket.
```
Pak Haji Somad — tetua desa (`haji.png`)
```
Indonesian elderly man, 68 years old, respected village elder, calm and talkative storyteller.
Slightly plump, light brown skin, short neat white beard, kind wrinkled eyes behind old reading glasses.
Wearing a white peci (skullcap), a light grey koko shirt, a checkered sarong, prayer beads in one hand.
```
Bu Wati — ibu Sekar (`wati.png`)
```
Indonesian woman, 43 years old, widowed seamstress, gentle, tired and proud of her daughter.
Thin build, light tan skin, kind face with faint dark circles, squinting slightly because her eyesight is weakening.
Wearing a simple dusty-blue headscarf (kerudung), a long-sleeve brown blouse, a measuring tape around her neck.
```

### Tokoh tambahan lain — `rt`, `kades`, `mantri`, `rani`, `dodi`, `dino`, `raka`
Ekspresi: cukup gambar biasa (+ `senyum` bila sering muncul).

Pak RT Sobari (`rt.png`)
```
Indonesian man, 50 years old, village neighborhood head (RT), cheerful, busy and a bit talkative.
Medium build with a small belly, light brown skin, short black hair, thick mustache, friendly wide smile.
Wearing a light khaki government-style short-sleeve shirt with a pen in the pocket, a black peci, holding a rolled paper announcement.
```
Pak Kades (`kades.png`)
```
Indonesian man, 56 years old, village head of a coastal fishing village, dignified, idealistic, a little tired.
Tall and slim, tanned skin, neatly combed grey hair, thin glasses, calm determined eyes.
Wearing a neat light brown batik long-sleeve shirt, dark trousers, a small village emblem pin on his chest.
```
Mantri Joko (`mantri.png`)
```
Indonesian village health worker (mantri), 40 years old, calm, kind and serious.
Average build, light brown skin, short neat black hair, clean-shaven, gentle eyes.
Wearing a white short-sleeve shirt with a name tag, a stethoscope around his neck, carrying a worn brown leather medical bag.
```
Rani (`rani.png`)
```
Indonesian teenage girl, 15 years old, cheerful and confident classmate who is moving to the city for high school.
Shoulder-length wavy black hair with a small hair clip, bright smile, light tan skin.
Wearing a pastel yellow blouse and a denim jacket, a backpack strap on one shoulder.
```
Dodi (`dodi.png`)
```
Indonesian teenage boy, 15 years old, clumsy and emotional classmate who is moving to the city for high school.
Chubby round face, short buzz-cut black hair, light brown skin, slightly teary eyes but smiling.
Wearing a red polo shirt and carrying a big travel bag.
```
Dino (`dino.png`)
```
Indonesian boy, 11 years old, elementary school student, mischievous but not evil, a fisherman's son.
Small skinny build, tanned skin, spiky short black hair, a band-aid on his knee, a cheeky grin.
Wearing a red-and-white Indonesian elementary school uniform, untucked shirt, red shorts.
```
Raka muda (`raka.png`, seperti di foto lama Darto: ± 20 tahun lalu)
```
Indonesian young man, 18 years old, son of a dried-fish boss in a coastal village, hopeful and a little restless.
Slim build, tanned skin, short black hair, faint mustache, warm smile resembling his father's sharp eyes.
Wearing a simple white t-shirt under an open checkered flannel shirt, holding a small turtledove (perkutut) on his hand.
```

### Tokoh baru Bab 4 — `wulan` (desa) dan tokoh kota `ratmi`, `hartini`, `bram`, `rahma`, `reno`, `tiara`, `sum`, `hendra`, `tini`, `raka_dewasa`
Tempel blok gaya + tambahan karakter (bagian 2), lalu deskripsi di bawah. Ekspresi minimal `senyum`; yang punya adegan sedih/marah tambah ekspresinya
(tertulis di tiap tokoh).

Kak Wulan — tutor Paket C di PKBM kecamatan (`wulan.png`) · + `senyum`
```
Indonesian young woman, 24 years old, volunteer tutor at a community learning center, patient, warm and encouraging.
Slim build, light tan skin, shoulder-length straight black hair tucked behind one ear, round friendly face, small silver earrings.
Wearing a white blouse under a light green cardigan, a lanyard with a name card, holding a stack of worn textbooks.
```
Bu Ratmi — ibu kos Kampung Rel Utara (`ratmi.png`) · + `senyum`, `kesal`
```
Indonesian woman, 58 years old, landlady of a cheap boarding house beside the railway, chatty and bossy but kind-hearted, retired railway clerk's widow.
Short plump build, tanned skin, permed short greying hair, reading glasses hanging on a beaded chain, a faint laugh line.
Wearing a colorful batik daster (house dress), plastic sandals, a bunch of keys clipped at her waist.
```
Bu Hartini — ibu kos Melati, kawasan Cendana (`hartini.png`) · + `senyum`, `kesal`
```
Indonesian woman, 52 years old, strict landlady of a clean student boarding house, disciplined and proper, rarely smiles but fair.
Tall thin build, light skin, neat hair in a tight bun, thin eyebrows, sharp eyes.
Wearing a pressed light purple long-sleeve blouse, a long dark skirt, a small notebook of house rules in her hand.
```
Mas Bram — resepsionis Sakala Residence (`bram.png`) · + `senyum`
```
Indonesian young man, 27 years old, receptionist of an upscale boarding residence, polite, smooth-talking and calculating.
Average build, light brown skin, neatly styled short hair with pomade, clean-shaven, professional smile.
Wearing a fitted navy blazer over a white shirt, a name badge "BRAM", holding a tablet.
```
Bu Rahma — wali kelas X-3, guru Bahasa Indonesia (`rahma.png`) · + `senyum`, `kesal`, `sedih`
```
Indonesian female teacher, 45 years old, homeroom teacher, firm but caring, observant.
Medium build, light tan skin, kind but serious face, thin rectangular glasses.
Wearing a dark green hijab and an Indonesian civil-servant teacher uniform (khaki long-sleeve shirt with name tag), holding a register book.
```
Reno — teman sekelas, anak orang kaya (`reno.png`) · + `senyum`, `kesal`, `malu`
```
Indonesian teenage boy, 15 years old, rich classmate, show-off and loud but not cruel, secretly insecure.
Tall, light skin, trendy undercut hairstyle, smirk, an expensive smartwatch on his wrist.
Wearing an Indonesian public high school uniform (white short-sleeve shirt, grey trousers) with branded sneakers and a designer backpack strap.
```
Kak Tiara — ketua OSIS, pemandu MPLS (`tiara.png`) · + `senyum`, `kesal`
```
Indonesian teenage girl, 17 years old, student council president, energetic, bossy during orientation but kind.
Medium height, light tan skin, high ponytail, confident eyes, a whistle on a lanyard.
Wearing an Indonesian high school uniform (white short-sleeve shirt, grey skirt) with a dark blue student council armband, holding a clipboard.
```
Bu Sum — ibu Dimas, penjual nasi uduk (`sum.png`) · + `senyum`, `sedih`
```
Indonesian woman, 44 years old, single mother selling coconut rice (nasi uduk) by the railway, tired but warm and proud of her son.
Thin build, tanned skin, faint wrinkles, gentle smile.
Wearing a simple maroon headscarf, a faded floral blouse, an apron, holding a wooden rice paddle.
```
Pak Hendra — ayah Nadia, pemilik toko buku (`hendra.png`) · + `senyum`, `sedih`
```
Indonesian man, 50 years old, owner of an old bookshop in front of the central station, quiet, gentle, a little lonely.
Slim build, light skin, greying hair parted to the side, thick-rimmed glasses, a pencil behind his ear.
Wearing a brown knitted vest over a white long-sleeve shirt, sleeves rolled, holding an old hardcover book.
```
Bu Tini — pemilik warung makan (`tini.png`) · + `senyum`
```
Indonesian woman, 55 years old, owner of a busy cheap food stall, loud, motherly and generous to poor students.
Stout build, tanned skin, round cheerful face, hair tied back with a scarf.
Wearing a red floral blouse, an oil-stained apron, a ladle in her hand.
```
Bang Raka — anak Juragan Darto, kini mekanik bengkel kapal Pelabuhan (`raka_dewasa.png`) · + `sedih`, `kaget`
(lampirkan `raka.png` dan `darto.png` sebagai referensi wajah: harus terlihat seperti Raka muda 20 tahun kemudian, mirip ayahnya)
```
Indonesian man, 38 years old, ship-engine mechanic at a harbor workshop, quiet, ashamed, hard-working, carrying a heavy past.
Stocky build, sun-darkened skin, short black hair with early grey, a short beard, sharp eyes like his father, grease on his forearms.
Wearing a stained dark blue mechanic's coverall with the top half tied around his waist, a grey t-shirt, a small turtledove tattoo on his forearm.
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

### Daftar ekspresi per karakter (checklist)
✅ **Sudah terpasang (build 34):** ayah (senyum, sedih, kesal, kaget) · ibu (senyum, sedih, kesal, kaget, menangis) · laras (sedih, kesal, kaget, menangis) · bayu (senyum, sedih, kesal, kaget, malu) · sekar (semua 6) · darto (senyum, sedih, kesal, kaget) · dimas (senyum, sedih, kesal, kaget) · nadia (senyum, sedih, kesal, kaget, malu) · pemain_l/p (semua 6, disimpan). **Build 35:** sahabat_l (Arka) & sahabat_p (Alya) semua 6, bayu_malu. Semua ekspresi utama sudah lengkap.

> Catatan nama: `sahabat_l` = versi **laki-laki** (Arka), `sahabat_p` = versi **perempuan** (Alya). Game memilih yang berlawanan jenis dengan tokoh utama.

Prioritas: ★ = dipakai sistem ngobrol/hadiah untuk semua orang (😊 saat senang, 😠 saat topik/hadiah dibenci) atau sudah dipakai cerita · ☆ = untuk adegan cerita berikutnya · ○ = opsional.
Ekspresi yang belum ada otomatis memakai gambar biasa (tidak error).

| Karakter | senyum | sedih | kesal | kaget | malu | menangis |
|---|---|---|---|---|---|---|
| ayah | ★ | ☆ (Ibu sakit, hutang perahu) | ★ (tegas/marah) | ☆ | — | — |
| ibu | ★ | ★ (sudah dipakai) | ★ (cerewet) | ☆ | — | ☆ (anak pergi ke kota) |
| laras | — (gambar biasa sudah tersenyum lebar) | ★ (sudah dipakai) | ★ (cemberut/ngambek) | ☆ | — | ☆ |
| bayu | ★ (sudah dipakai) | ☆ (utang bapaknya) | ★ | ☆ | ☆ (soal Sekar) | — |
| sekar | ★ | ☆ | ★ | ☆ | ☆ | ☆ (cerita di stasiun) |
| darto | ★ (senyum tipis) | ○ (kisah anaknya) | ★ | ☆ | — | — |
| dimas | ★ (sudah dipakai) | ☆ (rumah digusur) | ★ | ☆ | — | — |
| nadia | ★ (sudah dipakai) | ☆ (ibunya di luar negeri) | ★ | ☆ | ☆ | — |
| sahabat_l / sahabat_p | ★ | ★ | ○ | ★ (bertemu lagi) | ☆ | ★ |
| sosok | — | — | — | — | — | — |

Nama file: `id_ekspresi.png`, contoh `ayah_kesal.png`, `sahabat_p_menangis.png`. Selalu lampirkan **gambar biasa karakter itu** (`ayah.png`, `sahabat_p.png`, dst.).

**Prompt lengkap per ekspresi** (sama untuk semua karakter, yang beda hanya gambar yang dilampirkan):

`id_senyum.png`
```
Use the attached image as the exact reference for this character. Keep EVERYTHING identical: same face shape, same eyes and eye color, same nose and mouth, same hairstyle and hair color, same skin tone, same body proportions, same outfit and colors, same accessories, same art style, same framing, same camera angle, same plain pure white background. This is an edit of the attached image, not a new character. Change ONLY the facial expression to: a warm happy smile, eyes slightly closed, cheeks lightly raised.
```
Khusus Darto (juragan yang jarang tersenyum) ganti akhir kalimat: `a thin satisfied smile on one side of the mouth, eyes calm and sharp.`

`id_sedih.png`
```
Use the attached image as the exact reference for this character. Keep EVERYTHING identical: same face shape, same eyes and eye color, same nose and mouth, same hairstyle and hair color, same skin tone, same body proportions, same outfit and colors, same accessories, same art style, same framing, same camera angle, same plain pure white background. This is an edit of the attached image, not a new character. Change ONLY the facial expression to: sad, eyebrows tilted up in the middle, eyes looking down, small frown, lips pressed together.
```

`id_kesal.png`
```
Use the attached image as the exact reference for this character. Keep EVERYTHING identical: same face shape, same eyes and eye color, same nose and mouth, same hairstyle and hair color, same skin tone, same body proportions, same outfit and colors, same accessories, same art style, same framing, same camera angle, same plain pure white background. This is an edit of the attached image, not a new character. Change ONLY the facial expression to: annoyed, eyebrows lowered and drawn together, slight frown, lips pressed together.
```
Khusus Laras (anak-anak) ganti akhir kalimat: `pouting like a sulking child, cheeks puffed, lips pushed out, eyebrows lowered.`

`id_kaget.png`
```
Use the attached image as the exact reference for this character. Keep EVERYTHING identical: same face shape, same eyes and eye color, same nose and mouth, same hairstyle and hair color, same skin tone, same body proportions, same outfit and colors, same accessories, same art style, same framing, same camera angle, same plain pure white background. This is an edit of the attached image, not a new character. Change ONLY the facial expression to: surprised, eyes wide open, eyebrows raised high, mouth slightly open.
```

`id_malu.png`
```
Use the attached image as the exact reference for this character. Keep EVERYTHING identical: same face shape, same eyes and eye color, same nose and mouth, same hairstyle and hair color, same skin tone, same body proportions, same outfit and colors, same accessories, same art style, same framing, same camera angle, same plain pure white background. This is an edit of the attached image, not a new character. Change ONLY the facial expression to: shy and embarrassed, soft pink blush on the cheeks, eyes looking away to the side, small awkward smile.
```

`id_menangis.png`
```
Use the attached image as the exact reference for this character. Keep EVERYTHING identical: same face shape, same eyes and eye color, same nose and mouth, same hairstyle and hair color, same skin tone, same body proportions, same outfit and colors, same accessories, same art style, same framing, same camera angle, same plain pure white background. This is an edit of the attached image, not a new character. Change ONLY the facial expression to: crying quietly, teary glistening eyes, tears running down both cheeks, eyebrows raised in the middle, trembling lips.
```

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

### Stasiun dengan kereta (edit dari gambar yang sudah ada)
Lampirkan gambar stasiunnya sendiri, lalu kirim prompt. Simpan dengan **nama file yang sama** (menggantikan yang lama).

`stasiun_desa.jpg` (lampirkan `stasiun_desa.jpg`) — dipakai pagi hari saat berangkat ke Kota Sakala:
```
Use the attached image as the exact reference background. Keep EVERYTHING identical: same composition, same camera angle, same platform, same "TANJUNG LENTERA" sign, same benches, same hills and sea in the distance, same morning sky and lighting, same art style. This is an edit of the attached image, not a new picture. ADD ONLY: a small old Indonesian passenger train arriving at the platform from the distance, slowing down on the track beside the platform, its headlight on, a little dust and wind around the wheels. Keep the train on the side of the image along the track, and keep the platform in the center empty and open. No people, no text other than the existing sign, vertical 9:16.
```

`stasiun_besar.jpg` (lampirkan `stasiun_besar.jpg`) — dipakai Sabtu sore di Bab 3:
```
Use the attached image as the exact reference background. Keep EVERYTHING identical: same composition, same camera angle, same platforms, same steel roof, same departure boards, same evening warm lighting, same art style. This is an edit of the attached image, not a new picture. ADD ONLY: a modern Indonesian commuter train arriving at one platform, slowing down with its headlights glowing, light reflections on its windows and on the floor. The crowd stays as soft blurred silhouettes far away. Keep the train on one side of the image, and keep the platform floor in the front center empty and open. No clear faces, no readable text, vertical 9:16.
```

### Kamar kos (3 jenis, sesuai kos yang dipilih pemain)
Game otomatis memakai gambar kos yang disewa; kalau belum ada, memakai `kos_kamar.jpg`.
Tempel blok gaya + tambahan background, lalu prompt di bawah. Lampirkan `kos_kamar.jpg` sebagai **referensi gaya** (bukan isi).

`kos_rel_utara.jpg` — Kos Bu Ratmi, Rp400.000 (murah, bising, kereta lewat tiap jam):
```
Style: modern Japanese anime visual novel art, clean thin lineart, soft cel shading, warm cinematic lighting, gentle pastel colors, detailed but clean, high quality. Background art only, no people, no characters, no text, vertical 9:16 composition, visual novel background. Use the attached image only as a style reference. A tiny cramped cheap rented room (kos) in a dense Indonesian city neighborhood next to railway tracks, about 2x3 meters, eye-level view from the doorway, a thin mattress on the floor with a faded blanket, an old standing fan, peeling painted walls with small cracks and water stains, a single bare bulb hanging from the ceiling, clothes hanging on a nail, a small window with rusty bars showing railway tracks and tangled electric wires very close outside, a travel bag still unpacked on the floor, evening light mixed with a dim yellow lamp, humble but hopeful mood, open empty floor space in the center.
```

`kos_cendana.jpg` — Kos Melati, Rp650.000 (bersih, tenang, dekat sekolah):
```
Style: modern Japanese anime visual novel art, clean thin lineart, soft cel shading, warm cinematic lighting, gentle pastel colors, detailed but clean, high quality. Background art only, no people, no characters, no text, vertical 9:16 composition, visual novel background. Use the attached image only as a style reference. A clean neat modest rented room (kos) in a quiet Indonesian city residential area, about 3x3 meters, eye-level view from the doorway, a simple single bed with a light blue bedsheet, a small wooden study desk with a desk lamp, a plastic wardrobe, a wall clock, light cream painted walls, tiled floor, a window with clean white curtains showing a quiet street with trees and neighboring houses, a small sign on the wall about curfew at 10 pm, a travel bag still unpacked on the floor, soft evening light and a warm desk lamp, calm tidy mood, open empty floor space in the center.
```

`kos_pusat.jpg` — Sakala Residence, Rp1.300.000 (mewah):
```
Style: modern Japanese anime visual novel art, clean thin lineart, soft cel shading, warm cinematic lighting, gentle pastel colors, detailed but clean, high quality. Background art only, no people, no characters, no text, vertical 9:16 composition, visual novel background. Use the attached image only as a style reference. A modern exclusive boarding room (kos) in the center of a big Indonesian city, spacious about 4x5 meters, eye-level view from the doorway, a queen-size bed with white sheets, an air conditioner on the wall, a wooden work desk with a chair, a built-in wardrobe, a small fridge, a glass door to a private bathroom, warm LED strip lighting, a large window showing skyscrapers and city lights at dusk, a travel bag still unpacked on the floor looking out of place, luxurious but lonely mood, open empty floor space in the center.
```

---

### Versi latar per waktu (opsional, lebih bagus dari pewarnaan otomatis)
Game sudah mewarnai latar sesuai jam secara otomatis. Tapi untuk tempat yang paling sering dipakai, versi asli per waktu
jauh lebih bagus (lampu menyala, bintang, bayangan panjang). Lampirkan gambar aslinya, lalu kirim prompt. Simpan dengan
nama `<nama>_<waktu>.jpg` — gambar asli JANGAN diganti.

Kalimat pengunci (tempel di depan setiap prompt):
```
Use the attached image as the exact reference background. Keep EVERYTHING identical: same composition, same camera angle, same buildings, furniture and objects in the same places, same art style. This is an edit of the attached image, not a new picture. Change ONLY the time of day and lighting as described. No people, no text, vertical 9:16.
```

| Simpan sebagai (lampirkan) | Tambahan prompt |
|---|---|
| `rumah_warung_sore.jpg` (rumah_warung.jpg) | `Late afternoon around 5 pm: warm orange sunlight from a low sun, long soft shadows on the road, golden sky over the sea, the lighthouse glowing in the sunset light.` |
| `rumah_warung_malam.jpg` (rumah_warung.jpg) | `Night around 8 pm: dark blue starry sky, a warm yellow bulb lit under the warung roof, soft lamp glow on the table and stools, the lighthouse dark in the distance, dim moonlight on the road, houses with lit windows.` |
| `kamar_pagi_malam.jpg` (kamar_pagi.jpg) | `Night: dark blue night outside the window, curtains half closed, a small warm desk lamp turned on, soft warm light on the bed and desk, the rest of the room in gentle shadow.` |
| `ruang_makan_malam.jpg` (ruang_makan.jpg) | `Night dinner time: dark night outside the window with a few village lights, the rattan ceiling lamp turned on with warm yellow light, warm glow on the dining table, cozy family dinner mood.` |
| `ruang_makan_siang.jpg` (ruang_makan.jpg) | `Midday: bright neutral daylight through the window, clear blue sky outside, short shadows, fresh lunch time mood.` |
| `kos_kamar_pagi.jpg` (kos_kamar.jpg) | `Early morning: pale blue morning light through the small window, the lamp turned off, soft cool daylight on the mattress, train tracks outside in morning haze.` |
| `gudang_ikan_sore.jpg` (gudang_ikan.jpg) | `Late afternoon: warm orange low sunlight, long shadows from the drying racks, golden reflections on the sea.` |
| `kota_kecil_malam.jpg` (kota_kecil.jpg) | `Night: dark blue sky full of stars, moon reflected on the calm sea, warm lit windows in the wooden houses, small lamps along the pier, the old lighthouse dark.` |

Urutan prioritas: rumah_warung_malam → kamar_pagi_malam → ruang_makan_malam → rumah_warung_sore → sisanya.

---

### Rumah desa: kamar → ruang tengah → ruang makan (set baru, saling terhubung)
Tata letak: kamar tokoh utama punya pintu ke ruang tengah. Ruang tengah punya pintu kamar, pintu depan (ke warung),
dan lorong terbuka tanpa pintu ke ruang makan di belakangnya. Simpan sebagai `kamar_pagi.jpg` (mengganti),
`ruang_tengah.jpg` (baru), `ruang_makan.jpg` (mengganti). Lampirkan gambar latar lama sebagai referensi gaya agar serasi.
Prompt lengkapnya ada di percakapan 19 Okt 2026 dan disalin di bawah.

**1. `kamar_pagi.jpg`**
```
Style: modern Japanese anime visual novel art, clean thin lineart, soft cel shading, warm cinematic lighting, gentle pastel colors, detailed but clean, high quality. Background art only, no people, no characters, no text, vertical 9:16 aspect ratio (1080x1920), visual novel background, eye-level camera at standing height.
A simple, cozy bedroom of a 15-year-old in a modest Indonesian fishing-village house, early morning around 6 am. The house style: whitewashed plaster walls slightly worn, dark brown wooden ceiling beams and wooden planks, warm brown wooden door and window frames, light cream ceramic tile floor.
LEFT side: a single bed with a thin mattress and a blue floral bedsheet, slightly messy blanket, a pillow; a small wooden shelf above it with a few books and a potted plant.
BACK wall center-left: a wooden window with louvered shutters open, thin white curtains, soft golden morning sunlight streaming in, palm trees and village roofs visible outside.
RIGHT side, clearly visible and fully inside the frame (around 65–75% of the image width, NOT at the very edge): a warm brown wooden door, half open, through which a glimpse of the living room is visible (part of an old sofa and a wall with a family photo).
Near the door: a small wooden study desk with a desk lamp, school books, a pencil cup, a wall calendar above it, a school bag hanging on the chair.
Center of the room: open empty floor space with a woven rug, leaving room for characters. Warm, peaceful, nostalgic mood.
```

**2. `ruang_tengah.jpg`**
```
Style: modern Japanese anime visual novel art, clean thin lineart, soft cel shading, warm cinematic lighting, gentle pastel colors, detailed but clean, high quality. Background art only, no people, no characters, no text, vertical 9:16 aspect ratio (1080x1920), visual novel background, eye-level camera at standing height.
The living room (ruang tengah) of the same modest Indonesian fishing-village house, morning. Same house style: whitewashed plaster walls slightly worn, dark brown wooden ceiling beams, warm brown wooden door and window frames, light cream ceramic tile floor.
LEFT side, fully inside the frame (around 20–30% of the image width): the wooden FRONT DOOR of the house, closed, with a small glass window, a pair of sandals on a mat in front of it, soft daylight around its edges.
RIGHT side, fully inside the frame (around 70–80% of the image width): a warm brown wooden BEDROOM DOOR, closed, the same style as the bedroom door.
BACK WALL CENTER: a wide open archway with NO door, leading straight to the dining room behind it; through the archway we see a wooden dining table with four wooden chairs, a woven bamboo food cover (tudung saji) on the table, a rattan pendant lamp, and a kitchen doorway at the far back.
Furniture: an old wooden sofa with faded cushions against the left wall, a low wooden coffee table with a teapot and glasses, an old TV on a wooden cabinet, a framed family photo and a wall clock, a small potted plant, a ceiling fan.
Center of the room: open empty floor space for characters. Warm, homely, slightly old but clean.
```

**3. `ruang_makan.jpg`**
```
Style: modern Japanese anime visual novel art, clean thin lineart, soft cel shading, warm cinematic lighting, gentle pastel colors, detailed but clean, high quality. Background art only, no people, no characters, no text, vertical 9:16 aspect ratio (1080x1920), visual novel background, eye-level camera at standing height.
The dining room at the back of the same modest Indonesian fishing-village house, morning. Same house style: whitewashed plaster walls slightly worn, dark brown wooden ceiling beams, warm brown wooden frames, light cream ceramic tile floor.
CENTER: a wooden dining table with a woven placemat and four simple wooden chairs (two on the left side, two on the right side, chairs slightly pulled out), plates, glasses, a teapot, rice and side dishes partly covered by a woven bamboo food cover (tudung saji). A rattan pendant lamp hangs above the table.
LEFT side, fully inside the frame (around 15–25% of the image width): a wide open archway with NO door leading back to the living room; through it we see part of the old wooden sofa and the family photo on the living room wall.
BACK WALL RIGHT, fully inside the frame (around 70–80% of the image width): an open doorway to a small kitchen with a stove, pots and pans hanging, spice jars on a shelf.
BACK WALL LEFT: a wooden window with morning sunlight, village roofs and the sea faintly visible outside. A wall calendar and a framed family photo on the wall.
Leave open floor space around the table for characters. Warm, cozy family breakfast mood.
```

### Latar baru Bab 1–4 (desa & kota)
Tempel blok gaya + tambahan background (bagian 2) di depan setiap prompt. Simpan di `app/src/main/assets/latar/`.

| File | Prompt |
|---|---|
| `smp_desa.jpg` | `A small modest junior high school in an Indonesian fishing village, one-story buildings with green doors, a red-and-white flag pole in a sandy schoolyard, an announcement board with a paper list, coconut trees, morning light.` (mengambil SKL, Bab 1) |
| `rumah_haji.jpg` | `The front yard of an old village elder's house in an Indonesian fishing village, a wide swept sandy yard full of fallen ketapang leaves, a bamboo rake leaning on a mango tree, a wooden boat shed on the side, a veranda with rattan chairs, afternoon.` (kerja serabutan) |
| `rumah_sekar.jpg` | `A tiny wooden house interior used as a tailor's workshop, an old foot-pedal sewing machine by the window, piles of fabric and school uniforms on a table, a measuring tape and scissors, a hanging lamp, warm late-afternoon light.` |
| `pasar_malam.jpg` | `A small night market on a village dirt field in Indonesia, a colorful old Ferris wheel and merry-go-round with string lights, food stalls with smoke, families walking, night sky with stars.` (Bab 3) |
| `pkbm.jpg` | `A simple community learning center classroom in an Indonesian sub-district town, plastic chairs, a whiteboard with handwritten math, posters about equivalency exams (Paket C), a ceiling fan, afternoon light.` |
| `stasiun_desa_pamit.jpg` (CG) | `A small rural Indonesian train station platform in the morning, a train arriving, a crowd of families saying goodbye to teenagers with big bags, emotional atmosphere, seen from a distance, no clear faces.` |
| `kereta_dalam.jpg` | `Inside an Indonesian economy-class train carriage, blue seats facing each other, a window showing rice fields and coastline passing by, bags on the luggage rack, warm afternoon light.` |
| `pasar_lama.jpg` | `A crowded traditional market in a big Indonesian city, narrow aisles with vegetable and fish stalls, hanging lamps, plastic tarps, motorbikes squeezing through, busy morning.` |
| `pelabuhan.jpg` | `A busy city harbor in Indonesia at late afternoon, cargo ships and rusty fishing boats, cranes, stacked containers, a ship-engine repair workshop with an open garage on the left, workers in coveralls.` |
| `bengkel_kapal.jpg` | `Inside a small ship-engine repair workshop at a harbor, a big disassembled boat engine on the floor, tools on the wall, an empty bamboo birdcage hanging near the door, oil stains, late afternoon light through the doorway.` |
| `kolong_jembatan.jpg` | `Under a concrete highway bridge near a city harbor at dusk, a large fresh mural on the pillar showing a white lighthouse and two small children sitting on a wooden pier, a tiny signature "A." in the corner, graffiti around, puddles reflecting the mural.` |
| `sma_lapangan.jpg` | `The courtyard of an Indonesian public high school, a flag pole with the red-and-white flag, students' classrooms around on two floors, a big banyan tree, morning sun.` (MPLS, upacara 17 Agustus) |
| `atap_kos.jpg` | `The rooftop of a cramped boarding house beside a railway in a big Indonesian city at night, laundry lines, water tanks, a plastic chair, city lights and fireworks in the sky, a train passing below.` |
| `asrama_sekar.jpg` | `A neat dormitory room of an elite high school, two beds, two study desks with lamps, a window with city lights, very tidy, slightly cold atmosphere, evening.` (opsional) |
| `warung_bu_tini.jpg` | `A busy cheap food stall (warung makan) in a city alley, a glass display of dishes, wooden benches, a sink full of dirty plates in the back, steam and warm light, evening.` |
| `rumah_dimas.jpg` | `A small house squeezed beside a railway track in a dense city kampung, a nasi uduk stall in front, the red spray-painted word "BONGKAR" on the wall, laundry, a train passing close, afternoon.` |

### Gambar latar + orang untuk menjelajah (desa)
Saat waktu bebas, latar berganti otomatis sesuai siapa yang sedang ada di lokasi itu. Satu gambar = latar + semua orang
yang ada di jam itu. Jadwal, daftar lengkap, dan alasannya: **[CERITA.md](CERITA.md) bagian 3–4**.

**Cara:** lampirkan latarnya (gambar kosong) + gambar biasa setiap karakter yang ada di gambar itu, lalu kirim prompt.
Simpan dengan nama persis di tabel (dua garis bawah, nama orang urut abjad), upload ke `app/src/main/assets/latar/`.
**Posisi orang harus sama di semua gambar lokasi itu** (kiri / tengah / kanan seperti di tabel), karena titik ketuknya tetap.

Kalimat pengunci (tempel paling atas):
```
Use the FIRST attached image as the exact background and keep it pixel-identical: same composition, camera angle, furniture, objects, lighting and art style. Use the other attached images only as references for the characters: same faces, hair, outfits and colors. ADD ONLY the characters described below into the scene, drawn in the same art style and lighting as the background, with correct perspective and size (adults about 40% of the image height when standing in the middle distance; the 11-year-old girl clearly smaller). Do not change anything else. No other people, no text. Vertical 9:16.
```

| Simpan sebagai (lampirkan) | Tambahan prompt |
|---|---|
| `rumah_warung__ibu.jpg` (rumah_warung + ibu) | `The mother stands behind the warung table on the LEFT, frying banana fritters in a wok, facing slightly toward the viewer.` |
| `rumah_warung__ayah_ibu.jpg` (rumah_warung + ayah + ibu) | `The mother stands behind the warung table on the LEFT, frying. The father sits cross-legged on a low bamboo bench on the RIGHT side of the road, repairing a green fishing net laid across his lap.` |
| `rumah_warung__ibu_laras.jpg` (rumah_warung + ibu + laras) | `The mother stands behind the warung table on the LEFT, frying. The little girl (11 years old, school uniform changed to a simple home dress) squats in the MIDDLE of the road near the warung, feeding a calico cat.` |
| `ruang_makan__ibu.jpg` (ruang_makan + ibu) | `The mother stands near the kitchen doorway on the RIGHT, carrying a plate of food toward the table, warm smile.` |
| `ruang_makan__ayah_ibu_laras.jpg` (ruang_makan + 3 karakter) | `The family eats together: the father sits on the LEFT side of the table, the little girl sits on the RIGHT side nearer the center, the mother sits on the far RIGHT. Warm family meal.` (boleh memakai gambar sarapan_keluarga) |
| `ruang_makan__ayah.jpg` (ruang_makan + ayah) | `The father sits alone on the LEFT side of the dining table, eating rice and fried fish with his right hand, hair still wet after waking up.` |
| `ruang_makan__laras.jpg` (ruang_makan + laras) | `The little girl (11 years old, red-and-white elementary school uniform) sits on the RIGHT side of the table nearer the center, eating lunch, school bag on the next chair.` |
| `ruang_tengah__laras.jpg` (ruang_tengah + laras) | `The little girl (11 years old, simple home dress) lies on her stomach on the woven mat in the MIDDLE, in front of the coffee table, drawing in a notebook with colored pencils.` |
| `ruang_tengah__ayah.jpg` (ruang_tengah + ayah) | `The father sits relaxed on the old wooden sofa on the LEFT, a glass of black coffee in his hand, an old radio on the cabinet behind him.` |
| `ruang_tengah__ibu_laras.jpg` (ruang_tengah + ibu + laras) | `Evening. The mother sits on the wooden sofa on the LEFT, folding clothes, looking at the TV on the right. The little girl sits on the mat in the MIDDLE in front of the TV, hugging her knees.` |
| `ruang_tengah__ibu.jpg` (ruang_tengah + ibu) | `Evening. The mother sits alone on the wooden sofa on the LEFT, folding clothes, a pile of laundry beside her, watching the TV on the right.` |
| `gudang_ikan__darto.jpg` (gudang_ikan + darto) | `The fish boss stands near the drying racks on the LEFT, hands on his hips, supervising, a small notebook in his shirt pocket.` |
| `lapangan__bayu.jpg` (lapangan + bayu) | `The teenage boy dribbles a worn plastic football on the RIGHT side of the dirt field, barefoot, grinning.` |
| `lapangan__laras.jpg` (lapangan + laras) | `The little girl (11 years old) runs on the LEFT side of the field holding a kite string, a colorful kite high in the sky.` |
| `dermaga__sekar.jpg` (dermaga + sekar) | `The teenage girl with long black hair, a dark purple headband and round glasses sits at the edge of the pier on the LEFT, legs dangling over the water, reading a worn novel.` |
| `dermaga__bayu.jpg` (dermaga + bayu) | `The teenage boy sits at the edge of the pier on the RIGHT, holding a bamboo fishing rod, a small bucket beside him.` |
| `dermaga__ayah.jpg` (dermaga + ayah) | `Dusk. The father stands in the MIDDLE beside his small faded-blue wooden fishing boat, coiling a mooring rope, looking at the western sky.` |

**Latar dasar baru (tanpa orang, pakai prompt latar biasa di atas):**
- `lapangan.jpg` — `A small dirt football field in an Indonesian fishing village, bamboo goalposts, a big ketapang (Indian almond) tree with reddish leaves on the left, wooden houses and coconut trees around, the old white lighthouse visible far away on the cape, sunny afternoon. Background art only, no people.`
- `dermaga.jpg` — `An old wooden pier in an Indonesian fishing village in the afternoon, small colorful wooden fishing boats moored on the left, the old white lighthouse on the rocky cape at the end, calm sea, ropes and baskets on the planks. Background art only, no people.` (versi malam sudah ada: dermaga_malam.jpg)

Prioritas: rumah_warung (3) → ruang_makan (3) → dermaga (dasar + 3) → lapangan (dasar + 2) → ruang_tengah (4) → gudang (1).
Selama belum ada, game memakai latar kosong + tanda 👤 nama orangnya.

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
