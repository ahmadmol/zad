package com.example.feature.splashScreen

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.example.feature.R
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

/** Brand splash green from the إحسان logo artwork. */
private val SplashBackground = Color(0xFF0B3026)

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
            .background(SplashBackground)
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_ihsan_logo),
            contentDescription = "إحسان",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
