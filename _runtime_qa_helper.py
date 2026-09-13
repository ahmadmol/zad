# Temporary runtime QA helper — do not commit.
import re
import html
import subprocess
import time
import sys

ADB = r"C:\android_sdk\platform-tools\adb.exe"


def adb(*args):
    return subprocess.check_output([ADB, *args], stderr=subprocess.STDOUT)


def resumed():
    out = adb("shell", "dumpsys", "activity", "activities").decode("utf-8", "ignore")
    for line in out.splitlines():
        if "topResumedActivity" in line:
            return line.strip()
    return ""


def dump_descs():
    adb("shell", "uiautomator", "dump", "/sdcard/ui_mol.xml")
    adb("pull", "/sdcard/ui_mol.xml", "ui_mol.xml")
    raw = open("ui_mol.xml", "r", encoding="utf-8").read()
    items = []
    for m in re.finditer(
        r'content-desc="([^"]*)"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', raw
    ):
        d = html.unescape(m.group(1)).replace("\n", " | ")
        if d.strip():
            items.append(
                (
                    d,
                    int(m.group(2)),
                    int(m.group(3)),
                    int(m.group(4)),
                    int(m.group(5)),
                )
            )
    return items


def tap_bounds(b):
    x = (b[1] + b[3]) // 2
    y = (b[2] + b[4]) // 2
    adb("shell", "input", "tap", str(x), str(y))


def find(items, *needles):
    for it in items:
        low = it[0]
        if all(n in low for n in needles):
            return it
    return None


def shot(name):
    adb("shell", "screencap", "-p", f"/sdcard/{name}")
    adb("pull", f"/sdcard/{name}", name)


def back_home():
    adb("shell", "input", "keyevent", "4")
    time.sleep(0.9)
    r = resumed()
    if "com.example.mol" not in r:
        adb("shell", "am", "start", "-n", "com.example.mol/.MainActivity")
        time.sleep(1.5)
        r = resumed()
    return r


def open_and_check(label, item):
    print(f"=== {label} ===")
    if not item:
        print("NOT FOUND")
        return False
    tap_bounds(item)
    time.sleep(2.0)
    r = resumed()
    print(r)
    ok = "com.example.mol" in r and "MainActivity" not in r.replace(
        "com.example.mol/.MainActivity", ""
    )
    # MainActivity still means home; for child we expect still mol package.
    # Better: child routes keep MainActivity in single-activity nav.
    # Nav host is single activity — child screens still MainActivity!
    # So check via UI dump for back button / title instead.
    shot(f"runtime-{label}.png")
    items = dump_descs()
    has_bottom = any("الرئيسية" in i[0] and "إحسان" in "".join(x[0] for x in items) for i in items)
    # Detect bottom nav by three labels present together
    texts = " | ".join(i[0] for i in items)
    bn_visible = ("الرئيسية" in texts) and ("إحسان" in texts) and ("حسابي" in texts)
    print(f"bottom_nav_visible={bn_visible}")
    print(f"sample_titles={[i[0][:30] for i in items[:8]]}")
    r2 = back_home()
    print(f"back={r2}")
    time.sleep(0.8)
    return True


def main():
    adb("shell", "am", "start", "-n", "com.example.mol/.MainActivity")
    time.sleep(2)
    print(resumed())
    items = dump_descs()
    for i in items:
        print(f"{i[0][:55]:55} [{i[1]},{i[2]}]-[{i[3]},{i[4]}]")

    targets = [
        ("quran", ("القرآن",)),
        ("hadith", ("الأحاديث",)),
        ("azkar", ("الأذكار",)),
        ("tasbih", ("التسبيح",)),
        ("prayer", ("مواقيت الصلاة",)),
        ("dua", ("دعاء",)),
        ("asma", ("أسماء الله",)),
        ("qibla", ("البوصلة",)),
        ("haram", ("بث الحرم",)),
        ("nabawi", ("بث المسجد النبوي",)),
        ("search", ("البحث",)),
        ("daily", ("النشاطات",)),
        ("reminders", ("التذكيرات",)),
        ("statistics", ("الإحصائيات",)),
    ]

    results = {}
    for key, needles in targets:
        items = dump_descs()
        # may need scroll for lower items
        it = None
        for nset in [needles]:
            it = find(items, *nset)
            if it:
                break
        if not it:
            adb("shell", "input", "swipe", "540", "1700", "540", "900", "300")
            time.sleep(0.8)
            items = dump_descs()
            it = find(items, *needles)
        results[key] = open_and_check(key, it)
        # ensure on home for next
        adb("shell", "am", "start", "-n", "com.example.mol/.MainActivity")
        time.sleep(1.2)

    print("RESULTS", results)


if __name__ == "__main__":
    main()
