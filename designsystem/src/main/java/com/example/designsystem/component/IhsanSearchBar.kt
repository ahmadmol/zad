package com.example.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme

@Composable
fun IhsanSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "بحث...",
    onClear: (() -> Unit)? = null
) {
    val fieldShape = RoundedCornerShape(IhsanTheme.dimens.radiusLarge)
    val fieldColors = IhsanTheme.colors

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(IhsanTheme.dimens.controlHeight)
            .border(width = 1.dp, color = fieldColors.fieldBorder, shape = fieldShape),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = {
                    onQueryChange("")
                    onClear?.invoke()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "مسح البحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        shape = fieldShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = fieldColors.fieldContainer,
            unfocusedContainerColor = fieldColors.fieldContainer,
            disabledContainerColor = fieldColors.fieldContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = true
    )
}

