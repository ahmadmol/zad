package com.example.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.R
import com.example.designsystem.theme.IhsanTheme

enum class IhsanTopBarStyle {
    Hero,
    Surface,
    CompactSkyline,
    Minimal
}

/**
 * Standardized canonical TopBar across all Ihsan project screens.
 * Supports [Hero] style with atmospheric background and [Surface] style for clean tool/detail screens.
 */
@Composable
fun IhsanTopBar(
    title: String,
    modifier: Modifier = Modifier,
    style: IhsanTopBarStyle = IhsanTopBarStyle.Surface,
    subtitle: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    navigationIconContentDescription: String = stringResource(R.string.back_desc),
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable (() -> Unit)? = null,
    containerColor: Color? = null,
    backgroundImageRes: Int? = null
) {
    when (style) {
        IhsanTopBarStyle.Hero, IhsanTopBarStyle.CompactSkyline -> {
            Column(modifier) {
                IhsanSkylineHeader(title = title, subtitle = subtitle, onBack = onNavigateBack,
                    backgroundImageRes = R.drawable.ihsan_reference_skyline,
                    compact = style == IhsanTopBarStyle.CompactSkyline,
                    actions = actions)
                bottomContent?.invoke()
            }
        }

        IhsanTopBarStyle.Surface, IhsanTopBarStyle.Minimal -> {
            val bg = containerColor ?: IhsanTheme.colors.surfaceElevated
            val textPrimary = IhsanTheme.colors.textPrimary
            val textSecondary = IhsanTheme.colors.textSecondary

            Surface(
                color = bg,
                shadowElevation = 0.5.dp,
                modifier = modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (onNavigateBack != null) {
                                IconButton(
                                    onClick = onNavigateBack,
                                    modifier = Modifier.size(IhsanTheme.dimens.minTouchTarget)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = navigationIconContentDescription,
                                        tint = textPrimary,
                                        modifier = Modifier.size(IhsanTheme.dimens.iconMedium)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Column {
                                IhsanTextWithKaabaIcon(
                                    text = title,
                                    color = textPrimary,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!subtitle.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    IhsanTextWithKaabaIcon(
                                        text = subtitle,
                                        color = textSecondary,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            content = actions
                        )
                    }

                    bottomContent?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        it()
                    }
                }
            }
        }
    }
}

