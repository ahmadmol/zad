package com.example.designsystem.component

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.designsystem.theme.IhsanTheme

/** Pass a null callback when a parent toggleable row owns the interaction. */
@Composable
fun IhsanSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier) {
    val colors = IhsanTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = if (IhsanTheme.isDark) colors.surfaceBase else colors.onBrand,
            checkedTrackColor = colors.accentContent,
            checkedBorderColor = colors.accentContent,
            uncheckedThumbColor = colors.textSecondary,
            uncheckedTrackColor = colors.surfaceMuted,
            uncheckedBorderColor = colors.textSecondary
        )
    )
}
