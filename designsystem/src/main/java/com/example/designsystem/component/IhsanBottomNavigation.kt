package com.example.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import kotlin.math.abs

private val IndicatorMotion = tween<Float>(durationMillis = 280, easing = FastOutSlowInEasing)

/** Compact bar / pill dimensions (within requested 68–74 / 72–88 / 42–48 ranges). */
internal val BottomNavBarHeight = 70.dp
private val IndicatorPillWidth = 80.dp
private val IndicatorPillHeight = 44.dp
private val IndicatorCorner = 22.dp
private val BarCorner = 24.dp

@Immutable
data class IhsanBottomNavDestination(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

/**
 * Presentation-only bottom navigation.
 *
 * Selection uses a **per-item** deep-green pill behind the icon (not a
 * sliding absolute-offset layer) so RTL layout cannot misplace the indicator.
 */
@Composable
fun IhsanBottomNavigation(
    destinations: List<IhsanBottomNavDestination>,
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    require(selectedIndex in destinations.indices) {
        "selectedIndex=$selectedIndex out of bounds for ${destinations.size} destinations"
    }
    val safeIndex = selectedIndex
    val barShape = RoundedCornerShape(BarCorner)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val navColors = IhsanTheme.colors

    val animatedIndex = remember { Animatable(safeIndex.toFloat()) }
    LaunchedEffect(safeIndex) {
        if (abs(animatedIndex.value - safeIndex) < 0.01f) return@LaunchedEffect
        animatedIndex.animateTo(safeIndex.toFloat(), animationSpec = IndicatorMotion)
    }

    Box(
        modifier = modifier
            .semantics { testTag = "ihsan_bottom_navigation" }
            .shadow(if (isDark) 1.dp else 2.dp, barShape, clip = false)
            .clip(barShape)
            .background(navColors.navigationSurface)
            .border(width = 1.dp, color = navColors.navigationOutline, shape = barShape)
            .height(BottomNavBarHeight)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEachIndexed { index, destination ->
                val distance = abs(animatedIndex.value - index)
                val fraction = (1f - distance).coerceIn(0f, 1f)

                IhsanBottomNavigationItem(
                    selectionFraction = fraction,
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
fun IhsanBottomNavigationItem(
    selectionFraction: Float,
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

    val navColors = IhsanTheme.colors
    val isVisualSelected = selectionFraction > 0.5f

    // White icon only when green pill is visible under it.
    val iconTint = lerp(navColors.unselectedContent, navColors.selectedContent, selectionFraction)
    val iconScale = androidx.compose.ui.util.lerp(1f, 1.05f, selectionFraction)
    val iconSize = lerp(22.dp, 24.dp, selectionFraction)
    val pillShape = RoundedCornerShape(IndicatorCorner)

    Column(
        modifier = modifier
            .semantics {
                role = Role.Tab
                contentDescription = label
                this.selected = selected
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 32.dp),
                role = Role.Tab,
                onClick = onClick
            )
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(IndicatorPillWidth)
                .height(IndicatorPillHeight),
            contentAlignment = Alignment.Center
        ) {
            if (selectionFraction > 0.01f) {
                Box(
                    modifier = Modifier
                        .semantics { testTag = "ihsan_bottom_nav_indicator" }
                        .matchParentSize()
                        .graphicsLayer { alpha = selectionFraction }
                        .clip(pillShape)
                        .background(navColors.navigationIndicator)
                )
            }
            Icon(
                imageVector = if (isVisualSelected) selectedIcon else icon,
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
        BottomNavigationLabel(text = label, selectionFraction = selectionFraction)
    }
}

@Composable
internal fun BottomNavigationLabel(
    text: String,
    selectionFraction: Float,
    modifier: Modifier = Modifier
) {
    val navColors = IhsanTheme.colors
    val labelColor = lerp(navColors.unselectedContent, MaterialTheme.colorScheme.primary, selectionFraction)

    Text(
        text = text,
        modifier = modifier,
        fontSize = 11.sp,
        fontWeight = if (selectionFraction > 0.5f) FontWeight.SemiBold else FontWeight.Medium,
        color = labelColor,
        maxLines = 1,
        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 14.sp)
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
    IhsanTheme { PreviewBar(selectedIndex = 0) }
}

@Preview(name = "Ehsan selected · Light · RTL · 360", widthDp = 360, locale = "ar", showBackground = true)
@Composable
private fun BottomNavEhsanPreview() {
    IhsanTheme { PreviewBar(selectedIndex = 1) }
}

@Preview(name = "Profile selected · Light · RTL · 360", widthDp = 360, locale = "ar", showBackground = true)
@Composable
private fun BottomNavProfilePreview() {
    IhsanTheme { PreviewBar(selectedIndex = 2) }
}

@Preview(
    name = "Ehsan · Dark · RTL · 360",
    widthDp = 360,
    locale = "ar",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun BottomNavDarkPreview() {
    IhsanTheme(darkTheme = true) { PreviewBar(selectedIndex = 1) }
}

@Preview(name = "Home · Light · RTL · 320", widthDp = 320, locale = "ar", showBackground = true)
@Composable
private fun BottomNavNarrowPreview() {
    IhsanTheme { PreviewBar(selectedIndex = 0) }
}

@Preview(
    name = "Profile · Dark · RTL · 360",
    widthDp = 360,
    locale = "ar",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun BottomNavProfileDarkPreview() {
    IhsanTheme(darkTheme = true) { PreviewBar(selectedIndex = 2) }
}

@Composable
private fun PreviewBar(selectedIndex: Int) {
    var index by remember { mutableStateOf(selectedIndex) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        IhsanBottomNavigation(
            destinations = PreviewDestinations,
            selectedIndex = index,
            onDestinationSelected = { index = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(BottomNavBarHeight)
        )
    }
}

/* endregion */
