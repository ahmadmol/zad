import zipfile
z = zipfile.ZipFile(r"C:\Users\WIN 10\Desktop\New folder (2)\Sdk\Sdk\mol\.artifacts\installed.apk")
# Check if there's a strings.arsc
arsc = z.read("resources.arsc")
needle1 = "أهلاً بك في إحسان".encode("utf-8")
needle2 = "يداً بيد".encode("utf-8")
needle3 = "إحسان المحلي".encode("utf-8")
needle4 = "يمكنك إضافة أول حالة".encode("utf-8")
for tag, nd in [("greeting_long", needle1), ("yadan", needle2), ("ehsan_local", needle3), ("empty_ehsan", needle4)]:
    if nd in arsc:
        print(f"FOUND in resources.arsc: {tag}")
    else:
        print(f"NOT in resources.arsc: {tag}")
