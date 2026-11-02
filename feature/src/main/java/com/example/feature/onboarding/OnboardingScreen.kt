package com.example.feature.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.designsystem.theme.*
import com.example.feature.splashScreen.StartupViewModel
import kotlinx.coroutines.launch

private data class WelcomePage(val eyebrow: String, val title: String, val description: String,
    val detail: String, val scene: IhsanScene)
private val pages = listOf(
    WelcomePage("أهلًا بك في إحسان", "خير يرافق يومك", "رفيقك للعبادة والخير، بخطوات صغيرة تصنع يومًا أجمل.",
        "مساحة هادئة للصلاة والقرآن والذكر", IhsanScene.WELCOME),
    WelcomePage("صلاتك ووقتك", "لكل صلاة مساحة", "تابع الصلاة القادمة ومواقيت يومك، واعرف اتجاه القبلة أينما كنت.",
        "موقع اختياري أو مدينة تختارها بنفسك", IhsanScene.PRAYER),
    WelcomePage("القرآن والأذكار", "قربٌ يتجدد", "أكمل قراءتك من موضعك المحفوظ، واجعل للأذكار نصيبًا من يومك.",
        "قراءة مريحة • علامات محفوظة • عداد أذكار", IhsanScene.QURAN),
    WelcomePage("رحلة الخير", "على قدر خطوتك", "اختر أهدافك اليومية، وتعلّم على مهل، ونظّم مبادرات الخير محليًا.",
        "إنجازك بتسجيلك، وبياناتك محفوظة على جهازك", IhsanScene.JOURNEY)
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit,
    viewModel: StartupViewModel = org.koin.androidx.compose.koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.completionSaved) { if (state.completionSaved) onFinish() }
    OnboardingContent(state.loading || state.saving || state.onboardingCompleted == null,
        state.error, viewModel::completeOnboarding, viewModel::retry)
}

@Composable
private fun OnboardingContent(busy: Boolean = false, error: String? = null,
    onComplete: () -> Unit = {}, onRetry: () -> Unit = {}) {
    val pager = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val motion = rememberIhsanMotionEnabled()
    val colors = IhsanTheme.colors
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(colors.surfaceMint, colors.surfaceBase)))
        .windowInsetsPadding(WindowInsets.safeDrawing)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IhsanTextWithKaabaIcon("إحسان", colors.accentContent, logoHeight = 40.dp)
            TextButton(onComplete, enabled = !busy, colors = ihsanTextButtonColors()) { Text("تخطي") }
        }
        HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth(), key = { it }) { index ->
            val page = pages[index]
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                IhsanIllustration(page.scene, Modifier.fillMaxWidth().heightIn(max = 330.dp).aspectRatio(1.08f))
                Text(page.eyebrow, style = MaterialTheme.typography.labelLarge, color = colors.accentContent,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
                Text(page.title, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center, color = colors.textPrimary)
                Text(page.description, Modifier.padding(top = 12.dp, bottom = 24.dp),
                    style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = colors.textSecondary)
                IhsanGlassCard(Modifier.fillMaxWidth()) {
                    Text(page.detail, Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium, color = colors.textBody)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.semantics { contentDescription = "الصفحة ${pager.currentPage + 1} من ${pages.size}" },
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(pages.size) { index ->
                    val width by animateDpAsState(if (index == pager.currentPage) 28.dp else 7.dp,
                        tween(if (motion) IhsanMotion.Quick else 0), label = "pageIndicator")
                    Box(Modifier.width(width).height(7.dp).background(
                        if (index == pager.currentPage) colors.accentContent else colors.borderSubtle, CircleShape))
                }
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                TextButton(onRetry) { Text("إعادة المحاولة") }
            }
            IhsanPrimaryButton(onClick = {
                if (pager.currentPage == pages.lastIndex) onComplete()
                else scope.launch { if (motion) pager.animateScrollToPage(pager.currentPage + 1) else pager.scrollToPage(pager.currentPage + 1) }
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), enabled = !busy) {
                Text(if (pager.currentPage == pages.lastIndex) "ابدأ الآن" else "التالي", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Preview(name = "Welcome", locale = "ar", showBackground = true)
@Preview(name = "Welcome large type", locale = "ar", fontScale = 2f, showBackground = true)
@Preview(name = "Welcome night", locale = "ar", uiMode = 0x20, showBackground = true)
@Composable
private fun WelcomePreview() { IhsanTheme { OnboardingContent() } }
