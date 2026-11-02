package com.example.mol.ui

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import androidx.test.platform.app.InstrumentationRegistry
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.dashboard.HomeDashboardScreen
import com.example.feature.ehsan.EhsanScreen
import com.example.feature.profile.ProfileScreen
import com.example.feature.prayer.PrayerScreen
import com.example.feature.sanhya.presentation.SanhyaMainScreen
import org.koin.androidx.compose.koinViewModel
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale

/** Production screens and real repositories rendered on an Android device. */
class SharedFoundationGateTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun captureProductionScreensInBothThemes() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val output = File(instrumentation.targetContext.getExternalFilesDir(null), "foundation-gate").apply { mkdirs() }
        val screen = mutableIntStateOf(0)
        val dark = mutableStateOf(false)
        compose.setContent {
            val config = Configuration(LocalConfiguration.current).apply {
                screenWidthDp = 393; screenHeightDp = 852; setLocale(Locale.forLanguageTag("ar"))
            }
            val density = instrumentation.targetContext.resources.displayMetrics.widthPixels / 393f
            CompositionLocalProvider(LocalConfiguration provides config,
                LocalDensity provides Density(density, 1f), LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme(darkTheme = dark.value) {
                    Scaffold(bottomBar = {
                        if (screen.intValue < 3) IhsanBottomNavigation(listOf(
                            IhsanBottomNavDestination("الرئيسية", Icons.Default.Home, Icons.Default.Home),
                            IhsanBottomNavDestination("إحسان", Icons.Default.Favorite, Icons.Default.Favorite),
                            IhsanBottomNavDestination("الملف الشخصي", Icons.Default.Person, Icons.Default.Person)
                        ), screen.intValue, {}, Modifier.navigationBarsPadding())
                    }, contentWindowInsets = WindowInsets(0,0,0,0)) { padding ->
                        Box(Modifier.padding(padding).fillMaxSize()) {
                            key(screen.intValue) {
                                when (screen.intValue) {
                                    0 -> HomeDashboardScreen()
                                    1 -> EhsanScreen()
                                    2 -> ProfileScreen()
                                    3 -> PrayerScreen()
                                    4 -> SanhyaMainScreen(koinViewModel(), {}, {}, {}, {}, {})
                                }
                            }
                        }
                    }
                }
            }
        }
        val names = listOf("home", "ihsan", "profile", "prayer", "learning")
        for (night in listOf(false, true)) for (index in names.indices) {
            compose.runOnIdle { dark.value = night; screen.intValue = index }
            compose.waitForIdle()
            SystemClock.sleep(1800)
            compose.waitForIdle()
            val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
            File(output, "393_${if (night) "dark" else "light"}_${names[index]}.png")
                .outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            compose.onRoot().assertExists()
        }
    }
}
