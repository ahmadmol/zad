package com.example.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    userPreferences: UserPreferences = koinInject()
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            title = "مواقيت الصلاة",
            description = "تنبيهات دقيقة لكل صلاة بناءً على موقعك الحالي",
            color = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            title = "اتجاه القبلة",
            description = "تحديد دقيق لاتجاه الكعبة المشرفة باستخدام حساسات جهازك",
            color = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            title = "مشاريع الإحسان",
            description = "ساهم في نشر الخير ودعم المشاريع الخيرية في مجتمعك بسهولة وأمان",
            color = MaterialTheme.colorScheme.tertiary
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            OnboardingPageContent(pages[pageIndex])
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    val color = if (pagerState.currentPage == index) 
                        MaterialTheme.colorScheme.primary else Color.LightGray
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Buttons
            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        scope.launch {
                            userPreferences.setOnboardingCompleted(true)
                            onFinish()
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(56.dp).padding(horizontal = 8.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == 2) "ابدأ الآن" else "التالي",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(page.color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for illustration
            Text(
                text = "✨",
                fontSize = 80.sp
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 28.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

private data class OnboardingPage(
    val title: String,
    val description: String,
    val color: Color
)
