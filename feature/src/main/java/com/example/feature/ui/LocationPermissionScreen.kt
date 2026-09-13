package com.example.feature.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import com.example.designsystem.component.IhsanCapabilityState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

@Composable
fun LocationPermissionScreen(
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    var requestCompletedWithoutGrant by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            onContinue()
        } else {
            requestCompletedWithoutGrant = true
        }
    }

    val activity = context.findActivity()
    val shouldShowRationale = activity != null && (
        ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    // `shouldShowRequestPermissionRationale == false` is meaningful here only after
    // this screen has actually completed a denied request in the current saved state.
    val shouldOpenSettings = requestCompletedWithoutGrant && !shouldShowRationale

    IhsanCapabilityState(
        title = stringResource(R.string.location_capability_title),
        body = stringResource(R.string.location_capability_body),
        icon = Icons.Outlined.LocationOn,
        primaryActionLabel = stringResource(
            if (shouldOpenSettings) {
                R.string.location_capability_open_settings
            } else {
                R.string.location_capability_enable
            }
        ),
        onPrimaryAction = {
            if (shouldOpenSettings) {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                )
            } else {
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        },
        secondaryActionLabel = stringResource(R.string.location_capability_continue),
        onSecondaryAction = onContinue,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
