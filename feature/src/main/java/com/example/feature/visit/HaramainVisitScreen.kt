package com.example.feature.visit

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.core.util.openExternalActivity

object HaramainOfficialSources {
    const val NUSUK = "https://www.nusuk.sa/"
    const val SERVICES = "https://www.nusuk.sa/all-services"
}

@Composable
fun HaramainVisitScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    fun open(url: String) { openExternalActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    Column(Modifier.fillMaxSize()) {
        IhsanTopBar("زيارة الحرمين الشريفين", onNavigateBack = onBack)
        LazyColumn(
            Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "visit_intro") {
                IhsanGlassCard(Modifier.fillMaxWidth()) {
                    IhsanIconContainer(Icons.Outlined.TravelExplore)
                    Text("رحلتك تبدأ بمعرفة", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "«لا تُشَدُّ الرِّحالُ إلَّا إلى ثلاثةِ مساجِدَ: المسجِدِ الحَرامِ، ومسجِدِ الرَّسولِ ﷺ، ومسجِدِ الأقْصى». مدخل إرشادي إلى المساجد الثلاثة المقدسة وخدمات الزائر الرسمية.",
                        color = IhsanTheme.colors.textSecondary
                    )
                }
            }
            item(key = "visit_makkah") {
                IhsanGlassCard(Modifier.fillMaxWidth()) {
                    IhsanIllustration(IhsanScene.MAKKAH_KAABA, Modifier.fillMaxWidth().height(180.dp))
                    Text("مكة المكرمة — الكعبة المشرفة", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "مهوى الأفئدة، وقبلة المسلمين، ومهد الرسالة الخاتمة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = IhsanTheme.colors.textSecondary
                    )
                    IhsanActionCard(
                        "مكة والعمرة",
                        "استكشف رحلة العمرة وإرشاداتها لدى نسك",
                        Icons.Outlined.Mosque,
                        { open(HaramainOfficialSources.NUSUK) }
                    )
                }
            }
            item(key = "visit_madinah") {
                IhsanGlassCard(Modifier.fillMaxWidth()) {
                    IhsanIllustration(IhsanScene.MADINAH_NABAWI, Modifier.fillMaxWidth().height(180.dp))
                    Text("المدينة المنورة — المسجد النبوي الشريف", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "طيبة الطيبة، القبة الخضراء، ومظلات السكينة والرحمة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = IhsanTheme.colors.textSecondary
                    )
                    IhsanActionCard(
                        "المدينة والمسجد النبوي",
                        "معلومات الزائر واكتشاف الحرمين في المصدر الرسمي",
                        Icons.Outlined.LocationCity,
                        { open(HaramainOfficialSources.NUSUK) }
                    )
                }
            }
            item(key = "visit_rawdah") {
                IhsanActionCard(
                    "الروضة الشريفة",
                    "راجع الخدمة وشروطها ومواعيدها لدى نسك",
                    Icons.Outlined.EventAvailable,
                    { open(HaramainOfficialSources.SERVICES) }
                )
            }
            item(key = "visit_aqsa") {
                IhsanGlassCard(Modifier.fillMaxWidth()) {
                    IhsanIllustration(IhsanScene.AQSA, Modifier.fillMaxWidth().height(180.dp))
                    Text("القدس الشريف — المسجد الأقصى وقبة الصخرة", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "أولى القبلتين وثالث الحرمين الشريفين ومسرى رسول الله ﷺ.",
                        style = MaterialTheme.typography.bodySmall,
                        color = IhsanTheme.colors.textSecondary
                    )
                }
            }
            item(key = "visit_services") {
                IhsanPrimaryButton({ open(HaramainOfficialSources.SERVICES) }, Modifier.fillMaxWidth()) {
                    Text("فتح دليل خدمات نسك الرسمي")
                }
            }
            item(key = "visit_boundary") {
                Text(
                    "يفتح الرابط في تطبيق خارجي. إحسان لا يصدر تصاريح ولا يعرض توافر مواعيد ولا يجمع بيانات سفر.",
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary
                )
            }
        }
    }
}
