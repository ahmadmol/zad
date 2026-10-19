"""Collect executed evidence. Never infer runtime PASS from source or build existence."""
from pathlib import Path
import json, re, xml.etree.ElementTree as ET, hashlib, shutil

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / '.artifacts/video-audit-20261015'
result = {'unit': {}, 'android': {}, 'matrix': {}, 'lint': {}, 'apks': {}}
for module in ('app', 'feature', 'designsystem'):
    suites = []
    for path in (ROOT / module / 'build/test-results/testDebugUnitTest').glob('TEST-*.xml'):
        tree = ET.parse(path).getroot()
        suites.append({'suite': tree.attrib['name'], **{k: int(tree.attrib.get(k, 0)) for k in ('tests', 'failures', 'errors', 'skipped')}})
    result['unit'][module] = {'tests': sum(s['tests'] for s in suites),
                             'failures': sum(s['failures'] + s['errors'] for s in suites), 'suites': suites}
    lint = ROOT / module / 'build/reports/lint-results-debug.xml'
    if lint.exists():
        issues = ET.parse(lint).getroot().findall('issue')
        result['lint'][module] = {'errors': sum(i.attrib.get('severity') in ('Error','Fatal') for i in issues),
                                 'warnings': sum(i.attrib.get('severity') == 'Warning' for i in issues),
                                 'report': lint.relative_to(ROOT).as_posix()}
for path in sorted((BASE / 'runtime').glob('*.log')):
    text = path.read_text(encoding='utf-8-sig', errors='replace')
    passed = re.search(r'OK \((\d+) tests?\)', text)
    if path.name.startswith(('pull-', 'crash-', 'youtube-')):
        continue
    entry = {'status': 'EXPECTED_RED_BEFORE_FIX' if path.stem == 'alarm-before-fix' else ('PASS' if passed else 'FAIL_OR_INCOMPLETE'), 'tests': int(passed[1]) if passed else 0,
             'log': path.relative_to(ROOT).as_posix(),
             'completed_methods': re.findall(r'INSTRUMENTATION_STATUS: test=([^\n]+)\nINSTRUMENTATION_STATUS_CODE: 0', text.replace('\r',''))}
    if path.name.startswith('matrix-'):
        final_folder = path.parent / 'final-source-matrix' / path.stem.removeprefix('matrix-')
        folder = final_folder if final_folder.exists() else path.parent / path.stem
        entry['capture_folder'] = folder.relative_to(ROOT).as_posix()
        count = folder / 'case-count.txt'
        entry['render_cases'] = int(count.read_text().strip()) if count.exists() else 0
        entry['screenshots'] = len(list(folder.glob('*.png')))
        for file, key in [('geometry.txt','geometry_differences'), ('overflow.txt','overflow_failures')]:
            evidence = folder / file
            entry[key] = len(evidence.read_text(encoding='utf-8').splitlines()) if evidence.exists() else None
        result['matrix'][path.stem] = entry
    else: result['android'][path.stem] = entry
for path in sorted((ROOT / 'docs/release/apk').glob('*20261015*.apk')):
    raw = path.read_bytes()
    result['apks'][path.name] = {'file': path.relative_to(ROOT).as_posix(), 'bytes': len(raw), 'sha256': hashlib.sha256(raw).hexdigest()}
result['unit_total'] = sum(m['tests'] for m in result['unit'].values())
result['unit_failures'] = sum(m['failures'] for m in result['unit'].values())
result['matrix_completed'] = sum(m['status'] == 'PASS' for m in result['matrix'].values())
result['render_cases'] = sum(m['render_cases'] for m in result['matrix'].values() if m['status'] == 'PASS')
final_names = {'p0-final', 'core-community-final', 'functional-final', 'system-delivery', 'alarm-after-fix',
               'restart-prepare', 'restart-verify', 'media-all-videos', 'native-and-mushaf', 'feature-instrumented', 'release-smoke',
               'final-interactions', 'timezone-change'}
result['android_passed_tests'] = sum(m['tests'] for name, m in result['android'].items() if m['status'] == 'PASS' and name in final_names)
(BASE / 'qa-results.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({k:v for k,v in result.items() if k not in ('unit','android','matrix','lint','apks')}, ensure_ascii=False))
