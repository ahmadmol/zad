"""Physical phone evidence helpers; never clears data or uninstalls packages."""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import tarfile
import time
import xml.etree.ElementTree as ET
import re

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / '.artifacts/physical-device-qa/session-20261016'
OUT.mkdir(parents=True, exist_ok=True)
ADB = r'C:\android_sdk\platform-tools\adb.exe'
SERIAL = 'ac190123'
PACKAGE = 'com.example.mol.referenceqa'

def adb(*args, binary=False):
    p = subprocess.run([ADB, '-s', SERIAL, *args], capture_output=True, check=True, timeout=60)
    return p.stdout if binary else p.stdout.decode('utf-8', errors='replace')

def guard():
    assert adb('get-state').strip() == 'device'
    assert adb('shell', 'getprop', 'ro.product.model').strip() == '23129RAA4G'
    assert adb('shell', 'getprop', 'ro.product.manufacturer').strip().lower() == 'xiaomi'
    assert adb('shell', 'getprop', 'ro.kernel.qemu').strip() != '1'

def install(path):
    env = dict(os.environ, JAVA_HOME=r'C:\Users\molha\.jdks\jbr-21.0.3')
    cert = subprocess.run([r'C:\android_sdk\build-tools\35.0.0\apksigner.bat', 'verify', '--print-certs', str(path)], env=env, capture_output=True, text=True, check=True).stdout
    assert '870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63' in cert
    manifest = subprocess.run([r'C:\android_sdk\build-tools\35.0.0\aapt.exe', 'dump', 'badging', str(path)], capture_output=True, text=True, encoding='utf-8', check=True).stdout
    assert f"package: name='{PACKAGE}'" in manifest
    result = adb('install', '-r', str(path))
    assert 'Success' in result, result
    with (OUT/'install-history.jsonl').open('a', encoding='utf-8') as f:
        f.write(json.dumps(dict(apk=str(path), sha256=hashlib.sha256(path.read_bytes()).hexdigest(), result=result))+'\n')
    print(result.strip())

def snapshot(label):
    adb('shell', 'am', 'force-stop', PACKAGE)
    target = OUT / f'{label}-private.tar'
    with target.open('wb') as f:
        subprocess.run([ADB, '-s', SERIAL, 'exec-out', 'run-as', PACKAGE, 'tar', '-cf', '-', '.'], stdout=f, stderr=subprocess.PIPE, check=True)
    with tarfile.open(target) as archive:
        records = []
        for member in archive.getmembers():
            if member.isfile():
                data = archive.extractfile(member).read()
                records.append(dict(name=member.name, size=len(data), sha256=hashlib.sha256(data).hexdigest()))
    assert any('/databases/' in x['name'] for x in records), 'No databases in backup'
    (OUT/f'{label}-files.json').write_text(json.dumps(records, indent=2), encoding='utf-8')
    print(f'Verified private backup: {target}; {len(records)} files')

def dump(label='current'):
    adb('shell','uiautomator','dump','/sdcard/ihsan-qa-window.xml')
    raw = adb('shell','cat','/sdcard/ihsan-qa-window.xml')
    (OUT/f'{label}.xml').write_text(raw,encoding='utf-8')
    return ET.fromstring(raw)

def ui(action, value='current'):
    if action == 'tap':
        tree = dump()
        matches = [n for n in tree.iter('node') if n.get('text') == value]
        if not matches:
            matches = [n for n in tree.iter('node') if n.get('content-desc') == value]
        assert len(matches) == 1, f'Expected one target {value!r}, found {len(matches)}'
        bounds = [int(x) for x in re.findall(r'\d+',matches[0].get('bounds'))]
        adb('shell','input','tap',str((bounds[0]+bounds[2])//2),str((bounds[1]+bounds[3])//2))
        time.sleep(.8)
    elif action == 'back':
        adb('shell','input','keyevent','4'); time.sleep(.7)
    elif action == 'scroll':
        adb('shell','input','swipe','540','1900','540','650','450'); time.sleep(.6)
    elif action == 'capture':
        (OUT/f'{value}.png').write_bytes(adb('exec-out','screencap','-p',binary=True))
    tree = dump(value if action == 'capture' else 'current')
    for n in tree.iter('node'):
        if n.get('text') or n.get('content-desc'):
            print(json.dumps({k:n.get(k) for k in ['text','content-desc','clickable','enabled','bounds']},ensure_ascii=False))
    with (OUT/'native-actions.jsonl').open('a',encoding='utf-8') as f:
        f.write(json.dumps(dict(time=time.time(),action=action,value=value),ensure_ascii=False)+'\n')

if __name__ == '__main__':
    guard()
    if sys.argv[1] == 'backup':
        install(ROOT/'.artifacts/physical-device-qa/offline/apk/app-referenceqa-debug.apk')
        snapshot('before-tests')
        install(ROOT/'docs/release/apk/Ihsan-physical-device-20261016-rc-514b7d10.apk')
    elif sys.argv[1] == 'snapshot':
        snapshot(sys.argv[2])
    elif sys.argv[1] == 'install':
        install(Path(sys.argv[2]).resolve())
    elif sys.argv[1] == 'capture':
        (OUT/f'{sys.argv[2]}.png').write_bytes(adb('exec-out', 'screencap', '-p', binary=True))
    elif sys.argv[1] == 'ui':
        ui(sys.argv[2],sys.argv[3] if len(sys.argv)>3 else 'current')
