"""Summarize physical evidence; never infer audible sound or visible frames from PLAYING.

--images-reviewed records an explicit human review of every generated contact sheet.
Without that flag, image results remain Not Tested. Does not interact with the phone.
"""
import argparse
import collections
import csv
import json
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = ROOT / 'docs/release/external-evidence/2026-10-17'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('directory', type=Path)
    parser.add_argument('--images-reviewed', action='store_true')
    parser.add_argument('--audio-confirmed-id', action='append', default=[])
    parser.add_argument('--lifecycle-confirmed-id', action='append', default=[])
    parser.add_argument('--release-confirmed-id', action='append', default=[])
    args = parser.parse_args()
    directory = args.directory.resolve()
    assert directory.is_relative_to(EVIDENCE.resolve())
    metadata = json.loads((EVIDENCE / 'video-metadata.json').read_text(encoding='utf-8'))
    raw = [json.loads(line) for line in (directory / 'video-results.jsonl').read_text().splitlines() if line.strip()]
    by_id = {row['videoId']: row for row in raw}
    for theme in ('light', 'dark'):
        for page in range((len(raw) + 23) // 24):
            canvas = Image.new('RGB', (1280, 1230), '#eee' if theme == 'light' else '#222')
            draw = ImageDraw.Draw(canvas)
            for index, row in enumerate(raw[page * 24:page * 24 + 24]):
                path = directory / (row['videoId'] + ('_dark' if theme == 'dark' else '') + '.png')
                if not path.exists():
                    continue
                image = Image.open(path).convert('RGB').crop((0, 0, 1080, 608)).resize((320, 180))
                x, y = index % 4 * 320, index // 4 * 205
                canvas.paste(image, (x, y))
                draw.text((x + 5, y + 183), f"{page * 24 + index + 1} {row['videoId']}", fill='black' if theme == 'light' else 'white')
            canvas.save(directory / f'contact-{theme}-{page + 1}.jpg', quality=92)
    result = []
    for source in metadata:
        video_id = source['video_id']
        probe = by_id.get(video_id, {})
        before, after = probe.get('before') or {}, probe.get('after') or {}
        rect = after.get('rect') or [0, 0, 0, 0]
        geometry = bool(rect[2] >= 200 and rect[3] >= 200)
        progress = probe.get('progressConfirmed') is True
        light_exists = (directory / (video_id + '.png')).exists()
        dark_exists = (directory / (video_id + '_dark.png')).exists()
        image_reviewed = args.images_reviewed and light_exists and dark_exists
        audio_confirmed = video_id in args.audio_confirmed_id
        lifecycle_confirmed = video_id in args.lifecycle_confirmed_id
        row = dict(collection=source['collection'], number=source['number'], videoId=video_id,
                   title=source['actual_title'], channel=source['actual_channel'],
                   watchUrl=f'https://www.youtube.com/watch?v={video_id}',
                   rawStatus=probe.get('status', 'Not Tested'), rawReason=probe.get('reason', ''),
                   identity='Pass' if probe.get('requestedIdentityConfirmed') else 'Not Tested',
                   progress='Pass' if progress else 'Not Tested',
                   iframeGeometry='Pass' if geometry else 'Not Tested',
                   lightImage='Pass' if image_reviewed else 'Not Tested',
                   darkImage='Pass' if image_reviewed else 'Not Tested',
                   audio='Pass (owner observed sample)' if audio_confirmed else 'Not Tested',
                   fullscreen='Pass (separate Dark lifecycle sample)' if lifecycle_confirmed else 'Not Tested',
                   background='Pass (separate lifecycle sample)' if lifecycle_confirmed else 'Not Tested',
                   releasePlayback='Pass (separate native sample)' if video_id in args.release_confirmed_id else 'Not Tested',
                   muted=after.get('muted', 'Not Tested'), playerVolume=after.get('volume', 'Not Tested'),
                   beforeSeconds=before.get('time', ''), afterSeconds=after.get('time', ''),
                   testDurationMs=probe.get('finishedAtEpochMs', 0) - probe.get('startedAtEpochMs', 0),
                   screenshot=video_id + '.png' if light_exists else '',
                   darkScreenshot=video_id + '_dark.png' if dark_exists else '')
        interrupted = 'external window' in probe.get('reason', '').lower()
        row['overall'] = ('Not Tested' if interrupted else
                          'Fail' if probe.get('status') == 'Fail' else
                          'Blocked' if probe.get('reason') == 'EMBED_UNAVAILABLE_SEE_CORRELATED_LOGCAT' else
                          'Pass (sample scope)' if audio_confirmed and lifecycle_confirmed and image_reviewed and progress and geometry else
                          'Partial' if progress and image_reviewed and geometry else 'Not Tested')
        result.append(row)
    (directory / 'adjudicated-video-results.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
    with (directory / 'adjudicated-video-results.csv').open('w', encoding='utf-8-sig', newline='') as stream:
        writer = csv.DictWriter(stream, fieldnames=list(result[0]))
        writer.writeheader()
        writer.writerows(result)
    summary = dict(inventory=len(metadata), recorded=len(raw),
                   progress=sum(row['progress'] == 'Pass' for row in result),
                   nonzeroIframe=sum(row['iframeGeometry'] == 'Pass' for row in result),
                   reviewedImages=sum(row['lightImage'] == 'Pass' and row['darkImage'] == 'Pass' for row in result),
                   rawStatuses=dict(collections.Counter(row['rawStatus'] for row in result)),
                   overallStatuses=dict(collections.Counter(row['overall'] for row in result)),
                   audioScope=f'Owner-observed samples for {len(set(args.audio_confirmed_id))} IDs only; other IDs Not Tested for audible audio.',
                   durationScope='Short playback excerpts only; no end-to-end full-length or sustained performance certification.')
    (directory / 'adjudication-summary.json').write_text(json.dumps(summary, indent=2), encoding='utf-8')
    print(json.dumps(summary, indent=2))


if __name__ == '__main__':
    main()
