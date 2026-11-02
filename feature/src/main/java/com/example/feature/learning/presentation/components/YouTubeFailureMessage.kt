package com.example.feature.learning.presentation.components

/** Provider IFrame codes describe a failure; they never grant rights or prove availability. */
internal fun youtubeFailureMessage(code: Int): String = when (code) {
    2 -> "رابط الفيديو غير صالح لدى YouTube. افتح المصدر الرسمي للتحقق."
    5 -> "واجه مشغل YouTube خطأ في العرض. أعد المحاولة أو افتح المصدر الرسمي."
    100 -> "الفيديو غير متاح؛ قد يكون خاصًا أو أُزيل من المصدر."
    101, 150 -> "لا يسمح الناشر بتضمين هذا الفيديو. يمكنك فتح المصدر الرسمي."
    153 -> "تعذر تحقق YouTube من هوية المشغل المضمّن. يمكنك فتح المصدر الرسمي."
    else -> "تعذر تشغيل الفيديو داخل التطبيق. أعد المحاولة أو افتح المصدر الرسمي."
}
