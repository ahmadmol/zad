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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.azkar.domain.model.Zikr
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    viewModel: TasbihViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val tasbih = uiState.currentTasbih
    val progress = remember(tasbih?.currentCount, tasbih?.targetCount) {
        val target = tasbih?.targetCount ?: 0
        val current = tasbih?.currentCount ?: 0
        if (target > 0) (current.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.tasbih_title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(IhsanTheme.dimens.minTouchTarget)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.onAction(TasbihAction.OnToggleVibration) },
                            modifier = Modifier.size(IhsanTheme.dimens.minTouchTarget)
                        ) {
                            Icon(
                                imageVector = if (uiState.isVibrationEnabled) {
                                    Icons.Filled.Vibration
                                } else {
                                    Icons.Outlined.Vibration
                                },
                                contentDescription = stringResource(R.string.cd_tasbih_vibration),
                                tint = if (uiState.isVibrationEnabled) {
                                    IhsanTheme.colors.brand
                                } else {
                                    IhsanTheme.colors.textSecondaryMuted
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = IhsanTheme.colors.brand,
                        navigationIconContentColor = IhsanTheme.colors.brand
                    )
                )
            },
            containerColor = IhsanTheme.colors.surfaceMuted
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.tasbihList.isNotEmpty()) {
                    DhikrChipsRow(
                        items = uiState.tasbihList,
                        selectedIndex = uiState.selectedTasbihIndex,
                        onSelect = { viewModel.onAction(TasbihAction.OnSelectTasbih(it)) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                DhikrTextCard(
                    tasbih = tasbih,
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                TasbihCounterButton(
                    count = tasbih?.currentCount ?: 0,
                    target = tasbih?.targetCount ?: 0,
                    progress = progress,
                    onClick = {
                        tasbih?.let {
                            if (uiState.isVibrationEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            viewModel.onAction(TasbihAction.OnIncrement(it.id))
                        }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = {
                        tasbih?.let { viewModel.onAction(TasbihAction.OnReset(it.id)) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                        .height(IhsanTheme.dimens.controlHeight),
                    shape = RoundedCornerShape(IhsanTheme.dimens.radiusLarge),
                    border = BorderStroke(1.dp, IhsanTheme.colors.brand.copy(alpha = 0.35f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = IhsanTheme.colors.brand
                    )
                ) {
                    Icon(
                        Icons.Default.RestartAlt,
                        contentDescription = stringResource(R.string.cd_tasbih_reset)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.tasbih_reset_label), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DhikrChipsRow(
    items: List<Zikr>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            val selected = index == selectedIndex
            Surface(
                onClick = { onSelect(index) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) IhsanTheme.colors.brand else MaterialTheme.colorScheme.surface,
                border = if (selected) null else BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
                shadowElevation = 0.dp
            ) {
                Text(
                    text = item.title.ifBlank { item.text.take(16) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = if (selected) IhsanTheme.colors.onBrand else IhsanTheme.colors.brand,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DhikrTextCard(
    tasbih: Zikr?,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceMint),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = tasbih?.text.orEmpty(),
                transitionSpec = {
                    fadeIn(tween(250)) togetherWith fadeOut(tween(200))
                },
                label = "dhikrText"
            ) { text ->
                Text(
                    text = text.ifBlank { "اختر ذكرًا للبدء" },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp
                    ),
                    color = IhsanTheme.colors.brand,
                    textAlign = TextAlign.Center
                )
            }

            if (tasbih != null && tasbih.targetCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "التقدم",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IhsanTheme.colors.brand.copy(alpha = 0.7f)
                    )
                    Surface(
                        color = IhsanTheme.colors.accentWarm,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = IhsanTheme.colors.onWarning
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = IhsanTheme.colors.brand,
                    trackColor = IhsanTheme.colors.brand.copy(alpha = 0.12f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${tasbih.currentCount} / ${tasbih.targetCount}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = IhsanTheme.colors.brand
                )
            }
        }
    }
}

@Composable
private fun TasbihCounterButton(
    count: Int,
    target: Int,
    progress: Float,
    onClick: () -> Unit
) {
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
        animationSpec = tween(300),
        label = "ringProgress"
    )
    val countDescription = stringResource(R.string.tasbih_count_semantics, count, target)
    val incrementCd = stringResource(R.string.cd_tasbih_increment)
    val brand = IhsanTheme.colors.brand
    val accent = IhsanTheme.colors.accentWarm

    Box(
        modifier = Modifier
            .size(260.dp)
            .scale(scale)
            .semantics {
                contentDescription = "$incrementCd. $countDescription"
                stateDescription = countDescription
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    pressed = true
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Progress ring track
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val inset = stroke / 2f + 4.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = brand.copy(alpha = 0.12f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            if (target > 0) {
                drawArc(
                    color = if (progress >= 1f) accent else brand,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(210.dp)
                .shadow(8.dp, CircleShape, ambientColor = brand.copy(alpha = 0.2f))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            brand,
                            brand.copy(alpha = 0.88f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(
                    targetState = count,
                    transitionSpec = {
                        (fadeIn(tween(120)) + scaleIn(initialScale = 0.88f)) togetherWith
                            fadeOut(tween(80))
                    },
                    label = "count"
                ) { value ->
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = IhsanTheme.colors.onBrand
                    )
                }
                Text(
                    text = if (target > 0) "من $target" else "اضغط للتسبيح",
                    style = MaterialTheme.typography.labelLarge,
                    color = IhsanTheme.colors.onBrand.copy(alpha = 0.75f)
                )
            }
        }
    }
}
