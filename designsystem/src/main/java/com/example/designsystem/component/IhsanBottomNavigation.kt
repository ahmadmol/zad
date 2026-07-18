package com.example.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme

@Composable
fun IhsanBottomNavigation(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    NavigationBar(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(IhsanTheme.dimens.radiusPill), clip = false)
            .clip(RoundedCornerShape(IhsanTheme.dimens.radiusPill)),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        content = content
    )
}

@Composable
fun RowScope.IhsanBottomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Box(
                modifier = Modifier
                    .semantics {
                        role = Role.Tab
                        contentDescription = label
                        this.selected = selected
                    }
                    .defaultMinSize(
                        minWidth = IhsanTheme.dimens.minTouchTarget,
                        minHeight = IhsanTheme.dimens.minTouchTarget
                    )
                    .then(
                        if (selected) {
                            Modifier
                                .clip(RoundedCornerShape(IhsanTheme.dimens.radiusPill))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        } else {
                            Modifier.padding(8.dp)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (selected) selectedIcon else icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    }
                )
            }
        },
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Gray
                }
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            unselectedTextColor = Color.Gray
        ),
        modifier = modifier
    )
}
