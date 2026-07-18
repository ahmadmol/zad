package com.example.feature.settings

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.settings.presentation.SettingsAction
import com.example.feature.settings.presentation.SettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val backCd = stringResource(R.string.cd_back)

    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(IhsanTheme.dimens.minTouchTarget)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backCd)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(IhsanTheme.dimens.screenHorizontal)
        ) {
            Text(stringResource(R.string.font_size_label), style = MaterialTheme.typography.titleMedium)
            Slider(
                value = uiState.fontSize,
                onValueChange = { onAction(SettingsAction.SetFontSize(it)) },
                valueRange = 16f..42f,
                modifier = Modifier.semantics {
                    contentDescription = "Font size"
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = IhsanTheme.spacing.medium))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.heightIn(min = IhsanTheme.dimens.minTouchTarget)
            ) {
                Text(
                    stringResource(R.string.dark_mode_label),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = uiState.isDarkMode,
                    onCheckedChange = { onAction(SettingsAction.SetDarkMode(it)) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = IhsanTheme.spacing.medium))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.heightIn(min = IhsanTheme.dimens.minTouchTarget)
            ) {
                Text(
                    stringResource(R.string.vibration_label),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = uiState.isVibrationEnabled,
                    onCheckedChange = { onAction(SettingsAction.SetVibration(it)) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = IhsanTheme.spacing.medium))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = IhsanTheme.dimens.minTouchTarget)
                    .clickable {
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
                    .padding(vertical = IhsanTheme.spacing.small)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.cd_notifications),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(IhsanTheme.spacing.medium))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.settings_adhan_sound_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        val soundName = uiState.adhanSoundUri?.let {
                            RingtoneManager.getRingtone(context, Uri.parse(it))?.getTitle(context)
                        } ?: stringResource(R.string.settings_adhan_sound_default)
                        Text(
                            soundName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = IhsanTheme.spacing.medium))

            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.settings_share_message))
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IhsanTheme.dimens.controlHeight),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.cd_share))
                Spacer(modifier = Modifier.width(IhsanTheme.spacing.small))
                Text(stringResource(R.string.settings_share_app))
            }
        }
    }
}
