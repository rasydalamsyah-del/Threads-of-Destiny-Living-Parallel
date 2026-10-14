#!/usr/bin/env python3
"""Baca hasil JUnit (XML) dan tampilkan tes yang gagal sebagai anotasi GitHub (::error) / ringkasan (::notice)."""
import glob, os, sys
import xml.etree.ElementTree as ET

folder = sys.argv[1] if len(sys.argv) > 1 else "."
ringkas = "--ringkas" in sys.argv
total = gagal = 0
for f in sorted(glob.glob(os.path.join(folder, "*.xml"))):
    root = ET.parse(f).getroot()
    for tc in root.iter("testcase"):
        total += 1
        for bad in list(tc.findall("failure")) + list(tc.findall("error")):
            gagal += 1
            msg = (bad.get("message") or bad.text or "").strip().replace("\n", " ")[:900]
            print(f"::error title=Tes gagal: {tc.get('classname','').split('.')[-1]}.{tc.get('name')}::{msg}")
print(f"::notice title=Unit test::{total - gagal} dari {total} tes lulus")
