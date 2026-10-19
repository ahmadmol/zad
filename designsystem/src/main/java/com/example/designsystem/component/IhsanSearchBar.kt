package com.example.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme

@Composable
fun IhsanSearchBar(modifier: Modifier = Modifier, query: String, onQueryChange: (String) -> Unit,
    placeholder: String = "بحث...", onClear: (() -> Unit)? = null, keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onFilterClick: (() -> Unit)? = null) {
    val focused = remember { mutableStateOf(false) }
    Surface(modifier.fillMaxWidth().heightIn(min = 42.dp), shape = RoundedCornerShape(20.dp),
        color = IhsanTheme.colors.fieldContainer, border = BorderStroke(1.dp, if (focused.value) IhsanTheme.colors.fieldFocusedBorder else IhsanTheme.colors.fieldBorder)) {
        BasicTextField(value = query, onValueChange = onQueryChange, singleLine = true,
            keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth().onFocusChanged { focused.value = it.isFocused },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(if (IhsanTheme.isDark) IhsanTheme.colors.accentContent else MaterialTheme.colorScheme.primary), decorationBox = { inner ->
                Row(Modifier.padding(start = 12.dp, end = if (query.isEmpty()) 12.dp else 0.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, Modifier.size(20.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).padding(vertical = 10.dp)) {
                        if (query.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        inner()
                    }
                    if (query.isNotEmpty()) IconButton(onClick = { onQueryChange(""); onClear?.invoke() }) {
                        Icon(Icons.Default.Close, "مسح البحث", Modifier.size(20.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    onFilterClick?.let { filter -> IconButton(onClick = filter) {
                        Icon(androidx.compose.material.icons.Icons.Default.FilterList, "تصفية", Modifier.size(20.dp), IhsanTheme.colors.accentContent)
                    } }
                }
            })
    }
}
