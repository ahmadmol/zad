package com.example.designsystem.theme

import android.provider.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Re-read on resume so a system animation-setting change is honored without restart. */
@Composable
fun rememberIhsanMotionEnabled(): Boolean {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    fun enabled() = runCatching { Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f }.getOrDefault(true)
    var result by remember { mutableStateOf(enabled()) }
    DisposableEffect(owner, context) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) result = enabled() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    return result
}

object IhsanMotion { const val Quick = 160; const val Standard = 240; const val Entrance = 360 }
