package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.dashboard.domain.model.HomeSectionState

@Composable
fun <T> HomeSectionStateCard(
    state: HomeSectionState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    when (state) {
        is HomeSectionState.Content -> Box(modifier = modifier) { content(state.value) }
        HomeSectionState.Loading -> HomeSectionStatusCard(
            modifier = modifier,
            message = stringResource(R.string.home_section_loading),
            showProgress = true
        )
        is HomeSectionState.Empty -> HomeSectionStatusCard(
            modifier = modifier,
            message = stringResource(R.string.home_section_empty)
        )
        is HomeSectionState.Error -> HomeSectionStatusCard(
            modifier = modifier,
            message = stringResource(R.string.home_section_error),
            onRetry = if (state.canRetry) onRetry else null
        )
    }
}

@Composable
fun HomeSectionStateNotice(
    state: HomeSectionState<*>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        HomeSectionState.Loading -> HomeSectionStatusCard(
            modifier = modifier,
            message = stringResource(R.string.home_section_loading),
            showProgress = true
        )
        is HomeSectionState.Empty -> HomeSectionStatusCard(
            modifier = modifier,
            message = stringResource(R.string.home_section_empty)
        )
        is HomeSectionState.Error -> HomeSectionStatusCard(
            modifier = modifier,
            message = stringResource(R.string.home_section_error),
            onRetry = if (state.canRetry) onRetry else null
        )
        is HomeSectionState.Content -> Unit
    }
}

@Composable
fun HomeRefreshErrorNotice(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeSectionStatusCard(message = message, onRetry = onRetry, modifier = modifier)
}

@Composable
private fun HomeSectionStatusCard(
    message: String,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
    onRetry: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(IhsanTheme.dimens.radiusPill),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showProgress) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (onRetry != null) {
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.home_retry))
                }
            }
        }
    }
}
