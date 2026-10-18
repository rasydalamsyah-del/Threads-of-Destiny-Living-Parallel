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
dokumen doc kejadian event tamat end pilihan choice mulai_ulang reset tunjuk show_ui info""".split())


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

# data/sosmed.txt: penulis harus karakter yang ada
sos = os.path.join(data, "sosmed.txt")
if os.path.exists(sos):
    for n, raw in enumerate(open(sos, encoding="utf-8"), 1):
        l = raw.strip()
        if l.lower().startswith("penulis") and "=" in l:
            who = l.split("=", 1)[1].strip()
            if who and who not in chars:
                err(sos, n, f"Penulis '{who}' tidak ada di data/karakter.txt")
# data/pekerjaan.txt: orang_dalam harus karakter yang ada
pk = os.path.join(data, "pekerjaan.txt")
for n, raw in enumerate(open(pk, encoding="utf-8"), 1):
    l = raw.strip()
    if l.lower().startswith("orang_dalam") and "=" in l:
        who = l.split("=", 1)[1].strip()
        if who and who not in chars:
            err(pk, n, f"Orang dalam '{who}' tidak ada di data/karakter.txt")

# data/obrolan.txt: karakter, jenis, kolom wajib, topik, info profil
ob = os.path.join(data, "obrolan.txt")
topics = sections(os.path.join(data, "topik.txt"))
FACTS = {"hobi", "kegemaran", "pelajaran", "keahlian", "cita_cita", "kisah_1", "kisah_2", "kisah_3"}
if os.path.exists(ob):
    def check_entry(eid, line_no, m):
        if not eid:
            return
        who = m.get("karakter", "")
        if who not in chars:
            err(ob, line_no, f"[{eid}] karakter '{who}' tidak ada di data/karakter.txt")
        if m.get("jenis", "santai") not in ("santai", "kenal", "khusus"):
            err(ob, line_no, f"[{eid}] jenis harus santai / kenal / khusus")
        for k in ("judul", "kamu", "jawab"):
            if not m.get(k):
                err(ob, line_no, f"[{eid}] kolom '{k}' kosong")
        if m.get("topik") and m["topik"] not in topics:
            err(ob, line_no, f"[{eid}] topik '{m['topik']}' tidak ada di data/topik.txt")
        if m.get("ungkap") and m["ungkap"] not in FACTS:
            err(ob, line_no, f"[{eid}] ungkap '{m['ungkap']}' bukan info profil")
        if m.get("jenis") == "kenal" and not m.get("ungkap"):
            err(ob, line_no, f"[{eid}] jenis kenal wajib punya 'ungkap'")
        if m.get("ungkap_lain"):
            p2 = m["ungkap_lain"].split()
            if len(p2) != 2 or p2[0] not in chars or p2[1] not in FACTS:
                err(ob, line_no, f"[{eid}] ungkap_lain harus 'id_karakter info'")
        if (m.get("akrab") or m.get("percaya")) and not m.get("tolak"):
            err(ob, line_no, f"[{eid}] punya syarat akrab/percaya tapi tidak ada 'tolak'")
    cur, start, fields, seen = None, 0, {}, set()
    for n, raw in enumerate(open(ob, encoding="utf-8"), 1):
        l = raw.strip()
        if not l or l.startswith("#"):
            continue
        if l.startswith("[") and l.endswith("]"):
            check_entry(cur, start, fields)
            cur, start, fields = l[1:-1].strip(), n, {}
            if cur in seen:
                err(ob, n, f"id obrolan '{cur}' dobel")
            seen.add(cur)
        elif "=" in l:
            k, v = l.split("=", 1)
            fields[k.strip().lower()] = v.strip()
    check_entry(cur, start, fields)

# data/pesan.txt: setiap pesan punya balasan lengkap; pesan cerita dari karakter harus ada entrinya
def entries(path):
    out, cur, start = [], None, 0
    for n, raw in enumerate(open(path, encoding="utf-8"), 1):
        l = raw.strip()
        if not l or l.startswith("#"):
            continue
        if l.startswith("[") and l.endswith("]"):
            cur = {"_id": l[1:-1].strip(), "_line": n}
            out.append(cur)
        elif "=" in l and cur is not None:
            k, v = l.split("=", 1)
            cur[k.strip().lower()] = v.strip()
    return out

ps = os.path.join(data, "pesan.txt")
msgs = entries(ps) if os.path.exists(ps) else []
for m in msgs:
    if m.get("dari", "*") != "*" and m.get("dari") not in chars:
        err(ps, m["_line"], f"[{m['_id']}] dari '{m.get('dari')}' tidak ada di data/karakter.txt")
    if m.get("jenis", "acak") not in ("cerita", "acak", "sistem"):
        err(ps, m["_line"], f"[{m['_id']}] jenis harus cerita / acak / sistem")
    for k in ("isi", "hangat", "balas_hangat", "singkat", "balas_singkat"):
        if not m.get(k):
            err(ps, m["_line"], f"[{m['_id']}] kolom '{k}' kosong")
name_to_id = {}
for c in entries(os.path.join(data, "karakter.txt")):
    for k in ("nama", "nama_l", "nama_p"):
        if c.get(k):
            name_to_id[c[k].lower()] = c["_id"]
for f in sorted(glob.glob(os.path.join(ROOT, "naskah", "*.txt"))):
    for n, raw in enumerate(open(f, encoding="utf-8"), 1):
        l = raw.strip()
        if not l.startswith("@hp pesan ") or "|" not in l:
            continue
        sender, text = l[len("@hp pesan "):].split("|", 1)
        cid = name_to_id.get(sender.strip().lower())
        if cid and not any(m.get("dari") in (cid, "*") and m.get("isi", "").strip() == text.strip() for m in msgs):
            err(f, n, f"Pesan dari {sender.strip()} belum punya pilihan balasan di data/pesan.txt (isi harus sama persis)")

# data/aktivitas.txt: batas & jeda
for a in entries(os.path.join(data, "aktivitas.txt")):
    if a.get("batas") and not a["batas"].isdigit():
        err(os.path.join(data, "aktivitas.txt"), a["_line"], f"[{a['_id']}] batas harus angka")
    if a.get("jeda") and not re.match(r"^\d+([.,]\d+)?\s*(jam|menit)?$", a["jeda"]):
        err(os.path.join(data, "aktivitas.txt"), a["_line"], f"[{a['_id']}] jeda contoh: 4 jam / 90 menit")

# data/lokasi.txt: pintu, benda, orang, posisi; lokasi karakter & aktivitas harus ada
lk = os.path.join(data, "lokasi.txt")
if os.path.exists(lk):
    locs = {e["_id"]: e for e in entries(lk)}
    act_ids = {a["_id"] for a in entries(os.path.join(data, "aktivitas.txt"))}
    def xy_ok(v):
        try:
            x, y = [float(t) for t in v.split(",")[:2]]
            return 0.0 <= x <= 1.0 and 0.0 <= y <= 1.0, x
        except Exception:
            return False, 0.5
    for lid, e in locs.items():
        if lid not in latar:
            err(lk, e["_line"], f"[{lid}] gambar latar/{lid} tidak ada")
        for part in [p for p in e.get("pintu", "").split(";") if p.strip()]:
            f = [t.strip() for t in part.split("|")]
            if len(f) < 3 or (f[0] != "kos" and f[0] not in locs):
                err(lk, e["_line"], f"[{lid}] pintu ke '{f[0]}' tidak ada di lokasi.txt")
            ok, x = xy_ok(f[2] if len(f) > 2 else "")
            if not ok:
                err(lk, e["_line"], f"[{lid}] posisi pintu salah: {part.strip()}")
            elif not 0.1 <= x <= 0.9:
                err(lk, e["_line"], f"[{lid}] pintu terlalu ke pinggir (x {x}), bisa terpotong layar")
        for part in [p for p in e.get("benda", "").split(";") if p.strip()]:
            f = [t.strip() for t in part.split("|")]
            if len(f) < 3 or f[0] not in act_ids:
                err(lk, e["_line"], f"[{lid}] benda '{f[0]}' bukan aktivitas di aktivitas.txt")
            elif not xy_ok(f[2])[0]:
                err(lk, e["_line"], f"[{lid}] posisi benda salah: {part.strip()}")
        for part in [p for p in e.get("orang", "").split(";") if p.strip()]:
            cid = part.strip().split()[0]
            if cid not in chars:
                err(lk, e["_line"], f"[{lid}] orang '{cid}' tidak ada di karakter.txt")
    for c in entries(os.path.join(data, "karakter.txt")):
        for part in [p for p in c.get("lokasi", "").split(",") if p.strip()]:
            loc = part.strip().split()[-1]
            if loc not in locs:
                err(os.path.join(data, "karakter.txt"), c["_line"], f"[{c['_id']}] lokasi '{loc}' tidak ada di lokasi.txt")
        tj = c.get("tempat_janji")
        if tj and tj != "kos" and tj not in locs:
            err(os.path.join(data, "karakter.txt"), c["_line"], f"[{c['_id']}] tempat_janji '{tj}' tidak ada di lokasi.txt")
    for a in entries(os.path.join(data, "aktivitas.txt")):
        for loc in [t.strip() for t in a.get("lokasi", "").split(",") if t.strip()]:
            if loc != "kos" and loc not in locs:
                err(os.path.join(data, "aktivitas.txt"), a["_line"], f"[{a['_id']}] lokasi '{loc}' tidak ada di lokasi.txt")

print(f"Pemeriksaan naskah selesai: {errors} kesalahan.")
sys.exit(1 if errors else 0)
