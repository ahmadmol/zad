package com.example.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import android.view.ViewTreeObserver
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.designsystem.R
import com.example.designsystem.theme.IhsanTheme
import com.example.designsystem.theme.LocalIhsanHeroCoverage

private class HeroWindowGeometry { var coordinates: LayoutCoordinates? = null }

/** The image remains visible while every foreground uses a paired on-brand color. */
@Composable
fun IhsanHeroScrim(modifier: Modifier = Modifier) {
    val colors = IhsanTheme.colors
    val heroCoverage = LocalIhsanHeroCoverage.current
    val statusBarHeight = WindowInsets.statusBars.getTop(LocalDensity.current)
    val view = LocalView.current
    val geometry = remember { HeroWindowGeometry() }
    val updateCoverage = {
        geometry.coordinates?.takeIf { it.isAttached }?.let { coordinates ->
            heroCoverage?.value = coordinates.positionInWindow().y + coordinates.size.height > statusBarHeight
        }
    }
    Box(modifier.onGloballyPositioned {
        geometry.coordinates = it
        updateCoverage()
    }.background(Brush.verticalGradient(listOf(
        (if (IhsanTheme.isDark) colors.surfaceBase else colors.brand).copy(alpha = if (IhsanTheme.isDark) 0.50f else 0.02f),
        (if (IhsanTheme.isDark) colors.surfaceBase else colors.brand).copy(alpha = if (IhsanTheme.isDark) 0.65f else 0.32f),
        (if (IhsanTheme.isDark) colors.surfaceBase else colors.brand).copy(alpha = if (IhsanTheme.isDark) 0.82f else 0.72f)
    ))))
    DisposableEffect(view, heroCoverage, statusBarHeight) {
        // Compose scrolling can move an ancestor layer without laying this child out.
        // Read its current window coordinates before drawing, rather than caching a position.
        val observer = view.viewTreeObserver
        val listener = ViewTreeObserver.OnPreDrawListener { updateCoverage(); true }
        observer.addOnPreDrawListener(listener)
        onDispose {
            if (observer.isAlive) observer.removeOnPreDrawListener(listener)
            heroCoverage?.value = false
        }
    }
}


/** Reference header: location on the right, real wordmark centered, controls on the left. */
@Composable
fun IhsanHeroTopRow(location: String, hijriDate: String, onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit, modifier: Modifier = Modifier) {
    val foreground = if (IhsanTheme.isDark) IhsanTheme.colors.heroSkyContent else IhsanTheme.colors.brand
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = foreground, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(3.dp))
                Text(location, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall,
                    color = foreground, maxLines = 2)
            }
            if (hijriDate.isNotBlank()) Text(hijriDate, style = MaterialTheme.typography.labelSmall,
                color = foreground, maxLines = 2)
        }
        Column(Modifier.width(78.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IhsanTextWithKaabaIcon(stringResource(R.string.ihsan_brand_name), foreground, logoHeight = 26.dp)
            Text(stringResource(R.string.ihsan_brand_tagline), style = MaterialTheme.typography.labelSmall, color = foreground)
        }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSearchClick, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.Search, stringResource(R.string.search_desc), tint = foreground, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onNotificationClick, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.Notifications, stringResource(R.string.notifications_desc), tint = foreground, modifier = Modifier.size(20.dp))
            }
        }
    }
}
