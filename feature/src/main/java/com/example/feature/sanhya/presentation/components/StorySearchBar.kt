package com.example.feature.sanhya.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.example.designsystem.component.IhsanSearchBar

@Composable
fun StorySearchBar(query: String, onQueryChange: (String) -> Unit,
    onFilterClick: (() -> Unit)? = null,
    placeholderText: String = "ابحث عن قصة، سورة، شخصية أو معنى...", modifier: Modifier = Modifier) {
    val focus = LocalFocusManager.current
    IhsanSearchBar(modifier, query, onQueryChange, placeholderText,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
        onFilterClick = onFilterClick)
}
