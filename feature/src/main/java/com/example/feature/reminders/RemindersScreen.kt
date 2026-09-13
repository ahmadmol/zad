package com.example.feature.reminders

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import java.util.Locale
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onBack: () -> Unit) {
    val coordinator: AzkarReminderCoordinator = koinInject()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notificationsAllowed by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notificationsAllowed = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تذكيرات الأذكار") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("تذكيرات يومية تقريبية", style = MaterialTheme.typography.titleLarge)
            Text(
                "يحفظ التطبيق الوقت ويطلب من النظام تشغيل التذكير قريبًا منه. قد يؤخر Android التنفيذ لتحسين البطارية.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!notificationsAllowed && Build.VERSION.SDK_INT >= 33) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("الإشعارات غير مسموحة؛ سيبقى الإعداد محفوظًا لكن لن يظهر التنبيه.")
                        TextButton(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                            Text("السماح بالإشعارات")
                        }
                    }
                }
            }
            ReminderRow("أذكار الصباح", AzkarReminderKind.MORNING, coordinator) { kind, enabled, hour, minute ->
                scope.launch { coordinator.update(kind, enabled, hour, minute) }
            }
            ReminderRow("أذكار المساء", AzkarReminderKind.EVENING, coordinator) { kind, enabled, hour, minute ->
                scope.launch { coordinator.update(kind, enabled, hour, minute) }
            }
        }
    }
}

@Composable
private fun ReminderRow(
    title: String,
    kind: AzkarReminderKind,
    coordinator: AzkarReminderCoordinator,
    onUpdate: (AzkarReminderKind, Boolean, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val config by coordinator.config(kind).collectAsStateWithLifecycle(
        initialValue = AzkarReminderConfig(false, if (kind == AzkarReminderKind.MORNING) 7 else 17, 0, null)
    )
    val registration by coordinator.registration(kind).collectAsStateWithLifecycle(
        initialValue = ReminderRegistration(AzkarReminderCoordinator.canonicalName(kind))
    )
    val picker = remember(config.hour, config.minute, config.enabled) {
        TimePickerDialog(context, { _, hour, minute ->
            onUpdate(kind, config.enabled, hour, minute)
        }, config.hour, config.minute, false)
    }
    val registrationText = when {
        !config.enabled -> "الإعداد متوقف"
        registration.state == WorkInfo.State.ENQUEUED -> "الإعداد محفوظ والتذكير مسجل لدى النظام"
        registration.state == WorkInfo.State.RUNNING -> "يجري تنفيذ التذكير الآن"
        registration.state == WorkInfo.State.BLOCKED -> "الإعداد محفوظ وينتظر النظام"
        registration.state == WorkInfo.State.FAILED -> "فشل التسجيل؛ غيّر الإعداد لإعادة المحاولة"
        registration.workId == null -> "الإعداد محفوظ وجارٍ تسجيله"
        else -> "الإعداد محفوظ؛ حالة التسجيل ${registration.state}"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = picker::show, contentPadding = PaddingValues(0.dp)) {
                        Text(String.format(Locale.ROOT, "%02d:%02d", config.hour, config.minute))
                    }
                }
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { onUpdate(kind, it, config.hour, config.minute) }
                )
            }
            Text(registrationText, style = MaterialTheme.typography.bodySmall)
            config.lastRunAt?.let {
                Text(
                    "آخر تنفيذ معروف: ${DateUtils.getRelativeTimeSpanString(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
