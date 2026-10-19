package com.example.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.designsystem.theme.IhsanTheme

@Composable
fun IhsanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = IhsanTheme.dimens.controlHeightCompact),
        enabled = enabled,
        shape = RoundedCornerShape(IhsanTheme.dimens.radiusMedium),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        content = content
    )
}

@Composable
fun IhsanSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    IhsanButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = if (IhsanTheme.isDark) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.secondary,
        contentColor = if (IhsanTheme.isDark) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondary,
        enabled = enabled,
        content = content
    )
}
