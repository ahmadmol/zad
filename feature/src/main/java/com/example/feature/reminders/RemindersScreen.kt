package com.example.feature.reminders

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.work.*
import com.example.feature.R
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reminders_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") 
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.reminders_subtitle), style = MaterialTheme.typography.titleLarge)
            
            ReminderItemWithPicker(
                title = stringResource(R.string.category_morning),
                defaultHour = 7,
                defaultMinute = 0,
                onSchedule = { hour, minute ->
                    scheduleReminder(workManager, "أذكار الصباح", context.getString(R.string.morning_reminder_msg), hour, minute)
                }
            )
            
            ReminderItemWithPicker(
                title = stringResource(R.string.category_evening),
                defaultHour = 17,
                defaultMinute = 0,
                onSchedule = { hour, minute ->
                    scheduleReminder(workManager, "أذكار المساء", context.getString(R.string.evening_reminder_msg), hour, minute)
                }
            )
        }
    }
}

@Composable
fun ReminderItemWithPicker(
    title: String,
    defaultHour: Int,
    defaultMinute: Int,
    onSchedule: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(defaultHour) }
    var selectedMinute by remember { mutableStateOf(defaultMinute) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            selectedHour = hour
            selectedMinute = minute
            if (isEnabled) onSchedule(hour, minute)
        },
        selectedHour,
        selectedMinute,
        false
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { timePickerDialog.show() }, contentPadding = PaddingValues(0.dp)) {
                Text(
                    text = String.format("%02d:%02d", selectedHour, selectedMinute),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = { 
                isEnabled = it
                if (it) onSchedule(selectedHour, selectedMinute)
                else WorkManager.getInstance(context).cancelUniqueWork(title)
            }
        )
    }
}

fun scheduleReminder(workManager: WorkManager, title: String, message: String, hour: Int, minute: Int) {
    val data = workDataOf("title" to title, "message" to message)
    
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        if (before(Calendar.getInstance())) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    val delay = calendar.timeInMillis - System.currentTimeMillis()

    val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
        .build()

    workManager.enqueueUniquePeriodicWork(
        title,
        ExistingPeriodicWorkPolicy.UPDATE,
        reminderRequest
    )
}
