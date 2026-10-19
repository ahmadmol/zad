package com.example.feature.settings

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.component.IhsanTopBar
import com.example.designsystem.component.IhsanTopBarStyle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.azkar.data.local.SettingsManager.ThemeMode
import com.example.feature.prayer.presentation.CitySelectionBottomSheet
import com.example.feature.settings.presentation.SettingsAction
import com.example.feature.settings.presentation.SettingsUiState

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val backCd = stringResource(R.string.cd_back)
    val isDark = IhsanTheme.isDark

    var showFontSizeSlider by remember { mutableStateOf(false) }
    var showCitySheet by remember { mutableStateOf(false) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        onAction(SettingsAction.SetMorningReminder(granted))
        if (!granted) com.example.feature.core.notification.UserMessageNotifier.notify(context, "لم يُمنح إذن الإشعارات؛ التذكيرات غير مفعلة")
    }

    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            uri?.let {
                onAction(SettingsAction.SetAdhanSound(it.toString()))
            }
        }
    }

    val darkTealColor = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF003B46)
    val darkTealSubtext = if (isDark) IhsanTheme.colors.textSecondary else Color(0xFF1B535D)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IhsanTheme.colors.surfaceBase)
    ) {
        // 1. Hero Header
        IhsanTopBar(
            title = stringResource(R.string.settings_title),
            subtitle = stringResource(R.string.settings_subtitle),
            style = IhsanTopBarStyle.Hero, backgroundImageRes = com.example.feature.R.drawable.ihsan_home_hero_background,
            onNavigateBack = onBack
        )

        // 2. Main Scrollable Content Overlay
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = IhsanTheme.colors.surfaceBase,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IhsanTheme.colors.selectedContent)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- SECTION 1: المظهر ---
                    SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                        CardGroup {
                            ThemeModeRow(
                                selected = uiState.themeMode,
                                onSelected = { onAction(SettingsAction.SetThemeMode(it)) }
                            )

                            HorizontalDivider(color = IhsanTheme.colors.divider)

                            // Font Size Row
                            SettingsClickableRow(
                                icon = Icons.Default.TextFields,
                                title = stringResource(R.string.font_size_scope_label),
                                subtitle = "${uiState.fontSize.toInt()} نقطة",
                                onClick = { showFontSizeSlider = !showFontSizeSlider }
                            )

                            AnimatedVisibility(visible = showFontSizeSlider) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(IhsanTheme.colors.surfaceMuted)
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "صغير (16)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = IhsanTheme.colors.textSecondary
                                        )
                                        Text(
                                            text = "${uiState.fontSize.toInt()} نقطة",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = IhsanTheme.colors.textPrimary
                                        )
                                        Text(
                                            text = "كبير (42)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = IhsanTheme.colors.textSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Slider(
                                        value = uiState.fontSize,
                                        onValueChange = { onAction(SettingsAction.SetFontSize(it)) },
                                        valueRange = 16f..42f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = IhsanTheme.colors.selectedContent,
                                            activeTrackColor = IhsanTheme.colors.selectedContainer
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics {
                                                contentDescription = "حجم الخط ${uiState.fontSize.toInt()}"
                                            }
                                    )
                                }
                            }
                        }
                    }

                    // --- SECTION 2: الإشعارات ---
                    SettingsSection(title = stringResource(R.string.settings_section_notifications)) {
                        CardGroup {
                            // Adhan Sound Picker Row
                            val soundName = uiState.adhanSoundUri?.let { uri ->
                                RingtoneManager.getRingtone(context, Uri.parse(uri))?.getTitle(context)
                            } ?: stringResource(R.string.settings_adhan_sound_default)

                            SettingsClickableRow(
                                icon = Icons.Default.Notifications,
                                title = stringResource(R.string.settings_adhan_sound_title),
                                subtitle = soundName,
                                onClick = {
                                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                        putExtra(
                                            RingtoneManager.EXTRA_RINGTONE_TITLE,
                                            context.getString(R.string.settings_adhan_sound_picker_title)
                                        )
                                        putExtra(
                                            RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                            uiState.adhanSoundUri?.let { Uri.parse(it) }
                                        )
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                    }
                                    soundPickerLauncher.launch(intent)
                                }
                            )

                            HorizontalDivider(color = IhsanTheme.colors.divider)

                            // Vibration Toggle Row
                            SettingsSwitchRow(
                                icon = Icons.Default.Vibration,
                                title = stringResource(R.string.vibration_label),
                                subtitle = if (uiState.isVibrationEnabled) "مفعل" else "معطل",
                                checked = uiState.isVibrationEnabled,
                                onCheckedChange = { onAction(SettingsAction.SetVibration(it)) }
                            )

                            HorizontalDivider(color = IhsanTheme.colors.divider)

                            // Azkar Reminders Toggle Row
                            SettingsSwitchRow(
                                icon = Icons.Default.AccessTime,
                                title = stringResource(R.string.settings_reminders_title),
                                subtitle = stringResource(R.string.settings_reminders_subtitle),
                                checked = uiState.morningReminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= 33 &&
                                        androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                        notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    } else onAction(SettingsAction.SetMorningReminder(enabled))
                                }
                            )
                        }
                    }

                    // --- SECTION 3: عام ---
                    SettingsSection(title = stringResource(R.string.settings_section_general)) {
                        CardGroup {
                            // Location Row
                            SettingsClickableRow(
                                icon = Icons.Default.Place,
                                title = stringResource(R.string.settings_location_title),
                                subtitle = uiState.locationCity.ifBlank { stringResource(R.string.default_city) },
                                onClick = { showCitySheet = true }
                            )

                            HorizontalDivider(color = IhsanTheme.colors.divider)

                            // Share App Row
                            SettingsClickableRow(
                                icon = Icons.Default.Share,
                                title = stringResource(R.string.settings_share_app_title),
                                subtitle = stringResource(R.string.settings_share_app),
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.settings_share_message))
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, null))
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // City Selection BottomSheet
    if (showCitySheet) {
        CitySelectionBottomSheet(
            onDismiss = { showCitySheet = false },
            onCitySelected = { city ->
                onAction(SettingsAction.SetManualLocation(city.name, city.lat, city.lng))
                showCitySheet = false
            }
        )
    }

}

