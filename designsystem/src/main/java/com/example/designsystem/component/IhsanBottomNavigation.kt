package com.example.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import kotlin.math.roundToInt

private val IndicatorMotion = tween<Float>(durationMillis = 280, easing = FastOutSlowInEasing)
private val IndicatorDpMotion = tween<Dp>(durationMillis = 280, easing = FastOutSlowInEasing)
private val ContentMotion = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)
private val ColorMotion = tween<Color>(durationMillis = 200, easing = FastOutSlowInEasing)

private val IndicatorPillWidth = 76.dp
private val IndicatorPillHeight = 48.dp
private val IndicatorCorner = 24.dp

@Immutable
data class IhsanBottomNavDestination(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

/**
 * Presentation-only bottom navigation with a shared sliding selection indicator.
 * Navigation callbacks and destination selection remain owned by the caller.
 */
@Composable
fun IhsanBottomNavigation(
    destinations: List<IhsanBottomNavDestination>,
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemCount = destinations.size.coerceAtLeast(1)
    val safeIndex = selectedIndex.coerceIn(0, itemCount - 1)
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val barShape = RoundedCornerShape(IhsanTheme.dimens.radiusPill)
    // Follow app IhsanTheme / Material scheme, not the OS system night setting.
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val navColors = IhsanTheme.colors

    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    var containerHeightPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .semantics { testTag = "ihsan_bottom_navigation" }
            .shadow(if (isDark) 2.dp else 8.dp, barShape, clip = false)
            .clip(barShape)
            .background(navColors.navigationSurface)
            .border(width = 1.dp, color = navColors.navigationOutline, shape = barShape)
            .defaultMinSize(minHeight = 72.dp)
            .onSizeChanged {
                containerWidthPx = it.width.toFloat()
                containerHeightPx = it.height.toFloat()
            }
    ) {
        if (containerWidthPx > 0f && destinations.isNotEmpty()) {
            AnimatedSelectionIndicator(
                selectedIndex = safeIndex,
                itemCount = itemCount,
                containerWidthPx = containerWidthPx,
                containerHeightPx = containerHeightPx,
                layoutDirection = layoutDirection,
                density = density
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEachIndexed { index, destination ->
                IhsanBottomNavigationItem(
                    selected = index == safeIndex,
                    onClick = { onDestinationSelected(index) },
                    icon = destination.icon,
                    selectedIcon = destination.selectedIcon,
                    label = destination.label,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
internal fun AnimatedSelectionIndicator(
    selectedIndex: Int,
    itemCount: Int,
    containerWidthPx: Float,
    containerHeightPx: Float,
    layoutDirection: LayoutDirection,
    density: Density
) {
    val navColors = IhsanTheme.colors
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val shadowTint = navColors.navigationIndicator.copy(alpha = if (isDark) 0.12f else 0.28f)
    val pillWidthPx = with(density) { IndicatorPillWidth.toPx() }
    val pillHeightPx = with(density) { IndicatorPillHeight.toPx() }
    val slotWidthPx = containerWidthPx / itemCount.coerceAtLeast(1)

    fun targetLeftPx(index: Int): Float {
        val centerX = when (layoutDirection) {
            LayoutDirection.Ltr -> (index + 0.5f) * slotWidthPx
            LayoutDirection.Rtl -> containerWidthPx - (index + 0.5f) * slotWidthPx
        }
        return (centerX - pillWidthPx / 2f).coerceIn(0f, (containerWidthPx - pillWidthPx).coerceAtLeast(0f))
    }

    // Vertically center the pill on the icon band (above the label).
    val targetTopPx = ((containerHeightPx * 0.42f) - (pillHeightPx / 2f))
        .coerceIn(0f, (containerHeightPx - pillHeightPx).coerceAtLeast(0f))

    val offsetX = remember { Animatable(targetLeftPx(selectedIndex)) }
    var isMoving by remember { mutableStateOf(false) }

    // Geometry / RTL changes should snap without a misleading slide.
    LaunchedEffect(containerWidthPx, itemCount, layoutDirection) {
        offsetX.snapTo(targetLeftPx(selectedIndex))
        isMoving = false
    }

    LaunchedEffect(selectedIndex) {
        val target = targetLeftPx(selectedIndex)
        if (kotlin.math.abs(offsetX.value - target) < 0.5f) return@LaunchedEffect
        isMoving = true
        offsetX.animateTo(target, animationSpec = IndicatorMotion)
        isMoving = false
    }

    val elevation by animateDpAsState(
        targetValue = when {
            isDark && isMoving -> 3.dp
            isDark -> 1.dp
            isMoving -> 9.dp
            else -> 5.dp
        },
        animationSpec = IndicatorDpMotion,
        label = "bottomNavIndicatorElevation"
    )

    Box(
        modifier = Modifier
            .semantics { testTag = "ihsan_bottom_nav_indicator" }
            // Coordinates are already RTL-aware; do not mirror again via relative offset.
            .absoluteOffset {
                IntOffset(
                    x = offsetX.value.roundToInt(),
                    y = targetTopPx.roundToInt()
                )
            }
            .width(IndicatorPillWidth)
            .height(IndicatorPillHeight)
            .graphicsLayer {
                shadowElevation = with(density) { elevation.toPx() }
                shape = RoundedCornerShape(IndicatorCorner)
                clip = false
                ambientShadowColor = shadowTint
                spotShadowColor = shadowTint
            }
            .clip(RoundedCornerShape(IndicatorCorner))
            .background(navColors.navigationIndicator)
    )
}

@Composable
fun IhsanBottomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "bottomNavPressScale"
    )
    val contentOffsetY by animateDpAsState(
        targetValue = if (selected) (-1).dp else 0.dp,
        animationSpec = IndicatorDpMotion,
        label = "bottomNavIconOffsetY"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = ContentMotion,
        label = "bottomNavIconScale"
    )
    val iconSize by animateDpAsState(
        targetValue = if (selected) 28.dp else 26.dp,
        animationSpec = IndicatorDpMotion,
        label = "bottomNavIconSize"
    )
    val navColors = IhsanTheme.colors
    val iconTint by animateColorAsState(
        targetValue = if (selected) {
            navColors.selectedContent
        } else {
            navColors.unselectedContent
        },
        animationSpec = ColorMotion,
        label = "bottomNavIconTint"
    )

    Column(
        modifier = modifier
            .semantics {
                role = Role.Tab
                contentDescription = label
                this.selected = selected
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 36.dp),
                role = Role.Tab,
                onClick = onClick
            )
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(
                    minWidth = IhsanTheme.dimens.minTouchTarget,
                    minHeight = IhsanTheme.dimens.minTouchTarget
                )
                .offset(y = contentOffsetY),
            contentAlignment = Alignment.Center
        ) {
            SelectedIconCircle(selected = selected)
            Icon(
                imageVector = if (selected) selectedIcon else icon,
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
                tint = iconTint
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        BottomNavigationLabel(text = label, selected = selected)
    }
}

@Composable
internal fun SelectedIconCircle(
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val circleSize by animateDpAsState(
        targetValue = if (selected) 44.dp else 34.dp,
        animationSpec = IndicatorDpMotion,
        label = "selectedIconCircleSize"
    )
    val circleAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = ContentMotion,
        label = "selectedIconCircleAlpha"
    )
    val circleElevation by animateDpAsState(
        targetValue = if (selected) 3.dp else 0.dp,
        animationSpec = IndicatorDpMotion,
        label = "selectedIconCircleElevation"
    )
    val navColors = IhsanTheme.colors
    val circleFill = navColors.navigationIconHalo
    val circleBorder = navColors.selectedContent.copy(alpha = 0.22f)
    val shadowTint = navColors.navigationIndicator.copy(alpha = 0.2f)

    if (circleAlpha <= 0.01f) return

    Box(
        modifier = modifier
            .size(circleSize)
            .graphicsLayer {
                alpha = circleAlpha
                shadowElevation = circleElevation.toPx()
                shape = CircleShape
                clip = false
                ambientShadowColor = shadowTint
                spotShadowColor = shadowTint
            }
            .clip(CircleShape)
            .background(circleFill)
            .border(width = 1.dp, color = circleBorder, shape = CircleShape)
    )
}

@Composable
internal fun BottomNavigationLabel(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val navColors = IhsanTheme.colors
    val labelColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            navColors.unselectedContent
        },
        animationSpec = ColorMotion,
        label = "bottomNavLabelColor"
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.65f,
        animationSpec = ContentMotion,
        label = "bottomNavLabelAlpha"
    )

    Text(
        text = text,
        modifier = modifier.graphicsLayer { alpha = labelAlpha },
        fontSize = 11.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        color = labelColor,
        maxLines = 1
    )
}

/* region Previews */

private val PreviewDestinations = listOf(
    IhsanBottomNavDestination("الرئيسية", Icons.Outlined.Home, Icons.Filled.Home),
    IhsanBottomNavDestination("إحسان", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    IhsanBottomNavDestination("حسابي", Icons.Outlined.Person, Icons.Filled.Person)
)

@Preview(name = "Home selected · Light · RTL · 360", widthDp = 360, locale = "ar", showBackground = true)
@Composable
private fun BottomNavHomePreview() {
    IhsanTheme {
        PreviewBar(selectedIndex = 0)
    }
}

@Preview(name = "Ehsan selected · Light · RTL · 360", widthDp = 360, locale = "ar", showBackground = true)
@Composable
private fun BottomNavEhsanPreview() {
    IhsanTheme {
        PreviewBar(selectedIndex = 1)
    }
}

@Preview(name = "Profile selected · Light · RTL · 360", widthDp = 360, locale = "ar", showBackground = true)
@Composable
private fun BottomNavProfilePreview() {
    IhsanTheme {
        PreviewBar(selectedIndex = 2)
    }
}

@Preview(name = "Ehsan · Dark · RTL · 360", widthDp = 360, locale = "ar", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun BottomNavDarkPreview() {
    IhsanTheme(darkTheme = true) {
        PreviewBar(selectedIndex = 1)
    }
}

@Preview(name = "Ehsan · RTL · 320", widthDp = 320, locale = "ar", showBackground = true)
@Composable
private fun BottomNavNarrowPreview() {
    IhsanTheme {
        PreviewBar(selectedIndex = 1)
    }
}

@Preview(name = "Ehsan · RTL · 430", widthDp = 430, locale = "ar", showBackground = true)
@Composable
private fun BottomNavWidePreview() {
    IhsanTheme {
        PreviewBar(selectedIndex = 1)
    }
}

@Composable
private fun PreviewBar(selectedIndex: Int) {
    var index by remember { mutableStateOf(selectedIndex) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        IhsanBottomNavigation(
            destinations = PreviewDestinations,
            selectedIndex = index,
            onDestinationSelected = { index = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        )
    }
}

/* endregion */
