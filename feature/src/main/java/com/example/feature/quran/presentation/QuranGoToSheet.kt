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
    var jumpMode by remember { mutableIntStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val pageStarts by produceState<List<Pair<Int, Int>>>(emptyList()) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val rows = org.json.JSONArray(context.assets.open("mushaf_page_starts.json").bufferedReader().use { it.readText() })
            (0 until rows.length()).map { rows.getJSONArray(it).let { row -> row.getInt(0) to row.getInt(1) } }
        }
    }

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

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("سورة / آية", "صفحة", "جزء").forEachIndexed { index, label ->
                    FilterChip(selected = jumpMode == index, onClick = { jumpMode = index; ayahNumber = "" }, label = { Text(label) })
                }
            }
            
            if (jumpMode == 0) {
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
            }

            val valid = when (jumpMode) {
                0 -> com.example.feature.quran.domain.model.QuranNavigation.validAyah(selectedSurah, ayahNumber)
                1 -> ayahNumber.toIntOrNull()?.let { it in 1..pageStarts.size } == true
                else -> ayahNumber.toIntOrNull()?.let { it in 1..30 } == true
            }

            OutlinedTextField(
                colors = if (com.example.designsystem.theme.IhsanTheme.isDark) OutlinedTextFieldDefaults.colors(
                    cursorColor = com.example.designsystem.theme.IhsanTheme.colors.accentContent,
                    focusedContainerColor = com.example.designsystem.theme.IhsanTheme.colors.fieldContainer,
                    unfocusedContainerColor = com.example.designsystem.theme.IhsanTheme.colors.fieldContainer,
                    focusedBorderColor = com.example.designsystem.theme.IhsanTheme.colors.fieldFocusedBorder,
                    unfocusedBorderColor = com.example.designsystem.theme.IhsanTheme.colors.fieldBorder,
                    focusedLabelColor = com.example.designsystem.theme.IhsanTheme.colors.textSecondary,
                    unfocusedLabelColor = com.example.designsystem.theme.IhsanTheme.colors.textSecondary,
                    focusedTextColor = com.example.designsystem.theme.IhsanTheme.colors.textBody,
                    unfocusedTextColor = com.example.designsystem.theme.IhsanTheme.colors.textBody
                ) else OutlinedTextFieldDefaults.colors(),
                value = ayahNumber,
                onValueChange = { if (it.all { char -> char.isDigit() }) ayahNumber = it },
                label = { Text(when (jumpMode) { 0 -> "رقم الآية (اختياري)"; 1 -> "رقم الصفحة (1–604)"; else -> "رقم الجزء (1–30)" }) },
                isError = ayahNumber.isNotBlank() && !valid,
                supportingText = { if (ayahNumber.isNotBlank() && !valid) Text("أدخل رقمًا ضمن النطاق الصحيح") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Button(
                onClick = {
                    when (jumpMode) {
                        0 -> selectedSurahId?.let { onNavigate(it, ayahNumber.toIntOrNull()) }
                        1 -> pageStarts.getOrNull((ayahNumber.toIntOrNull() ?: 0) - 1)?.let { onNavigate(it.first, it.second) }
                        else -> com.example.feature.quran.domain.model.QuranNavigation.juzStarts.getOrNull((ayahNumber.toIntOrNull() ?: 0) - 1)?.let { onNavigate(it.first, it.second) }
                    }
                },
                enabled = valid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("انتقال", fontWeight = FontWeight.Bold)
            }
        }
    }
}
