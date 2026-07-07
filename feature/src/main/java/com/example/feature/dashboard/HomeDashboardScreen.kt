package com.example.feature.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.DailyActivityItemData
import com.example.designsystem.component.DailyActivityCard
import com.example.designsystem.component.DashboardHeader
import com.example.designsystem.component.IhsanActionCard
import com.example.designsystem.component.LastReadCard
import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.dashboard.presentation.HomeDashboardAction
import com.example.feature.dashboard.presentation.HomeDashboardViewModel
import com.example.feature.prayer.presentation.CitySelectionBottomSheet
import com.example.feature.prayer.presentation.PrayerDetailsBottomSheet
import com.example.feature.prayer.presentation.PrayerSettingsBottomSheet
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    onNavigateToPrayer: () -> Unit = {},
    onNavigateToAsma: () -> Unit = {},
    onNavigateToDailyActivities: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Notification Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "يجب منح إذن الإشعارات لتفعيل التنبيهات", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val actions = remember {
        listOf(
            HomeIslamicAction("القبلة", Icons.Default.Explore, "qibla"),
            HomeIslamicAction("أسماء الله", Icons.Default.AutoAwesome, "asma"),
            HomeIslamicAction("دعاء", Icons.Default.VolunteerActivism, "dua"),
            HomeIslamicAction("قرآن", Icons.AutoMirrored.Filled.MenuBook, "quran"),
            HomeIslamicAction("حديث", Icons.Default.AutoStories, "hadith"),
            HomeIslamicAction("أذكار", Icons.Default.SelfImprovement, "azkar")
        )
    }

    val onActionClick: (String) -> Unit = remember(onNavigateToQibla, onNavigateToQuran, onNavigateToAzkar, onNavigateToDua, onNavigateToHadith, onNavigateToAsma) {
        { route ->
            when (route) {
                "qibla" -> onNavigateToQibla()
                "quran" -> onNavigateToQuran()
                "azkar" -> onNavigateToAzkar()
                "dua" -> onNavigateToDua()
                "hadith" -> onNavigateToHadith()
                "asma" -> onNavigateToAsma()
                else -> Toast.makeText(context, "قريبًا، سيتم تفعيل هذه الميزة لاحقًا", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val prayerTimesDisplay = remember(uiState.data.allPrayers) {
        uiState.data.allPrayers.map { it.nameAr to it.time }
    }

    val activePrayerIndex = remember(uiState.data.allPrayers) {
        uiState.data.allPrayers.indexOfFirst { it.isActive }.takeIf { it != -1 } ?: 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        DashboardHeader(
            userName = uiState.data.userName.ifBlank { "مستخدم" },
            currentTime = uiState.data.currentTime.ifBlank { "00:00" },
            hijriDate = uiState.data.hijriDate.ifBlank { HijriDateFormatter.nowFormatted() },
            location = uiState.data.location,
            nextPrayerInfo = "${uiState.data.nextPrayerName} خلال ${uiState.data.nextPrayerTimeLeft}",
            prayerTimes = prayerTimesDisplay,
            activePrayerIndex = activePrayerIndex,
            onMenuClick = { Toast.makeText(context, "قريبًا، سيتم تفعيل هذه الميزة لاحقًا", Toast.LENGTH_SHORT).show() },
            onNotificationClick = onNavigateToSearch,
            onPrayerClick = { index -> viewModel.onAction(HomeDashboardAction.OnPrayerClick(index)) }
        )

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Islamic Actions Section
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(actions) { action ->
                        IslamicActionItem(action, onActionClick)
                    }
                }

                // Spotlight Allah Name Card
                if (uiState.data.spotlightAllahName.isNotEmpty()) {
                    SpotlightAllahNameCard(
                        name = uiState.data.spotlightAllahName,
                        transliteration = uiState.data.spotlightTransliteration,
                        meaning = uiState.data.spotlightMeaning,
                        onClick = onNavigateToAsma
                    )
                }

                LastReadCard(
                    surahName = "سورة البقرة",
                    surahNumber = 2,
                    onContinueClick = onNavigateToQuran
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IhsanActionCard(
                        title = "القبلة",
                        subtitle = "تحديد الاتجاه",
                        icon = Icons.Default.Explore,
                        onClick = onNavigateToQibla,
                        modifier = Modifier.weight(1f)
                    )
                    IhsanActionCard(
                        title = "تبرع قريب",
                        subtitle = "ابحث الآن",
                        icon = Icons.Default.Place,
                        onClick = onNavigateToDonations,
                        modifier = Modifier.weight(1f)
                    )
                }

                // New Supplications Section
                Text(
                    text = "دعاء اليوم",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    items(uiState.data.dailyDuas) { zikr ->
                        SupplicationCard(zikr)
                    }
                }

                DailyActivityCard(
                    activities = uiState.data.dailyActivities,
                    onGoToChecklist = onNavigateToDailyActivities,
                    onActivityClick = { activity ->
                        viewModel.onAction(HomeDashboardAction.OnDailyActivityClick(activity.id))
                    },
                    onActivityOpenRoute = { route ->
                        when (route) {
                            "qibla_screen" -> onNavigateToQibla()
                            "quran_list" -> onNavigateToQuran()
                            "azkar_screen" -> onNavigateToAzkar()
                            "dua_screen" -> onNavigateToDua()
                            "asma_screen" -> onNavigateToAsma()
                            else -> Toast.makeText(context, "قريبًا، سيتم تفعيل هذه الميزة لاحقًا", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // Prayer Details Bottom Sheet
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

    // Prayer Settings Bottom Sheet
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

    // City Selection Bottom Sheet
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
fun SpotlightAllahNameCard(
    name: String,
    transliteration: String,
    meaning: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F6))
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "أسماء الله الحسنى",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF0D4D3D).copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D4D3D)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transliteration,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF0D4D3D).copy(alpha = 0.8f)
                )
            }
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF0D4D3D).copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = meaning,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF0D4D3D),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun IslamicActionItem(action: HomeIslamicAction, onClick: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick(action.route) }
            .width(65.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .rotate(45f)
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE8F5E9), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                tint = Color(0xFF0D4D3D),
                modifier = Modifier
                    .size(24.dp)
                    .rotate(-45f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = action.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D4D3D),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SupplicationCard(zikr: Zikr) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0D4D3D),
                            Color(0xFF1E824C)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = zikr.category,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = zikr.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 3,
                    textAlign = TextAlign.End,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
