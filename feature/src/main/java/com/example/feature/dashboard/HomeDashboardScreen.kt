package com.example.feature.dashboard

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.designsystem.component.DailyActivityCard
import com.example.designsystem.component.DailyActivityItemData
import com.example.designsystem.component.DashboardHeader
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.core.notification.UserMessageNotifier
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.dashboard.presentation.HomeDashboardAction
import com.example.feature.dashboard.presentation.HomeDashboardViewModel
import com.example.feature.dashboard.presentation.components.AsmaHighlightCard
import com.example.feature.dashboard.presentation.components.DailyExperienceCard
import com.example.feature.dashboard.presentation.components.HomeQuickActions
import com.example.feature.dashboard.presentation.components.HomeRefreshErrorNotice
import com.example.feature.dashboard.presentation.components.HomeSectionStateCard
import com.example.feature.dashboard.presentation.components.HomeSectionStateNotice
import com.example.feature.dashboard.presentation.components.HomeServicesSection
import com.example.feature.dashboard.presentation.components.NearbyCharityCard
import com.example.feature.dashboard.presentation.components.QiblaShortcutCard
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
    onNavigateToIhsanPlusDaily: (() -> Unit)? = null,
    onContinueLastRead: (surahId: Int, ayahNumber: Int) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLiveChooser by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            UserMessageNotifier.notify(
                context,
                "يجب منح إذن الإشعارات لتفعيل التنبيهات",
                title = "التنبيهات"
            )
        }
    }

    LaunchedEffect(Unit) {
        UserMessageNotifier.ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val primaryActions = remember {
        listOf(
            HomeIslamicAction("البوصلة", Icons.Default.Explore, "qibla"),
            HomeIslamicAction("أسماء الله", Icons.Default.AutoAwesome, "asma"),
            HomeIslamicAction("دعاء", Icons.Default.VolunteerActivism, "dua"),
            HomeIslamicAction("القرآن", Icons.AutoMirrored.Filled.MenuBook, "quran"),
            HomeIslamicAction("بث مباشر", Icons.Default.LiveTv, "live_chooser")
        )
    }

    val serviceActions = remember {
        listOf(
            HomeIslamicAction("الأحاديث", Icons.Default.AutoStories, "hadith"),
            HomeIslamicAction("الأذكار", Icons.Default.SelfImprovement, "azkar"),
            HomeIslamicAction("التسبيح", Icons.Default.BrightnessLow, "tasbih"),
            HomeIslamicAction("مواقيت الصلاة", Icons.Default.AccessTime, "prayer"),
            HomeIslamicAction("بث الحرم المكي", Icons.Default.LiveTv, "haram"),
            HomeIslamicAction("بث المسجد النبوي", Icons.Default.LiveTv, "nabawi"),
            HomeIslamicAction("البحث", Icons.Default.Search, "search"),
            HomeIslamicAction("النشاطات اليومية", Icons.AutoMirrored.Filled.List, "daily"),
            HomeIslamicAction("التذكيرات", Icons.Default.Notifications, "reminders"),
            HomeIslamicAction("الإحصائيات", Icons.Default.BarChart, "statistics")
        )
    }

    val onActionClick: (String) -> Unit = remember(
        onNavigateToQibla, onNavigateToQuran, onNavigateToAzkar,
        onNavigateToDua, onNavigateToHadith, onNavigateToAsma,
        onNavigateToTasbih, onNavigateToHaramLive, onNavigateToNabawiLive,
        onNavigateToPrayer, onNavigateToSearch, onNavigateToDailyActivities,
        onNavigateToReminders, onNavigateToStatistics
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
                "live_chooser" -> showLiveChooser = true
                else -> UserMessageNotifier.notify(
                    context,
                    "قريبًا، سيتم تفعيل هذه الميزة لاحقًا"
                )
            }
        }
    }

    // Compact hero shows five daytime cards; Maghrib stays on the full prayer screen.
    val visiblePrayers = remember(uiState.data.allPrayers) {
        uiState.data.allPrayers.filterNot { it.nameAr.contains("المغرب") }
    }
    val prayerTimesDisplay = remember(visiblePrayers) {
        visiblePrayers.map { it.nameAr to it.time }
    }

    val activePrayerIndex = remember(visiblePrayers) {
        visiblePrayers.indexOfFirst { it.isActive }.takeIf { it != -1 } ?: 0
    }

    val greeting = remember(uiState.data.userName) {
        val name = uiState.data.userName.trim()
        if (name.isNotEmpty() && name != "مستخدم إحسان") {
            "أهلاً بك، $name"
        } else {
            "أهلاً بك في إحسان"
        }
    }

    val nextPrayerInfo = remember(
        uiState.data.nextPrayerName,
        uiState.data.nextPrayerTimeLeft
    ) {
        val name = uiState.data.nextPrayerName
        val left = uiState.data.nextPrayerTimeLeft
        when {
            name.isNotBlank() && left.isNotBlank() -> "$name خلال $left"
            name.isNotBlank() -> name
            else -> ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .verticalScroll(rememberScrollState())
    ) {
        DashboardHeader(
            currentTime = uiState.data.currentTime.ifBlank { "—" },
            hijriDate = uiState.data.hijriDate.ifBlank { HijriDateFormatter.nowFormatted() },
            location = uiState.data.location,
            nextPrayerInfo = nextPrayerInfo,
            prayerTimes = prayerTimesDisplay,
            activePrayerIndex = activePrayerIndex,
            greeting = greeting,
            onNotificationClick = onNavigateToReminders,
            onPrayerClick = { index -> viewModel.onAction(HomeDashboardAction.OnPrayerClick(index)) }
        )

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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

                HomeQuickActions(
                    actions = primaryActions,
                    onActionClick = onActionClick
                )

                HomeServicesSection(
                    services = serviceActions,
                    onServiceClick = onActionClick
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeSectionStateCard(
                        state = uiState.asma,
                        onRetry = { viewModel.onAction(HomeDashboardAction.OnRefresh) },
                        modifier = Modifier.weight(1f)
                    ) { asma ->
                        AsmaHighlightCard(
                            name = asma.name,
                            transliteration = asma.transliteration,
                            meaning = asma.meaning,
                            onClick = onNavigateToAsma
                        )
                    }
                    HomeSectionStateCard(
                        state = uiState.charity,
                        onRetry = { viewModel.onAction(HomeDashboardAction.OnRefresh) },
                        modifier = Modifier.weight(1f)
                    ) { charity ->
                        NearbyCharityCard(
                            offersCount = charity.offersCount,
                            requestsCount = charity.requestsCount,
                            onClick = onNavigateToDonations,
                            onRetry = { viewModel.onAction(HomeDashboardAction.OnRefresh) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onNavigateToIhsanPlusDaily != null) {
                        DailyExperienceCard(
                            onClick = onNavigateToIhsanPlusDaily,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    QiblaShortcutCard(
                        onClick = onNavigateToQibla,
                        modifier = Modifier.weight(1f)
                    )
                }

                HomeSectionStateCard(
                    state = uiState.dailyActivities,
                    onRetry = { viewModel.onAction(HomeDashboardAction.OnRefresh) }
                ) { dailyActivities ->
                    DailyActivityCard(
                        activities = dailyActivities.items.map { item ->
                            DailyActivityItemData(
                                id = item.id,
                                title = item.title,
                                currentCount = item.currentCount,
                                targetCount = item.targetCount,
                                unit = item.unit,
                                isCompleted = item.isCompleted,
                                route = item.route
                            )
                        },
                        onGoToChecklist = onNavigateToDailyActivities
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
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

@Composable
private fun HomeDashboardPreviewContent(
    showDailyExperience: Boolean = true
) {
    IhsanTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
        ) {
            DashboardHeader(
                currentTime = "04:23 م",
                hijriDate = "السبت، ٤ صفر ١٤٤٨",
                location = "حلب",
                nextPrayerInfo = "العصر خلال 00:03:40",
                prayerTimes = listOf(
                    "الفجر" to "03:42 ص",
                    "الشروق" to "05:28 ص",
                    "الظهر" to "12:39 م",
                    "العصر" to "04:27 م",
                    "العشاء" to "09:07 م"
                ),
                activePrayerIndex = 0,
                greeting = "أهلاً بك في إحسان",
                onNotificationClick = {}
            )
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                color = IhsanTheme.colors.surfaceMuted,
                shape = RoundedCornerShape(
                    topStart = IhsanTheme.dimens.radiusSheet,
                    topEnd = IhsanTheme.dimens.radiusSheet
                )
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HomeQuickActions(
                        actions = listOf(
                            HomeIslamicAction("البوصلة", Icons.Default.Explore, "qibla"),
                            HomeIslamicAction("أسماء الله", Icons.Default.AutoAwesome, "asma"),
                            HomeIslamicAction("دعاء", Icons.Default.VolunteerActivism, "dua"),
                            HomeIslamicAction("القرآن", Icons.AutoMirrored.Filled.MenuBook, "quran"),
                            HomeIslamicAction("بث مباشر", Icons.Default.LiveTv, "haram")
                        ),
                        onActionClick = {}
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AsmaHighlightCard(
                            name = "الرَّحْمَنُ",
                            transliteration = "Ar-Rahman",
                            meaning = "The Entirely Merciful",
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                        NearbyCharityCard(
                            offersCount = 3,
                            requestsCount = 2,
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (showDailyExperience) {
                            DailyExperienceCard(onClick = {}, modifier = Modifier.weight(1f))
                        }
                        QiblaShortcutCard(onClick = {}, modifier = Modifier.weight(1f))
                    }
                    DailyActivityCard(
                        activities = listOf(
                            DailyActivityItemData("1", "أذكار", 1, 6, "مرة", false, "azkar")
                        ),
                        onGoToChecklist = {}
                    )
                }
            }
        }
    }
}

@Preview(name = "Home Light 360", locale = "ar", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun HomeDashboardPreviewLight360() {
    HomeDashboardPreviewContent()
}

@Preview(name = "Home Light 430", locale = "ar", widthDp = 430, heightDp = 900, showBackground = true)
@Composable
private fun HomeDashboardPreviewLight430() {
    HomeDashboardPreviewContent()
}

@Preview(
    name = "Home Dark",
    locale = "ar",
    widthDp = 360,
    heightDp = 800,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun HomeDashboardPreviewDark() {
    HomeDashboardPreviewContent()
}

@Preview(name = "Home FontScale", locale = "ar", widthDp = 360, heightDp = 800, fontScale = 1.3f)
@Composable
private fun HomeDashboardPreviewFontScale() {
    HomeDashboardPreviewContent(showDailyExperience = false)
}
