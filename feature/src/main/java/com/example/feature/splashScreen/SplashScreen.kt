package com.example.feature.splashScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.R
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

private val DeepTeal = Color(0xFF0F5247)
private val DarkBackgroundScrim = Color(0x66000000)

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMain: () -> Unit,
    userPreferences: UserPreferences = koinInject()
) {
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        delay(1500L)
        val hasCompletedOnboarding = userPreferences.hasCompletedOnboarding.first()

        if (!hasCompletedOnboarding) {
            onNavigateToOnboarding()
        } else {
            onNavigateToMain()
        }
    }

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
                    .background(DarkBackgroundScrim)
            )
        }

        // Center Content (Logo & Brand Typography)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ihsan Real Logo
            Image(
                painter = painterResource(id = R.drawable.splash_ihsan_logo_transparent),
                contentDescription = "إحسان",
                modifier = Modifier.size(160.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "إحسان",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else DeepTeal,
                letterSpacing = 1.sp
            )

            Text(
                text = "خير دائم",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFB2DFDB) else DeepTeal.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "معاً لقرب إلى الله ولمجتمع أكثر تماسكاً",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF2C5E55),
                textAlign = TextAlign.Center
            )
        }

        // Bottom Loading & Quranic Verse
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .width(160.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (isDark) Color(0xFF80CBC4) else DeepTeal,
                trackColor = if (isDark) Color.White.copy(alpha = 0.2f) else DeepTeal.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "جاري التحميل ...",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = if (isDark) Color.White.copy(alpha = 0.7f) else DeepTeal.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "﴿ وَأَحْسِنُوا إِنَّ اللَّهَ يُحِبُّ الْمُحْسِنِينَ ﴾",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFFE0F2F1) else DeepTeal,
                textAlign = TextAlign.Center
            )
        }
    }
}
