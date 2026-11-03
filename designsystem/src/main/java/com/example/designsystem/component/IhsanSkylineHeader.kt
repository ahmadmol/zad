package com.example.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.designsystem.R
import com.example.designsystem.theme.IhsanTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IhsanSkylineHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    location: String? = null,
    hijriDate: String? = null,
    showBrand: Boolean = false,
    onBack: (() -> Unit)? = null,
    @Suppress("UNUSED_PARAMETER") backgroundImageRes: Int = R.drawable.ihsan_reference_skyline,
    actions: @Composable RowScope.() -> Unit = {},
    compact: Boolean = false
) {
    val hasLocation = !location.isNullOrBlank()
    val hasDate = !hijriDate.isNullOrBlank()
    val hasMeta = hasLocation || hasDate

    IhsanTopBar(
        title = title,
        modifier = modifier,
        style = if (compact) IhsanTopBarStyle.Surface else IhsanTopBarStyle.Hero,
        subtitle = subtitle,
        onNavigateBack = onBack,
        actions = actions,
        leadingContent = if (showBrand && !compact) {
            { IhsanTextWithKaabaIcon("إحسان", IhsanTheme.colors.accentContent, logoHeight = 32.dp) }
        } else null,
        bottomContent = if ((showBrand && compact) || hasMeta) {
            {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (showBrand && compact) {
                        IhsanTextWithKaabaIcon("إحسان", IhsanTheme.colors.accentContent, logoHeight = 24.dp)
                    }
                    if (hasLocation) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = IhsanTheme.colors.accentContent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = location.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = IhsanTheme.colors.textSecondary
                            )
                        }
                    }
                    if (hasLocation && hasDate) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = IhsanTheme.colors.textSecondary
                        )
                    }
                    if (hasDate) {
                        Text(
                            text = hijriDate.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = IhsanTheme.colors.textSecondary
                        )
                    }
                }
            }
        } else null
    )
}
