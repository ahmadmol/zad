package com.example.feature.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.Grey
import com.example.designsystem.theme.White
import com.example.feature.R
import com.example.feature.azkar.presentation.AzkarUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(state: AzkarUiState, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") 
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(stringResource(R.string.chart_title), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                DailyProgressChart(stats = state.last7DaysStats)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.total_daily_count), style = MaterialTheme.typography.titleMedium)
                        val totalDaily = state.azkarList.sumOf { it.dailyProgress }
                        Text(
                            text = totalDaily.toString(),
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            item {
                Text(stringResource(R.string.daily_details_title), style = MaterialTheme.typography.titleLarge)
            }

            items(state.azkarList) { zikr ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(zikr.text, modifier = Modifier.weight(1f), fontSize = 18.sp)
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = White,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("${zikr.dailyProgress}", modifier = Modifier.padding(4.dp))
                    }
                }
                HorizontalDivider(color = Grey.copy(alpha = 0.2f))
            }
        }
    }
}

@Composable
fun DailyProgressChart(stats: List<com.example.feature.azkar.domain.model.DailyStat>) {
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
