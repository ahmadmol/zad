package com.example.feature.ihsanplus.daily.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme
import org.koin.androidx.compose.koinViewModel
import com.example.feature.ihsanplus.daily.presentation.IhsanPlusDailyViewModel
import com.example.feature.ihsanplus.daily.presentation.components.*
import com.example.feature.ihsanplus.daily.presentation.state.IhsanPlusDailyAction
import com.example.feature.ihsanplus.daily.presentation.state.IhsanPlusDailyUiState

@Composable
fun IhsanPlusDailyExperienceScreen(
    viewModel: IhsanPlusDailyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            when (val state = uiState) {
                is IhsanPlusDailyUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is IhsanPlusDailyUiState.Success -> {
                    val data = state.data
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        item {
                            IhsanPlusDailyHeader(
                                greeting = data.greetingName,
                                hijriDate = data.hijriDateLabel,
                                gregorianDate = data.gregorianDateLabel
                            )
                        }
                        item {
                            IhsanPlusPrayerSummaryCard(
                                summary = data.nextPrayer,
                                onToggleNotification = { viewModel.onAction(IhsanPlusDailyAction.OnTogglePrayerNotification) }
                            )
                        }
                        item {
                            IhsanPlusQuranContinueCard(
                                progress = data.quranProgress,
                                onClick = { viewModel.onAction(IhsanPlusDailyAction.OnOpenQuran) }
                            )
                        }
                        item {
                            IhsanPlusDailyDuaCard(
                                dua = data.dailyDua,
                                onShare = { viewModel.onAction(IhsanPlusDailyAction.OnShareDua) }
                            )
                        }
                        item {
                            IhsanPlusDailyHadithCard(
                                hadith = data.dailyHadith,
                                onShare = { viewModel.onAction(IhsanPlusDailyAction.OnShareHadith) }
                            )
                        }
                        item {
                            IhsanPlusDhikrProgressCard(
                                progress = data.dhikrProgress,
                                onAddClick = { viewModel.onAction(IhsanPlusDailyAction.OnAddDhikr) }
                            )
                        }
                        item {
                            IhsanPlusCharitySuggestionCard(
                                suggestion = data.charitySuggestion,
                                onDonateClick = { viewModel.onAction(IhsanPlusDailyAction.OnDonateClick) }
                            )
                        }
                        item {
                            IhsanPlusDailyActivityList(
                                activities = data.activityItems,
                                onActivityClick = { id -> viewModel.onAction(IhsanPlusDailyAction.OnActivityClick(id)) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(IhsanTheme.spacing.large))
                            Text(
                                text = "تم تحسين هذه الواجهة بنمط ViewModel و MVI Actions، كجزء من تطوير IhsanPlus.",
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
                is IhsanPlusDailyUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.onAction(IhsanPlusDailyAction.Refresh) }) {
                                Text("إعادة المحاولة")
                            }
                        }
                    }
                }
            }
        }
    }
}
