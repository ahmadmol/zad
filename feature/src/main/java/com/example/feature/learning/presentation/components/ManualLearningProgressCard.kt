package com.example.feature.learning.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.learning.data.LearningEpisodeProgress

@Composable
fun ManualLearningProgressCard(id: String, progress: LearningEpisodeProgress?, busy: Boolean,
    error: String?, onSave: (Int, Boolean) -> Unit) {
    var seconds by rememberSaveable(id, progress?.seconds) { mutableStateOf((progress?.seconds ?: 0).toString()) }
    var confirmComplete by rememberSaveable(id) { mutableStateOf(false) }
    val value = seconds.toIntOrNull()?.takeIf { it in 0..86400 }
    Card(colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceElevated)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("متابعة التعلم المحلية", style = MaterialTheme.typography.titleMedium, color = IhsanTheme.colors.accentContent)
            Text(if (progress?.completed == true) "أكدت إكمال هذه الحلقة" else "لم تؤكد إكمال هذه الحلقة", color = IhsanTheme.colors.textBody)
            Text("احفظ موضع الاستئناف يدويًا بالثواني. فتح الحلقة أو تشغيلها لا يسجل إكمالًا.", color = IhsanTheme.colors.textSecondary)
            OutlinedTextField(value = seconds, onValueChange = { seconds = it.take(5) },
                modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("موضع الاستئناف بالثواني (0–86400)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = value == null, enabled = !busy)
            progress?.let { Text("آخر حفظ محلي: ${java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(java.util.Date(it.updatedAt))}", color = IhsanTheme.colors.textSecondary) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { value?.let { onSave(it, progress?.completed == true) } }, enabled = !busy && value != null,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("حفظ موضع الاستئناف") }
            OutlinedButton(onClick = { confirmComplete = true }, enabled = !busy && value != null,
                colors = com.example.designsystem.theme.ihsanOutlinedButtonColors(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(if (progress?.completed == true) "التراجع عن الإكمال" else "تأكيد إكمال الحلقة") }
        }
    }
    if (confirmComplete) AlertDialog(onDismissRequest = { if (!busy) confirmComplete = false },
        title = { Text(if (progress?.completed == true) "إلغاء تأكيد الإكمال؟" else "هل أكملت هذه الحلقة؟") },
        text = { Text("يُحفظ هذا الاختيار محليًا بإدخالك فقط. يبقى موضع الاستئناف محفوظًا.") },
        confirmButton = { TextButton(onClick = { value?.let { onSave(it, progress?.completed != true) }; confirmComplete = false }, enabled = !busy) { Text("تأكيد") } },
        dismissButton = { TextButton(onClick = { confirmComplete = false }) { Text("إلغاء") } })
}
