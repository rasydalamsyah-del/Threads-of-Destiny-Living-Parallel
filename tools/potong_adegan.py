#!/usr/bin/env python3
# Pemakaian: python3 tools/potong_adegan.py latar_asli.jpg latar_dengan_karakter.jpg app/src/main/assets/adegan/ruang_makan__ayah.webp
# Butuh: pip install pillow numpy scipy
"""Ambil karakter dari gambar adegan (latar yang sama + karakter) → lapisan transparan seukuran latar.

Gambar hasil edit AI biasanya sedikit berbeda di seluruh bagian (warna, detail kecil), jadi yang diambil
hanya daerah yang berubah BESAR dan menyatu (badan karakter), bukan perbedaan kecil di mana-mana.
"""
import sys
import numpy as np
from PIL import Image
from scipy import ndimage as ndi

base_p, scene_p, out_p = sys.argv[1], sys.argv[2], sys.argv[3]
base = Image.open(base_p).convert("RGB")
scene = Image.open(scene_p).convert("RGB").resize(base.size, Image.LANCZOS)
B = np.asarray(base).astype(np.float32)
S = np.asarray(scene).astype(np.float32)
# perbedaan warna, dihaluskan agar derau kecil hilang
diff = np.sqrt(((ndi.gaussian_filter(S, (2, 2, 0)) - ndi.gaussian_filter(B, (2, 2, 0))) ** 2).sum(2))
thr = max(38.0, float(np.percentile(diff, 90)) * 0.9)
mask = diff > thr
mask = ndi.binary_opening(mask, iterations=2)
mask = ndi.binary_closing(mask, iterations=6)
lab, n = ndi.label(mask)
if n == 0:
    sys.exit("Tidak ada perbedaan yang cukup besar — apakah karakternya sudah ditambahkan?")
sizes = ndi.sum(mask, lab, range(1, n + 1))
keep = [i + 1 for i, s in enumerate(sizes) if s >= max(sizes) * 0.15]
mask = np.isin(lab, keep)
# tutup lubang di badan yang warnanya mirip latar (mis. celana gelap di depan kursi gelap)
mask = ndi.binary_closing(mask, structure=np.ones((3, 3)), iterations=18)
mask = ndi.binary_fill_holes(mask)
mask = ndi.binary_dilation(mask, iterations=2)
alpha = ndi.gaussian_filter(mask.astype(np.float32), 2.0)
alpha = np.clip((alpha - 0.15) / 0.7, 0, 1)
out = np.dstack([S, alpha * 255]).round().astype(np.uint8)
Image.fromarray(out, "RGBA").save(out_p, "WEBP", quality=90, method=4)
ys, xs = np.where(mask)
print(f"OK: {out_p}  kotak karakter x {xs.min()}-{xs.max()}, y {ys.min()}-{ys.max()}  ({mask.mean()*100:.1f}% gambar)")
