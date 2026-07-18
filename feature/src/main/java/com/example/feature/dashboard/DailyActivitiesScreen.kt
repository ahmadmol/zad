package com.example.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.designsystem.component.DailyActivityItemData
import com.example.designsystem.component.IhsanEmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyActivitiesScreen(
    activities: List<DailyActivityItemData>,
    onActivityIncrease: (String) -> Unit,
    onActivityOpenRoute: (String) -> Unit,
    onBack: () -> Unit
) {
    val overallProgress = if (activities.isNotEmpty()) {
        activities.map { it.progress }.average().toFloat().coerceIn(0f, 1f)
    } else 0f
    
    val percentage = (overallProgress * 100).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "النشاطات اليومية") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SummaryCard(percentage = percentage, progress = overallProgress)
            }

            item {
                Text(
                    text = "قائمة النشاطات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (activities.isEmpty()) {
                item {
                    IhsanEmptyState(
                        title = "لا توجد نشاطات بعد",
                        message = "ستظهر قائمة النشاطات اليومية هنا بعد تحميل بياناتك."
                    )
                }
            } else {
                items(activities) { activity ->
                    DailyActivityDetailCard(
                        activity = activity,
                        onIncrease = { onActivityIncrease(activity.id) },
                        onOpenRoute = { onActivityOpenRoute(activity.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(percentage: Int, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F6))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ملخص اليوم",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "استمر في نشاطاتك لتحقيق هدفك",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    color = Color(0xFFC66927),
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
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "التقدم العام: $percentage%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DailyActivityDetailCard(
    activity: DailyActivityItemData,
    onIncrease: () -> Unit,
    onOpenRoute: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${activity.currentCount} من ${activity.targetCount} ${activity.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onOpenRoute) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "فتح الموديول")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { activity.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onIncrease,
                    enabled = !activity.isCompleted,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = if (activity.isCompleted) "مكتمل" else "زيادة")
                }
                OutlinedButton(onClick = onOpenRoute, modifier = Modifier.weight(1f)) {
                    Text(text = "فتح")
                }
            }
        }
    }
}
