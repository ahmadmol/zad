package com.example.feature.fahmanallah.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme

@Composable
fun FahmHeroHeader(modifier: Modifier = Modifier, title: String = "الفهم عن الله",
    subtitle: String = "رحلة لفهم تدبير الله والوصول إلى الإحسان", tagText: String = "الجزء الأول • رمضان 2023",
    onBackClick: (() -> Unit)? = null, onSearchClick: (() -> Unit)? = null,
    onSavedClick: (() -> Unit)? = null, onSettingsClick: (() -> Unit)? = null) {
    IhsanTopBar(title, modifier, IhsanTopBarStyle.Hero, subtitle, onBackClick,
        actions = {
            onSearchClick?.let { IconButton(it, Modifier.size(48.dp)) { Icon(Icons.Outlined.Search, "بحث", tint = IhsanTheme.colors.accentContent) } }
            onSavedClick?.let { IconButton(it, Modifier.size(48.dp)) { Icon(Icons.Outlined.BookmarkBorder, "المحفوظات", tint = IhsanTheme.colors.accentContent) } }
            onSettingsClick?.let { IconButton(it, Modifier.size(48.dp)) { Icon(Icons.Outlined.Settings, "الإعدادات", tint = IhsanTheme.colors.accentContent) } }
        }, bottomContent = {
            if (tagText.isNotBlank()) Text(tagText, style = MaterialTheme.typography.labelMedium, color = IhsanTheme.colors.textSecondary)
        })
}
