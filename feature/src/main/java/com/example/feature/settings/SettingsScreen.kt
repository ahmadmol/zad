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
import androidx.compose.ui.unit.dp
import com.example.feature.R
import com.example.feature.azkar.presentation.AzkarAction
import com.example.feature.azkar.presentation.AzkarUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: AzkarUiState,
    onAction: (AzkarAction) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            uri?.let {
                onAction(AzkarAction.OnAdhanSoundChanged(it.toString()))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") 
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text(stringResource(R.string.font_size_label), style = MaterialTheme.typography.titleMedium)
            Slider(
                value = state.fontSize,
                onValueChange = { onAction(AzkarAction.OnFontSizeChanged(it)) },
                valueRange = 16f..42f
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.dark_mode_label), modifier = Modifier.weight(1f))
                Switch(
                    checked = state.isDarkMode,
                    onCheckedChange = { onAction(AzkarAction.OnDarkModeToggle(it)) }
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.vibration_label), modifier = Modifier.weight(1f))
                Switch(
                    checked = state.isVibrationEnabled,
                    onCheckedChange = { onAction(AzkarAction.OnVibrationToggle(it)) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Adhan Sound Selection
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "اختر صوت الأذان")
                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, state.adhanSoundUri?.let { Uri.parse(it) })
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        }
                        soundPickerLauncher.launch(intent)
                    }
                    .padding(vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("صوت الأذان", style = MaterialTheme.typography.titleMedium)
                        val soundName = state.adhanSoundUri?.let {
                            RingtoneManager.getRingtone(context, Uri.parse(it))?.getTitle(context)
                        } ?: "الافتراضي"
                        Text(soundName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Share App Button
            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "جرب تطبيق إحسان الرائع للتكافل والعبادات: [رابط التطبيق هنا]")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("مشاركة التطبيق مع الأصدقاء")
            }
        }
    }
}
