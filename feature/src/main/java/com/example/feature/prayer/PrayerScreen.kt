package com.example.feature.prayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.prayer.presentation.PrayerAction
import com.example.feature.prayer.presentation.CitySelectionBottomSheet
import com.example.feature.prayer.presentation.PrayerDetailsBottomSheet
import com.example.feature.prayer.presentation.PrayerSettingsBottomSheet
import com.example.feature.prayer.presentation.PrayerUiState
import com.example.feature.prayer.presentation.PrayerViewModel
import org.koin.androidx.compose.koinViewModel

data class PrayerTime(
    val nameAr: String,
    val nameEn: String,
    val time: String,
    val timestamp: Long = 0,
    val isPast: Boolean = false,
    val isActive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(
    viewModel: PrayerViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToQibla: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = IhsanTheme.colors
    var showCitySelection by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.surfaceMuted)
        ) {
            // Header Hero Background Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ihsan_home_hero_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter
                )

                if (IhsanTheme.isDark) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f))
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                ) {
                    // Top Navigation & Location Header Bar
                    PrayerTopBar(
                        locationName = uiState.locationName,
                        hijriDate = uiState.hijriDate.ifBlank { HijriDateFormatter.nowFormatted() },
                        gregorianDate = uiState.currentDate,
                        onNavigateBack = onNavigateBack
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Prayer Hero Focal Area
                    PrayerHeroSection(
                        nextPrayerName = uiState.nextPrayerName,
                        countdown = uiState.nextPrayerCountdown,
                        onQiblaClick = onNavigateToQibla
                    )
                }
            }

            // Body Content Surface
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 300.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(colors.surfaceMuted)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Quran Verse Card
                    item {
                        QuranVerseCard()
                    }

                    // 2. Prayer Times List Header
                    item {
                        PrayerTimesHeader(
                            onShowCalendar = {
                                viewModel.onAction(PrayerAction.OnToggleCalendarSheet(true))
                            }
                        )
                    }

                    // 3. Prayer Times Rows
                    items(uiState.prayerTimes) { prayer ->
                        val isNext = prayer.nameAr == uiState.nextPrayerName || prayer.isActive
                        PrayerTimeRow(
                            prayer = prayer,
                            isNext = isNext,
                            countdown = if (isNext) uiState.nextPrayerCountdown else null,
                            onClick = {
                                viewModel.onAction(PrayerAction.OnSelectPrayer(prayer))
                            }
                        )
                    }

                    // 4. Prayer Settings Section ("إعدادات الصلاة")
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        PrayerSettingsSection(
                            uiState = uiState,
                            onAction = viewModel::onAction
                        )
                    }

                }
            }

            // Settings Bottom Sheet
            if (uiState.showSettingsBottomSheet) {
                PrayerSettingsBottomSheet(
                    onDismiss = { viewModel.onAction(PrayerAction.OnToggleSettingsSheet(false)) },
                    onUpdateMethod = { methodStr ->
                        viewModel.onAction(PrayerAction.OnUpdateCalculationMethod(methodStr))
                    },
                    onUpdateMadhab = { viewModel.onAction(PrayerAction.OnUpdateMadhhab(it)) },
                    onUpdateLocationMode = { viewModel.onAction(PrayerAction.OnUpdateLocationMode(it)) },
                    onUpdateSound = { viewModel.onAction(PrayerAction.OnUpdateSound(it)) },
                    onSelectCityClick = {
                        viewModel.onAction(PrayerAction.OnToggleSettingsSheet(false))
                        showCitySelection = true
                    }
                )
            }

            if (showCitySelection) {
                CitySelectionBottomSheet(
                    onDismiss = { showCitySelection = false },
                    onCitySelected = { city ->
                        viewModel.onAction(PrayerAction.OnUpdateManualLocation(city.name, city.lat, city.lng))
                        showCitySelection = false
                    }
                )
            }

            // Prayer Details Bottom Sheet
            uiState.selectedPrayer?.let { selected ->
                PrayerDetailsBottomSheet(
                    selectedPrayer = selected,
                    allPrayers = uiState.prayerTimes,
                    location = uiState.locationName,
                    countdown = uiState.nextPrayerCountdown,
                    onDismiss = { viewModel.onAction(PrayerAction.OnSelectPrayer(null)) },
                    onNavigateToFullPrayer = { viewModel.onAction(PrayerAction.OnSelectPrayer(null)) },
                    onUpdatePrePrayer = { mins ->
                        viewModel.onAction(PrayerAction.OnUpdatePrePrayerMinutes(mins))
                    }
                )
            }
        }
    }
}

