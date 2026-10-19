package com.example.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme

@Composable
fun IhsanFilterChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.clickable(role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) IhsanTheme.colors.selectedContainer else IhsanTheme.colors.surfaceMint) {
        Text(text, Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) IhsanTheme.colors.selectedContent else if (IhsanTheme.isDark) IhsanTheme.colors.textBody else IhsanTheme.colors.accentContent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IhsanFilterSheet(onDismiss: () -> Unit, sheetState: SheetState,
    modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface, modifier = modifier, content = content)
}

@Composable
fun IhsanStatusChip(label: String, modifier: Modifier = Modifier,
    container: androidx.compose.ui.graphics.Color = IhsanTheme.colors.selectedContainer,
    content: androidx.compose.ui.graphics.Color = IhsanTheme.colors.selectedContent) {
    Surface(modifier, color = container, shape = RoundedCornerShape(20.dp)) {
        Text(label, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = content, fontSize = 11.sp)
    }
}
