package com.example.feature.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.DailyProgressSummary
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.dashboard.domain.model.HomeDailyActivityItem
import com.example.feature.dashboard.domain.model.HomeDailyActivitySummary
import com.example.feature.dashboard.domain.model.HomeSectionState

private const val PROGRESS_ANIMATION_MILLIS = 200

@Composable
fun DailyActivitiesScreen(
    activityState: HomeSectionState<HomeDailyActivitySummary>,
    onActivityOpenRoute: (String) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit = {}
) {
    val colors = IhsanTheme.colors

    Scaffold(
        containerColor = colors.surfaceBase,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DailyActivitiesHero(onBack = onBack)

            when (activityState) {
                HomeSectionState.Loading -> IhsanLoadingState(
                    message = stringResource(R.string.daily_activities_loading),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                )

                is HomeSectionState.Error -> IhsanErrorState(
                    title = stringResource(R.string.daily_activities_error_title),
                    message = stringResource(R.string.daily_activities_error_body),
                    retryLabel = if (activityState.canRetry) {
                        stringResource(R.string.common_retry)
                    } else {
                        null
                    },
                    onRetry = if (activityState.canRetry) onRetry else null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                )

                is HomeSectionState.Empty -> DailyActivitiesEmpty()

                is HomeSectionState.Content -> {
                    val activities = activityState.value.items
                    if (activities.isEmpty()) {
                        DailyActivitiesEmpty()
                    } else {
                        DailyActivitiesContent(
                            activities = activities,
                            onActivityOpenRoute = onActivityOpenRoute
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyActivitiesHero(onBack: () -> Unit) {
    val colors = IhsanTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.surfaceMint, colors.surfaceBase)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .heightIn(min = 82.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = colors.textPrimary
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.daily_activities_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = stringResource(R.string.daily_activities_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DailyActivitiesContent(
    activities: List<HomeDailyActivityItem>,
    onActivityOpenRoute: (String) -> Unit
) {
    val summary = DailyProgressSummary(
        totalCount = activities.size,
        doneCount = activities.count { it.isCompleted }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            DailySummaryCard(summary = summary)
        }

        item {
            Text(
                text = stringResource(R.string.daily_activities_list_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IhsanTheme.colors.textPrimary,
                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
            )
        }

        items(
            items = activities,
            key = { it.id }
        ) { activity ->
            DailyActivityRow(
                activity = activity,
                onClick = { onActivityOpenRoute(activity.route) }
            )
        }

        if (summary.totalCount > 0 && summary.doneCount == summary.totalCount) {
            item {
                Text(
                    text = stringResource(R.string.daily_activities_all_complete),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = IhsanTheme.colors.success,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun DailySummaryCard(summary: DailyProgressSummary) {
    val colors = IhsanTheme.colors
    val animatedProgress by animateFloatAsState(
        targetValue = summary.overallProgress,
        animationSpec = tween(PROGRESS_ANIMATION_MILLIS),
        label = "daily-summary-progress"
    )
    val semanticLabel = stringResource(
        R.string.daily_activities_summary_semantics,
        summary.doneCount,
        summary.totalCount,
        summary.percentage
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = semanticLabel },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
        border = BorderStroke(1.dp, colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.daily_activities_summary_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.daily_activities_summary_count,
                        summary.doneCount,
                        summary.totalCount
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier.size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = colors.progressActive,
                    trackColor = colors.progressTrack,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = stringResource(
                        R.string.daily_activities_percentage,
                        summary.percentage
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun DailyActivityRow(
    activity: HomeDailyActivityItem,
    onClick: () -> Unit
) {
    val colors = IhsanTheme.colors
    val progress by animateFloatAsState(
        targetValue = activityProgress(activity),
        animationSpec = tween(PROGRESS_ANIMATION_MILLIS),
        label = "${activity.id}-progress"
    )
    val progressLabel = if (activity.targetCount > 0) {
        stringResource(
            R.string.daily_activities_item_progress,
            activity.currentCount,
            activity.targetCount,
            activity.unit
        )
    } else {
        stringResource(
            R.string.daily_activities_item_count,
            activity.currentCount,
            activity.unit
        )
    }
    val completionLabel = stringResource(
        if (activity.isCompleted) {
            R.string.daily_activities_completed
        } else {
            R.string.daily_activities_not_completed
        }
    )
    val semanticLabel = stringResource(
        R.string.daily_activities_item_semantics,
        activity.title,
        progressLabel,
        completionLabel
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = semanticLabel
                role = Role.Button
                onClick {
                    onClick()
                    true
                }
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
        border = BorderStroke(
            width = 1.dp,
            color = if (activity.isCompleted) {
                colors.success.copy(alpha = 0.42f)
            } else {
                colors.borderSubtle
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(activityIconContainer(activity.id)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activityIcon(activity.id),
                    contentDescription = null,
                    tint = if (activity.isCompleted) colors.success else colors.progressActive,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = progressLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (activity.isCompleted) colors.success else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                if (activity.isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.success.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(28.dp),
                        color = colors.progressActive,
                        trackColor = colors.progressTrack,
                        strokeWidth = 3.dp,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun DailyActivitiesEmpty() {
    IhsanEmptyState(
        title = stringResource(R.string.daily_activities_empty_title),
        message = stringResource(R.string.daily_activities_empty_body),
        icon = Icons.Default.Today,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    )
}

private fun activityProgress(activity: HomeDailyActivityItem): Float =
    if (activity.targetCount > 0) {
        activity.currentCount.coerceAtMost(activity.targetCount).toFloat() /
            activity.targetCount.toFloat()
    } else {
        0f
    }

@Composable
private fun activityIconContainer(activityId: String) = when (activityId) {
    DailyActivityIds.DAILY_DUA -> IhsanTheme.colors.surfaceWarm
    DailyActivityIds.MORNING_AZKAR,
    DailyActivityIds.EVENING_AZKAR -> IhsanTheme.colors.surfaceMint
    else -> IhsanTheme.colors.surfaceMuted
}

private fun activityIcon(activityId: String): ImageVector = when (activityId) {
    DailyActivityIds.QURAN_READING -> Icons.AutoMirrored.Filled.MenuBook
    DailyActivityIds.MORNING_AZKAR,
    DailyActivityIds.EVENING_AZKAR -> Icons.Default.SelfImprovement
    DailyActivityIds.TASBEEH -> Icons.Default.BrightnessLow
    DailyActivityIds.DAILY_DUA -> Icons.Default.VolunteerActivism
    DailyActivityIds.DAILY_NAME -> Icons.Default.AutoAwesome
    else -> Icons.Default.Today
}
