package com.example.feature.core.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ContentAttributionsDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("المصادر والتراخيص") },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("نسخة محلية للفحص. المحتوى الذي يحتاج ترخيصًا أو مراجعة إضافية لا يعد معتمدًا للإطلاق.")
                Text("القرآن: Tanzil Project — النص العثماني، إصدار 1.1. ترخيص CC BY 3.0 مع شرط الحفاظ على النص دون تعديل. إشعار الحقوق ونسخة المصدر الأصلية مرفقان بالتطبيق.")
                SourceLink("مصدر القرآن", "https://tanzil.net/docs/Text_License")
                SourceLink("ترخيص CC BY 3.0", "https://creativecommons.org/licenses/by/3.0/")
                Text("حدود صفحات المصحف والأجزاء: بيانات Tanzil، قورنت الصفحات الـ604 والأجزاء الـ30. الخط Amiri Quran بترخيص OFL 1.1.")
                SourceLink("بيانات Tanzil", "https://tanzil.net/docs/Quran_Metadata")
                SourceLink("خط Amiri وترخيصه", "https://github.com/aliftype/amiri")
                Text("الحديث: 3582 سجلًا من موسوعة HadeethEnc.com، إصدار العربية v1.7.0 بتاريخ تحديث الناشر 2025-11-12. النص والشرح والفوائد والحكم محفوظة كما في المصدر. الأذكار تعرض المراجع المتوفرة؛ بعض أهداف العدّاد اختيارية وليست أعدادًا منسوبة للسنة.")
                SourceLink("موسوعة الحديث وشروط النشر", "https://hadeethenc.com/ar/")
                Text("الأدعية: 30 آية كاملة من Tanzil و9 أحاديث كاملة السياق من HadeethEnc v1.7.0، مع المراجع؛ تصنيفات التصفح لا تضيف أوقاتًا أو فضائل دينية. معاني الأسماء تحريرية وتحتاج توثيقًا ومراجعة. التفسير الموثق غير متاح حاليًا.")
                Text("الصلاة: حساب محلي باستخدام Adhan من Batoul Apps (MIT)، بناءً على المدينة أو الموقع والطريقة المختارة. الحساب قد يختلف عن التقويم المحلي. التاريخ الهجري حسابي ولا يثبت رؤية الهلال.")
                SourceLink("مكتبة حساب الصلاة", "https://github.com/batoulapps/adhan-kotlin")
                Text("تسجيلات EveryAyah وQuran Foundation: PENDING_LICENSE؛ التشغيل والتنزيل غير مفعّلين حتى اعتماد الحقوق وإعداد الوصول المسموح. الفيديوهات تفتح عبر مشغل YouTube الرسمي أو المصدر الخارجي؛ لا يعاد استضافتها. البث يتبع توفر الناشر واتصالك.")
                SourceLink("EveryAyah", "https://everyayah.com/")
                SourceLink("بوابة الحرمين الرسمية — مشاهدة خارج التطبيق", com.example.feature.live.LiveStreamSources.OFFICIAL_LIVE_PORTAL)
                Text("الصور والخلفيات الحالية محفوظة. توثيق ملكية بعض الأصول القديمة وإذن توزيع تسجيل الأذان العام ما زال قيد الاستكمال؛ لا يثبت وجود الملف حق إعادة توزيعه.")
                Text("مجتمع إحسان محلي على جهازك. سجلات DEMO خيالية، وحالتها ليست تبرعًا أو دفعًا أو تواصلًا حقيقيًا. بيانات المستخدم مستقلة عن أمثلة DEMO.")
            }
        }, confirmButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = onDismiss) { Text("إغلاق") } })
}

@Composable
private fun SourceLink(title: String, url: String) {
    val context = LocalContext.current
    TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            .onFailure { com.example.feature.core.notification.UserMessageNotifier.notify(context, "تعذر فتح المصدر في المتصفح") }
    }) { Text(title) }
}
