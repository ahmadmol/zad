# نطاق تغييرات مهمة المصادر — 17 أكتوبر 2026

المشروع كان يحتوي تعديلات كثيرة سابقة غير محفوظة؛ لم تُرجع أو تُحذف. هذه القائمة تخص هذه المهمة فقط، ولا تدّعي أن كل `git status` نتج عنها.

- `LiveStreamSources.kt` و`LiveStreamScreen.kt`: رابط بوابة الحرمين الرسمية الخارجي، بنفس مكونات وألوان النظام الحالي.
- `ContentAttributionsDialog.kt`: الوصول إلى البوابة، وإفصاح حقوق الصور القديمة والأذان.
- ملفات `SanhyaFixtureData.kt` و`NabiIhsanFixtureData.kt` و`FahmFixtureData.kt`: إصلاح روابط المصدر/المشاركة المختصرة التي أعادت 404 إلى روابط YouTube الصحيحة المقابلة؛ لا تبديل للمعرفات أو العناوين أو الصور.
- `PhysicalDeviceMediaAuditTest.kt`: إطلاق نشاط QA أماميًا على الهاتف، إبقاء نافذته مضاءة، التوقف عند فقدان التركيز، وتسجيل البثين. لا تغيير إعدادات OEM أو مسح بيانات. تأكيد PLAYING لا يعد نجاحًا بصريًا، وقد أثبت المالك مشكلة الصورة أثناء الجولة.
- سكربتات جديدة: `external_resources_audit.py`، `external_asset_visual_inventory.py`، `validate_external_resources.py`، `finalize_external_resource_qa.py`، `run_external_media_phone.ps1`، `restore_external_phone_qa.ps1`.
- سجلا CSV، سجل القرارات، الإسناد، تدقيق الحاجة البصرية، التقرير وتحقق QA، ولقطات/استجابات وبصمات الأدلة داخل `docs/release/`. تحديث سجلّي المصادر السابقين بروابط التدقيق الجديد مع حفظ الأساس التاريخي.

لم تُعدل ملفات الصور أو القرآن أو الحديث أو Room أو DataStore أو مخازن بيانات الهاتف. لم يضف Backend أو مفتاح API أو تسجيل تلاوة أو صوت أذان جديد، ولم يُنفذ Commit أو Push أو Git Reset. النسخة الأصلية لـQA محفوظة محليًا وتُعاد دون إزالة تثبيت.
