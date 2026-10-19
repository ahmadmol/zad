package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.designsystem.component.IhsanTopBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

data class ServiceItemData(val title: String, val icon: ImageVector, val route: String)

@Composable
fun HomeDiscoverServicesGrid(onServiceClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val services = listOf(
        ServiceItemData(stringResource(R.string.polish_service_quran), Icons.AutoMirrored.Outlined.MenuBook, "quran"),
        ServiceItemData(stringResource(R.string.polish_service_prayer), Icons.Outlined.Mosque, "prayer"),
        ServiceItemData(stringResource(R.string.polish_service_qibla), Icons.Outlined.Explore, "qibla"),
        ServiceItemData(stringResource(R.string.polish_service_dua), Icons.Outlined.VolunteerActivism, "dua"),
        ServiceItemData(stringResource(R.string.polish_service_tasbih), Icons.Outlined.TouchApp, "tasbih"),
        ServiceItemData(stringResource(R.string.polish_service_azkar), Icons.Outlined.WbSunny, "azkar"),
        ServiceItemData(stringResource(R.string.polish_service_hadith), Icons.Outlined.AutoStories, "hadith"),
        ServiceItemData(stringResource(R.string.polish_service_asma), Icons.Outlined.TextFields, "asma"),
        ServiceItemData(stringResource(R.string.polish_service_live), Icons.Outlined.LiveTv, "live_chooser"),
        ServiceItemData(stringResource(R.string.polish_service_donation), Icons.Outlined.FavoriteBorder, "donations"),
        ServiceItemData(stringResource(R.string.polish_service_daily), Icons.Outlined.Checklist, "daily"),
        ServiceItemData(stringResource(R.string.polish_service_statistics), Icons.Outlined.BarChart, "statistics"),
        ServiceItemData(stringResource(R.string.common_search), Icons.Outlined.Search, "search"),
        ServiceItemData(stringResource(R.string.polish_service_fahm), Icons.Outlined.School, "fahm"),
        ServiceItemData(stringResource(R.string.polish_service_nabi), Icons.Outlined.Mosque, "nabi_ihsan"),
        ServiceItemData(stringResource(R.string.polish_service_sanhya), Icons.Outlined.HistoryEdu, "sanhya")
    )
    var showAll by remember { mutableStateOf(false) }
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 290.dp) 3 else 2

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.correction_services_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.home_services_subtitle), style = MaterialTheme.typography.bodySmall,
                        color = IhsanTheme.colors.textSecondary)
                }
                TextButton({ showAll = true }, contentPadding = PaddingValues(4.dp), colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
                    Text(stringResource(R.string.correction_view_all), style = MaterialTheme.typography.labelMedium)
                }
            }
            ServiceGrid(services.take(6), columns, onServiceClick)
        }
    }
    if (showAll) Dialog(onDismissRequest = { showAll = false },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column {
                IhsanTopBar(stringResource(R.string.polish_services_title), onNavigateBack = { showAll = false })
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding()) {
                    ServiceGrid(services, 3) { route -> showAll = false; onServiceClick(route) }
                }
            }
        }
    }
}

@Composable
private fun ServiceGrid(services: List<ServiceItemData>, columns: Int, onServiceClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        services.chunked(columns).forEach { rowItems ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { service ->
                    ServiceTileCard(service, { onServiceClick(service.route) }, Modifier.weight(1f).fillMaxHeight())
                }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ServiceTileCard(service: ServiceItemData, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick, modifier.heightIn(min = 68.dp), shape = RoundedCornerShape(12.dp),
        color = IhsanTheme.colors.surfaceMint) {
        Column(Modifier.padding(horizontal = 6.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(service.icon, null, tint = if (IhsanTheme.isDark) IhsanTheme.colors.textSecondary else IhsanTheme.colors.accentContent, modifier = Modifier.size(24.dp))
            Text(service.title, style = MaterialTheme.typography.labelMedium,
                color = IhsanTheme.colors.textPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
