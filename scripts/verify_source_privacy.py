"""Check tracked source or the prospective Git index; never print matched values.

Optional known-profile matching reads a LOCAL SQLite fixture read-only. No values
or value hashes are persisted. This check does not certify historical commits,
image pixels, content licenses, or distribution approval.
"""
import argparse
import json
import re
import sqlite3
import subprocess
from pathlib import Path


def git(*args):
    return subprocess.check_output(['git', *args])


def raw_artifact(path):
    suffix = Path(path).suffix.lower()
    return (
        path.startswith(('.artifacts/', '.project-preservation/', '.agents/', '.kilo/', '.vscode/'))
        or path in ('_runtime_qa_helper.py', 'IHSAN_MOL_BOTTOM_NAVIGATION_FIX_AGENT_PROMPT.md')
        or ('/' not in path and (path.startswith(('runtime-', 'ui_', 'ihsan_master_db'))
                                or path in ('lock.xml', 'qibla-online.xml')))
        or suffix in {'.apk', '.aab', '.apks', '.db', '.sqlite', '.jks', '.keystore',
                      '.p12', '.pfx', '.p8', '.pem', '.key', '.bundle', '.hprof', '.dmp', '.log', '.logcat', '.har'}
        or path.endswith(('.db-wal', '.db-shm', '.sqlite-wal', '.sqlite-shm'))
        or path.endswith(('local.properties', 'signing.properties', 'keystore.properties'))
        or (Path(path).name.startswith('.env') and Path(path).name not in {'.env.example', '.env.sample'})
        or (path.startswith(('docs/release/external-evidence/', 'docs/screenshots/'))
            and suffix in {'.png', '.jpg', '.jpeg', '.mp4', '.xml', '.txt', '.body',
                           '.headers', '.json', '.zip'})
    )


SECRET = re.compile(
    rb'-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----'
    rb'|\bgh[pousr]_[A-Za-z0-9]{30,}\b'
    rb'|\bgithub_pat_[A-Za-z0-9_]{40,}\b'
    rb'|\bAKIA[0-9A-Z]{16}\b'
    rb'|\bxox[baprs]-[A-Za-z0-9-]{20,}\b'
    rb'|https?://[^\s/:]+:[^\s/@]+@', re.IGNORECASE
)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--index', action='store_true', help='Scan staged blobs instead of working copies')
    parser.add_argument('--known-profile-db', type=Path, help='Optional local read-only known-phone fixture')
    args = parser.parse_args()
    root = Path(git('rev-parse', '--show-toplevel').decode().strip())
    needles = []
    names = []
    if args.known_profile_db:
        uri = args.known_profile_db.resolve().as_uri() + '?mode=ro&immutable=1'
        connection = sqlite3.connect(uri, uri=True)
        try:
            for first, last, phone in connection.execute('SELECT firstName, lastName, phoneNumber FROM users'):
                if phone:
                    needles.extend(str(phone).encode(enc) for enc in ('utf-8', 'utf-16-le'))
                if first and last:
                    name = str(first).strip() + ' ' + str(last).strip()
                    # Match whole Unicode words: short names can occur inside
                    # ordinary Arabic words in the immutable religious corpus.
                    names.append(re.compile(r'(?<!\w)' + re.escape(name) + r'(?!\w)'))
        finally:
            connection.close()
        if not needles:
            raise SystemExit('Known-profile fixture contains no phone values; verification incomplete')
    entries = []
    for row in git('ls-files', '--stage', '-z').split(b'\0'):
        if not row:
            continue
        metadata, path = row.split(b'\t', 1)
        mode, oid, stage = metadata.decode().split()
        if stage != '0':
            raise SystemExit('Resolve index conflicts before verifying candidates')
        entries.append((path.decode(), oid))
    findings = []
    reader = subprocess.Popen(['git', 'cat-file', '--batch'], stdin=subprocess.PIPE, stdout=subprocess.PIPE) if args.index else None
    try:
        for path, oid in entries:
            reasons = []
            if raw_artifact(path):
                reasons.append('private-or-generated-artifact-path')
            if reader:
                reader.stdin.write((oid+'\n').encode()); reader.stdin.flush()
                header = reader.stdout.readline().split()
                data = reader.stdout.read(int(header[2])); reader.stdout.read(1)
            else:
                data = (root/path).read_bytes()
            if data.startswith(b'SQLite format 3\0'):
                reasons.append('sqlite-database')
            if SECRET.search(data):
                reasons.append('high-confidence-secret-pattern')
            if any(needle in data for needle in needles):
                reasons.append('known-profile-phone')
            texts = [data.decode('utf-8', errors='ignore')]
            if b'\0' in data:
                texts.append(data.decode('utf-16-le', errors='ignore'))
            if any(name.search(text) for name in names for text in texts):
                reasons.append('known-profile-full-name-review')
            if reasons:
                findings.append({'path': path, 'reasons': reasons})
    finally:
        if reader:
            reader.stdin.close(); reader.stdout.close(); reader.wait()
    print(json.dumps({'mode': 'index' if args.index else 'working-tree', 'files': len(entries),
                      'known_profile_check': bool(needles), 'findings': findings,
                      'limits': 'Current candidates only; no history/OCR/license certification'}, indent=2))
    return bool(findings)


if __name__ == '__main__':
    raise SystemExit(main())
