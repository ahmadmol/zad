package com.example.feature.nabiihsan.presentation

import androidx.compose.runtime.Composable
import com.example.feature.learning.presentation.components.LearningSettingsScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun NabiIhsanSettingsScreen(viewModel: NabiIhsanViewModel = koinViewModel(), onNavigateBack: () -> Unit = {}) {
    LearningSettingsScreen("إعدادات نبي الإحسان", onNavigateBack)
}
