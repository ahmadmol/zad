"""Inventory the existing approved catalog; metadata probes never certify playback or rights.

Writes public catalog metadata only. No phone, credentials, or private databases are used.
Run with --probe to recheck the provider's public oEmbed metadata.
"""
import argparse
import concurrent.futures
import csv
import datetime
import json
import re
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def catalog():
    rows = []
    for section, relative in [
        ("sanhya", "sanhya/data/datasource/SanhyaFixtureData.kt"),
        ("nabi", "nabiihsan/data/datasource/NabiIhsanFixtureData.kt"),
        ("fahm", "fahmanallah/data/datasource/FahmFixtureData.kt"),
    ]:
        source = ROOT / "feature/src/main/java/com/example/feature" / relative
        text = source.read_text(encoding="utf-8")
        program = re.search(r'const val PROGRAM_URL = "([^"]+)"', text)
        channel = re.search(r'const val OFFICIAL_CHANNEL_ID = "([^"]+)"', text).group(1)
        for index, record in enumerate(re.findall(r'^\s*EpisodeRecord\((.+)\),?$', text, re.M), 1):
            tokens = re.findall(r'"(?:\\.|[^"\\])*"|\bnull\b|\b\d+\b|\btrue\b|\bfalse\b', record)
            values = [json.loads(token) for token in tokens]
            number, title, video = (values[0], values[1], values[2]) if section != "fahm" else (index, values[0], values[1])
            rows.append(dict(section=section, id=f"ep_{number:02d}", title=title,
                video_id=video or "", original_source_id=str(values[5]) if section != "fahm" else f"playlist:{number}",
                url=f"https://www.youtube.com/watch?v={video}" if video else "",
                catalog_source=program.group(1) if program else "https://amrkhaled.net/",
                expected_channel=channel, source_file=source.relative_to(ROOT).as_posix()))
    for name, video in [("haram", "Cm1v4bteXbI"), ("nabawi", "9A1S0xAPVIs")]:
        rows.append(dict(section="live", id=name, title="قناة القرآن الكريم" if name == "haram" else "قناة السنة النبوية",
            video_id=video, original_source_id=video, url=f"https://www.youtube.com/watch?v={video}",
            catalog_source="https://www.sba.sa/", expected_channel="Saudi Broadcasting Authority",
            source_file="feature/src/main/java/com/example/feature/live/LiveStreamSources.kt"))
    return rows


def probe(row):
    result = dict(row, checked_utc=datetime.datetime.now(datetime.timezone.utc).isoformat(),
        metadata_status="NOT_PROBED", provider_title="", provider_author="", provider_author_url="",
        publisher_identity="UNCONFIRMED", content_match="NOT_VIEWED; METADATA_ONLY",
        title_match="NOT_VERIFIED", embedding="NOT_EXECUTED", rights="CONTENT/RIGHTS PENDING",
        in_app_playback="VISUAL NOT VERIFIED")
    if not row["video_id"]:
        result.update(metadata_status="CATALOG_UNAVAILABLE", title_match="MISSING_APPROVED_MAPPING")
        return result
    url = "https://www.youtube.com/oembed?" + urllib.parse.urlencode({"url": row["url"], "format": "json"})
    try:
        request = urllib.request.Request(url, headers={"User-Agent": "Ihsan-source-metadata-audit/1.0"})
        with urllib.request.urlopen(request, timeout=18) as response:
            metadata = json.load(response)
        result.update(metadata_status="OEMBED_METADATA_RETURNED", provider_title=metadata.get("title", ""),
            provider_author=metadata.get("author_name", ""), provider_author_url=metadata.get("author_url", ""))
        if metadata.get("author_name"):
            result["publisher_identity"] = "PUBLIC_AUTHOR_METADATA_ONLY; RIGHTS_NOT_GRANTED"
        normalize = lambda value: " ".join(value.split())
        if not metadata.get("title") or not metadata.get("author_name"):
            result["metadata_status"] = "OEMBED_HTML_ONLY; PUBLISHER_UNCONFIRMED"
        result["title_match"] = "EXACT_METADATA" if normalize(row["title"]) == normalize(result["provider_title"]) else "OWNER_REVIEW_REQUIRED"
        result["embedding"] = "OEMBED_HTML_RETURNED; ACTUAL_PLAYBACK_PENDING" if metadata.get("html") else "NO_EMBED_METADATA"
    except urllib.error.HTTPError as error:
        result["metadata_status"] = f"HTTP_{error.code}; AVAILABILITY_UNCONFIRMED"
    except Exception as error:
        result["metadata_status"] = f"PROBE_FAILED_{type(error).__name__}; AVAILABILITY_UNCONFIRMED"
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--probe", action="store_true")
    parser.add_argument("--output", default="docs/qa/IHSAN_PREMIUM_MEDIA_INVENTORY.csv")
    args = parser.parse_args()
    rows = catalog()
    if args.probe:
        with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
            rows = list(pool.map(probe, rows))
    else:
        rows = [dict(row, metadata_status="NOT_PROBED", rights="CONTENT/RIGHTS PENDING", in_app_playback="VISUAL NOT VERIFIED") for row in rows]
    destination = ROOT / args.output
    destination.parent.mkdir(parents=True, exist_ok=True)
    with destination.open("w", encoding="utf-8-sig", newline="") as output:
        writer = csv.DictWriter(output, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)
    counts = {}
    for row in rows:
        counts[row["metadata_status"]] = counts.get(row["metadata_status"], 0) + 1
    print(json.dumps({"rows": len(rows), "metadata_results": counts}, ensure_ascii=False))


if __name__ == "__main__":
    main()
