package com.example.feature.ihsanplus.prayerassist.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.prayerassist.domain.usecase.GetIhsanPlusPrayerAssistDashboardUseCase
import com.example.feature.ihsanplus.prayerassist.presentation.components.*
import com.example.feature.ihsanplus.prayerassist.presentation.state.IhsanPlusPrayerAssistUiState

@Composable
fun IhsanPlusPrayerAssistScreen(
    useCase: GetIhsanPlusPrayerAssistDashboardUseCase = GetIhsanPlusPrayerAssistDashboardUseCase()
) {
    var uiState by remember { mutableStateOf<IhsanPlusPrayerAssistUiState>(IhsanPlusPrayerAssistUiState.Loading) }

    LaunchedEffect(Unit) {
        val data = useCase()
        uiState = IhsanPlusPrayerAssistUiState.Success(data)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            when (val state = uiState) {
                is IhsanPlusPrayerAssistUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is IhsanPlusPrayerAssistUiState.Success -> {
                    val data = state.data
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        item {
                            IhsanPlusPrayerAssistHeader(
                                location = data.locationLabel,
                                method = data.calculationMethodLabel,
                                madhab = data.madhabLabel
                            )
                        }
                        item {
                            IhsanPlusNextPrayerCard(nextPrayer = data.nextPrayer)
                        }
                        item {
                            IhsanPlusPrayerNotificationPreferencesCard(preferences = data.notificationPreferences)
                        }
                        item {
                            IhsanPlusPrayerReminderPreviewList(items = data.reminderPreviewItems)
                        }
                        item {
                            IhsanPlusQiblaAssistCard(assist = data.qiblaAssist)
                        }
                        item {
                            IhsanPlusCompassCalibrationTipsList(tips = data.calibrationTips)
                        }
                        item {
                            IhsanPlusPrayerAssistQuickActions(actions = data.quickActions)
                        }
                        item {
                            Spacer(modifier = Modifier.height(IhsanTheme.spacing.large))
                            Text(
                                text = "هذه واجهة تأسيسية معزولة لتحسين الأذان والقبلة، ولم يتم ربطها بالمنبّه أو الحساسات أو الملاحة الحالية بعد.",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(IhsanTheme.spacing.medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(IhsanTheme.spacing.large))
                        }
                    }
                }
                is IhsanPlusPrayerAssistUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
