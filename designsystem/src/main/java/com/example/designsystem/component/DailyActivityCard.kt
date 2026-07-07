package com.example.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DailyActivityCard(
    activities: List<DailyActivityItemData>,
    onGoToChecklist: () -> Unit,
    onActivityClick: (DailyActivityItemData) -> Unit,
    onActivityOpenRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "النشاط اليومي",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "أكمل قائمة نشاطاتك اليومية",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = Color(0xFFC66927),
                shape = RoundedCornerShape(12.dp)
            ) {
                val overallProgress = activities.map { it.progress }.average().toFloat().coerceIn(0f, 1f)
                Text(
                    text = "${(overallProgress * 100).toInt()}%",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val overallProgress = activities.map { it.progress }.average().toFloat().coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = overallProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "التقدم العام ${((overallProgress) * 100).toInt()}%",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        activities.forEach { activity ->
            ActivityItem(
                activity = activity,
                onClick = { onActivityClick(activity) },
                onOpenRoute = { onActivityOpenRoute(activity.route) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGoToChecklist,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "الذهاب إلى القائمة", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActivityItem(
    activity: DailyActivityItemData,
    onClick: () -> Unit,
    onOpenRoute: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${activity.currentCount} من ${activity.targetCount} ${activity.unit}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenRoute) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "فتح",
                        tint = MaterialTheme.colorScheme.primary
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
