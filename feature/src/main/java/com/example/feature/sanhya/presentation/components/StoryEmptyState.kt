package com.example.feature.sanhya.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.designsystem.component.IhsanEmptyState

@Composable
fun StoryEmptyState(title: String, message: String, modifier: Modifier = Modifier,
    onRetryClick: (() -> Unit)? = null) {
    IhsanEmptyState(title = title, message = message, modifier = modifier,
        primaryActionLabel = if (onRetryClick != null) "إعادة التعيين" else null, onPrimaryAction = onRetryClick)
}
