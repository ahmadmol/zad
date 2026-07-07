package com.example.feature.ui.interfaces.splashScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val SPLASH_DELAY_MS = 1_500L

/**
 * Initial branding screen.
 *
 * Displays the Ihsan logo with a subtle scale-and-fade animation,
 * waits [SPLASH_DELAY_MS], then triggers [onTimeout] so the navigator
 * can move the user to the next destination.
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(initialValue = 0.7f) }
    val opacity = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(Unit) {
        // Run animation and the delay in parallel; total duration ~ SPLASH_DELAY_MS.
        scale.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 700))
        opacity.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 700))
        delay(SPLASH_DELAY_MS - 700L)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .scale(scale.value)
                .alpha(opacity.value)
        ) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }
            Text(
                text = "إحسان",
                modifier = Modifier.padding(top = 24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "شارك الخير مع مجتمعك",
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}
