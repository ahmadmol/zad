package com.example.feature.sanhya.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.feature.learning.presentation.components.LearningSettingsScreen

@Composable
fun SanhyaSettingsScreen(viewModel: SanhyaViewModel, onNavigateBack: () -> Unit, modifier: Modifier = Modifier,
    onNavigateToGeneralSettings: (() -> Unit)? = null) {
    LearningSettingsScreen("إعدادات سنحيا بالقرآن", onNavigateBack, modifier, onNavigateToGeneralSettings)
}
