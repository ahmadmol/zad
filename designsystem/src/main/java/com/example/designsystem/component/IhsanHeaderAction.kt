package com.example.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme

/**
 * Standardized header action button used across Home, Community, Profile, and secondary screens.
 * Enforces unified 48dp minimum touch-target geometry, 12dp rounded container shape, and high contrast.
 */
@Composable
fun IhsanHeaderAction(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = IhsanTheme.colors.textPrimary,
    containerColor: Color = IhsanTheme.colors.surfaceElevated,
    border: BorderStroke? = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
    size: Dp = 48.dp,
    iconSize: Dp = 20.dp
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(containerColor)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
