package com.example.feature.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val isDark = isSystemInDarkTheme()
    val heroTextColor = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF073E46)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 110.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ihsan_home_hero_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp)
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
                    tint = heroTextColor
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 52.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.daily_activities_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = heroTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.daily_activities_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = heroTextColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val isNarrow = this.maxWidth < 340.dp || density.fontScale > 1.15f

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Today Summary Card
            item {
                DailySummaryCard(summary = summary)
            }

            // 2. Activities Section Title
            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = stringResource(R.string.daily_activities_list_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = IhsanTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.daily_activities_list_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = IhsanTheme.colors.textSecondary
                    )
                }
            }

            // 3. Grid / List of Activity Cards
            if (isNarrow) {
                items(
                    count = activities.size,
                    key = { index -> activities[index].id }
                ) { index ->
                    val activity = activities[index]
                    DailyActivityGridCard(
                        activity = activity,
                        onClick = { onActivityOpenRoute(activity.route) }
                    )
                }
            } else {
                val chunks = activities.chunked(2)
                items(
                    count = chunks.size,
                    key = { index -> chunks[index].first().id }
                ) { rowIndex ->
                    val rowItems = chunks[rowIndex]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { activity ->
                            Box(modifier = Modifier.weight(1f)) {
                                DailyActivityGridCard(
                                    activity = activity,
                                    onClick = { onActivityOpenRoute(activity.route) }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 4. All complete praise banner if applicable
            if (summary.totalCount > 0 && summary.doneCount == summary.totalCount) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(IhsanTheme.colors.success.copy(alpha = 0.12f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.daily_activities_all_complete),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = IhsanTheme.colors.success,
                            textAlign = TextAlign.Center
                        )
                    }
                }
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

    val dynamicCopy = when (summary.doneCount) {
        0 -> stringResource(R.string.daily_activities_summary_copy_0)
        summary.totalCount -> stringResource(R.string.daily_activities_summary_copy_complete)
        else -> stringResource(R.string.daily_activities_summary_copy_progress)
    }

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
                .padding(horizontal = 18.dp, vertical = 16.dp),
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
                    text = "${summary.doneCount} / ${summary.totalCount}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.progressActive
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        R.string.daily_activities_summary_count,
                        summary.doneCount,
                        summary.totalCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = dynamicCopy,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.progressActive
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = colors.progressActive,
                    trackColor = colors.progressTrack,
                    strokeWidth = 7.dp,
                    strokeCap = StrokeCap.Round
                )

                if (summary.totalCount > 0 && summary.doneCount == summary.totalCount) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.success),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    Text(
                        text = "${summary.doneCount}/${summary.totalCount}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyActivityGridCard(
    activity: HomeDailyActivityItem,
    onClick: () -> Unit
) {
    val colors = IhsanTheme.colors
    val animatedProgress by animateFloatAsState(
        targetValue = activityProgress(activity),
        animationSpec = tween(PROGRESS_ANIMATION_MILLIS),
        label = "${activity.id}-progress"
    )

    val statusText = when {
        activity.isCompleted -> stringResource(R.string.daily_activities_completed)
        activity.currentCount > 0 -> "${activity.currentCount} / ${activity.targetCount}"
        else -> stringResource(R.string.daily_activities_not_started)
    }

    val semanticLabel = stringResource(
        R.string.daily_activities_item_semantics,
        activity.title,
        statusText,
        if (activity.isCompleted) stringResource(R.string.daily_activities_completed) else stringResource(R.string.daily_activities_not_completed)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 104.dp)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = semanticLabel
                role = Role.Button
                onClick {
                    onClick()
                    true
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
        border = BorderStroke(
            width = 1.dp,
            color = if (activity.isCompleted) {
                colors.success.copy(alpha = 0.35f)
            } else {
                colors.borderSubtle
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Icon on start, state indicator on end
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(activityIconContainer(activity.id)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = activityIcon(activity.id),
                        contentDescription = null,
                        tint = if (activity.isCompleted) colors.success else colors.progressActive,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (activity.isCompleted) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(colors.success),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else if (activity.currentCount > 0) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(26.dp),
                            color = colors.progressActive,
                            trackColor = colors.progressTrack,
                            strokeWidth = 3.dp,
                            strokeCap = StrokeCap.Round
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, colors.borderSubtle, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = activity.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (activity.isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    activity.isCompleted -> colors.success
                    activity.currentCount > 0 -> colors.textSecondary
                    else -> colors.textSecondary.copy(alpha = 0.7f)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
    DailyActivityIds.MORNING_AZKAR -> Icons.Default.WbSunny
    DailyActivityIds.EVENING_AZKAR -> Icons.Default.NightsStay
    DailyActivityIds.TASBEEH -> Icons.Default.BrightnessLow
    DailyActivityIds.DAILY_DUA -> Icons.Default.VolunteerActivism
    DailyActivityIds.DAILY_NAME -> Icons.Default.AutoAwesome
    else -> Icons.Default.Today
}
