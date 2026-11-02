package com.example.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.designsystem.R
import com.example.designsystem.theme.IhsanTheme

/** Compatibility styles map to two layouts: standard and large. */
enum class IhsanTopBarStyle { Hero, Surface, CompactSkyline, Minimal }

@Composable
fun IhsanTopBar(
    title: String, modifier: Modifier = Modifier,
    style: IhsanTopBarStyle = IhsanTopBarStyle.Surface, subtitle: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    navigationIconContentDescription: String = stringResource(R.string.back_desc),
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable (() -> Unit)? = null,
    containerColor: Color? = null,
    @Suppress("UNUSED_PARAMETER") backgroundImageRes: Int? = null
) {
    val large = style == IhsanTopBarStyle.Hero
    Surface(modifier.fillMaxWidth(), color = containerColor ?: IhsanTheme.colors.surfaceBase) {
        Column(Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                onNavigateBack?.let { back ->
                    FilledTonalIconButton(back, Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = IhsanTheme.colors.surfaceElevated,
                            contentColor = IhsanTheme.colors.textPrimary)) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, navigationIconContentDescription, Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                }
                if (!large) Column(Modifier.weight(1f).semantics { heading() }) {
                    Text(title, style = MaterialTheme.typography.titleLarge, color = IhsanTheme.colors.textPrimary)
                    subtitle?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary)
                    }
                } else Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically, content = actions)
            }
            if (large) {
                Text(title, Modifier.padding(top = 12.dp).semantics { heading() },
                    style = MaterialTheme.typography.headlineLarge, color = IhsanTheme.colors.textPrimary)
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Text(it, Modifier.padding(top = 6.dp, bottom = 8.dp),
                        style = MaterialTheme.typography.bodyMedium, color = IhsanTheme.colors.textSecondary)
                }
            }
            bottomContent?.let { Spacer(Modifier.height(12.dp)); it() }
        }
    }
}
