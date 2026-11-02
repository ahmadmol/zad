package com.example.feature.sanhya.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.sanhya.data.datasource.SanhyaFixtureData
import com.example.feature.sanhya.domain.model.StoryFilterState
import com.example.feature.sanhya.domain.model.StorySortOption
import com.example.feature.sanhya.presentation.components.StoryTagChip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SanhyaFilterSheet(
    filterState: StoryFilterState,
    onDismiss: () -> Unit,
    onApply: (StoryFilterState) -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    var selectedCategory by rememberSaveable { mutableStateOf(filterState.selectedCategory) }
    var selectedEpisodeFilter by rememberSaveable { mutableStateOf(filterState.selectedEpisodeFilter) }
    var selectedDurationFilter by rememberSaveable { mutableStateOf(filterState.selectedDurationFilter) }
    var sortOption by rememberSaveable { mutableStateOf(filterState.sortOption) }

    com.example.designsystem.component.IhsanFilterSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = IhsanTheme.colors.textSecondary
                    )
                }

                Text(
                    text = "تصفية القصص",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = IhsanTheme.colors.accentContent
                )

                Spacer(modifier = Modifier.width(32.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories (التصنيفات)
            Text(
                text = "التصنيفات",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = IhsanTheme.colors.accentContent
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SanhyaFixtureData.categories.forEach { category ->
                    StoryTagChip(
                        text = category,
                        isSelected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Episode Count Filter (عدد الحلقات)
            Text(
                text = "عدد الحلقات",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = IhsanTheme.colors.accentContent
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("الكل", "أكثر من حلقة", "أكثر من 5 حلقات").forEach { epOption ->
                    StoryTagChip(
                        text = epOption,
                        isSelected = selectedEpisodeFilter == epOption,
                        onClick = { selectedEpisodeFilter = epOption }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sort Order (ترتيب العرض)
            Text("مدة الحلقة — تظهر القصة إذا طابقت إحدى حلقاتها", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                com.example.feature.learning.data.LearningDuration.filters.forEach { duration ->
                    StoryTagChip(duration, selectedDurationFilter == duration, { selectedDurationFilter = duration })
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "ترتيب العرض",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = IhsanTheme.colors.accentContent
            )
            Spacer(modifier = Modifier.height(8.dp))
            StorySortOption.entries.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = sortOption == option,
                        onClick = { sortOption = option },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option.label,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons (إعادة التعيين & تطبيق)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedCategory = "الكل"
                        selectedEpisodeFilter = "الكل"
                        selectedDurationFilter = "الكل"
                        sortOption = StorySortOption.NEWEST_FIRST
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(23.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(IhsanTheme.colors.borderSubtle)
                    )
                , colors = com.example.designsystem.theme.ihsanOutlinedButtonColors()) {
                    Text(
                        text = "إعادة التعيين",
                        color = IhsanTheme.colors.accentContent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        onApply(
                            StoryFilterState(
                                selectedCategory = selectedCategory,
                                selectedEpisodeFilter = selectedEpisodeFilter,
                                selectedDurationFilter = selectedDurationFilter,
                                sortOption = sortOption
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(23.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "تطبيق",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
