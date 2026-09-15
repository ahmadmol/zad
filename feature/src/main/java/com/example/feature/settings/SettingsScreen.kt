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
        // 1. Hero Header (Nusuk / Home skyline family)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ihsan_home_hero_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.matchParentSize()
            )

            if (isDark) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Navigation Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = backCd
                            }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backCd,
                            tint = darkTealColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(R.string.settings_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkTealColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hero Subtitle Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ihsan_brand_mark),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.settings_subtitle),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = darkTealSubtext
                        )
                    }
                }
            }
        }

        // 2. Main Scrollable Content Overlay
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-16).dp),
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
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
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
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
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
                                onCheckedChange = { onAction(SettingsAction.SetMorningReminder(it)) }
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

                    Spacer(modifier = Modifier.height(32.dp))
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
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DarkMode, contentDescription = null, tint = IhsanTheme.colors.selectedContent)
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.theme_mode_label),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = IhsanTheme.colors.textPrimary
            )
        }
        Spacer(Modifier.height(12.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val choices = listOf(
                ThemeMode.SYSTEM to R.string.theme_system,
                ThemeMode.LIGHT to R.string.theme_light,
                ThemeMode.DARK to R.string.theme_dark
            )
            choices.forEachIndexed { index, (mode, label) ->
                SegmentedButton(
                    selected = selected == mode,
                    onClick = { onSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index, choices.size),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text(stringResource(label)) }
            }
        }
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
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tonal Icon Halo
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(IhsanTheme.colors.surfaceMuted),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IhsanTheme.colors.selectedContent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = IhsanTheme.colors.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
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
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tonal Icon Halo
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(IhsanTheme.colors.surfaceMuted),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IhsanTheme.colors.selectedContent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = IhsanTheme.colors.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IhsanTheme.colors.textSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = IhsanTheme.colors.selectedContainer,
                uncheckedThumbColor = IhsanTheme.colors.textSecondary,
                uncheckedTrackColor = IhsanTheme.colors.surfaceMuted
            )
        )
    }
}
