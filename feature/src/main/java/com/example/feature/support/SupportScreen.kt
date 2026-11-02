package com.example.feature.support

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.core.util.openExternalActivity

/** Populate only after the owner approves a public contact address. Never derive it from a private profile. */
object IhsanPublicSupport { val approvedEmail: String? = null }

@Composable
fun SupportScreen(onBack: () -> Unit, approvedEmail: String? = IhsanPublicSupport.approvedEmail) {
    val context = LocalContext.current
    val version = remember(context) { runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull().orEmpty().ifBlank { "غير متاح" } }
    var message by rememberSaveable { mutableStateOf("") }
    var suggestion by rememberSaveable { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    val contact = approvedEmail?.takeIf { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }
    val subject = if (suggestion) "اقتراح لإحسان" else "الإبلاغ عن مشكلة في إحسان"
    val body = "$subject\nإصدار التطبيق: $version\n\n$message"
    Column(Modifier.fillMaxSize()) {
        IhsanTopBar("الدعم والمساعدة", onNavigateBack = onBack)
        LazyColumn(Modifier.weight(1f).imePadding().navigationBarsPadding(), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item(key = "support_intro") {
                IhsanGlassCard(Modifier.fillMaxWidth()) {
                    IhsanIconContainer(Icons.Outlined.HelpOutline)
                    Text("معك في كل خطوة", style = MaterialTheme.typography.headlineSmall)
                    Text("إجابات واضحة، ومكان لأفكارك", style = MaterialTheme.typography.bodyLarge,
                        color = IhsanTheme.colors.textSecondary)
                }
            }
            item(key = "support_howto") {
                IhsanContentCard {
                    IhsanSectionHeader("ابدأ من هنا")
                    Text("من الرئيسية افتح الصلاة لضبط المدينة والتنبيهات، أو أكمل القرآن من موضعك. في «عرض الكل» تجد جميع الأدوات وأقسام التعلم.")
                    Text("الأهداف اليومية اختيارية، ولا تسجّل الصلاة أو الصيام تلقائيًا. سجلات العبادات لها إدخالات مستقلة منك.")
                }
            }
            item(key = "support_faq") {
                IhsanContentCard {
                    IhsanSectionHeader("الأسئلة الشائعة")
                    Faq("كيف أستخدم التطبيق دون إذن الموقع؟", "اختر مدينتك يدويًا من إعدادات الموقع أو شاشة الصلاة. يمكنك تعديلها في أي وقت.")
                    Faq("لماذا لا تصل التذكيرات؟", "راجع إذن الإشعارات وتنبيهات الصلاة وإعدادات البطارية في النظام. قد تتأثر مواعيد التنبيه بقيود الجهاز؛ الإذن وحده لا يضمن وصولها.")
                    Faq("أين تُحفظ بياناتي؟", "الملف والسجلات والمفضلة والمسودات محفوظة محليًا. لا توجد مزامنة سحابية أو مصادقة عامة حاليًا. الوسائط والخرائط تستخدم خدمات خارجية عند فتحها.")
                    Faq("لماذا لا يعمل بعض الصوت أو الفيديو؟", "تتوفر الوسائط بحسب المصدر والاتصال وحقوق الاستخدام. عند تعذر التضمين يمكنك فتح المصدر الرسمي. لا يُعد فتح صفحة الفيديو تأكيدًا لإكمال الدرس.")
                }
            }
            item(key = "support_feedback") {
                IhsanContentCard {
                    IhsanSectionHeader("اقتراح أو مشكلة")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(!suggestion, { suggestion = false; copied = false }, label = { Text("مشكلة") })
                        FilterChip(suggestion, { suggestion = true; copied = false }, label = { Text("اقتراح") })
                    }
                    OutlinedTextField(message, { message = it.take(3000); copied = false }, Modifier.fillMaxWidth(),
                        label = { Text("اكتب التفاصيل") }, minLines = 4, shape = MaterialTheme.shapes.medium)
                    Text("لا تُرفق بيانات شخصية أو سجلات خاصة. لا تُرسل هذه المسودة تلقائيًا.",
                        style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary)
                    IhsanSecondaryButton({
                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(subject, body))
                        copied = true
                    }, enabled = message.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text(if (copied) "تم نسخ الرسالة" else "نسخ الرسالة") }
                    if (contact != null) IhsanPrimaryButton({
                        openExternalActivity(context, Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", contact, null))
                            .putExtra(Intent.EXTRA_SUBJECT, subject).putExtra(Intent.EXTRA_TEXT, body))
                    }, enabled = message.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("فتح تطبيق البريد") }
                    else Text("لم تُحدد وسيلة تواصل معتمدة للنشر بعد. يمكنك الاحتفاظ بالنص المنسوخ؛ لم يُرسل بلاغ أو يُفتح طلب دعم.",
                        style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary)
                }
            }
            item(key = "support_about") {
                IhsanContentCard {
                    IhsanSectionHeader("حول إحسان")
                    Text("إحسان رفيق يومي يجمع القرآن والأذكار والأدعية ومواقيت الصلاة والقبلة، مع أدوات للقراءة والتعلم والتقويم وسجلات عبادة يدوِّنها المستخدم. يتيح تنظيم عروض الخير وطلبات المساعدة على هذا الجهاز.")
                    Text("المجتمع العام والنشر والمزامنة تحتاج خدمات خادم في مرحلة لاحقة. المحتوى غير المتاح وحقوق الوسائط موضّحة في مكانها.")
                    Text("الإصدار $version", style = MaterialTheme.typography.labelLarge, color = IhsanTheme.colors.accentContent)
                }
            }
        }
    }
}

@Composable
private fun Faq(title: String, answer: String) {
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    TextButton({ expanded = !expanded }, modifier = Modifier.fillMaxWidth(), colors = com.example.designsystem.theme.ihsanTextButtonColors()) { Text(title, Modifier.fillMaxWidth()) }
    if (expanded) Text(answer, style = MaterialTheme.typography.bodyMedium, color = IhsanTheme.colors.textSecondary)
    HorizontalDivider(color = IhsanTheme.colors.divider)
}
