package com.example.feature.splashScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme

@Composable
fun SplashScreen(onNavigateToOnboarding: () -> Unit, onNavigateToMain: () -> Unit,
    viewModel: StartupViewModel = org.koin.androidx.compose.koinViewModel(), onStartupResolved: () -> Unit = {}) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.loading, state.onboardingCompleted, state.error) {
        if (!state.loading) onStartupResolved()
        when (state.onboardingCompleted) {
            true -> onNavigateToMain()
            false -> onNavigateToOnboarding()
            null -> Unit
        }
    }
    if (state.error != null) IhsanErrorState(title = "تعذر بدء التطبيق", message = state.error,
        onRetry = viewModel::retry, modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding())
    else SplashScreenContent(IhsanTheme.isDark)
}

@Composable
fun SplashScreenContent(@Suppress("UNUSED_PARAMETER") isDark: Boolean) {
    val colors = IhsanTheme.colors
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(colors.surfaceMint, colors.surfaceBase)))
        .windowInsetsPadding(WindowInsets.safeDrawing), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            IhsanTextWithKaabaIcon("إحسان", colors.accentContent, logoHeight = 88.dp)
            Text("خير يرافق يومك", style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = colors.accentContent)
        }
    }
}
