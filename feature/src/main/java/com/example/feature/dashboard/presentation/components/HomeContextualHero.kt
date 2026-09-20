package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeContextualHero(
    location: String,
    hijriDate: String,
    currentPrayerName: String,
    countdownText: String,
    nextPrayerName: String,
    nextPrayerTime: String,
    dailyVerseText: String,
    dailyVerseSource: String,
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit,
    onQiblaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = IhsanTheme.isDark
    val darkTealColor = if (isDark) IhsanTheme.colors.textPrimary else IhsanTheme.colors.brand
    val darkTealSubtext = if (isDark) IhsanTheme.colors.textSecondary else IhsanTheme.colors.brand.copy(alpha = 0.82f)
    val isPrayerUnavailable = currentPrayerName.isBlank() || currentPrayerName == "—"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        // 1. Hero Background Image (Sky + Curved Division + Dark Teal Mosque Skyline)
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

        // 2. Foreground Content Layer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            // 1. Top Sky Bar: Location & Date (Right in RTL), Brand Logo (Center), Search & Notifications (Left in RTL)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // RTL Right: Location & Hijri Date
                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ihsan_icon_location),
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location.ifBlank { stringResource(R.string.default_city) },
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkTealColor
                        )
                    }
                    Text(
                        text = hijriDate,
                        fontSize = 11.5.sp,
                        color = darkTealSubtext,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 21.dp)
                    )
                }

                // Center: Brand Logo & Title & Subtitle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ihsan_brand_mark),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = stringResource(R.string.home_hero_title),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkTealColor,
                        letterSpacing = 0.2.sp
                    )
                    Text(
                        text = stringResource(R.string.home_hero_subtitle),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = darkTealSubtext.copy(alpha = 0.85f)
                    )
                }

                // RTL Left: Search & Notification Buttons (Touch Target >= 48dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Search Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(onClick = onSearchClick)
                            .semantics {
                                role = Role.Button
                                contentDescription = "بحث"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ihsan_icon_search),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Notification Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(onClick = onNotificationClick)
                            .semantics {
                                role = Role.Button
                                contentDescription = "التنبيهات"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ihsan_icon_notification),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Prayer Focal Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
            ) {
                // Responsive White Prayer Arc Canvas + Sun Indicator Badge at Apex
                val arcWidth = minOf(LocalConfiguration.current.screenWidthDp.dp * 0.62f, 230.dp)
                val arcHeight = 76.dp

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .size(arcWidth, arcHeight)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val rx = w / 2f
                        val ry = h
                        val cx = w / 2f
                        val cy = h

                        val strokePx = 1.6.dp.toPx()

                        drawArc(
                            color = Color.White.copy(alpha = 0.48f),
                            startAngle = 195f,
                            sweepAngle = 150f,
                            useCenter = false,
                            topLeft = Offset.Zero,
                            size = Size(w, h * 2f),
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )

                        // Base tip dots
                        val startRad = Math.toRadians(195.0)
                        val endRad = Math.toRadians(345.0)

                        val xStart = cx + rx * cos(startRad).toFloat()
                        val yStart = cy + ry * sin(startRad).toFloat()

                        val xEnd = cx + rx * cos(endRad).toFloat()
                        val yEnd = cy + ry * sin(endRad).toFloat()

                        val dotRadius = 2.4.dp.toPx()
                        val dotColor = Color.White.copy(alpha = 0.85f)

                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(xStart, yStart))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(xEnd, yEnd))
                    }

                    // Sun Indicator Badge centered over top apex of Prayer Arc
                    Image(
                        painter = painterResource(id = R.drawable.ihsan_icon_prayer_indicator),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-14).dp)
                            .size(30.dp)
                    )
                }

                // Center Prayer Block (framed by arc)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 22.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_hero_prayer_time_label),
                        fontSize = 12.5.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = currentPrayerName.ifBlank { "—" },
                        fontSize = if (isPrayerUnavailable) 24.sp else 32.sp,
                        fontWeight = if (isPrayerUnavailable) FontWeight.Normal else FontWeight.Bold,
                        color = if (isPrayerUnavailable) Color.White.copy(alpha = 0.6f) else Color.White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    if (countdownText.isNotBlank()) {
                        Text(
                            text = "يتبقى $countdownText",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.95f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Qibla Button Pill
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable(onClick = onQiblaClick)
                            .semantics {
                                role = Role.Button
                                contentDescription = "اتجاه القبلة"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(0.6.dp, Color.White.copy(alpha = 0.30f), CircleShape)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ihsan_icon_qibla),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.home_hero_qibla_btn),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Side Sections: Daily Verse (RTL Right) and Next Prayer (RTL Left)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    val maxW = this.maxWidth
                    val sideWidth = minOf(maxW * 0.28f, 110.dp)

                    // RTL Right: Daily Verse
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(y = 42.dp)
                            .widthIn(max = sideWidth),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = dailyVerseText.ifBlank { stringResource(R.string.daily_verse_default) },
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.92f),
                            fontWeight = FontWeight.Normal,
                            lineHeight = 15.5.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dailyVerseSource.ifBlank { stringResource(R.string.daily_verse_source_default) },
                            fontSize = 9.5.sp,
                            color = Color.White.copy(alpha = 0.70f),
                            textAlign = TextAlign.Start
                        )
                    }

                    // RTL Left: Next Prayer Note
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(y = 38.dp)
                            .widthIn(max = sideWidth),
                        horizontalAlignment = Alignment.End
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ihsan_icon_prayer),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(bottom = 2.dp)
                        )
                        Text(
                            text = stringResource(R.string.home_hero_next_prayer),
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = nextPrayerName.ifBlank { "—" },
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.End
                        )
                        if ((nextPrayerTime.isNotBlank()) && (nextPrayerTime != "—")) {
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = nextPrayerTime,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.95f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}
