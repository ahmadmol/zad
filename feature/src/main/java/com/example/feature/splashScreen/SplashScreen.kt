package com.example.feature.splashScreen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.R
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

private val DeepTeal = Color(0xFF0E5B52)
private val LightTealTrack = Color(0xFFD6E4E2)

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMain: () -> Unit,
    userPreferences: UserPreferences = koinInject()
) {
    LaunchedEffect(Unit) {
        delay(1500L)
        val hasCompletedOnboarding = userPreferences.hasCompletedOnboarding.first()

        if (!hasCompletedOnboarding) {
            onNavigateToOnboarding()
        } else {
            onNavigateToMain()
        }
    }

    SplashScreenContent(isDark = com.example.designsystem.theme.IhsanTheme.isDark)
}

@Composable
fun SplashScreenContent(isDark: Boolean) {
    val textColor = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.textPrimary else DeepTeal
    val subTextColor = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.textBody else Color(0xFF0C4E47)

    val infiniteTransition = rememberInfiniteTransition(label = "splash_progress")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Full screen spiritual background
        Image(
            painter = painterResource(id = R.drawable.ihsan_splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Scrim in dark mode
        if (isDark) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(
                        com.example.designsystem.theme.IhsanTheme.colors.surfaceBase.copy(alpha = .08f),
                        com.example.designsystem.theme.IhsanTheme.colors.surfaceBase.copy(alpha = .12f),
                        com.example.designsystem.theme.IhsanTheme.colors.surfaceBase.copy(alpha = .72f))))
            )
        }

        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().statusBarsPadding().padding(top = 104.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            com.example.designsystem.component.IhsanTextWithKaabaIcon("إحسان", textColor, logoHeight = 80.dp)
            Text("خير دائم", color = if (isDark) com.example.designsystem.theme.IhsanTheme.colors.heroSkyContent else textColor, fontSize = 19.sp, lineHeight = 27.sp)
        }
        Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 28.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("معًا ...\nلنبني مجتمعًا أكثر تماسكًا\nفي مدينتنا الحبيبة", color = textColor,
                fontSize = 18.sp, lineHeight = 28.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(44.dp))
            LinearProgressIndicator(progress = { progress }, color = textColor, trackColor = LightTealTrack,
                modifier = Modifier.width(136.dp).height(5.dp).clip(RoundedCornerShape(3.dp)))
            Text("نسخة 1.0.0", color = subTextColor, fontSize = 11.sp, modifier = Modifier.padding(top = 18.dp))
        }
    }
}

@Composable
private fun IslamicOrnamentDivider(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension / 2.8f

        val path = Path().apply {
            moveTo(cx, cy - radius)
            lineTo(cx + radius, cy)
            lineTo(cx, cy + radius)
            lineTo(cx - radius, cy)
            close()
        }
        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx()))

        val dotRadius = 1.8.dp.toPx()
        val offset = radius * 1.35f
        drawCircle(color = color, radius = dotRadius, center = Offset(cx, cy - offset))
        drawCircle(color = color, radius = dotRadius, center = Offset(cx + offset, cy))
        drawCircle(color = color, radius = dotRadius, center = Offset(cx, cy + offset))
        drawCircle(color = color, radius = dotRadius, center = Offset(cx - offset, cy))
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreenContent(isDark = false)
}

