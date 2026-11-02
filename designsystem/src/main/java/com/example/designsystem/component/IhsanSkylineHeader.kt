package com.example.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.designsystem.R
import com.example.designsystem.theme.IhsanTheme

@Composable
fun IhsanSkylineHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier,
    location: String? = null, hijriDate: String? = null, showBrand: Boolean = false,
    onBack: (() -> Unit)? = null, @Suppress("UNUSED_PARAMETER") backgroundImageRes: Int = R.drawable.ihsan_reference_skyline,
    actions: @Composable RowScope.() -> Unit = {}, compact: Boolean = false) {
    IhsanTopBar(title, modifier, if (compact) IhsanTopBarStyle.Surface else IhsanTopBarStyle.Hero,
        subtitle, onBack, actions = actions, bottomContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (showBrand) IhsanTextWithKaabaIcon("إحسان", IhsanTheme.colors.accentContent, logoHeight = 28.dp)
                location?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary) }
                hijriDate?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary) }
            }
        })
}
