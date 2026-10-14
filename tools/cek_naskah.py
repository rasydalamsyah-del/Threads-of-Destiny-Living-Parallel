#!/usr/bin/env python3
"""
Pemeriksa naskah & data (dijalankan otomatis di GitHub Actions sebelum build).
Mencari: perintah @ yang tidak dikenal, label yang hilang, id karakter/barang/pekerjaan/kos/kejadian
yang tidak ada di folder data. Kesalahan ditampilkan sebagai anotasi di tab Actions.
"""
import glob
import os
import re
import sys

ROOT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets")
KNOWN = set("""latar bg gelap black terang lift waktu timecard kedip blink jam clock jeda wait hp phone tunggu_hp
wait_phone musik music suara sound sfx lompat jump goto jika if atur set tambah add hidup life kalender calendar
bebas free maju advance uang money stat barang item lokasi location tampil show sembunyi hide kenal meet cinta love
percaya trust perilaku behave bohong lie terbongkar reveal ungkapkan confess naskah script tanggal date kos kiriman
allowance kerja_mulai sekolah_mulai job_start kerja_berhenti sekolah_berhenti job_stop lamar apply catatan note
dokumen doc kejadian event tamat end pilihan choice""".split())


def sections(path):
    ids = set()
    if os.path.exists(path):
        for line in open(path, encoding="utf-8"):
            line = line.strip()
            if line.startswith("[") and line.endswith("]"):
                ids.add(line[1:-1].strip())
    return ids


data = os.path.join(ROOT, "data")
chars = sections(os.path.join(data, "karakter.txt"))
items = sections(os.path.join(data, "barang.txt"))
jobs = sections(os.path.join(data, "pekerjaan.txt"))
kos = sections(os.path.join(data, "kos.txt"))
events = sections(os.path.join(data, "kejadian.txt"))
docs = sections(os.path.join(data, "dokumen.txt"))
scripts = {os.path.basename(p) for p in glob.glob(os.path.join(ROOT, "naskah", "*.txt"))}
latar = {os.path.splitext(os.path.basename(p))[0] for p in glob.glob(os.path.join(ROOT, "latar", "*"))}
karakter_img = {os.path.splitext(os.path.basename(p))[0] for p in glob.glob(os.path.join(ROOT, "karakter", "*"))}

errors = 0


def err(f, n, msg):
    global errors
    errors += 1
    rel = os.path.relpath(f, os.path.join(ROOT, "..", "..", "..", ".."))
    print(f"::error file={rel},line={n}::{msg}")


for f in sorted(glob.glob(os.path.join(ROOT, "naskah", "*.txt"))):
    labels, jumps = set(), []
    for n, raw in enumerate(open(f, encoding="utf-8"), 1):
        l = raw.strip()
        if not l or l.startswith("#"):
            continue
        if l.startswith("::"):
            labels.add(l[2:].strip())
            continue
        if l.startswith(">"):
            if "->" not in l:
                err(f, n, "Pilihan harus berformat: > teks -> nama_label")
            else:
                jumps.append((l.rsplit("->", 1)[1].strip(), n))
            continue
        if not l.startswith("@"):
            continue
        parts = l[1:].split()
        name = parts[0].lower() if parts else ""
        args = [a.lower() for a in parts[1:]]
        if name not in KNOWN:
            err(f, n, f"Perintah @{name} tidak dikenal")
            continue
        m = re.search(r"->\s*(\w+)(?:\s*,\s*(\w+))?$", l)
        if name in ("lompat", "jump", "goto") and args:
            jumps.append((parts[1], n))
        elif m and name in ("jika", "if", "lamar", "apply", "ungkapkan", "confess"):
            jumps.append((m.group(1), n))
            if m.group(2):
                jumps.append((m.group(2), n))
        a0 = args[0] if args else ""
        if name in ("latar", "bg") and parts[1:] and parts[1] not in latar:
            err(f, n, f"Gambar latar '{parts[1]}' tidak ada di folder latar")
        if name in ("kenal", "cinta", "percaya", "perilaku", "ungkapkan") and a0 and a0 not in chars:
            err(f, n, f"Karakter '{a0}' tidak ada di data/karakter.txt")
        if name in ("tampil", "show") and a0 and a0 not in chars and a0 not in karakter_img:
            err(f, n, f"Karakter / gambar '{a0}' tidak ditemukan")
        if name in ("barang", "item") and a0 and a0 not in items:
            err(f, n, f"Barang '{a0}' tidak ada di data/barang.txt")
        if name in ("kerja_mulai", "sekolah_mulai", "kerja_berhenti", "sekolah_berhenti", "lamar") and a0 and a0 not in jobs:
            err(f, n, f"Pekerjaan '{a0}' tidak ada di data/pekerjaan.txt")
        if name == "kos" and a0 and a0 not in kos and a0 not in ("kosong", "tidak_ada"):
            err(f, n, f"Kos '{a0}' tidak ada di data/kos.txt")
        if name in ("kejadian", "event") and a0 and a0 not in events:
            err(f, n, f"Kejadian '{a0}' tidak ada di data/kejadian.txt")
        if name in ("dokumen", "doc") and a0 and a0 not in docs:
            err(f, n, f"Dokumen '{a0}' tidak ada di data/dokumen.txt")
        if name in ("naskah", "script") and parts[1:]:
            target = parts[1] if "." in parts[1] else parts[1] + ".txt"
            if target not in scripts:
                err(f, n, f"File naskah '{target}' tidak ada")
    for j, n in jumps:
        if j not in labels:
            err(f, n, f"Label ':: {j}' tidak ditemukan")

print(f"Pemeriksaan naskah selesai: {errors} kesalahan.")
sys.exit(1 if errors else 0)
