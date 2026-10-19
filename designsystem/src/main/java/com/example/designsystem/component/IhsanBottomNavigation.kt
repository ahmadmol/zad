package com.example.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme

internal val BottomNavBarHeight = 60.dp

@Immutable
data class IhsanBottomNavDestination(val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

/** Compact navigation: selection colors the icon and a small underline. */
@Composable
fun IhsanBottomNavigation(
    destinations: List<IhsanBottomNavDestination>, selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit, modifier: Modifier = Modifier
) {
    require(selectedIndex in destinations.indices)
    Surface(modifier.semantics { testTag = "ihsan_bottom_navigation" },
        shape = RoundedCornerShape(24.dp), color = IhsanTheme.colors.navigationSurface) {
        Row(Modifier.fillMaxWidth().height(BottomNavBarHeight * LocalDensity.current.fontScale.coerceAtLeast(1f)),
            verticalAlignment = Alignment.CenterVertically) {
            destinations.forEachIndexed { index, item ->
                val fraction by animateFloatAsState(if (index == selectedIndex) 1f else 0f,
                    animationSpec = tween(180), label = "navigationSelection")
                IhsanBottomNavigationItem(fraction, index == selectedIndex, { onDestinationSelected(index) },
                    item.icon, item.selectedIcon, item.label, Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
fun IhsanBottomNavigationItem(selectionFraction: Float, selected: Boolean, onClick: () -> Unit,
    icon: ImageVector, selectedIcon: ImageVector, label: String, modifier: Modifier = Modifier) {
    val color = lerp(IhsanTheme.colors.unselectedContent, IhsanTheme.colors.navigationIndicator, selectionFraction)
    Column(modifier.semantics { role = Role.Tab; contentDescription = label; this.selected = selected }
        .clickable(role = Role.Tab, onClick = onClick).padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(if (selected) selectedIcon else icon, null, tint = color, modifier = Modifier.size(23.dp))
        Spacer(Modifier.height(2.dp))
        BottomNavigationLabel(label, selectionFraction)
        Spacer(Modifier.height(3.dp))
        Box(Modifier.width(32.dp).height(2.dp).graphicsLayer { alpha = selectionFraction }
            .background(IhsanTheme.colors.navigationIndicator, RoundedCornerShape(1.dp))
            .semantics { testTag = "ihsan_bottom_nav_indicator" })
    }
}

@Composable
internal fun BottomNavigationLabel(text: String, selectionFraction: Float, modifier: Modifier = Modifier) {
    Text(text, modifier, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2, textAlign = TextAlign.Center,
        fontWeight = if (selectionFraction > .5f) FontWeight.SemiBold else FontWeight.Normal,
        color = lerp(IhsanTheme.colors.unselectedContent, if (IhsanTheme.isDark) IhsanTheme.colors.textPrimary else IhsanTheme.colors.navigationIndicator, selectionFraction))
}
