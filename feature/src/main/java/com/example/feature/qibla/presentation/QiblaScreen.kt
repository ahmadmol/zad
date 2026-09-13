package com.example.feature.qibla.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.QiblaCompass
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

    val darkTealColor = Color(0xFF003B46)
    val darkTealSubtext = Color(0xFF1B535D)

    // Opaque full screen surface container - no HOME visible underneath
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top Header Celestial Background Image
            Image(
                painter = painterResource(id = R.drawable.ihsan_home_hero_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )

            // Screen Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Top Header Row: Back button (RTL) & Title
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        // Back Button
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.65f))
                                .clickable(onClick = onNavigateBack)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "رجوع"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = darkTealColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Screen Title
                        Text(
                            text = "القبلة",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkTealColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Top Location & Status Pills Row
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Location Pill
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.75f))
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
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.ihsan_icon_location),
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.locationName.ifBlank { stringResource(R.string.default_city) },
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = darkTealColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = darkTealSubtext,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Refresh Status Pill
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.60f))
                                .padding(horizontal = 12.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.error != null) Color(0xFFEF4444) else Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.isLoading) "جاري التحديث..."
                                    else if (uiState.error != null) "خطأ في التحديث"
                                    else uiState.cityAndCountry.ifBlank { "تم التحديث الآن" },
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = darkTealSubtext
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Large Compass Focal Area
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            Box(
                                modifier = Modifier.size(250.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF0F766E))
                            }
                        } else if (uiState.error != null && uiState.qiblaAngle == 0f) {
                            Box(
                                modifier = Modifier
                                    .size(250.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
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
                                            tint = Color(0xFF0F766E)
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
                                modifier = Modifier.size(250.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Degree / Bearing Display
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "اتجاه القبلة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = darkTealSubtext
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "${uiState.qiblaAngle.toInt().toArabicDigits()}°",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = darkTealColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Info Chips Row (Accuracy + Saved Location)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                    ) {
                        // Accuracy Badge
                        val accuracyText = when {
                            uiState.calibrationMessage != null -> "تحتاج معايرة"
                            uiState.sensorAccuracy != null -> "الدقة جيدة"
                            else -> "البوصلة نشطة"
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFEFF8F6))
                                .border(0.6.dp, Color(0xFFCBE3DF), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = accuracyText,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF046A38)
                                )
                            }
                        }

                        // Saved Location Badge
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFEFF8F6))
                                .border(0.6.dp, Color(0xFFCBE3DF), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.ihsan_icon_location),
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = uiState.locationName.ifBlank { stringResource(R.string.default_city) },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = darkTealColor
                                    )
                                    Text(
                                        text = uiState.cityAndCountry.ifBlank { "آخر موقع محفوظ" },
                                        fontSize = 8.5.sp,
                                        color = darkTealSubtext
                                    )
                                }
                            }
                        }
                    }
                }

                // Calibration Guidance Card (anchored at bottom)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEDF6F7)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(Color(0xFFD0E8EC))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left side (RTL Start): Calibration Graphic Image
                        Image(
                            painter = painterResource(id = R.drawable.ihsan_qibla_calibration),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Middle text
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "حرّك الهاتف بلطف",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkTealColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.calibrationMessage ?: "لتحسين دقة البوصلة",
                                fontSize = 11.sp,
                                color = darkTealSubtext
                            )
                        }

                        // Right side (RTL End): Info Icon
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = darkTealSubtext.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
