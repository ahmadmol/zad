package com.example.feature.sanhya.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.designsystem.component.IhsanFilterChip

@Composable
fun StoryTagChip(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IhsanFilterChip(text, isSelected, onClick, modifier)
}