@Composable
fun PrayerTopBar(
    locationName: String,
    hijriDate: String,
    gregorianDate: String,
    onNavigateBack: () -> Unit
) {
    val isDark = IhsanTheme.isDark
    val primaryText = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF0F2A30)
    val subtext = if (isDark) IhsanTheme.colors.textSecondary else Color(0xFF4A6068)
    val backBg = if (isDark) IhsanTheme.colors.surfaceElevated else Color.White.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(40.dp)
                .background(backBg, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = primaryText
            )
        }

        // Title
        Text(
            text = "الصلاة",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = primaryText
            )
        )

        // Location & Date
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ihsan_icon_location),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = locationName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        fontSize = 14.sp
                    )
                )
            }
            Text(
                text = hijriDate,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = subtext,
                    fontSize = 11.sp
                )
            )
            Text(
                text = gregorianDate,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = subtext,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun PrayerHeroSection(
    nextPrayerName: String,
    countdown: String,
    onQiblaClick: () -> Unit
) {
    val isDark = IhsanTheme.isDark
    val primaryText = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF0F2A30)
    val subtext = if (isDark) IhsanTheme.colors.textSecondary else Color(0xFF1E3F47)
    val countdownText = if (isDark) IhsanTheme.colors.selectedContent else Color(0xFF0F2A30)
    val qiblaBg = if (isDark) IhsanTheme.colors.selectedContainer else Color.White.copy(alpha = 0.35f)
    val qiblaBorder = if (isDark) IhsanTheme.colors.selectedContent.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f)
    val qiblaText = if (isDark) IhsanTheme.colors.selectedContent else Color(0xFF0F2A30)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Responsive White Arc
        PrayerArcCanvas(
            modifier = Modifier
                .width(260.dp)
                .height(130.dp)
                .padding(top = 18.dp)
        )

        // Indicator at Peak Apex
        Image(
            painter = painterResource(id = R.drawable.ihsan_icon_prayer_indicator),
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.TopCenter)
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "موعد الصلاة القادمة",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = subtext,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            )

            Text(
                text = nextPrayerName,
                style = MaterialTheme.typography.displayMedium.copy(
                    color = primaryText,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "يتبقى ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = countdownText,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = countdown,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = countdownText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Qibla CTA Pill Button
            Surface(
                onClick = onQiblaClick,
                shape = CircleShape,
                color = qiblaBg,
                border = BorderStroke(1.dp, qiblaBorder),
                modifier = Modifier
                    .height(36.dp)
                    .defaultMinSize(minWidth = 120.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ihsan_icon_qibla),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "اتجاه القبلة",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = qiblaText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerArcCanvas(
    modifier: Modifier = Modifier,
    arcColor: Color = Color.White.copy(alpha = 0.9f),
    strokeWidth: Dp = 2.dp,
    dotRadius: Dp = 3.5.dp
) {
    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }
    val dotRadiusPx = with(LocalDensity.current) { dotRadius.toPx() }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val startAngle = 195f
        val sweepAngle = 150f

        val rect = Rect(
            left = 0f,
            top = 0f,
            right = width,
            bottom = height * 2f
        )

        drawArc(
            color = arcColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = rect.topLeft,
            size = rect.size,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        val startRad = Math.toRadians(startAngle.toDouble())
        val endRad = Math.toRadians((startAngle + sweepAngle).toDouble())
        val rx = width / 2f
        val ry = height

        val startX = rx + rx * Math.cos(startRad).toFloat()
        val startY = ry + ry * Math.sin(startRad).toFloat()

        val endX = rx + rx * Math.cos(endRad).toFloat()
        val endY = ry + ry * Math.sin(endRad).toFloat()

        drawCircle(color = arcColor, radius = dotRadiusPx, center = Offset(startX, startY))
        drawCircle(color = arcColor, radius = dotRadiusPx, center = Offset(endX, endY))
    }
}

@Composable
fun QuranVerseCard() {
    val isDark = IhsanTheme.isDark
    val cardBg = if (isDark) Color(0xFF1B1A17) else MaterialTheme.colorScheme.surface
    val titleText = if (isDark) Color(0xFFEEE9DD) else Color(0xFF1B3B32)
    val bodyText = if (isDark) Color(0xFFEEE9DD) else Color(0xFF112A23)
    val subText = if (isDark) Color(0xFFAAA498) else Color(0xFF4A6D63)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isDark) {
                Image(
                    painter = painterResource(id = R.drawable.ihsan_quran_card_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = titleText,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "أَقِمِ ٱلصَّلَوٰةَ إِنَّ ٱلصَّلَوٰةَ تَنْهَىٰ عَنِ ٱلْفَحْشَاءِ وَٱلْمُنكَرِ ۗ",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = bodyText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "(العنكبوت ٤٥)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = subText,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PrayerTimesHeader(onShowCalendar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "مواقيت الصلاة",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onShowCalendar() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = IhsanTheme.colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "عرض التقويم",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = IhsanTheme.colors.textSecondary,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
fun PrayerTimeRow(
    prayer: PrayerTime,
    isNext: Boolean,
    countdown: String?,
    onClick: () -> Unit
) {
    val isDark = IhsanTheme.isDark
    val colors = IhsanTheme.colors

    val containerColor = if (isNext) (if (isDark) IhsanTheme.colors.selectedContainer else Color(0xFFD8EFEB)) else colors.surfaceElevated
    val borderColor = if (isNext) (if (isDark) IhsanTheme.colors.selectedContent.copy(alpha = 0.4f) else Color(0xFFAADCD5)) else colors.borderSubtle
    val contentColor = if (isNext) (if (isDark) IhsanTheme.colors.selectedContent else Color(0xFF0D4E4A)) else MaterialTheme.colorScheme.onSurface
    val badgeBg = if (isDark) IhsanTheme.colors.selectedContent.copy(alpha = 0.2f) else Color(0xFF2C7A75)
    val badgeFg = if (isDark) IhsanTheme.colors.selectedContent else Color.White

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isNext) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(badgeBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ihsan_icon_prayer),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = prayerIconVector(prayer.nameAr),
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.nameAr,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 16.sp,
                                color = contentColor
                            )
                        )
                        if (isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = badgeBg,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "القادمة",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = badgeFg,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isNext) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = prayer.time,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = contentColor
                    )
                )
            }
        }
    }
}

