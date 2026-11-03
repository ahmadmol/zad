package com.example.feature.splashScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

@Composable
fun SplashScreen(onNavigateToOnboarding: () -> Unit, onNavigateToMain: () -> Unit,
    viewModel: StartupViewModel = org.koin.androidx.compose.koinViewModel(), onStartupResolved: () -> Unit = {}) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.loading, state.onboardingCompleted, state.error) {
        if (!state.loading && state.error == null) {
            when (state.onboardingCompleted) {
                true -> {
                    onStartupResolved()
                    onNavigateToMain()
                }
                false -> {
                    onStartupResolved()
                    onNavigateToOnboarding()
                }
                null -> Unit
            }
        }
    }
    if (state.error != null) IhsanErrorState(title = "تعذر بدء التطبيق", message = state.error,
        onRetry = viewModel::retry, modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding())
    else SplashScreenContent(IhsanTheme.isDark)
}

@Composable
fun SplashScreenContent(isDark: Boolean) {
    val colors = IhsanTheme.colors
    val textColor = if (isDark) Color(0xFFE2EBE5) else Color(0xFF1B3B34)
    val goldColor = Color(0xFFC7A35C)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF111315) else Color(0xFFFAF8F3))
    ) {
        // Full screen background with Dome of the Rock, sky, and golden arch
        Image(
            painter = painterResource(id = R.drawable.ihsan_splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Subtle dark scrim for dark theme readability
        if (isDark) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x73111816))
            )
        }

        // Top Brand & Slogan area positioned inside the golden arch
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 80.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IhsanTextWithKaabaIcon("إحسان", colors.accentContent, logoHeight = 92.dp)
            Spacer(Modifier.height(14.dp))
            Text(
                text = "رفيقك للعبادة والخير كل يوم",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = textColor
            )

            // Golden Islamic Rosette Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 14.dp)
            ) {
                Box(
                    Modifier
                        .width(48.dp)
                        .height(1.dp)
                        .background(Brush.horizontalGradient(listOf(Color.Transparent, goldColor)))
                )
                Spacer(Modifier.width(8.dp))
                Canvas(Modifier.size(16.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.minDimension / 2f
                    val star = Path().apply {
                        moveTo(cx, cy - r)
                        lineTo(cx + r, cy)
                        lineTo(cx, cy + r)
                        lineTo(cx - r, cy)
                        close()
                    }
                    drawPath(star, goldColor)
                    rotate(45f, Offset(cx, cy)) {
                        drawPath(star, goldColor)
                    }
                    drawCircle(Color.White.copy(alpha = 0.7f), r * 0.28f, Offset(cx, cy))
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .width(48.dp)
                        .height(1.dp)
                        .background(Brush.horizontalGradient(listOf(goldColor, Color.Transparent)))
                )
            }
        }

        // Curved cream bottom container with loading indicator and preparation status
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(topStart = 160.dp, topEnd = 160.dp),
            color = if (isDark) Color(0xF0182420) else Color(0xF5F6F0E6)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.5.dp,
                    color = if (isDark) Color(0xFF33C6AE) else Color(0xFF086B56),
                    trackColor = (if (isDark) Color(0xFF33C6AE) else Color(0xFF086B56)).copy(alpha = 0.18f)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "جاري التحضير...",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Splash Light", locale = "ar", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun SplashScreenPreviewLight() {
    IhsanTheme(darkTheme = false) {
        SplashScreenContent(isDark = false)
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Splash Dark", locale = "ar", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun SplashScreenPreviewDark() {
    IhsanTheme(darkTheme = true) {
        SplashScreenContent(isDark = true)
    }
}
