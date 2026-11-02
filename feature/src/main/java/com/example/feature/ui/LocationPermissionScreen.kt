package com.example.feature.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.util.openExternalActivity
import com.example.feature.prayer.presentation.CitySelectionBottomSheet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Optional, contextual setup. A denied permission never blocks entry. */
@Composable
fun LocationPermissionScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val settings: SettingsManager = org.koin.compose.koinInject()
    val scope = rememberCoroutineScope()
    fun granted(permission: String) = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    fun locationGranted() = granted(Manifest.permission.ACCESS_COARSE_LOCATION) || granted(Manifest.permission.ACCESS_FINE_LOCATION)
    fun notificationGranted() = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled() &&
        (Build.VERSION.SDK_INT < 33 || granted(Manifest.permission.POST_NOTIFICATIONS))
    var location by remember { mutableStateOf(locationGranted()) }
    var notifications by remember { mutableStateOf(notificationGranted()) }
    var locationDenied by rememberSaveable { mutableStateOf(false) }
    var notificationDenied by rememberSaveable { mutableStateOf(false) }
    var city by rememberSaveable { mutableStateOf<String?>(null) }
    var citySheet by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<String?>(null) }
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        location = locationGranted(); locationDenied = !location
        if (location) scope.launch {
            try { settings.setUseAutoLocation(true) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { failure = "تعذر حفظ إعداد الموقع؛ يمكنك المحاولة من الإعدادات" }
        }
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notifications = notificationGranted(); notificationDenied = !notifications
    }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) {
            location = locationGranted(); notifications = notificationGranted()
        } }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    BackHandler { if (!busy) onContinue() }
    fun openSettings() { openExternalActivity(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null))) }
    Column(Modifier.fillMaxSize().background(IhsanTheme.colors.surfaceBase).windowInsetsPadding(WindowInsets.safeDrawing)
        .verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        IhsanTextWithKaabaIcon("إحسان", IhsanTheme.colors.accentContent, logoHeight = 44.dp)
        Text("يومك، على اختيارك", style = MaterialTheme.typography.headlineLarge)
        Text("جهّز ما يناسبك الآن، أو تابع واضبطه لاحقًا من الإعدادات.", style = MaterialTheme.typography.bodyLarge,
            color = IhsanTheme.colors.textSecondary)
        IhsanContentCard {
            IhsanIconContainer(Icons.Outlined.LocationOn)
            Text("مواقيت تناسب موقعك", style = MaterialTheme.typography.titleLarge)
            Text("نستخدم الموقع أثناء الاستخدام للصلاة والقبلة. يمكنك اختيار مدينة يدويًا دون منح الإذن.", style = MaterialTheme.typography.bodyMedium)
            if (city != null) Text("المدينة المختارة: $city", color = IhsanTheme.colors.accentContent)
            if (locationDenied) Text("لم يُمنح الإذن. اختر مدينة، أو فعّل الموقع من إعدادات النظام.", style = MaterialTheme.typography.bodySmall)
            IhsanPrimaryButton(onClick = {
                if (locationDenied) openSettings() else locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
            }, enabled = !location && !busy, modifier = Modifier.fillMaxWidth()) {
                Text(if (location) "إذن الموقع مفعّل" else if (locationDenied) "إعدادات الموقع" else "تفعيل الموقع")
            }
            IhsanSecondaryButton({ citySheet = true }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("اختيار المدينة يدويًا") }
        }
        IhsanContentCard {
            IhsanIconContainer(Icons.Outlined.Notifications)
            Text("تذكير في وقته", style = MaterialTheme.typography.titleLarge)
            Text("اسمح بإشعارات الصلاة والتذكيرات التي تختارها. منح الإذن لا يسجّل أداء أي عبادة.", style = MaterialTheme.typography.bodyMedium)
            if (notificationDenied) Text("يمكنك المتابعة دون إشعارات وإعادة تفعيلها من إعدادات النظام.", style = MaterialTheme.typography.bodySmall)
            IhsanSecondaryButton({
                if (notificationDenied || Build.VERSION.SDK_INT < 33) openSettings()
                else notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }, enabled = !notifications, modifier = Modifier.fillMaxWidth()) {
                Text(if (notifications) "إذن الإشعارات متاح" else if (notificationDenied) "إعدادات الإشعارات" else "السماح بالإشعارات")
            }
        }
        failure?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        IhsanPrimaryButton(onContinue, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("الدخول إلى إحسان") }
        Text("يمكن تغيير هذه الخيارات في أي وقت", style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary)
    }
    if (citySheet) CitySelectionBottomSheet(onDismiss = { citySheet = false }, onCitySelected = { selected ->
        if (!busy) scope.launch {
            busy = true
            try { settings.setManualLocation(selected.name, selected.lat, selected.lng); city = selected.name; failure = null }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { failure = "تعذر حفظ المدينة؛ أعد المحاولة" }
            finally { busy = false }
        }
    })
}
