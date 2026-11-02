"""Exercise the privacy gate in disposable repositories, without real user data."""
import json
import sqlite3
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

SCANNER = Path(__file__).with_name('verify_source_privacy.py').resolve()


class SourcePrivacyTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix='ihsan-privacy-')
        self.root = Path(self.temp.name)
        subprocess.run(['git', 'init', '-q', str(self.root)], check=True)

    def tearDown(self):
        self.temp.cleanup()

    def stage(self, path, data):
        target = self.root/path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
        subprocess.run(['git', 'add', '--', path], cwd=self.root, check=True, capture_output=True)

    def scan(self, *args):
        result = subprocess.run([sys.executable, str(SCANNER), *args], cwd=self.root, capture_output=True, text=True)
        return result, json.loads(result.stdout)

    def test_build_inputs_and_room_schemas_remain_eligible(self):
        self.stage('feature/schemas/11.json', b'{"version":11}')
        self.stage('gradle/wrapper/gradle-wrapper.jar', b'synthetic wrapper fixture')
        result, report = self.scan('--index')
        self.assertEqual(0, result.returncode)
        self.assertEqual(2, report['files'])

    def test_raw_qa_output_is_rejected_even_without_personal_content(self):
        self.stage('runtime-example.txt', b'synthetic')
        result, report = self.scan('--index')
        self.assertEqual(1, result.returncode)
        self.assertIn('private-or-generated-artifact-path', report['findings'][0]['reasons'])

    def test_sqlite_header_is_rejected_under_an_innocent_extension(self):
        self.stage('fixture.bin', b'SQLite format 3\0synthetic')
        result, report = self.scan('--index')
        self.assertEqual(1, result.returncode)
        self.assertIn('sqlite-database', report['findings'][0]['reasons'])

    def test_index_scan_checks_staged_content_and_never_prints_a_match(self):
        token = ('ghp_' + 'x' * 36).encode()
        self.stage('example.txt', token)
        (self.root/'example.txt').write_text('safe working copy')
        result, report = self.scan('--index')
        self.assertEqual(1, result.returncode)
        self.assertNotIn(token.decode(), result.stdout + result.stderr)
        self.assertIn('high-confidence-secret-pattern', report['findings'][0]['reasons'])
        result, report = self.scan()
        self.assertEqual(0, result.returncode)

    def test_known_identity_check_uses_a_read_only_local_fixture_without_disclosure(self):
        fixture = self.root/'private.sqlite'
        connection = sqlite3.connect(fixture)
        connection.execute('CREATE TABLE users(firstName TEXT,lastName TEXT,phoneNumber TEXT)')
        connection.execute('INSERT INTO users VALUES(?,?,?)', ('Private', 'Fixture Identity', 'NONCALLABLE-TEST-PHONE'))
        connection.commit(); connection.close()
        before = fixture.read_bytes()
        self.stage('example.txt', b'Private Fixture Identity')
        result, report = self.scan('--index', '--known-profile-db', str(fixture))
        self.assertEqual(1, result.returncode)
        self.assertIn('known-profile-full-name-review', report['findings'][0]['reasons'])
        self.assertNotIn('Private Fixture Identity', result.stdout + result.stderr)
        self.assertNotIn('NONCALLABLE-TEST-PHONE', result.stdout + result.stderr)
        self.assertEqual(before, fixture.read_bytes())


if __name__ == '__main__':
    unittest.main()
