package com.example.feature.quran.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.feature.quran.domain.model.Surah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranGoToSheet(
    surahs: List<Surah>,
    onDismiss: () -> Unit,
    onNavigate: (Int, Int?) -> Unit
) {
    var selectedSurahId by remember { mutableStateOf<Int?>(null) }
    var ayahNumber by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "انتقال سريع",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            var expanded by remember { mutableStateOf(false) }
            val selectedSurah = surahs.find { it.id == selectedSurahId }
            
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        selectedSurah?.name ?: "اختر السورة",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 300.dp)
                ) {
                    surahs.forEach { surah ->
                        DropdownMenuItem(
                            text = { Text(surah.name) },
                            onClick = {
                                selectedSurahId = surah.id
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = ayahNumber,
                onValueChange = { if (it.all { char -> char.isDigit() }) ayahNumber = it },
                label = { Text("رقم الآية (اختياري)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Button(
                onClick = {
                    selectedSurahId?.let { id ->
                        onNavigate(id, ayahNumber.toIntOrNull())
                    }
                },
                enabled = selectedSurahId != null,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("انتقال", fontWeight = FontWeight.Bold)
            }
        }
    }
}
