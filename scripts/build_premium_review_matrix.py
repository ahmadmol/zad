"""Append scoped premium source evidence to the historical screen matrix, preserving its fields.

This is a source inventory. It does not render a screen or assert visual/device acceptance.
"""
import csv
import hashlib
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MATRIX = ROOT / "docs/planning/IHSAN_UI_UX_SCREEN_MATRIX.csv"

SCOPES = {
    "UI-001": "Readiness-driven launch; native API31 splash; restoration release; no fixed delay",
    "UI-002": "Separated header; original Aqsa illustration; prayer hero; six services; explicit goals; responsive journey; timeline; lazy content",
    "UI-004": "Unified header; original prayer artwork; neutral hero; stable prayer keys; existing facade/settings/journal",
    "UI-005": "Reading introduction; shared header; stable surah/search keys; existing reader navigation and search retained",
    "UI-006": "Approved paper reader and canonical text preserved; shared surrounding theme/components only",
    "UI-007": "Shared large header; visible favorite action; existing category/counter persistence retained",
    "UI-011": "Three persisted palettes; appearance/language/prayer/location/privacy/local-data/support groups",
    "UI-014": "Shared community header; full-width 16:9 images with crop/error fallback; status colors; readable cards/tabs",
    "UI-015": "Quiet profile header; existing avatar/ownership/data/settings; support entry",
    "UI-025": "Shared header; photo preview; confirmed draft removal; failed writes and stored profile retained",
    "UI-026": "Shared header; explicit refresh permission action; sensor/location error/lifecycle preserved",
    "UI-027": "Four original-vector pager pages; one CTA; animated accessible indicator; saved completion; large-type scrolling",
    "UI-028": "Optional location and notification setup; manual city; denial/settings/continue; lifecycle permission refresh",
    "UI-029": "Unified search header; semantic palette accents; existing saved query/results/navigation retained",
    "UI-030": "Shared palette/components; explicit recorded daily counts retained",
    "UI-031": "Unified header adapter; semantic palette accents; redundant scenic layer removed; explicit accepted counts retained",
    "UI-033": "Shared restricted native YouTube owner; provider error reason; retry; lifecycle/fullscreen/disposal; official fallback",
    "UI-034": "Shared restricted native YouTube owner; provider error reason; retry; lifecycle/fullscreen/disposal; official fallback",
    "UI-070": "Neutral widget night resource colors; existing layout/provider/facade/launcher behavior retained",
}


def main():
    with MATRIX.open(encoding="utf-8-sig", newline="") as source:
        reader = csv.DictReader(source)
        original_fields = list(reader.fieldnames)
        rows = list(reader)
    # Idempotence: four product additions, not a new campaign/batch.
    rows = [row for row in rows if row["screen_id"] not in {"UI-071", "UI-072", "UI-073", "UI-074"}]
    for ident, name, route, component, file, scope in [
        ("UI-071", "Services", "services", "AllServicesScreen", "dashboard/presentation/components/HomeDiscoverServicesGrid.kt", "Real destination; searchable grouped services; saved query/grid state; child Back preserves origin"),
        ("UI-072", "DailyGoals", "daily_goals", "DailyGoalsScreen", "goals/DailyGoalsScreen.kt", "Explicit local goals CRUD/archive/count/undo/weekdays/stats; atomic additive DataStore persistence"),
        ("UI-073", "Support", "support", "SupportScreen", "support/SupportScreen.kt", "Usage/FAQ/feedback draft/copy/about/version; configurable approved email; no fabricated submission"),
        ("UI-074", "HaramainVisit", "haramain_visit", "HaramainVisitScreen", "visit/HaramainVisitScreen.kt", "Official Nusuk information/service links; external provider; no booking, availability or travel data collection"),
    ]:
        row = {field: "" for field in original_fields}
        row.update(screen_id=ident, screen=name, route_pattern=route, case_id=route,
            in_existing_59_cases="new-local-surface", component=component,
            source_files="feature/src/main/java/com/example/feature/" + file,
            evidence="Premium host compilation/JVM/source inventory; Android tests compiled only",
            status="IMPLEMENTED / SOURCE VERIFIED / VISUAL NOT VERIFIED")
        rows.append(row)
        SCOPES[ident] = scope
    changed = set(subprocess.check_output(["git", "diff", "--name-only", "HEAD"], cwd=ROOT).decode().splitlines())
    extras = ["premium_status", "premium_scope", "premium_source_hashes", "premium_visual", "premium_backend", "premium_content"]
    fields = original_fields + [field for field in extras if field not in original_fields]
    backend_ids = {f"UI-{number:03}" for number in [14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 68, 69]}
    content_ids = {f"UI-{number:03}" for number in [5, 6, 9, 10, 33, 34, 36, 37, 38, 46, 47, 48, 52, 55, 65, 67]}
    compact = []
    for row in rows:
        ident = row["screen_id"]
        paths = re.findall(r"(?:feature|app|designsystem)/[^;؛\s]+?\.(?:kt|xml)", row["source_files"])
        hashes = []
        for relative in paths:
            path = ROOT / relative
            if not path.is_file():
                raise SystemExit(f"Missing source for {ident}: {relative}")
            hashes.append(relative + "=" + hashlib.sha256(path.read_bytes()).hexdigest())
        if not hashes:
            raise SystemExit(f"No source evidence for {ident}")
        scope = SCOPES.get(ident, "Screen presentation and shared system updated; existing data/actions retained" if any(path in changed for path in paths)
            else "Shared theme/header/component inheritance; existing leaf layout and behavior retained")
        backend = "BACKEND DEPENDENT: public sync/auth/publication only; local behavior preserved" if ident in backend_ids else "Local scope"
        content = "CONTENT/RIGHTS PENDING: see completion report and media inventory" if ident in content_ids else "Existing approved data retained; new vector artwork original"
        flags = "IMPLEMENTED; SOURCE VERIFIED; VISUAL NOT VERIFIED"
        if ident in backend_ids:
            flags += "; BACKEND DEPENDENT"
        if ident in content_ids:
            flags += "; CONTENT/RIGHTS PENDING"
        row.update(premium_status=flags, premium_scope=scope, premium_source_hashes="; ".join(hashes),
            premium_visual="VISUAL NOT VERIFIED; no phone/emulator/host screenshot execution", premium_backend=backend, premium_content=content)
        compact.append(dict(screen_id=ident, screen=row["screen"], route=row["route_pattern"], source_files=row["source_files"],
            implementation_scope=scope, status=flags, source_verification="Source exists/hash recorded; shared Android compilation and host regressions; not device execution",
            visual="VISUAL NOT VERIFIED", backend=backend, content=content, source_hashes="; ".join(hashes)))
    for path, data, columns in [(MATRIX, rows, fields), (ROOT / "docs/qa/IHSAN_PREMIUM_UI_SCREEN_MATRIX.csv", compact, list(compact[0]))]:
        with path.open("w", encoding="utf-8-sig", newline="") as output:
            writer = csv.DictWriter(output, fieldnames=columns)
            writer.writeheader(); writer.writerows(data)
    print(f"Recorded {len(rows)} source surfaces; all visual acceptance explicitly unverified.")


if __name__ == "__main__":
    main()
