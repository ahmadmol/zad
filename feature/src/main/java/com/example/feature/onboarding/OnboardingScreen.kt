package com.example.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
private val DarkSurface = Color(0xFF141D1B)
private val DarkCardBackground = Color(0xFF1F2A28)

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
    val isDark = isSystemInDarkTheme()

    val pages = listOf(
        OnboardingPageData(
            title = "عبادتك اليومية",
            subtitle = "تتبع صلواتك، واقرأ القرآن، واذكر الله بسهولة",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) DarkSurface else LightCream)
        ) {
            // Header skyline area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ihsan_onboarding_header),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (isDark) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )
                }

                // Top Skip button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { finishOnboarding() }
                    ) {
                        Text(
                            text = "تخطي",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else DeepTeal
                        )
                    }
                }

                // Header Title inside skyline
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مرحباً بك في إحسان",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else DeepTeal,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "تطبيق إسلامي شامل لحياة أكثر قرباً من الله",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else DeepTeal.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Pager for Onboarding Pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                OnboardingPageContent(
                    page = pages[pageIndex],
                    isDark = isDark
                )
            }

            // Bottom Navigation & Actions Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        val color = if (isSelected) {
                            if (isDark) Color(0xFF80CBC4) else DeepTeal
                        } else {
                            if (isDark) Color.White.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.3f)
                        }
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 24.dp else 8.dp)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Action Button (Next / Start)
                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            finishOnboarding()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF00695C) else DeepTeal,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (pagerState.currentPage == 2) "ابدأ الآن" else "التالي",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "لنجعل كل يوم أفضل",
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
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
            color = if (isDark) DarkCardBackground else Color.White,
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
                                    if (isDark) Color(0xFF0F3832) else Color(0xFFE8F5E9)
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
                                color = if (isDark) Color.White else DeepTeal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = feature.description,
                                fontSize = 12.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
