package com.example.mol

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.settings.presentation.SettingsViewModel
import com.example.mol.ui.MainScreen
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private var widgetRequest by androidx.compose.runtime.mutableStateOf<com.example.mol.widget.WidgetOpenRequest?>(null)
    private fun readWidgetIntent(intent: Intent?) {
        val action = intent?.action
        if (action == com.example.mol.widget.WIDGET_OPEN_PRAYER || action == com.example.mol.widget.WIDGET_CONTINUE_QURAN) {
            widgetRequest = com.example.mol.widget.WidgetOpenRequest(android.os.SystemClock.elapsedRealtimeNanos(), action)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        maybeTriggerDebugAdhanTest(intent)
        readWidgetIntent(intent)
        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            val useDarkTheme = when (settingsState.themeMode) {
                com.example.feature.azkar.data.local.SettingsManager.ThemeMode.SYSTEM -> isSystemInDarkTheme()
                com.example.feature.azkar.data.local.SettingsManager.ThemeMode.LIGHT -> false
                com.example.feature.azkar.data.local.SettingsManager.ThemeMode.DARK -> true
            }
            IhsanTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(widgetRequest = widgetRequest, onWidgetConsumed = { widgetRequest = null; intent.action = Intent.ACTION_MAIN })
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        maybeTriggerDebugAdhanTest(intent)
        readWidgetIntent(intent)
    }

    /**
     * Debug-only adhan sound verification path (no production UI).
     * adb shell am start -n com.example.mol/.MainActivity -a com.example.mol.DEBUG_ADHAN_TEST
     */
    private fun maybeTriggerDebugAdhanTest(intent: Intent?) {
        if (!BuildConfig.DEBUG) return
        if (intent?.action != ACTION_DEBUG_ADHAN_TEST) return
        com.example.feature.prayer.util.AdhanNotificationChannelFactory.showNotification(
            context = this,
            title = intent.getStringExtra("title") ?: "AdhanTest",
            message = intent.getStringExtra("message") ?: "AlarmUsageCheck",
            soundType = "DEFAULT_ATHAN",
            customAdhanUri = null
        )
    }

    companion object {
        const val ACTION_DEBUG_ADHAN_TEST = "com.example.mol.DEBUG_ADHAN_TEST"
    }
}
