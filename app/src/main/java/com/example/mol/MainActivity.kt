package com.example.mol

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanCapabilityState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.settings.presentation.SettingsViewModel
import com.example.mol.ui.MainScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            IhsanTheme(darkTheme = settingsState.isDarkMode) {
                val locationPermissionState = rememberPermissionState(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (locationPermissionState.status.isGranted) {
                        MainScreen()
                    } else {
                        LocationPermissionScreen(
                            shouldShowRationale = locationPermissionState.status.shouldShowRationale,
                            onRequestPermission = {
                                locationPermissionState.launchPermissionRequest()
                            },
                            onOpenSettings = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationPermissionScreen(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    IhsanCapabilityState(
        title = stringResource(R.string.location_permission_title),
        body = if (shouldShowRationale) {
            stringResource(R.string.location_permission_rationale)
        } else {
            stringResource(R.string.location_permission_body)
        },
        icon = Icons.Default.LocationOff,
        primaryActionLabel = stringResource(R.string.location_permission_grant),
        onPrimaryAction = onRequestPermission,
        secondaryActionLabel = if (!shouldShowRationale) {
            stringResource(R.string.location_permission_open_settings)
        } else {
            null
        },
        onSecondaryAction = if (!shouldShowRationale) onOpenSettings else null,
        modifier = Modifier
            .fillMaxSize()
            .padding(IhsanTheme.spacing.extraLarge)
    )
}
