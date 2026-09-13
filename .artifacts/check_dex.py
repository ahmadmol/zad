import zipfile
import os

def check(path):
    if not os.path.exists(path):
        print(f"NOT FOUND: {path}")
        return
    z = zipfile.ZipFile(path)
    needle1 = "أهلاً بك".encode("utf-8")
    needle2 = "يداً بيد".encode("utf-8")
    needle3 = b"Entirely Merciful"
    needle4 = b"Ar-Rahman"
    found = set()
    for n in z.namelist():
        if n.endswith(".dex"):
            data = z.read(n)
            for tag, nd in [("greeting_old", needle1), ("yadan_bayd_new", needle2),
                            ("Entirely_Merciful", needle3), ("Ar_Rahman", needle4)]:
                if nd in data:
                    found.add(tag)
    print(path)
    print("  FOUND:", found)

check(r"C:\Users\WIN 10\Desktop\New folder (2)\Sdk\Sdk\mol\app\build\outputs\apk\debug\app-debug.apk")
check(r"C:\Users\WIN 10\Desktop\New folder (2)\Sdk\Sdk\mol\app\build\intermediates\apk\debug\app-debug.apk")
check(r"C:\Users\WIN 10\Desktop\New folder (2)\Sdk\Sdk\mol\.artifacts\installed.apk")
