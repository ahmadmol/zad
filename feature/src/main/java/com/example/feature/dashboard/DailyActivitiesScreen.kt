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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.component.DailyProgressSummary
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.dashboard.domain.model.HomeDailyActivityItem
import com.example.feature.dashboard.domain.model.HomeDailyActivitySummary
import com.example.feature.dashboard.domain.model.HomeSectionState

private const val PROGRESS_ANIMATION_MILLIS = 200

@Composable
fun DailyActivitiesScreen(
    activityState: HomeSectionState<HomeDailyActivitySummary>,
    onActivityOpenRoute: (String) -> Unit,
    onBack: () -> Unit = {},
    onRetry: () -> Unit = {},
    onNavigateToCourse: (String) -> Unit = onActivityOpenRoute
) {
    val colors = IhsanTheme.colors

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = colors.surfaceBase,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                DailyActivitiesHero(
                    onBack = onBack,
                    onSearchClick = { onActivityOpenRoute("global_search") },
                    onNotificationClick = { onActivityOpenRoute("reminders_screen") }
                )

                when (activityState) {
                    HomeSectionState.Loading -> DailyActivitiesSkeletonLoading()

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
                                onActivityOpenRoute = onActivityOpenRoute,
                                onNavigateToCourse = onNavigateToCourse
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyActivitiesHero(onBack: () -> Unit, onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}) {
    com.example.designsystem.component.IhsanTopBar(title = stringResource(R.string.correction_daily_title),
        style = com.example.designsystem.component.IhsanTopBarStyle.Hero, backgroundImageRes = com.example.feature.R.drawable.ihsan_home_hero_background, onNavigateBack = onBack,
        actions = {
            IconButton(onSearchClick) { Icon(Icons.Default.Search, stringResource(R.string.common_search)) }
            IconButton(onNotificationClick) { Icon(Icons.Default.Notifications, stringResource(R.string.cd_notifications)) }
        })
}

@Composable
private fun DailyActivitiesContent(
    activities: List<HomeDailyActivityItem>,
    onActivityOpenRoute: (String) -> Unit,
    onNavigateToCourse: (String) -> Unit
) {
    val summary = DailyProgressSummary(
        totalCount = activities.size,
        doneCount = activities.count { it.isCompleted }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val isNarrow = true

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Today Summary & Continuity Card
            item {
                DailySummaryAndStreakCard(summary = summary)
            }

            // 2. Activities Section Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.daily_activities_list_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Bold,
                            color = IhsanTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "أعمال صغيرة .. لقلب أقرب إلى الله",
                            style = MaterialTheme.typography.bodySmall,
                            color = IhsanTheme.colors.textSecondary
                        )
                    }

                    Text(
                        text = "عرض الكل  ›",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = if (IhsanTheme.isDark) IhsanTheme.colors.textBody else Color(0xFF007A6E),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // 3. Grid / List of Activity Cards (6 Cards: Quran, Morning, Tasbeeh, Evening, Name, Dua)
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

            // 4. Weekly Activity Card ("نشاطي هذا الأسبوع")
            item {
                WeeklyActivityCard()
            }

            // 5. Educational Journey Card ("رحلتي التعليمية")
            item {
                EducationalJourneyCard(onNavigateToCourse = onNavigateToCourse)
            }

            // 6. Hadith / Quote Banner
            item {
                HadithQuoteCard()
            }
        }
    }
}

