package com.example.feature.ehsan.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EhsanAdvancedFilters(state: EhsanUiState, onDismiss: () -> Unit,
    onApply: (String, String, String) -> Unit) {
    var location by remember { mutableStateOf(state.selectedLocation) }
    var category by remember { mutableStateOf(state.selectedCategory) }
    var sort by remember { mutableStateOf(state.selectedSort) }
    IhsanFilterSheet(onDismiss, rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("البحث والتصفية", style = MaterialTheme.typography.titleLarge)
            Text("الموقع", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                (listOf("الكل") + state.donations.map { it.location }.filter { it.isNotBlank() }.distinct()).forEach {
                    IhsanFilterChip(it, location == it, { location = it })
                }
            }
            Text("التصنيف", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                (listOf("الكل") + state.donations.map { it.category }.filter { it.isNotBlank() }.distinct()).forEach {
                    IhsanFilterChip(it, category == it, { category = it })
                }
            }
            Text("ترتيب النتائج", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IhsanFilterChip("الافتراضي", sort == "DEFAULT", { sort = "DEFAULT" })
                IhsanFilterChip("الأحدث", sort == "LATEST", { sort = "LATEST" })
            }
            Button(onClick = { onApply(location, category, sort) }, modifier = Modifier.fillMaxWidth()) { Text("تطبيق") }
            OutlinedButton(colors = com.example.designsystem.theme.ihsanOutlinedButtonColors(), onClick = { location = "الكل"; category = "الكل"; sort = "DEFAULT" },
                modifier = Modifier.fillMaxWidth()) { Text("إعادة التعيين") }
        }
    }
}
