package com.example.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    } else 0f
    
    val percentage = (overallProgress * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onGoToChecklist() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceMint)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "النشاط اليومي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "تابع نشاطاتك اليومية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    color = IhsanTheme.colors.accentWarm,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$percentage%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "التقدم العام",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                TextButton(
                    onClick = onGoToChecklist,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "فتح القائمة",
                        color = IhsanTheme.colors.accentWarm,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = IhsanTheme.colors.accentWarm
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
        get() = if (targetCount > 0) (currentCount.coerceAtMost(targetCount).toFloat() / targetCount.toFloat()) else 0f
}