@Composable
private fun DailySummaryAndStreakCard(summary: DailyProgressSummary) {
    val colors = IhsanTheme.colors
    val progress by animateFloatAsState(summary.overallProgress, tween(PROGRESS_ANIMATION_MILLIS), label = "daily-summary-progress")
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceMint)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.daily_activities_summary_title), style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.correction_journey_subtitle), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxSize(),
                    color = IhsanTheme.colors.accentContent, trackColor = colors.borderSubtle,
                    strokeWidth = 4.dp, strokeCap = StrokeCap.Round)
                Text("${summary.doneCount}/${summary.totalCount}", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
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
    val progress by animateFloatAsState(activityProgress(activity), tween(PROGRESS_ANIMATION_MILLIS), label = "${activity.id}-progress")
    val status = when {
        activity.isCompleted -> stringResource(R.string.daily_activities_completed)
        activity.currentCount > 0 -> "${activity.currentCount} / ${activity.targetCount}"
        else -> stringResource(R.string.daily_activities_not_completed)
    }
    val semanticLabel = stringResource(R.string.daily_activities_item_semantics, activity.title, status,
        if (activity.isCompleted) stringResource(R.string.daily_activities_completed) else stringResource(R.string.daily_activities_not_completed))
    Card(modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).clickable(onClick = onClick)
        .clearAndSetSemantics { contentDescription = semanticLabel; role = Role.Button; onClick { onClick(); true } },
        shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, colors.borderSubtle),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(activityIcon(activity.id), null, Modifier.size(24.dp), MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(activity.title, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                Text(status, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
            }
            if (activity.isCompleted) Icon(Icons.Default.Check, null, Modifier.size(24.dp), colors.success)
            else CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(22.dp),
                color = IhsanTheme.colors.accentContent, trackColor = colors.borderSubtle, strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun WeeklyActivityCard() {
    val colors = IhsanTheme.colors
    val days = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
    // Active days matching reference design (Saturday, Sunday, Tuesday, Wednesday, Thursday, Friday active; Monday inactive)
    val activeState = listOf(true, true, false, true, true, true, true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
        border = BorderStroke(1.dp, colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "نشاطي هذا الأسبوع",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "مواصلة الخير تصنع الفرق",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                days.forEachIndexed { index, day ->
                    val isActive = activeState.getOrElse(index) { false }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isActive) colors.success else Color.Transparent)
                                .border(
                                    width = if (isActive) 0.dp else 1.5.dp,
                                    color = if (isActive) Color.Transparent else colors.borderSubtle,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isActive) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.onSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Medium,
                            color = if (isActive) colors.textPrimary else colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EducationalJourneyCard(onNavigateToCourse: (String) -> Unit) {
    val colors = IhsanTheme.colors
    val isDark = IhsanTheme.isDark
    val primaryTeal = if (isDark) colors.selectedContent else Color(0xFF007A6E)
    val darkButtonTeal = if (isDark) colors.selectedContainer else Color(0xFF073E46)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
        border = BorderStroke(1.dp, colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "رحلتي التعليمية",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "📖", fontSize = 16.sp)
                }

                Text(
                    text = "عرض الكل  ›",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = primaryTeal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigateToCourse("sanhya_main") }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Text(
                text = "معرفة أعمق .. وإيمان أرسخ",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Course 1: سنحيا بالقرآن
                CourseCardItem(
                    title = "سنحيا بالقرآن",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    progress = 7f / 29f,
                    progressText = "7 / 29",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToCourse("sanhya_main") }
                )

                // Course 2: نبي الإحسان
                CourseCardItem(
                    title = "نبي الإحسان",
                    icon = Icons.Default.AutoAwesome,
                    progress = 4f / 29f,
                    progressText = "4 / 29",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToCourse("nabi_ihsan_main") }
                )

                // Course 3: الفهم عن الله
                CourseCardItem(
                    title = "الفهم عن الله",
                    icon = Icons.Default.SelfImprovement,
                    progress = 11f / 29f,
                    progressText = "11 / 29",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToCourse("fahm_main") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onNavigateToCourse("sanhya_main") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkButtonTeal)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "متابعة التعلم",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseCardItem(
    title: String,
    icon: ImageVector,
    progress: Float,
    progressText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = IhsanTheme.colors
    val isDark = IhsanTheme.isDark
    val primaryTeal = if (isDark) colors.selectedContent else Color(0xFF007A6E)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) colors.surfaceMuted else Color(0xFFEBF5F1))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDark) colors.surfaceElevated else Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryTeal,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = primaryTeal,
                trackColor = if (isDark) colors.surfaceMuted else Color(0xFFC8DCD5)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = progressText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun HadithQuoteCard() {
    val colors = IhsanTheme.colors
    val isDark = IhsanTheme.isDark
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) colors.surfaceElevated else Color(0xFFF4F8F6)),
        border = BorderStroke(1.dp, if (isDark) colors.borderSubtle else Color(0xFFE0ECE8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "🍃",
                fontSize = 28.sp,
                modifier = Modifier.align(Alignment.BottomStart)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "” وَمَا تَقَرَّبَ إِلَيَّ عَبْدِي بِشَيْءٍ أَحَبَّ إِلَيَّ مِمَّا افْتَرَضْتُ عَلَيْهِ “",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) colors.textPrimary else Color(0xFF073E46),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "رواه البخاري",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = if (isDark) colors.selectedContent else Color(0xFF007A6E),
                    textAlign = TextAlign.Center
                )
            }
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
private fun DailyActivitiesSkeletonLoading() {
    val colors = IhsanTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surfaceElevated.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = colors.selectedContent,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.daily_activities_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }

        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surfaceElevated.copy(alpha = 0.4f))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surfaceElevated.copy(alpha = 0.4f))
                )
            }
        }
    }
}

private fun activityIcon(activityId: String): ImageVector = when (activityId) {
    DailyActivityIds.QURAN_READING -> Icons.AutoMirrored.Filled.MenuBook
    DailyActivityIds.MORNING_AZKAR -> Icons.Default.WbSunny
    DailyActivityIds.EVENING_AZKAR -> Icons.Default.NightsStay
    DailyActivityIds.TASBEEH -> Icons.Default.BrightnessLow
    DailyActivityIds.DAILY_DUA -> Icons.Default.VolunteerActivism
    DailyActivityIds.DAILY_NAME -> Icons.Default.Description
    else -> Icons.Default.Today
}
