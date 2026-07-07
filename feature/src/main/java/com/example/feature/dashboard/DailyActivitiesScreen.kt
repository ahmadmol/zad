package com.example.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.DailyActivityItemData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyActivitiesScreen(
    activities: List<DailyActivityItemData>,
    onActivityIncrease: (String) -> Unit,
    onActivityOpenRoute: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "النشاطات اليومية") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "عودة")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "اكتشف تقدمك اليومي وأكمل النشاطات",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "فتح الموديول")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = activity.progress.coerceIn(0f, 1f),
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
                Button(onClick = onIncrease, modifier = Modifier.weight(1f)) {
                    Text(text = if (activity.isCompleted) "مكتمل" else "زيادة")
                }
                OutlinedButton(onClick = onOpenRoute, modifier = Modifier.weight(1f)) {
                    Text(text = "فتح")
                }
            }
        }
    }
}
