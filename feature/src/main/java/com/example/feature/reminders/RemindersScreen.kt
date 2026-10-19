package com.example.feature.reminders

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import java.util.Calendar
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private sealed interface RemindersUiState {
    data object Loading : RemindersUiState
    data class Content(val reminders: List<ReminderItem>) : RemindersUiState
    data object Error : RemindersUiState
}

private data class ReminderItem(
    val kind: AzkarReminderKind,
    val config: AzkarReminderConfig
)

@Composable
fun RemindersScreen(onBack: () -> Unit) {
    val coordinator: AzkarReminderCoordinator = koinInject()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updateFailed by remember { mutableStateOf(false) }
    var notificationsAllowed by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notificationsAllowed = it }
    // The state is assigned by both Flow terminal callbacks below.
    @Suppress("ProduceStateDoesNotAssignValue")
    val uiState by produceState<RemindersUiState>(RemindersUiState.Loading, coordinator) {
        combine(
            coordinator.config(AzkarReminderKind.MORNING),
            coordinator.config(AzkarReminderKind.EVENING)
        ) { morning, evening ->
            listOf(
                ReminderItem(AzkarReminderKind.MORNING, morning),
                ReminderItem(AzkarReminderKind.EVENING, evening)
            )
        }.catch {
            value = RemindersUiState.Error
        }.collect { reminders ->
            value = RemindersUiState.Content(reminders)
        }
    }

    val updateReminder: (AzkarReminderKind, Boolean, Int, Int) -> Unit = { kind, enabled, hour, minute ->
        scope.launch {
            runCatching { coordinator.update(kind, enabled, hour, minute) }
                .onSuccess { updateFailed = false }
                .onFailure { updateFailed = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IhsanTheme.colors.surfaceBase)
    ) {
        RemindersHero(onBack = onBack)
        Surface(
            modifier = Modifier
                .fillMaxSize()
                ,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            color = IhsanTheme.colors.surfaceBase
        ) {
            when (val state = uiState) {
                RemindersUiState.Loading -> IhsanLoadingState(
                    modifier = Modifier.fillMaxSize(),
                    message = stringResource(R.string.reminders_loading)
                )
                RemindersUiState.Error -> IhsanErrorState(
                    modifier = Modifier.fillMaxSize(),
                    title = stringResource(R.string.reminders_error_title),
                    message = stringResource(R.string.reminders_error_body)
                )
                is RemindersUiState.Content -> {
                    if (state.reminders.isEmpty()) {
                        IhsanEmptyState(
                            modifier = Modifier.fillMaxSize(),
                            title = stringResource(R.string.reminders_empty_title),
                            message = stringResource(R.string.reminders_empty_body),
                            icon = Icons.Outlined.NotificationsNone
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .navigationBarsPadding()
                                .imePadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!notificationsAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                NotificationPermissionCard {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            if (updateFailed) {
                                IhsanErrorState(
                                    title = stringResource(R.string.reminders_update_error_title),
                                    message = stringResource(R.string.reminders_update_error_body)
                                )
                            }
                            state.reminders.forEach { reminder ->
                                ReminderCard(reminder, updateReminder)
                            }
                            Text(
                                text = stringResource(R.string.reminders_approximate_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = IhsanTheme.colors.textSecondary,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RemindersHero(onBack: () -> Unit) {
    com.example.designsystem.component.IhsanTopBar(title = stringResource(R.string.reminders_redesign_title),
        style = com.example.designsystem.component.IhsanTopBarStyle.Hero, backgroundImageRes = com.example.feature.R.drawable.ihsan_home_hero_background, onNavigateBack = onBack)
}

@Composable
private fun ReminderCard(
    reminder: ReminderItem,
    onUpdate: (AzkarReminderKind, Boolean, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val config = reminder.config
    val isMorning = reminder.kind == AzkarReminderKind.MORNING
    val title = stringResource(
        if (isMorning) R.string.reminders_morning_title else R.string.reminders_evening_title
    )
    val enabledDescription = stringResource(
        if (config.enabled) R.string.reminders_enabled else R.string.reminders_disabled
    )
    val time = remember(config.hour, config.minute, context) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, config.hour)
            set(Calendar.MINUTE, config.minute)
        }
        com.example.feature.dashboard.presentation.ArabicClockFormatter.sanitize(
            DateFormat.getTimeFormat(context).format(calendar.time))
    }
    val timePicker = remember(config.hour, config.minute, config.enabled, context) {
        TimePickerDialog(
            context,
            { _, hour, minute -> onUpdate(reminder.kind, config.enabled, hour, minute) },
            config.hour,
            config.minute,
            DateFormat.is24HourFormat(context)
        )
    }
    val icon = if (isMorning) Icons.Default.WbSunny else Icons.Outlined.DarkMode

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = timePicker::show)
            .sizeIn(minHeight = 58.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceElevated),
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = IhsanTheme.colors.surfaceWarm
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = IhsanTheme.colors.accentWarm,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = IhsanTheme.colors.textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.reminders_schedule_format, time),
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary
                )
            }
            Switch(
                checked = config.enabled,
                onCheckedChange = { enabled ->
                    onUpdate(reminder.kind, enabled, config.hour, config.minute)
                },
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .semantics {
                        contentDescription = title
                        stateDescription = enabledDescription
                    },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = IhsanTheme.colors.accentContent,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedBorderColor = IhsanTheme.colors.selectedContainer,
                    uncheckedThumbColor = IhsanTheme.colors.textSecondary,
                    uncheckedTrackColor = IhsanTheme.colors.surfaceMuted,
                    uncheckedBorderColor = IhsanTheme.colors.borderSubtle
                )
            )
        }
    }
}

@Composable
private fun NotificationPermissionCard(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceWarm),
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = IhsanTheme.colors.accentWarm,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.reminders_permission_body),
                style = MaterialTheme.typography.bodySmall,
                color = IhsanTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onRequestPermission,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            , colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
                Text(
                    text = stringResource(R.string.reminders_permission_action),
                    color = IhsanTheme.colors.textPrimary
                )
            }
        }
    }
}
