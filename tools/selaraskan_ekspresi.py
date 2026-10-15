#!/usr/bin/env python3
# Pemakaian: python3 tools/selaraskan_ekspresi.py app/src/main/assets/karakter folder_hasil_hapus_latar folder_tujuan
# Butuh: pip install opencv-python-headless pillow numpy
"""Selaraskan gambar ekspresi ke gambar biasa (SIFT + affine), simpan WebP."""
import cv2, numpy as np, sys, os, json
from PIL import Image
K, src_dir, dst_dir = sys.argv[1], sys.argv[2], sys.argv[3]
sift = cv2.SIFT_create(5000)
def load(p):
    im = cv2.imread(p, cv2.IMREAD_UNCHANGED)
    a = im[..., 3:4] / 255.0
    g = cv2.cvtColor((im[..., :3] * a + 255 * (1 - a)).astype(np.uint8), cv2.COLOR_BGR2GRAY)
    return im, g, (im[..., 3] > 128).astype(np.uint8) * 255
report = {}
for f in sorted(os.listdir(src_dir)):
    cid = f.rsplit('_', 1)[0]
    _, gb, mb = load(os.path.join(K, cid + '.png'))
    im, ge, me = load(os.path.join(src_dir, f))
    kb, db = sift.detectAndCompute(gb, mb); ke, de = sift.detectAndCompute(ge, me)
    good = [a for a, b in cv2.BFMatcher().knnMatch(de, db, k=2) if a.distance < 0.75 * b.distance]
    s = np.float32([ke[g.queryIdx].pt for g in good]); d = np.float32([kb[g.trainIdx].pt for g in good])
    M, inl = cv2.estimateAffinePartial2D(s, d, method=cv2.RANSAC, ransacReprojThreshold=3)
    report[f] = (int(inl.sum()), round(float(np.hypot(M[0, 0], M[1, 0])), 4), M[:, 2].round(1).tolist())
    # premultiplied warp
    rgba = im.astype(np.float32); a = rgba[..., 3:4] / 255.0
    pm = np.dstack([rgba[..., :3] * a, rgba[..., 3:4]])
    w = cv2.warpAffine(pm, M, (im.shape[1], im.shape[0]), flags=cv2.INTER_CUBIC, borderMode=cv2.BORDER_REPLICATE)
    # bawah gambar: lanjutkan (karakter terpotong di bawah); sisi lain kosong
    al = np.clip(w[..., 3:4], 0, 255)
    col = np.where(al > 0.5, w[..., :3] / np.maximum(al / 255.0, 1e-3), 0)
    out = np.dstack([np.clip(col, 0, 255), al]).astype(np.uint8)
    out = cv2.cvtColor(out, cv2.COLOR_BGRA2RGBA)
    Image.fromarray(out, 'RGBA').save(os.path.join(dst_dir, f.replace('.png', '.webp')), 'WEBP', quality=90, method=4)
for k, v in report.items(): print(k, v)
