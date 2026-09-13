package com.example.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.core.notification.UserMessageNotifier
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.dashboard.presentation.HomeDashboardAction
import com.example.feature.dashboard.presentation.HomeDashboardViewModel
import com.example.feature.dashboard.presentation.components.HomeContextualHero
import com.example.feature.dashboard.presentation.components.HomeDailyJourneyCard
import com.example.feature.dashboard.presentation.components.HomeDiscoverServicesGrid
import com.example.feature.dashboard.presentation.components.HomeEhsanCommunityCard
import com.example.feature.dashboard.presentation.components.HomeLiveStreamCard
import com.example.feature.dashboard.presentation.components.HomeQuranContinueCard
import com.example.feature.dashboard.presentation.components.HomeRefreshErrorNotice
import com.example.feature.dashboard.presentation.components.HomeSectionStateNotice
import com.example.feature.prayer.presentation.CitySelectionBottomSheet
import com.example.feature.prayer.presentation.PrayerDetailsBottomSheet
import com.example.feature.prayer.presentation.PrayerSettingsBottomSheet
import org.koin.androidx.compose.koinViewModel

data class HomeIslamicAction(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun HomeDashboardScreen(
    viewModel: HomeDashboardViewModel = koinViewModel(),
    onNavigateToQuran: () -> Unit = {},
    onNavigateToAzkar: () -> Unit = {},
    onNavigateToHadith: () -> Unit = {},
    onNavigateToDua: () -> Unit = {},
    onNavigateToDonations: () -> Unit = {},
    onNavigateToQibla: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToPrayer: () -> Unit = {},
    onNavigateToAsma: () -> Unit = {},
    onNavigateToDailyActivities: () -> Unit = {},
    onNavigateToTasbih: () -> Unit = {},
    onNavigateToHaramLive: () -> Unit = {},
    onNavigateToNabawiLive: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onNavigateToIhsanPlusDaily: (() -> Unit)? = null,
    onContinueLastRead: (surahId: Int, ayahNumber: Int) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLiveChooser by remember { mutableStateOf(false) }

    val onActionClick: (String) -> Unit = remember(
        onNavigateToQibla, onNavigateToQuran, onNavigateToAzkar,
        onNavigateToDua, onNavigateToHadith, onNavigateToAsma,
        onNavigateToTasbih, onNavigateToHaramLive, onNavigateToNabawiLive,
        onNavigateToPrayer, onNavigateToSearch, onNavigateToDailyActivities,
        onNavigateToReminders, onNavigateToStatistics, onNavigateToDonations
    ) {
        { route ->
            when (route) {
                "qibla" -> onNavigateToQibla()
                "quran" -> onNavigateToQuran()
                "azkar" -> onNavigateToAzkar()
                "dua" -> onNavigateToDua()
                "hadith" -> onNavigateToHadith()
                "asma" -> onNavigateToAsma()
                "tasbih" -> onNavigateToTasbih()
                "haram" -> onNavigateToHaramLive()
                "nabawi" -> onNavigateToNabawiLive()
                "prayer" -> onNavigateToPrayer()
                "search" -> onNavigateToSearch()
                "daily" -> onNavigateToDailyActivities()
                "reminders" -> onNavigateToReminders()
                "statistics" -> onNavigateToStatistics()
                "donations" -> onNavigateToDonations()
                "live_chooser" -> showLiveChooser = true
                else -> UserMessageNotifier.notify(
                    context,
                    "قريبًا، سيتم تفعيل هذه الميزة لاحقًا"
                )
            }
        }
    }

    // 1. Current active prayer comes ONLY from an item in allPrayers where isActive == true
    val activePrayer = remember(uiState.data.allPrayers) {
        uiState.data.allPrayers.firstOrNull { it.isActive }
    }
    val currentPrayerTitle = activePrayer?.nameAr ?: "—"

    // 2. Next prayer name comes directly from domain result uiState.data.nextPrayerName
    val nextPrayerTitle = uiState.data.nextPrayerName.ifBlank { "—" }

    // 3. Next prayer time is safely matched against the full allPrayers list (presentation-only)
    val nextPrayerItem = remember(uiState.data.allPrayers, nextPrayerTitle) {
        if (nextPrayerTitle != "—") {
            uiState.data.allPrayers.firstOrNull {
                it.nameAr == nextPrayerTitle || nextPrayerTitle.contains(it.nameAr) || it.nameAr.contains(nextPrayerTitle)
            }
        } else null
    }
    val nextPrayerTimeLabel = nextPrayerItem?.time ?: "—"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Contextual Skyline Hero Banner
        HomeContextualHero(
            location = uiState.data.location,
            hijriDate = uiState.data.hijriDate.ifBlank { HijriDateFormatter.nowFormatted() },
            currentPrayerName = currentPrayerTitle,
            countdownText = uiState.data.nextPrayerTimeLeft,
            nextPrayerName = nextPrayerTitle,
            nextPrayerTime = nextPrayerTimeLabel,
            dailyVerseText = uiState.data.dailyVerse,
            dailyVerseSource = uiState.data.dailyVerseSource,
            onNotificationClick = onNavigateToReminders,
            onSearchClick = onNavigateToSearch,
            onQiblaClick = onNavigateToQibla
        )

        // 2. Main Content Surface Overlay
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-16).dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.refreshErrorMessage?.let { message ->
                    HomeRefreshErrorNotice(
                        message = message,
                        onRetry = { viewModel.onAction(HomeDashboardAction.OnRefresh) }
                    )
                }

                HomeSectionStateNotice(
                    state = uiState.prayer,
                    onRetry = { viewModel.onAction(HomeDashboardAction.OnRetryPrayer) }
                )

                // 2.1 Continue / Start Quran Card
                HomeQuranContinueCard(
                    surahName = uiState.data.lastReadSurahName,
                    surahId = uiState.data.lastReadSurahId,
                    ayahNumber = uiState.data.lastReadAyahNumber,
                    onContinueClick = onContinueLastRead,
                    onStartQuranClick = onNavigateToQuran
                )

                // 2.2 Daily Journey ("مسيرتك اليوم")
                HomeDailyJourneyCard(
                    activities = uiState.data.dailyActivities,
                    onGoToChecklist = onNavigateToDailyActivities
                )

                // 2.3 Ehsan in Your Community ("إحسان في مجتمعك")
                HomeEhsanCommunityCard(
                    offersCount = uiState.data.communityOffersCount,
                    requestsCount = uiState.data.communityRequestsCount,
                    onNavigateToDonations = onNavigateToDonations
                )

                // 2.4 Live Content Banner ("البث المباشر")
                HomeLiveStreamCard(
                    onLiveClick = { showLiveChooser = true }
                )

                // 2.5 Discover Services ("اكتشف الخدمات")
                HomeDiscoverServicesGrid(
                    onServiceClick = onActionClick
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    if (showLiveChooser) {
        AlertDialog(
            onDismissRequest = { showLiveChooser = false },
            title = { Text(stringResource(R.string.home_live_chooser_title)) },
            text = { Text(stringResource(R.string.home_live_chooser_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLiveChooser = false
                        onNavigateToHaramLive()
                    }
                ) {
                    Text(stringResource(R.string.home_live_haram))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLiveChooser = false
                        onNavigateToNabawiLive()
                    }
                ) {
                    Text(stringResource(R.string.home_live_nabawi))
                }
            }
        )
    }

    uiState.data.selectedPrayerIndex?.let { index ->
        val selectedPrayer = uiState.data.allPrayers.getOrNull(index)
        if (selectedPrayer != null) {
            PrayerDetailsBottomSheet(
                selectedPrayer = selectedPrayer,
                allPrayers = uiState.data.allPrayers,
                location = uiState.data.location,
                countdown = uiState.data.nextPrayerTimeLeft,
                onDismiss = { viewModel.onAction(HomeDashboardAction.OnDismissPrayerDetails) },
                onNavigateToFullPrayer = {
                    viewModel.onAction(HomeDashboardAction.OnDismissPrayerDetails)
                    onNavigateToPrayer()
                },
                onSettingsClick = {
                    viewModel.onAction(HomeDashboardAction.OnDismissPrayerDetails)
                    viewModel.onAction(HomeDashboardAction.OnShowPrayerSettings)
                },
                onUpdatePrePrayer = { mins ->
                    viewModel.onAction(HomeDashboardAction.OnUpdatePrePrayerNotification(mins))
                },
                onUpdateIqamah = { mins ->
                    viewModel.onAction(HomeDashboardAction.OnUpdateIqamahNotification(mins))
                }
            )
        }
    }

    if (uiState.data.isPrayerSettingsVisible) {
        PrayerSettingsBottomSheet(
            onDismiss = { viewModel.onAction(HomeDashboardAction.OnDismissPrayerSettings) },
            onUpdateMethod = { viewModel.onAction(HomeDashboardAction.OnUpdateCalculationMethod(it)) },
            onUpdateMadhab = { viewModel.onAction(HomeDashboardAction.OnUpdateMadhab(it)) },
            onUpdateLocationMode = { viewModel.onAction(HomeDashboardAction.OnUpdateLocationMode(it)) },
            onUpdateSound = { viewModel.onAction(HomeDashboardAction.OnUpdateNotificationSound(it)) },
            onSelectCityClick = {
                viewModel.onAction(HomeDashboardAction.OnDismissPrayerSettings)
                viewModel.onAction(HomeDashboardAction.OnShowCitySelection)
            }
        )
    }

    if (uiState.data.isCitySelectionVisible) {
        CitySelectionBottomSheet(
            onDismiss = { viewModel.onAction(HomeDashboardAction.OnDismissCitySelection) },
            onCitySelected = { city ->
                viewModel.onAction(HomeDashboardAction.OnSelectCity(city.name, city.lat, city.lng))
            }
        )
    }
}

@Preview(name = "Home Screen Light", locale = "ar", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun HomeDashboardPreviewLight() {
    IhsanTheme {
        HomeContextualHero(
            location = "حلب",
            hijriDate = "12 ربيع الأول 1448",
            currentPrayerName = "الظهر",
            countdownText = "1:14",
            nextPrayerName = "العصر",
            nextPrayerTime = "3:42 م",
            dailyVerseText = "وَأَقِمِ الصَّلَاةَ إِنَّ الصَّلَاةَ تَنْهَىٰ عَنِ الْفَحْشَاءِ وَالْمُنْكَرِ",
            dailyVerseSource = "سورة العنكبوت (٤٥)",
            onNotificationClick = {},
            onSearchClick = {},
            onQiblaClick = {}
        )
    }
}
