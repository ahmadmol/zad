package com.example.feature.splashScreen

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToPermission: () -> Unit,
    userPreferences: UserPreferences = koinInject()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(1500L)
        val hasCompletedOnboarding = userPreferences.hasCompletedOnboarding.first()
        
        if (!hasCompletedOnboarding) {
            onNavigateToOnboarding()
        } else {
            val hasLocationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            
            if (hasLocationPermission) {
                onNavigateToMain()
            } else {
                onNavigateToPermission()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mosque,
            contentDescription = "Ihsan Logo",
            modifier = Modifier.size(120.dp),
            tint = Color.White
        )
    }
}
