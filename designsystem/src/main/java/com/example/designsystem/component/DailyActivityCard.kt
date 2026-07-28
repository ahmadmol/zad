package com.example.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme

@Composable
fun DailyActivityCard(
    activities: List<DailyActivityItemData>,
    onGoToChecklist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overallProgress = if (activities.isNotEmpty()) {
        activities.map { it.progress }.average().toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val percentage = (overallProgress * 100).toInt()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val semanticColors = IhsanTheme.colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val hasProgress = overallProgress > 0.001f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "النشاط اليومي، التقدم $percentage بالمئة"
            }
            .clickable { onGoToChecklist() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) semanticColors.surfaceElevated else Color(0xFFF3F8F7)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // RTL Row: first = title (visual right), second = circle (visual left)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "النشاط اليومي",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                    Text(
                        text = "تابع نشاطاتك اليومية",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = primaryColor.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = primaryColor,
                        trackColor = primaryColor.copy(alpha = 0.12f),
                        strokeCap = if (hasProgress) StrokeCap.Round else StrokeCap.Butt
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(60.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = primaryColor,
                        trackColor = primaryColor.copy(alpha = 0.12f),
                        strokeWidth = 6.dp,
                        strokeCap = if (hasProgress) StrokeCap.Round else StrokeCap.Butt
                    )
                    Text(
                        text = "$percentage%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // RTL: first = التقدم العام (visual right), second = فتح القائمة (visual left)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "التقدم العام",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(primaryColor.copy(alpha = 0.08f))
                        .clickable { onGoToChecklist() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "فتح القائمة",
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = primaryColor
                    )
                }
            }
        }
    }
}

data class DailyActivityItemData(
    val id: String,
    val title: String,
    val currentCount: Int,
    val targetCount: Int,
    val unit: String,
    val isCompleted: Boolean,
    val route: String
) {
    val progress: Float
        get() = if (targetCount > 0) {
            currentCount.coerceAtMost(targetCount).toFloat() / targetCount.toFloat()
        } else {
            0f
        }
}