@Composable
private fun ThemeModeRow(selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.DarkMode, null, tint = if (IhsanTheme.isDark) IhsanTheme.colors.textSecondary else IhsanTheme.colors.accentContent, modifier = Modifier.size(22.dp))
        Text("الوضع الداكن", Modifier.weight(1f).padding(horizontal = 10.dp), style = MaterialTheme.typography.bodyMedium,
            color = IhsanTheme.colors.textPrimary)
        IconButton(onClick = { onSelected(ThemeMode.SYSTEM) }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.SettingsBrightness, "اتباع مظهر النظام", tint = IhsanTheme.colors.textSecondary, modifier = Modifier.size(18.dp))
        }
        Switch(checked = if (selected == ThemeMode.SYSTEM) IhsanTheme.isDark else selected == ThemeMode.DARK,
            onCheckedChange = { onSelected(if (it) ThemeMode.DARK else ThemeMode.LIGHT) },
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = IhsanTheme.colors.textPrimary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        content()
    }
}

@Composable
private fun CardGroup(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = IhsanTheme.colors.surfaceElevated,
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tonal Icon Halo
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(IhsanTheme.colors.surfaceMuted),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (IhsanTheme.isDark) IhsanTheme.colors.textSecondary else IhsanTheme.colors.accentContent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = IhsanTheme.colors.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = IhsanTheme.colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tonal Icon Halo
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(IhsanTheme.colors.surfaceMuted),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (IhsanTheme.isDark) IhsanTheme.colors.textSecondary else IhsanTheme.colors.accentContent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = IhsanTheme.colors.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = IhsanTheme.colors.textSecondary,
                uncheckedTrackColor = IhsanTheme.colors.surfaceMuted,
                disabledCheckedThumbColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
                disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                disabledUncheckedThumbColor = IhsanTheme.colors.textSecondary.copy(alpha = 0.38f),
                disabledUncheckedTrackColor = IhsanTheme.colors.surfaceMuted.copy(alpha = 0.12f)
            )
        )
    }
}
