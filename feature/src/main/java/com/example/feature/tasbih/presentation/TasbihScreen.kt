package com.example.feature.tasbih.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.azkar.domain.model.Zikr
import kotlin.math.cos
import kotlin.math.sin
import org.koin.androidx.compose.koinViewModel

private val DarkTeal = Color(0xFF073E46)
private val AccentMint = Color(0xFF388E8E)
private val OffWhiteSurface = Color(0xFFFAFAF7)

@Composable
fun TasbihScreen(
    viewModel: TasbihViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val tasbih = uiState.currentTasbih
    val isDark = IhsanTheme.isDark

    val brandAccent = if (isDark) IhsanTheme.colors.selectedContent else DarkTeal
    val bgSurface = if (isDark) IhsanTheme.colors.surfaceBase else OffWhiteSurface

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = bgSurface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Scenic Mosque Header Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ihsan_home_hero_background),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay to smoothly blend background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        (if (isDark) IhsanTheme.colors.surfaceBase else Color.Black).copy(alpha = if (isDark) 0.20f else 0.12f),
                                        bgSurface.copy(alpha = 0.70f),
                                        bgSurface
                                    )
                                )
                            )
                    )
                }

                // Main Scrollable Screen Column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Top Bar Header
                    TasbihTopHeader(
                        locationName = uiState.locationName,
                        hijriDate = uiState.hijriDate.ifBlank { "—" },
                        title = stringResource(R.string.tasbih_title),
                        subtitle = "سبح اسم ربك الأعلى",
                        brandAccent = brandAccent,
                        onNavigateBack = onNavigateBack,
                        onSearchClick = {},
                        onNotificationClick = {}
                    )

                    // Body Content
                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                IhsanLoadingState(message = stringResource(R.string.common_loading))
                            }
                        }
                        uiState.error != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                IhsanErrorState(
                                    title = stringResource(R.string.common_retry),
                                    message = uiState.error
                                )
                            }
                        }
                        uiState.tasbihList.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                IhsanEmptyState(
                                    title = stringResource(R.string.tasbih_empty_title),
                                    message = stringResource(R.string.tasbih_empty_subtitle)
                                )
                            }
                        }
                        else -> {
                            val count = tasbih?.currentCount ?: 0
                            val target = tasbih?.targetCount ?: 33
                            val progress = remember(count, target) {
                                if (target > 0) (count.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Tasbih Selector Row (if multiple items exist)
                                    if (uiState.tasbihList.size > 1) {
                                        DhikrSelectorChips(
                                            items = uiState.tasbihList,
                                            selectedIndex = uiState.selectedTasbihIndex,
                                            onSelect = { viewModel.onAction(TasbihAction.OnSelectTasbih(it)) }
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }

                                    // Central Circular Tasbih Widget with Overlapping Plus Button & Action Flanks
                                    TasbihMainCounterWidget(
                                        count = count,
                                        target = target,
                                        progress = progress,
                                        onIncrement = {
                                            tasbih?.let {
                                                if (uiState.isVibrationEnabled) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                                viewModel.onAction(TasbihAction.OnIncrement(it.id))
                                            }
                                        },
                                        onReset = {
                                            tasbih?.let { viewModel.onAction(TasbihAction.OnReset(it.id)) }
                                        },
                                        onShowStats = {
                                            onNavigateToStatistics()
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Dhikr Title Text Below Counter
                                    AnimatedContent(
                                        targetState = tasbih?.text.orEmpty().ifBlank { "سبحان الله" },
                                        transitionSpec = {
                                            fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                                        },
                                        label = "dhikrText"
                                    ) { text ->
                                        Text(
                                            text = text,
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 21.sp
                                            ),
                                            color = brandAccent,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Statistics Summary Cards (Today, This Week, This Month)
                                    Text("إجمالي الذكر المسجل فعليًا", style = MaterialTheme.typography.labelSmall,
                                        color = IhsanTheme.colors.textSecondary, modifier = Modifier.padding(bottom = 6.dp))
                                    TasbihThreeStatsCards(
                                        todayCount = uiState.todayCount,
                                        weekCount = uiState.weekCount,
                                        monthCount = uiState.monthCount
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // Preview Bottom Navigation Bar (Matching exact design)
                                TasbihBottomNavBar(onHomeClick = onNavigateHome)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TasbihTopHeader(
    locationName: String,
    hijriDate: String,
    title: String,
    subtitle: String,
    brandAccent: Color,
    onNavigateBack: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    com.example.designsystem.component.IhsanSkylineHeader(title = title, subtitle = subtitle,
        location = locationName, hijriDate = hijriDate, onBack = onNavigateBack)

}

@Composable
private fun DhikrSelectorChips(
    items: List<Zikr>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val primary = if (IhsanTheme.isDark) IhsanTheme.colors.textPrimary else IhsanTheme.colors.accentContent
    val surface = IhsanTheme.colors.surfaceElevated
    val secondary = IhsanTheme.colors.textSecondary

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            val selected = index == selectedIndex
            Surface(
                onClick = { onSelect(index) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) IhsanTheme.colors.selectedContainer else surface,
                border = BorderStroke(1.dp, if (IhsanTheme.isDark) IhsanTheme.colors.borderSubtle else if (selected) DarkTeal else Color(0xFFE2E8E5)),
                shadowElevation = if (selected) 2.dp else 0.dp,
                modifier = Modifier.defaultMinSize(minHeight = 38.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title.ifBlank { item.text.take(16) },
                        color = if (selected) IhsanTheme.colors.selectedContent else primary,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TasbihMainCounterWidget(
    count: Int,
    target: Int,
    progress: Float,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onShowStats: () -> Unit
) {
    val ringColor = if (IhsanTheme.isDark) IhsanTheme.colors.progressTrack else AccentMint
    val activeProgress = IhsanTheme.colors.progressActive
    val primary = if (IhsanTheme.isDark) IhsanTheme.colors.textPrimary else IhsanTheme.colors.accentContent
    val surface = IhsanTheme.colors.surfaceElevated
    val secondary = IhsanTheme.colors.textSecondary

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = { pressed = false },
        label = "counterScale"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = if (target > 0) progress else 0f,
        animationSpec = tween(220),
        label = "ringProgress"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val circleDiameter = minOf(this.maxWidth * 0.70f, 250.dp)
        val buttonSize = 56.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flanking Button Right: Bar Chart / Statistics Icon
            Surface(
                onClick = onShowStats,
                shape = CircleShape,
                color = surface,
                border = BorderStroke(1.dp, if (IhsanTheme.isDark) IhsanTheme.colors.borderSubtle else Color(0xFFE2E8E5)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(50.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "الإحصائيات"
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "الإحصائيات",
                        tint = primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Center Counter Ring Box
            Box(
                modifier = Modifier
                    .padding(bottom = buttonSize / 2) // Extra space for bottom overlapping plus button
                    .size(circleDiameter)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                // Layer 1: Prayer Beads Arc/Circle Visual + Twin Rings
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val strokeWidth = 3.dp.toPx()
                    val radius = (size.width - strokeWidth) / 2f - 8.dp.toPx()
                    val outerBeadRadius = radius + 10.dp.toPx()

                    // Outer Thin Ring 1
                    drawCircle(
                        color = ringColor.copy(alpha = 0.45f),
                        radius = outerBeadRadius,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Draw Decorative 33 Prayer Beads around circle
                    val totalBeads = 33
                    val beadColor = ringColor.copy(alpha = 0.5f)
                    val beadRadius = 3.dp.toPx()

                    for (i in 0 until totalBeads) {
                        val angle = Math.toRadians((i.toDouble() * 360.0 / totalBeads) - 90.0)
                        val bx = center.x + (outerBeadRadius * cos(angle)).toFloat()
                        val by = center.y + (outerBeadRadius * sin(angle)).toFloat()
                        drawCircle(
                            color = beadColor,
                            radius = beadRadius,
                            center = Offset(bx, by)
                        )
                    }

                    // Inner Track Ring
                    drawCircle(
                        color = ringColor.copy(alpha = 0.25f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Progress Ring Arc (if target > 0)
                    if (target > 0) {
                        val arcSize = Size(radius * 2, radius * 2)
                        val topLeft = Offset(center.x - radius, center.y - radius)

                        drawArc(
                            color = activeProgress,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Layer 2: Inner White Circular Surface
                Surface(
                    modifier = Modifier
                        .size(circleDiameter - 36.dp)
                        .shadow(2.dp, CircleShape, ambientColor = DarkTeal.copy(alpha = 0.15f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                pressed = true
                                onIncrement()
                            }
                        )
                        .semantics {
                            contentDescription = "عداد التسبيح. $count من $target"
                        },
                    shape = CircleShape,
                    color = surface,
                    border = BorderStroke(1.5.dp, if (IhsanTheme.isDark) IhsanTheme.colors.borderSubtle else AccentMint.copy(alpha = 0.30f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Top Misbaha / Prayer Beads Icon (Canvas)
                        Canvas(modifier = Modifier.size(34.dp, 34.dp)) {
                            val iconCenter = Offset(size.width / 2f, size.height / 3f)
                            drawCircle(color = primary, radius = 5.dp.toPx(), center = iconCenter)
                            drawCircle(color = surface, radius = 1.8.dp.toPx(), center = iconCenter - Offset(1.2.dp.toPx(), 1.2.dp.toPx()))
                            val path = Path().apply {
                                moveTo(iconCenter.x, iconCenter.y + 5.dp.toPx())
                                lineTo(iconCenter.x - 6.dp.toPx(), iconCenter.y + 16.dp.toPx())
                                moveTo(iconCenter.x, iconCenter.y + 5.dp.toPx())
                                lineTo(iconCenter.x + 6.dp.toPx(), iconCenter.y + 16.dp.toPx())
                            }
                            drawPath(path = path, color = primary, style = Stroke(width = 1.8.dp.toPx()))
                            drawCircle(color = primary, radius = 3.dp.toPx(), center = Offset(iconCenter.x - 6.dp.toPx(), iconCenter.y + 16.dp.toPx()))
                            drawCircle(color = primary, radius = 3.dp.toPx(), center = Offset(iconCenter.x + 6.dp.toPx(), iconCenter.y + 16.dp.toPx()))
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Center Big Counter Number
                        AnimatedContent(
                            targetState = count,
                            transitionSpec = {
                                (fadeIn(tween(100)) + scaleIn(initialScale = 0.90f)) togetherWith fadeOut(tween(80))
                            },
                            label = "counterNumber"
                        ) { value ->
                            Text(
                                text = value.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = primary
                            )
                        }

                        // Subtitle: Target Count
                        Text(
                            text = "من $target",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = secondary
                        )
                    }
                }

                // Layer 3: Overlapping Central Floating Plus (+) Button
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = buttonSize / 2)
                        .size(buttonSize)
                        .shadow(2.dp, CircleShape, ambientColor = DarkTeal.copy(alpha = 0.35f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                pressed = true
                                onIncrement()
                            }
                        )
                        .semantics {
                            role = Role.Button
                            contentDescription = "زيادة العداد"
                        },
                    shape = CircleShape,
                    color = IhsanTheme.colors.brand
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = IhsanTheme.colors.onBrand,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Flanking Button Left: Undo / Reset Arrow Icon
            Surface(
                onClick = onReset,
                shape = CircleShape,
                color = surface,
                border = BorderStroke(1.dp, if (IhsanTheme.isDark) IhsanTheme.colors.borderSubtle else Color(0xFFE2E8E5)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(50.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "إعادة ضبط العداد"
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "إعادة ضبط",
                        tint = primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TasbihThreeStatsCards(
    todayCount: Int,
    weekCount: Int,
    monthCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1 (Right in RTL): This Month ("هذا الشهر")
        SingleStatCard(
            modifier = Modifier.weight(1f),
            value = monthCount.toString(),
            label = "هذا الشهر"
        )

        // Card 2 (Middle in RTL): This Week ("هذا الأسبوع")
        SingleStatCard(
            modifier = Modifier.weight(1f),
            value = weekCount.toString(),
            label = "هذا الأسبوع"
        )

        // Card 3 (Left in RTL): Today ("اليوم")
        SingleStatCard(
            modifier = Modifier.weight(1f),
            value = todayCount.toString(),
            label = "اليوم"
        )
    }
}

@Composable
private fun SingleStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    val primary = if (IhsanTheme.isDark) IhsanTheme.colors.textPrimary else IhsanTheme.colors.accentContent
    val surface = IhsanTheme.colors.surfaceElevated
    val secondary = IhsanTheme.colors.textSecondary

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = surface
        ),
        border = BorderStroke(1.dp, if (IhsanTheme.isDark) IhsanTheme.colors.borderSubtle else Color(0xFFE8EFEB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = primary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = secondary
            )
        }
    }
}

@Composable
private fun TasbihBottomNavBar(
    onHomeClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = IhsanTheme.colors.navigationSurface,
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Tab ("الرئيسية")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        onClick = onHomeClick,
                        role = Role.Button,
                        onClickLabel = "الذهاب للرئيسية"
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "الرئيسية",
                    tint = if (IhsanTheme.isDark) IhsanTheme.colors.navigationIndicator else DarkTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "الرئيسية",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = if (IhsanTheme.isDark) IhsanTheme.colors.navigationIndicator else DarkTeal
                )
            }

            // Ihsan Tab ("إحسان")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "إحسان",
                    tint = if (IhsanTheme.isDark) IhsanTheme.colors.unselectedContent else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "إحسان",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp
                    ),
                    color = if (IhsanTheme.isDark) IhsanTheme.colors.unselectedContent else Color.Gray
                )
            }

            // Profile Tab ("الملف الشخصي")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "الملف الشخصي",
                    tint = if (IhsanTheme.isDark) IhsanTheme.colors.unselectedContent else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "الملف الشخصي",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp
                    ),
                    color = if (IhsanTheme.isDark) IhsanTheme.colors.unselectedContent else Color.Gray
                )
            }
        }
    }
}
