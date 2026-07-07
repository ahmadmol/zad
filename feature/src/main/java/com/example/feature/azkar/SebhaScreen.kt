package com.example.feature.azkar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.azkar.presentation.SebhaStats
import com.example.feature.azkar.presentation.SebhaUiEffect
import com.example.feature.azkar.presentation.SebhaViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SebhaScreen(
    viewModel: SebhaViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val sebhahs by viewModel.sebhahs.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Zikr?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SebhaUiEffect.ShowMessage -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("المسابح") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.Edit, contentDescription = null) }
            }, actions = {
                IconButton(onClick = { editTarget = null; showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            })
        }
    ) { inner ->
        Column(modifier = Modifier.padding(inner).padding(16.dp)) {
            SebhaStatsCard(stats = stats)
            Spacer(modifier = Modifier.height(12.dp))
            if (sebhahs.isEmpty()) {
                Text("لا توجد مسبحات. أضف مسبحة جديدة.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sebhahs, key = { it.id }) { s ->
                        SebhaItem(
                            zikr = s,
                            onEdit = { editTarget = s; showDialog = true },
                            onDelete = { viewModel.deleteSebha(s.id) },
                            onInc = { viewModel.increment(s.id) },
                            onReset = { viewModel.reset(s.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        SebhaEditDialog(
            initial = editTarget,
            onDismiss = { showDialog = false; editTarget = null },
            onSave = { title, text, target ->
                viewModel.saveSebha(title, text, target, editTarget)
                showDialog = false
                editTarget = null
            }
        )
    }
}

@Composable
private fun SebhaStatsCard(stats: SebhaStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("الإحصائيات", style = MaterialTheme.typography.titleMedium)
            Text("اليوم: ${stats.todayCount}")
            Text("هذا الأسبوع: ${stats.thisWeekCount}")
            Text("هذا الشهر: ${stats.thisMonthCount}")
            Text("الإجمالي: ${stats.totalCount}")
        }
    }
}

@Composable
private fun SebhaItem(zikr: Zikr, onEdit: () -> Unit, onDelete: () -> Unit, onInc: () -> Unit, onReset: () -> Unit) {
    var animated by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (animated) 1.05f else 1f, animationSpec = tween(180), label = "sebha-scale")
    val containerColor by animateColorAsState(targetValue = if (zikr.isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, label = "sebha-color")

    Card(modifier = Modifier.fillMaxWidth().scale(scale)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = zikr.title, style = MaterialTheme.typography.titleMedium)
            Text(text = zikr.text.ifBlank { "" }, style = MaterialTheme.typography.bodyMedium)
            Text(text = "الهدف: ${zikr.targetCount} • التقدم: ${zikr.currentCount}/${zikr.targetCount}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    animated = true
                    onInc()
                }) {
                    Text("تسبيح ${zikr.currentCount}/${zikr.targetCount}")
                }
                OutlinedButton(onClick = {
                    animated = false
                    onReset()
                }) { Text("إعادة تعيين") }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null) }
            }
            if (zikr.isCompleted) {
                Text("تم الوصول إلى الهدف", color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    LaunchedEffect(animated) {
        if (animated) {
            kotlinx.coroutines.delay(180)
            animated = false
        }
    }
}

@Composable
private fun SebhaEditDialog(initial: Zikr?, onDismiss: () -> Unit, onSave: (String, String, Int) -> Unit) {
    var title by remember { mutableStateOf(initial?.title ?: "مسبحة جديدة") }
    var text by remember { mutableStateOf(initial?.text ?: "") }
    var target by remember { mutableIntStateOf(initial?.targetCount ?: 33) }
    val presetTargets = listOf(33, 100, 1000)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val normalizedTarget = target.coerceAtLeast(1)
                if (title.trim().isBlank()) return@TextButton
                onSave(title.trim(), text.trim(), normalizedTarget)
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        },
        text = {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("الاسم") })
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("وصف/ذِكر") })
                Text("الأهداف الجاهزة", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetTargets.forEach { preset ->
                        OutlinedButton(onClick = { target = preset }) { Text(preset.toString()) }
                    }
                }
                OutlinedTextField(
                    value = target.toString(),
                    onValueChange = { newValue ->
                        val parsed = newValue.toIntOrNull()
                        target = when {
                            newValue.isBlank() -> 0
                            parsed != null && parsed > 0 -> parsed
                            else -> target
                        }
                    },
                    label = { Text("هدف مخصص") }
                )
                Text("الهدف يجب أن يكون رقمًا صحيحًا أكبر من صفر", style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}
