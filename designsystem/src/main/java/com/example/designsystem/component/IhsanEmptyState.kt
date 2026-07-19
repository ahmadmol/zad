package com.example.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme

@Composable
fun IhsanEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector = Icons.Outlined.Info,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.extraLarge)
            .semantics {
                contentDescription = buildString {
                    if (!title.isNullOrBlank()) {
                        append(title)
                        append(". ")
                    }
                    append(message)
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = IhsanTheme.colors.textSecondary.copy(alpha = 0.65f)
        )
        Spacer(modifier = Modifier.height(IhsanTheme.spacing.medium))
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = IhsanTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )
        if (onPrimaryAction != null && !primaryActionLabel.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.large))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IhsanButton(onClick = onPrimaryAction) {
                    Text(primaryActionLabel)
                }
                if (onSecondaryAction != null && !secondaryActionLabel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(IhsanTheme.spacing.small))
                    TextButton(onClick = onSecondaryAction) {
                        Text(secondaryActionLabel)
                    }
                }
            }
        }
    }
}