private fun prayerIconVector(prayerNameAr: String): ImageVector = when (prayerNameAr) {
    "الفجر" -> Icons.Default.WbTwilight
    "الشروق" -> Icons.Default.WbSunny
    "الظهر" -> Icons.Default.LightMode
    "العصر" -> Icons.Default.Mosque
    "المغرب" -> Icons.Default.NightsStay
    "العشاء" -> Icons.Default.Bedtime
    else -> Icons.Default.AccessTime
}

@Composable
fun PrayerSettingsSection(
    uiState: PrayerUiState,
    onAction: (PrayerAction) -> Unit
) {
    val colors = IhsanTheme.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "إعدادات الصلاة",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tile 1: Calculation Method
            Surface(
                onClick = { onAction(PrayerAction.OnToggleSettingsSheet(true)) },
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.borderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "طريقة الحساب",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${uiState.calculationMethodName} >",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Tile 2: Pre-prayer notification
            Surface(
                onClick = { onAction(PrayerAction.OnToggleSettingsSheet(true)) },
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.borderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "التنبيه قبل الصلاة",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val reminderText = if (uiState.prePrayerReminderMinutes > 0) {
                            "${uiState.prePrayerReminderMinutes} دقائق >"
                        } else {
                            "إيقاف >"
                        }
                        Text(
                            text = reminderText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Tile 3: Adhan Switch
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.borderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "الأذان",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Switch(
                            checked = uiState.adhanEnabled,
                            onCheckedChange = { onAction(PrayerAction.OnToggleAdhan(it)) },
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivePrayerCard(prayer: PrayerTime, countdown: String, location: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "${prayer.nameAr}، الوقت المتبقي $countdown، $location"
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = prayer.nameAr,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "الوقت المتبقي: $countdown",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = location,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            )
        }
    }
}

@Composable
fun StandardPrayerCard(prayer: PrayerTime) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prayer.nameAr,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = prayer.time,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

