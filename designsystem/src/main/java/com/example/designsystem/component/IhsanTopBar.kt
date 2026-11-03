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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.designsystem.R
import com.example.designsystem.theme.IhsanTheme

/**
 * Two canonical header geometry modes:
 * - [Compact]: Single-row bar with title/subtitle inline between navigation and actions.
 * - [Expanded]: Two-row hero bar with brand/navigation/actions on top and prominent title/subtitle/metadata below.
 */
enum class IhsanHeaderMode { Compact, Expanded }

/** Compatibility styles map deterministically to the two canonical header modes: Compact and Expanded. */
enum class IhsanTopBarStyle { Hero, Surface, CompactSkyline, Minimal }

fun IhsanTopBarStyle.toHeaderMode(): IhsanHeaderMode =
    if (this == IhsanTopBarStyle.Hero) IhsanHeaderMode.Expanded else IhsanHeaderMode.Compact

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
    @Suppress("UNUSED_PARAMETER") backgroundImageRes: Int? = null,
    leadingContent: @Composable (() -> Unit)? = null
) {
    val mode = style.toHeaderMode()
    val expanded = mode == IhsanHeaderMode.Expanded
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor ?: IhsanTheme.colors.surfaceBase
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                onNavigateBack?.let { back ->
                    IhsanHeaderAction(
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = navigationIconContentDescription,
                        onClick = back
                    )
                    Spacer(Modifier.width(10.dp))
                }
                if (expanded) {
                    if (leadingContent != null) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            leadingContent()
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .semantics { heading() },
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = IhsanTheme.colors.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        subtitle?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = IhsanTheme.colors.textSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    content = actions
                )
            }
            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = IhsanTheme.colors.textPrimary
                    )
                    subtitle?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = IhsanTheme.colors.textSecondary
                        )
                    }
                }
            }
            bottomContent?.let { it() }
        }
    }
}
