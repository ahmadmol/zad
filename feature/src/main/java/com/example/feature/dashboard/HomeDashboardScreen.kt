package com.example.feature.dashboard

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import com.example.designsystem.component.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
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
import androidx.compose.ui.Alignment
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
import com.example.feature.dashboard.presentation.HomeDashboardAction
import com.example.feature.dashboard.presentation.HomeDashboardViewModel
import com.example.feature.dashboard.presentation.components.HomeContextualHero
import com.example.feature.dashboard.presentation.components.HomeDailyJourneyCard
import com.example.feature.dashboard.presentation.components.HomeDiscoverServicesGrid
import com.example.feature.dashboard.presentation.components.HomeEhsanCommunityCard
import com.example.feature.dashboard.presentation.components.HomeQuranContinueCard
import com.example.feature.dashboard.presentation.components.HomeRefreshErrorNotice
import com.example.feature.dashboard.presentation.components.HomeSectionStateNotice
import com.example.feature.dashboard.presentation.components.HomeLiveStreamCard
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
    onNavigateToServices: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToVisit: () -> Unit = {},
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
    onNavigateToRamadan: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToPrayerJournal: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToSanhya: () -> Unit = {},
    onNavigateToNabiIhsan: () -> Unit = {},
    onNavigateToFahm: () -> Unit = {},
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
        onNavigateToReminders, onNavigateToStatistics, onNavigateToDonations,
        onNavigateToSanhya, onNavigateToNabiIhsan, onNavigateToFahm, onNavigateToCalendar
    ) {
        { route ->
            when (route) {
                "calendar" -> onNavigateToCalendar()
                "fahm" -> onNavigateToFahm()
                "nabi_ihsan" -> onNavigateToNabiIhsan()
                "sanhya" -> onNavigateToSanhya()
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

    val data = uiState.data
    val activePrayer = data.allPrayers.firstOrNull { it.isActive }
    val listState = rememberLazyListState()
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), state = listState,
        contentPadding = PaddingValues(bottom = 24.dp)) {
        item(key = "home_header_prayer") {
            HomeContextualHero(location = data.location, hijriDate = data.hijriDate,
                currentPrayerName = activePrayer?.nameAr.orEmpty(), countdownText = data.nextPrayerTimeLeft,
                nextPrayerName = data.nextPrayerName, nextPrayerTime = data.nextPrayerTimeLabel,
                dailyVerseText = data.dailyVerse, dailyVerseSource = data.dailyVerseSource,
                onNotificationClick = onNavigateToReminders, onSearchClick = onNavigateToSearch,
                onQiblaClick = onNavigateToQibla, onPrayerClick = onNavigateToPrayer,
                onLocationClick = { viewModel.onAction(HomeDashboardAction.OnShowCitySelection) }, userName = data.userName)
        }
        item(key = "home_notices") {
            Column(Modifier.padding(horizontal = 20.dp)) {
                uiState.refreshErrorMessage?.let { HomeRefreshErrorNotice(it, onRetry = { viewModel.onAction(HomeDashboardAction.OnRefresh) }) }
                HomeSectionStateNotice(uiState.prayer, onRetry = { viewModel.onAction(HomeDashboardAction.OnRetryPrayer) })
            }
        }
        item(key = "home_services") {
            HomeDiscoverServicesGrid(onActionClick, Modifier.padding(horizontal = 20.dp, vertical = 12.dp), onNavigateToServices)
        }
        item(key = "home_quran") {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                HomeQuranContinueCard(data.lastReadSurahName, data.lastReadSurahId, data.lastReadAyahNumber,
                    onContinueLastRead, onNavigateToQuran)
            }
        }
        item(key = "home_journey") {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                HomeDailyJourneyCard(onNavigateToDailyActivities, activities = data.dailyActivities,
                    onPrayerClick = onNavigateToPrayerJournal, onQuranClick = onNavigateToQuran, onAzkarClick = onNavigateToAzkar)
            }
        }
        item(key = "home_goals") {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                com.example.feature.goals.GoalsHomeSummary(onNavigateToGoals)
            }
        }
        item(key = "home_qibla") {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                IhsanActionCard("اتجاه القبلة", "افتح البوصلة للتحقق من الموقع والمستشعر", Icons.Outlined.Explore, onNavigateToQibla)
            }
        }
        if (data.allPrayers.isNotEmpty()) item(key = "home_timeline") {
            IhsanPrayerTimeline(data.allPrayers.filter { it.nameEn != "Sunrise" }.map {
                IhsanPrayerTimelineItem(it.nameEn, it.nameAr, it.time, it.timestamp == data.nextPrayerEpochMillis, it.isPast)
            }, onNavigateToPrayer, Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
        }
        item(key = "home_journals") {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                com.example.feature.prayer.presentation.HomePrayerJournalSummary(onOpen = onNavigateToPrayerJournal)
                com.example.feature.ramadan.RamadanHomeEntry(onOpen = onNavigateToRamadan)
            }
        }
        item(key = "home_community") {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) { HomeEhsanCommunityCard(onNavigateToDonations = onNavigateToDonations) }
        }
        item(key = "home_discover") {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HomeLiveStreamCard(onLiveClick = { showLiveChooser = true })
                IhsanActionCard("المساجد القريبة", "ابحث عبر تطبيق الخرائط • النتائج من مزود الخرائط", Icons.Outlined.Map,
                    { com.example.feature.core.util.openNearbyMosques(context) })
                IhsanActionCard("زيارة الحرمين", "دليلك إلى خدمات نسك الرسمية", Icons.Outlined.TravelExplore, onNavigateToVisit)
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
                , colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
                    Text(stringResource(R.string.home_live_haram))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLiveChooser = false
                        onNavigateToNabawiLive()
                    }
                , colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
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
