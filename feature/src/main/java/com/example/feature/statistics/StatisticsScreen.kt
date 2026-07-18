package com.example.feature.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.Grey
import com.example.designsystem.theme.IhsanTheme
import com.example.designsystem.theme.White
import com.example.feature.R
import com.example.feature.azkar.domain.model.DailyStat
import com.example.feature.statistics.presentation.StatisticsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(IhsanTheme.dimens.minTouchTarget)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                IhsanLoadingState(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    message = stringResource(R.string.common_loading)
                )
            }
            state.error != null -> {
                IhsanErrorState(
                    title = stringResource(R.string.statistics_load_error),
                    message = null,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    retryLabel = stringResource(R.string.common_retry),
                    onRetry = null
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(IhsanTheme.dimens.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(IhsanTheme.dimens.sectionSpacing)
                ) {
                    item {
                        Text(stringResource(R.string.chart_title), style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(IhsanTheme.spacing.medium))
                        DailyProgressChart(stats = state.last7DaysStats)
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(IhsanTheme.dimens.radiusLarge),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(IhsanTheme.spacing.medium)) {
                                Text(
                                    stringResource(R.string.total_daily_count),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(
                                        R.string.statistics_total_count_value,
                                        state.summary.totalDailyCount
                                    ),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            stringResource(R.string.daily_details_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    if (state.isEmpty) {
                        item {
                            IhsanEmptyState(
                                title = stringResource(R.string.statistics_empty_title),
                                message = stringResource(R.string.statistics_empty_body)
                            )
                        }
                    }

                    items(state.dailyItems, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = IhsanTheme.spacing.small),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = White,
                                modifier = Modifier.padding(start = IhsanTheme.spacing.small)
                            ) {
                                Text("${item.dailyProgress}", modifier = Modifier.padding(4.dp))
                            }
                        }
                        HorizontalDivider(color = Grey.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun DailyProgressChart(stats: List<DailyStat>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = Grey

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val spacing = 16.dp.toPx()
        val barWidth = 30.dp.toPx()
        val maxCount = (stats.maxOfOrNull { it.totalCount } ?: 1).coerceAtLeast(1)
        val canvasHeight = size.height - 30.dp.toPx()

        val reversedStats = stats.reversed()
        val totalWidth = (barWidth + spacing) * reversedStats.size - spacing
        val startX = (size.width - totalWidth) / 2

        reversedStats.forEachIndexed { index, stat ->
            val barHeight = (stat.totalCount.toFloat() / maxCount) * canvasHeight
            val x = startX + index * (barWidth + spacing)
            val y = canvasHeight - barHeight

            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.apply {
                val text = stat.date.takeLast(2)
                val paint = android.graphics.Paint().apply {
                    color = labelColor.toArgb()
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(text, x + barWidth / 2, size.height - 5.dp.toPx(), paint)
            }
        }
    }
}
