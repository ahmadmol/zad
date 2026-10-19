package com.example.feature.nabiihsan.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme

@Composable
fun NabiHeroHeader(title: String, subtitle: String, onBackClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    IhsanTopBar(title, modifier, IhsanTopBarStyle.Hero, subtitle, onBackClick,
        actions = { onSettingsClick?.let { IconButton(it) { Icon(Icons.Default.Settings, "الإعدادات", tint = IhsanTheme.colors.accentContent) } } })
}
