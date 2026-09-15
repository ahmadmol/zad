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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Vibration
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
import androidx.compose.ui.semantics.stateDescription
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

@Composable
fun TasbihScreen(
    viewModel: TasbihViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val tasbih = uiState.currentTasbih
    val isDark = isSystemInDarkTheme()

    // Contrast-aware brand accent color (light teal in dark mode, dark teal in light mode)
    val brandAccent = if (isDark) IhsanTheme.colors.selectedContent else IhsanTheme.colors.brand

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = IhsanTheme.colors.surfaceBase
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Hero Scenic Mosque Background (Home Design Anchor)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ihsan_home_hero_background),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay to blend softly into background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = if (isDark) 0.65f else 0.15f),
                                        IhsanTheme.colors.surfaceBase.copy(alpha = 0.85f),
                                        IhsanTheme.colors.surfaceBase
                                    )
                                )
                            )
                    )
                }

                // Main Layout Column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // Header Row: Back button, Title & Subtitle
                    TasbihHeader(
                        title = stringResource(R.string.tasbih_title),
                        subtitle = stringResource(R.string.tasbih_subtitle),
                        onNavigateBack = onNavigateBack
                    )

                    // Loading, Error, Empty or Main Content state
                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                IhsanLoadingState(message = stringResource(R.string.common_loading))
                            }
                        }
                        uiState.error != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
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
                                modifier = Modifier.fillMaxSize(),
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
                            val target = tasbih?.targetCount ?: 0
                            val progress = remember(count, target) {
                                if (target > 0) (count.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
                            }
                            val isCompleted = target > 0 && count >= target

                            // Dhikr Selector Row (if multiple azkar available in category "تسبيح")
                            if (uiState.tasbihList.size > 1) {
                                DhikrSelectorRow(
                                    items = uiState.tasbihList,
                                    selectedIndex = uiState.selectedTasbihIndex,
                                    onSelect = { viewModel.onAction(TasbihAction.OnSelectTasbih(it)) }
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))

                                // Hero Circular Counter with Prayer Beads (Visual Anchor)
                                TasbihHeroCounter(
                                    count = count,
                                    target = target,
                                    progress = progress,
                                    isCompleted = isCompleted,
                                    brandAccent = brandAccent,
                                    onIncrement = {
                                        tasbih?.let {
                                            if (uiState.isVibrationEnabled) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                            viewModel.onAction(TasbihAction.OnIncrement(it.id))
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Actions Row: Reset & Vibration Toggle
                                TasbihActionsRow(
                                    isVibrationEnabled = uiState.isVibrationEnabled,
                                    brandAccent = brandAccent,
                                    onReset = {
                                        tasbih?.let { viewModel.onAction(TasbihAction.OnReset(it.id)) }
                                    },
                                    onToggleVibration = {
                                        viewModel.onAction(TasbihAction.OnToggleVibration)
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Active Dhikr Title
                                AnimatedContent(
                                    targetState = tasbih?.text.orEmpty(),
                                    transitionSpec = {
                                        fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                                    },
                                    label = "dhikrText"
                                ) { text ->
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        ),
                                        color = IhsanTheme.colors.textPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Real Statistics / Progress Summary Cards (100% Product Truth)
                                TasbihRealStatsCards(
                                    currentCount = count,
                                    targetCount = target,
                                    brandAccent = brandAccent
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TasbihHeader(
    title: String,
    subtitle: String,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Back Button (Ensuring strict >= 48dp touch target)
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(IhsanTheme.dimens.minTouchTarget)
                .clip(CircleShape)
                .background(IhsanTheme.colors.surfaceElevated.copy(alpha = 0.85f))
                .semantics {
                    role = Role.Button
                    contentDescription = "رجوع"
                }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = IhsanTheme.colors.textPrimary
            )
        }

        // Title & Subtitle Centered
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = IhsanTheme.colors.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = IhsanTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun DhikrSelectorRow(
    items: List<Zikr>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            val selected = index == selectedIndex
            Surface(
                onClick = { onSelect(index) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) IhsanTheme.colors.selectedContainer else IhsanTheme.colors.surfaceElevated.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, if (selected) IhsanTheme.colors.selectedContainer else IhsanTheme.colors.borderSubtle),
                shadowElevation = if (selected) 2.dp else 0.dp,
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title.ifBlank { item.text.take(16) },
                        color = if (selected) IhsanTheme.colors.selectedContent else IhsanTheme.colors.textPrimary,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TasbihHeroCounter(
    count: Int,
    target: Int,
    progress: Float,
    isCompleted: Boolean,
    brandAccent: Color,
    onIncrement: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
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

    val incrementCd = stringResource(R.string.cd_tasbih_increment)
    val countDescription = stringResource(R.string.tasbih_count_semantics, count, target)
    val accent = IhsanTheme.colors.accentWarm
    val isDark = isSystemInDarkTheme()

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val circleDiameter = minOf(this.maxWidth * 0.68f, 250.dp)
        val buttonSize = 64.dp

        Box(
            modifier = Modifier
                .padding(bottom = buttonSize / 2) // Space for overlapping plus button
                .size(circleDiameter)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            // Layer 1: Prayer Beads Arc/Circle Visual + Progress Ring (Canvas)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val strokeWidth = 10.dp.toPx()
                val radius = (size.width - strokeWidth) / 2f - 12.dp.toPx()
                val beadsRadius = radius + 14.dp.toPx()

                // Draw Decorative Prayer Beads (السبحة) around outer ring
                val totalBeads = 33
                val beadColor = if (isDark) brandAccent.copy(alpha = 0.45f) else brandAccent.copy(alpha = 0.25f)
                val beadRadius = 4.dp.toPx()

                for (i in 0 until totalBeads) {
                    val angle = Math.toRadians((i.toDouble() * 360.0 / totalBeads) - 90.0)
                    val bx = center.x + (beadsRadius * cos(angle)).toFloat()
                    val by = center.y + (beadsRadius * sin(angle)).toFloat()
                    drawCircle(
                        color = beadColor,
                        radius = beadRadius,
                        center = Offset(bx, by)
                    )
                }

                // Top Imam Bead / Tassel decorative knot
                val topX = center.x
                val topY = center.y - beadsRadius - 2.dp.toPx()
                drawCircle(
                    color = brandAccent.copy(alpha = 0.7f),
                    radius = 6.dp.toPx(),
                    center = Offset(topX, topY)
                )

                // Draw Track Ring
                drawCircle(
                    color = brandAccent.copy(alpha = 0.12f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Draw Progress Ring (if target exists)
                if (target > 0) {
                    val arcSize = Size(radius * 2, radius * 2)
                    val topLeft = Offset(center.x - radius, center.y - radius)

                    drawArc(
                        color = if (isCompleted) accent else brandAccent,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Layer 2: Soft Inner Circular Surface
            Surface(
                modifier = Modifier
                    .size(circleDiameter - 44.dp)
                    .shadow(8.dp, CircleShape, ambientColor = brandAccent.copy(alpha = 0.2f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            pressed = true
                            onIncrement()
                        }
                    )
                    .semantics {
                        contentDescription = "$incrementCd. $countDescription"
                        stateDescription = countDescription
                    },
                shape = CircleShape,
                color = if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFF2FAF6),
                border = BorderStroke(1.5.dp, brandAccent.copy(alpha = 0.22f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Top Decorative Prayer Beads Icon (Canvas)
                    Canvas(modifier = Modifier.size(32.dp, 32.dp)) {
                        val iconCenter = Offset(size.width / 2f, size.height / 3f)
                        drawCircle(color = brandAccent.copy(alpha = 0.85f), radius = 5.dp.toPx(), center = iconCenter)
                        drawCircle(color = Color.White.copy(alpha = 0.7f), radius = 1.8.dp.toPx(), center = iconCenter - Offset(1.2.dp.toPx(), 1.2.dp.toPx()))
                        val path = Path().apply {
                            moveTo(iconCenter.x, iconCenter.y + 5.dp.toPx())
                            lineTo(iconCenter.x - 5.dp.toPx(), iconCenter.y + 15.dp.toPx())
                            moveTo(iconCenter.x, iconCenter.y + 5.dp.toPx())
                            lineTo(iconCenter.x + 5.dp.toPx(), iconCenter.y + 15.dp.toPx())
                        }
                        drawPath(path = path, color = brandAccent.copy(alpha = 0.85f), style = Stroke(width = 1.5.dp.toPx()))
                        drawCircle(color = brandAccent.copy(alpha = 0.85f), radius = 2.5.dp.toPx(), center = Offset(iconCenter.x - 5.dp.toPx(), iconCenter.y + 15.dp.toPx()))
                        drawCircle(color = brandAccent.copy(alpha = 0.85f), radius = 2.5.dp.toPx(), center = Offset(iconCenter.x + 5.dp.toPx(), iconCenter.y + 15.dp.toPx()))
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Center Big Number
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
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = brandAccent
                        )
                    }

                    // Target / Label text
                    Text(
                        text = if (target > 0) stringResource(R.string.tasbih_target_prefix, target) else stringResource(R.string.tasbih_count_unit),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = IhsanTheme.colors.textSecondary
                    )
                }
            }

            // Layer 3: Overlapping Central `+` Button (64dp diameter)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = buttonSize / 2)
                    .size(buttonSize)
                    .shadow(6.dp, CircleShape, ambientColor = brandAccent.copy(alpha = 0.35f))
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
                        contentDescription = incrementCd
                    },
                shape = CircleShape,
                color = if (isDark) IhsanTheme.colors.selectedContainer else IhsanTheme.colors.brand
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isDark) IhsanTheme.colors.selectedContent else IhsanTheme.colors.onBrand,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TasbihActionsRow(
    isVibrationEnabled: Boolean,
    brandAccent: Color,
    onReset: () -> Unit,
    onToggleVibration: () -> Unit
) {
    val resetCd = stringResource(R.string.cd_tasbih_reset)
    val vibrationCd = stringResource(R.string.cd_tasbih_vibration)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset Action (48dp touch target)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                onClick = onReset,
                shape = CircleShape,
                color = IhsanTheme.colors.surfaceElevated,
                border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = resetCd
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = resetCd,
                        tint = brandAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.tasbih_reset_label),
                style = MaterialTheme.typography.labelSmall,
                color = IhsanTheme.colors.textSecondary
            )
        }

        // Vibration Toggle Action (48dp touch target)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                onClick = onToggleVibration,
                shape = CircleShape,
                color = IhsanTheme.colors.surfaceElevated,
                border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = vibrationCd
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isVibrationEnabled) Icons.Filled.Vibration else Icons.Outlined.Vibration,
                        contentDescription = vibrationCd,
                        tint = if (isVibrationEnabled) brandAccent else IhsanTheme.colors.textDisabled,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isVibrationEnabled) stringResource(R.string.tasbih_vibration_active) else stringResource(R.string.tasbih_vibration_muted),
                style = MaterialTheme.typography.labelSmall,
                color = IhsanTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun TasbihRealStatsCards(
    currentCount: Int,
    targetCount: Int,
    brandAccent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Current Count Card ("الحالي")
        StatCard(
            modifier = Modifier.weight(1f),
            value = currentCount.toString(),
            label = stringResource(R.string.tasbih_stat_current),
            brandAccent = brandAccent
        )

        // Target Card ("الهدف")
        StatCard(
            modifier = Modifier.weight(1f),
            value = if (targetCount > 0) targetCount.toString() else "—",
            label = stringResource(R.string.tasbih_stat_target),
            brandAccent = brandAccent
        )

        // Remaining Card ("المتبقي")
        StatCard(
            modifier = Modifier.weight(1f),
            value = if (targetCount > 0) (targetCount - currentCount).coerceAtLeast(0).toString() else "—",
            label = stringResource(R.string.tasbih_stat_remaining),
            brandAccent = brandAccent
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    brandAccent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = IhsanTheme.colors.surfaceElevated
        ),
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = brandAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp
                ),
                color = IhsanTheme.colors.textSecondary
            )
        }
    }
}
