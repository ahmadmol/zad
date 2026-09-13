package com.example.feature.prayer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.R
import com.example.feature.prayer.domain.model.ManualPrayerStatus
import org.koin.androidx.compose.koinViewModel

/**
 * Phase 6 — Manual Prayer Tracker surface.
 *
 * Deliberately neutral in tone: an unrecorded prayer shows an empty circle, never a
 * warning. The app never claims a prayer was missed.
 */
@Composable
fun ManualPrayerTrackerCard(
    modifier: Modifier = Modifier,
    viewModel: ManualPrayerTrackerViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.prayer_tracker_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.prayer_tracker_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                state.trackable.forEach { prayer ->
                    val status = state.logs[prayer]
                    val statusLabel = when (status) {
                        ManualPrayerStatus.PERFORMED ->
                            stringResource(R.string.prayer_tracker_performed)
                        ManualPrayerStatus.LATE ->
                            stringResource(R.string.prayer_tracker_late)
                        ManualPrayerStatus.NOT_PERFORMED ->
                            stringResource(R.string.prayer_tracker_not_performed)
                        null -> stringResource(R.string.prayer_tracker_unrecorded)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            // 48dp minimum touch target (Phase 9 accessibility rule).
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.onPrayerTapped(prayer) }
                            .semantics {
                                role = Role.Button
                                contentDescription = prayer.arabic + ": " + statusLabel
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        StatusDot(status)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = prayer.arabic,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(
                    R.string.prayer_tracker_summary,
                    state.performedCount,
                    state.trackable.size
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusDot(status: ManualPrayerStatus?) {
    val scheme = MaterialTheme.colorScheme
    val background = when (status) {
        ManualPrayerStatus.PERFORMED -> scheme.primary
        ManualPrayerStatus.LATE -> scheme.tertiary
        ManualPrayerStatus.NOT_PERFORMED -> scheme.outline
        null -> scheme.surface
    }

    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = background,
        border = if (status == null) {
            androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant)
        } else {
            null
        }
    ) {
        val icon = when (status) {
            ManualPrayerStatus.PERFORMED -> Icons.Default.Check
            ManualPrayerStatus.LATE -> Icons.Default.Schedule
            ManualPrayerStatus.NOT_PERFORMED -> Icons.Default.Close
            null -> null
        }
        if (icon != null) {
            androidx.compose.foundation.layout.Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.background(background)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
