package com.example.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun IhsanErrorState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector = Icons.Outlined.ErrorOutline,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.large)
            .semantics {
                contentDescription = buildString {
                    append(title)
                    if (!message.isNullOrBlank()) {
                        append(". ")
                        append(message)
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
        )
        Spacer(modifier = Modifier.height(IhsanTheme.spacing.medium))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (!message.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = IhsanTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
        if (onRetry != null && !retryLabel.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.large))
            IhsanButton(onClick = onRetry) {
                Text(retryLabel)
            }
        }
    }
}
