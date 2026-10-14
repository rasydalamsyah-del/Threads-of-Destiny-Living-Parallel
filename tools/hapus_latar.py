#!/usr/bin/env python3
# Pemakaian: python3 tools/hapus_latar.py gambar_asli.png app/src/main/assets/karakter/nama.png
# Butuh: pip install pillow numpy scipy
"""Hapus latar putih: alpha halus di tepi + celah putih di sela rambut, buang sisa putih."""
import sys
import numpy as np
from PIL import Image
from scipy import ndimage as ndi

src, dst = sys.argv[1], sys.argv[2]
P = np.asarray(Image.open(src).convert("RGB")).astype(np.float32)
mn = P.min(2); mx = P.max(2)
# 1) latar: keputihan yang tersambung ke tepi gambar
cand = (mn >= 222) & (mx - mn <= 28)
lab, _ = ndi.label(cand)
border = np.unique(np.concatenate([lab[0], lab[-1], lab[:, 0], lab[:, -1]]))
bg = np.isin(lab, border[border > 0])
dist_bg = ndi.distance_transform_edt(~bg)
# 2) celah putih di sela rambut, dicari bertahap (merambat dari celah ke celah)
whitish = (mn >= 195) & (mx - mn <= 32)
lab2, n2 = ndi.label(whitish & ~bg)
holes = np.zeros_like(bg)
if n2:
    idx = np.arange(1, n2 + 1)
    sizes = ndi.sum(np.ones_like(mn), lab2, idx)
    mean = ndi.mean(mn, lab2, idx)
    # kecerahan di sekeliling celah: celah rambut dikelilingi gelap, kilau baju dikelilingi terang
    ring_lab = np.where(~whitish, ndi.maximum_filter(lab2, 5), 0)
    with np.errstate(invalid="ignore"):
        ring = np.nan_to_num(ndi.mean(mn, ring_lab, idx), nan=255.0)
    dark_around = ring < 140
    for _ in range(6):
        d = ndi.distance_transform_edt(~(bg | holes))
        mind = ndi.minimum(d, lab2, idx)
        keep = idx[dark_around & (((sizes < 2500) & (mind <= 45)) | ((mean >= 240) & (sizes < 60000) & (mind <= 60)))]
        new_holes = np.isin(lab2, keep)
        if (new_holes & ~holes).sum() == 0:
            break
        holes |= new_holes
bg_all = bg | holes
# penyapu: bintik putih murni kecil yang menempel di daerah transparan
if n2:
    for _ in range(2):
        d = ndi.distance_transform_edt(~bg_all)
        mind = ndi.minimum(d, lab2, idx)
        extra = idx[(sizes < 400) & (mean >= 238) & (mind <= 2)]
        add = np.isin(lab2, extra) & ~bg_all
        if not add.any():
            break
        holes |= add
        bg_all |= add
dist_bg = ndi.distance_transform_edt(~bg_all)
# 3) daerah ragu = cincin tepi + celah; warna padat terdekat sebagai warna asli
unknown = holes | (~bg_all & (dist_bg <= 3))
solid = ~bg_all & ~unknown
_, (iy, ix) = ndi.distance_transform_edt(~solid, return_indices=True)
F = P[iy, ix]
c = np.argmax(255 - F, axis=2)
Fc = np.take_along_axis(F, c[..., None], 2)[..., 0]
Pc = np.take_along_axis(P, c[..., None], 2)[..., 0]
denom = 255 - Fc
a_est = np.clip((255 - Pc) / np.maximum(denom, 1), 0, 1)
a = np.where(bg, 0.0, 1.0)
a = np.where(unknown & (denom > 35), a_est, a)
# 4) warna tanpa campuran putih
safe = np.maximum(a, 1e-3)[..., None]
C = np.clip((P - (1 - a)[..., None] * 255) / safe, 0, 255)
wgt = np.clip((a - 0.15) / 0.5, 0, 1)[..., None]
C = np.where(unknown[..., None], C * wgt + F * (1 - wgt), P)
a = ndi.gaussian_filter(a, 0.5)
a[solid & (dist_bg > 5)] = 1.0
out = np.dstack([C, a * 255]).round().astype(np.uint8)
Image.fromarray(out, "RGBA").save(dst, optimize=True)
