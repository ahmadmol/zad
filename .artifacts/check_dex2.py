import zipfile
z = zipfile.ZipFile(r"C:\Users\WIN 10\Desktop\New folder (2)\Sdk\Sdk\mol\.artifacts\installed.apk")
needle = "أهلاً بك في إحسان".encode("utf-8")
for n in z.namelist():
    if n.endswith(".dex"):
        data = z.read(n)
        if needle in data:
            print(f"FOUND in {n}, size={len(data)}")
            # Find context around it
            idx = data.find(needle)
            ctx_before = data[max(0, idx-150):idx]
            ctx_after = data[idx:idx+200]
            try:
                print("BEFORE:", ctx_before.decode("utf-8", errors="replace"))
                print("MATCH:", ctx_after.decode("utf-8", errors="replace"))
            except Exception as e:
                print("decode error:", e)
