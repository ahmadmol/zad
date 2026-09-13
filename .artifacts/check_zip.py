import zipfile
z = zipfile.ZipFile(r"C:\Users\WIN 10\Desktop\New folder (2)\Sdk\Sdk\mol\.artifacts\installed.apk")
for n in z.namelist():
    if n.startswith("res/layout") or n.startswith("res/drawable") or "dashboard" in n.lower():
        print(n)
