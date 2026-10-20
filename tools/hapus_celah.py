#!/usr/bin/env python3
# Pemakaian: python3 tools/hapus_celah.py app/src/main/assets/karakter/*.png app/src/main/assets/karakter/*.webp
#            (tambahkan --cek untuk hanya melaporkan tanpa mengubah file)
# Butuh: pip install pillow numpy scipy
"""Hapus sisa latar putih yang TERKURUNG di dalam tokoh (celah antara lengan & badan, sela kaki, sela jari).

hapus_latar.py hanya membuang putih yang tersambung ke tepi gambar, jadi celah tertutup tetap putih.
Celah latar dibedakan dari baju/kerudung putih lewat kecerahannya: latar AI hampir 255 dan rata
(rata-rata >= 247), sedangkan kain putih selalu berbayang (rata-rata ± 235–242).
"""
import sys
import numpy as np
from PIL import Image
from scipy import ndimage as ndi

check_only = "--cek" in sys.argv
files = [a for a in sys.argv[1:] if not a.startswith("--")]
total = 0
for path in files:
    im = Image.open(path).convert("RGBA")
    A = np.asarray(im).astype(np.float32)
    rgb, al = A[..., :3], A[..., 3]
    mn, mx = rgb.min(2), rgb.max(2)
    whitish = (al > 200) & (mn >= 232) & (mx - mn <= 16)
    lab, n = ndi.label(whitish)
    if n == 0:
        continue
    idx = np.arange(1, n + 1)
    sizes = ndi.sum(whitish, lab, idx)
    means = ndi.mean(mn, lab, idx)
    keep = idx[(sizes >= 150) & (means >= 247)]
    if keep.size == 0:
        continue
    gap = np.isin(lab, keep)
    total += 1
    print(f"{path}: {int(gap.sum())} px celah putih ({keep.size} bagian)")
    if check_only:
        continue
    # inti celah → transparan; tepi 3 px di sekitarnya → alpha mengikuti seberapa putih pikselnya (anti-alias)
    ring = ndi.binary_dilation(gap, iterations=3) & ~gap & (al > 0)
    w = np.clip((mn - 205) / 50.0, 0, 1) * ((mx - mn) <= 30)
    new_al = al.copy()
    new_al[gap] = 0
    new_al[ring] = al[ring] * (1 - w[ring])
    # buang campuran putih di tepi (warna asli = (C - w*255) / (1 - w))
    out_rgb = rgb.copy()
    m = ring & (w > 0) & (w < 0.98)
    ww = w[m][:, None]
    out_rgb[m] = np.clip((rgb[m] - ww * 255) / (1 - ww), 0, 255)
    out = np.dstack([out_rgb, new_al]).astype(np.uint8)
    res = Image.fromarray(out, "RGBA")
    if path.lower().endswith(".webp"):
        res.save(path, "WEBP", quality=92, method=5)
    else:
        res.save(path, optimize=True)
print(f"{total} file {'perlu diperbaiki' if check_only else 'diperbaiki'}")
