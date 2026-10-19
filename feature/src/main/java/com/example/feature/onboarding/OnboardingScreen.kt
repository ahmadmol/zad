package com.example.feature.onboarding

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.R
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private val DeepTeal = Color(0xFF0F5247)
private val LightCream = Color(0xFFF7F9F8)

private data class OnboardingFeatureItem(
    val title: String,
    val description: String,
    val iconRes: Int
)

private data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val heroBgRes: Int,
    val features: List<OnboardingFeatureItem>
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    userPreferences: UserPreferences = koinInject()
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val isDark = com.example.designsystem.theme.IhsanTheme.isDark

    val pages = listOf(
        OnboardingPageData(
            title = "قربك من الله",
            subtitle = "كل ما تحتاجه في رحلة إيمانك اليومية",
            heroBgRes = R.drawable.ihsan_onboarding_prayer_quran,
            features = listOf(
                OnboardingFeatureItem(
                    title = "مواقيت الصلاة",
                    description = "دقيقة وموثوقة مع إشعارات في وقتها",
                    iconRes = R.drawable.ihsan_icon_prayer
                ),
                OnboardingFeatureItem(
                    title = "القرآن الكريم",
                    description = "تلاوة واستماع مع تجربة مريحة",
                    iconRes = R.drawable.ic_search_quran
                ),
                OnboardingFeatureItem(
                    title = "الأذكار والتسبيح",
                    description = "أذكار مأثورة وعداد تسبيح بسهولة",
                    iconRes = R.drawable.ic_search_azkar
                ),
                OnboardingFeatureItem(
                    title = "مجتمع إحسان",
                    description = "شارك الخير وتعاون مع مجتمعك",
                    iconRes = R.drawable.ic_search_ehsan
                )
            )
        ),
        OnboardingPageData(
            title = "كل ما تحتاجه في مكان واحد",
            subtitle = "أدوات إسلامية متكاملة تلازمك في كل مكان",
            heroBgRes = R.drawable.ihsan_onboarding_all_in_one,
            features = listOf(
                OnboardingFeatureItem(
                    title = "اتجاه القبلة",
                    description = "تحديد دقيق لاتجاه الكعبة المشرفة",
                    iconRes = R.drawable.ihsan_icon_qibla
                ),
                OnboardingFeatureItem(
                    title = "الأدعية المأثورة",
                    description = "مجموعة واسعة من الأدعية للمناسبات",
                    iconRes = R.drawable.ic_search_dua
                ),
                OnboardingFeatureItem(
                    title = "أسماء الله الحسنى",
                    description = "استمع وتدبر في معاني الأسماء المباركة",
                    iconRes = R.drawable.ic_search_asma
                ),
                OnboardingFeatureItem(
                    title = "التذكيرات الذكية",
                    description = "إشعارات مخصصة للأذكار والعبادات",
                    iconRes = R.drawable.ihsan_icon_notification
                )
            )
        ),
        OnboardingPageData(
            title = "مجتمع إحسان",
            subtitle = "عروض التبرع، طلبات المساعدة، والتكافل المحلي",
            heroBgRes = R.drawable.ihsan_onboarding_community,
            features = listOf(
                OnboardingFeatureItem(
                    title = "عروض التبرع",
                    description = "فرص تبرع موثوقة لدعم المحتاجين",
                    iconRes = R.drawable.ihsan_charity_growth
                ),
                OnboardingFeatureItem(
                    title = "طلبات المساعدة",
                    description = "قدم طلب مساعدة أو ساهم في تفريج كربة",
                    iconRes = R.drawable.ihsan_ehsan_care_support
                ),
                OnboardingFeatureItem(
                    title = "المجتمع المحلي",
                    description = "تواصل وشارك الخير في مجتمعك",
                    iconRes = R.drawable.ic_search_ehsan
                )
            )
        )
    )

    fun finishOnboarding() {
        scope.launch {
            userPreferences.setOnboardingCompleted(true)
            onFinish()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(Modifier.fillMaxSize().background(if (isDark) com.example.designsystem.theme.IhsanTheme.colors.surfaceBase else LightCream).statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { if (pagerState.currentPage < 2) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } else finishOnboarding() },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) MaterialTheme.colorScheme.primary else DeepTeal, contentColor = if (isDark) MaterialTheme.colorScheme.onPrimary else Color.White)) { Text("التالي", fontSize = 14.sp) }
                TextButton(onClick = { finishOnboarding() }, colors = com.example.designsystem.theme.ihsanTextButtonColors()) { Text("تخطي", color = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.textPrimary else DeepTeal, fontSize = 14.sp) }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(pages[page].heroBgRes), null, Modifier.fillMaxWidth().heightIn(max = 340.dp).aspectRatio(1.05f), contentScale = ContentScale.Fit)
                    Text(pages[page].title, fontSize = 25.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold,
                        color = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.textPrimary else DeepTeal, textAlign = TextAlign.Center)
                    Text(pages[page].subtitle, fontSize = 14.sp, lineHeight = 22.sp, color = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.textBody else DeepTeal,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                    Row(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) { index -> Box(Modifier.size(7.dp).background(if(index == page) (if (isDark) com.example.designsystem.theme.IhsanTheme.colors.accentContent else DeepTeal) else (if (isDark) com.example.designsystem.theme.IhsanTheme.colors.borderSubtle else Color(0xFFDCE6E1)), CircleShape)) }
                    }
                    val tiles = if (page == 0) listOf("مواقيت الصلاة" to R.drawable.ihsan_icon_prayer, "القرآن الكريم" to R.drawable.ic_search_quran,
                        "الأذكار" to R.drawable.ic_search_azkar, "التسبيح" to R.drawable.ic_search_tasbih,
                        "الأدعية" to R.drawable.ic_search_dua, "إحسان" to R.drawable.ic_search_ehsan)
                        else pages[page].features.map { it.title to it.iconRes }
                    tiles.chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (title, icon) -> Surface(Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                                color = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.surfaceElevated else Color(0xFFE9F1EC)) {
                                Column(Modifier.padding(horizontal = 4.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(when (icon) {
                                        R.drawable.ic_search_quran -> Icons.AutoMirrored.Filled.MenuBook
                                        R.drawable.ic_search_azkar -> Icons.Default.Eco
                                        R.drawable.ic_search_tasbih -> Icons.Default.TouchApp
                                        R.drawable.ic_search_dua -> Icons.Default.VolunteerActivism
                                        R.drawable.ic_search_ehsan -> Icons.Default.Favorite
                                        else -> Icons.Default.Mosque
                                    }, null, Modifier.size(24.dp), tint = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.textSecondary else DeepTeal)
                                    Text(title, fontSize = 11.sp, lineHeight = 16.sp, color = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.textPrimary else DeepTeal,
                                        textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
                                }
                            } }
                        }
                    }
                }
            }
            Button(onClick = { finishOnboarding() }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) MaterialTheme.colorScheme.primary else DeepTeal, contentColor = if (isDark) MaterialTheme.colorScheme.onPrimary else Color.White)) { Text("ابدأ الآن", fontSize = 17.sp) }
            Text("معًا ... لنبني مجتمعًا أكثر تماسكًا", color = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.textPrimary else DeepTeal,
                fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPageData,
    isDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Feature Items List in rounded surface card
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.surfaceElevated else Color.White,
            tonalElevation = 2.dp,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                page.features.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon circle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDark) com.example.designsystem.theme.IhsanTheme.colors.surfaceMuted else Color(0xFFE8F5E9)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = feature.iconRes),
                                contentDescription = feature.title,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = feature.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.textPrimary else DeepTeal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = feature.description,
                                fontSize = 12.sp,
                                color = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.textSecondary else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
