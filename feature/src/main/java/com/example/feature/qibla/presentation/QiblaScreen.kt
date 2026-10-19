package com.example.feature.qibla.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.QiblaCompass
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import org.koin.androidx.compose.koinViewModel

private fun Number.toArabicDigits(): String {
    val western = this.toString()
    val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return western.map { ch ->
        if (ch in '0'..'9') arabicChars[ch - '0'] else ch
    }.joinToString("")
}

@Composable
fun QiblaScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: QiblaViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner, viewModel) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> viewModel.setCompassActive(true)
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> viewModel.setCompassActive(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        viewModel.setCompassActive(lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED))
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer); viewModel.setCompassActive(false) }
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) viewModel.updateLocationAndCalculateQibla()
    }

    LaunchedEffect(uiState.isAligned) {
        if (uiState.isAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.updateLocationAndCalculateQibla()
    }

    val isDark = IhsanTheme.isDark
    val darkTealColor = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF003B46)
    val darkTealSubtext = if (isDark) IhsanTheme.colors.textSecondary else Color(0xFF1B535D)
    val cardBg = if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFEDF6F7)
    val cardBorder = if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFD4EBE9)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Hero Mosque Header with Title "القبلة" & Pills
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ihsan_home_hero_background),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                com.example.designsystem.component.IhsanHeroScrim(Modifier.matchParentSize())

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar (Back button on start, "القبلة" Title centered)
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(IhsanTheme.colors.surfaceElevated.copy(alpha = .9f))
                                .clickable(onClick = onNavigateBack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                                tint = darkTealColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = "القبلة",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkTealColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Location Pill
                    Surface(
                        shape = CircleShape,
                        color = IhsanTheme.colors.surfaceElevated.copy(alpha = .92f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                val coarseAllowed = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                val fineAllowed = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                if (coarseAllowed || fineAllowed) {
                                    viewModel.updateLocationAndCalculateQibla()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        )
                                    )
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ihsan_icon_location),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.locationName.ifBlank { stringResource(R.string.default_city) },
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkTealColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = darkTealSubtext,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Refresh Status Pill
                    Surface(
                        shape = CircleShape,
                        color = IhsanTheme.colors.surfaceElevated.copy(alpha = .85f),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.error != null) MaterialTheme.colorScheme.error else IhsanTheme.colors.success)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isLoading) "جاري التحديث..."
                                else if (uiState.error != null) "خطأ في التحديث"
                                else uiState.cityAndCountry.ifBlank { "تم التحديث الآن" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = darkTealSubtext
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Large Compass Dial
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(.86f).aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = IhsanTheme.colors.selectedContent)
                    }
                } else if (uiState.error != null && !uiState.hasCalculatedBearing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(.86f).aspectRatio(1f)
                            .clip(CircleShape)
                            .background(IhsanTheme.colors.surfaceElevated)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error ?: "خطأ في تحديد الموقع",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            IconButton(onClick = { viewModel.updateLocationAndCalculateQibla() }) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "إعادة المحاولة",
                                    tint = IhsanTheme.colors.selectedContent
                                )
                            }
                        }
                    }
                } else {
                    val animatedBearing by animateFloatAsState(
                        targetValue = uiState.compassRotation,
                        animationSpec = tween(durationMillis = 200)
                    )
                    QiblaCompass(
                        bearing = animatedBearing,
                        qiblaDirection = uiState.qiblaAngle,
                        isAligned = uiState.isAligned,
                        modifier = Modifier.fillMaxWidth(.86f).aspectRatio(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Qibla Degree & Bearing Display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "اتجاه القبلة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = darkTealSubtext
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (uiState.error != null && !uiState.hasCalculatedBearing) "—" else "°${uiState.qiblaAngle.toInt().toArabicDigits()}",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = darkTealColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Bottom Cards Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Cards Row (Saved Location Card + Sensor Accuracy Card)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Location Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(darkTealColor.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ihsan_icon_location),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = uiState.locationName.ifBlank { stringResource(R.string.default_city) },
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = darkTealColor
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = uiState.cityAndCountry.ifBlank { "آخر موقع محفوظ" },
                                    fontSize = 10.sp,
                                    color = darkTealSubtext
                                )
                            }
                        }
                    }

                    // Sensor Accuracy Card
                    val accuracyText = when {
                        !uiState.hasCompassSensors -> "لا يوجد مستشعر بوصلة — زاوية محسوبة فقط"
                        uiState.calibrationMessage != null -> "تحتاج معايرة"
                        uiState.sensorAccuracy != null -> "الدقة جيدة"
                        else -> "الدقة غير متاحة"
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(IhsanTheme.colors.success.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = IhsanTheme.colors.success,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = accuracyText,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IhsanTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "حالة البوصلة",
                                    fontSize = 10.sp,
                                    color = darkTealSubtext
                                )
                            }
                        }
                    }
                }

                // Calibration Guidance Banner Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ihsan_qibla_calibration),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "حرّك الهاتف بلطف",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkTealColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.calibrationMessage ?: "لتحسين دقة البوصلة",
                                fontSize = 11.5.sp,
                                color = darkTealSubtext
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = darkTealSubtext.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
