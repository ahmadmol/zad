package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme

@Composable
fun HomeContextualHero(
    location: String,
    hijriDate: String,
    currentPrayerName: String,
    countdownText: String,
    nextPrayerName: String,
    nextPrayerTime: String,
    @Suppress("UNUSED_PARAMETER") dailyVerseText: String,
    @Suppress("UNUSED_PARAMETER") dailyVerseSource: String,
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onQiblaClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPrayerClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    userName: String = ""
) {
    val colors = IhsanTheme.colors
    val isDark = IhsanTheme.isDark
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header Row: Brand Logo + Greeting at Start (Right), Actions at End (Left)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IhsanTextWithKaabaIcon("إحسان", colors.accentContent, logoHeight = 36.dp)
                Text(
                    text = if (userName.isBlank() || userName == "مستخدم إحسان") "أهلًا بك، طاب يومك" else "أهلًا ${userName.substringBefore(' ').take(24)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IhsanHeaderAction(
                    icon = Icons.Outlined.Search,
                    contentDescription = "بحث",
                    onClick = onSearchClick,
                    containerColor = if (isDark) colors.surfaceElevated else Color.White,
                    tint = colors.textPrimary
                )
                IhsanHeaderAction(
                    icon = Icons.Outlined.Notifications,
                    contentDescription = "التذكيرات",
                    onClick = onNotificationClick,
                    containerColor = if (isDark) colors.surfaceElevated else Color.White,
                    tint = colors.textPrimary
                )
            }
        }

        // Secondary Metadata Row: Hijri Date at Start (Right) + Location at End (Left)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = hijriDate.ifBlank { "23 جمادى الأولى 1448" },
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = colors.textSecondary
            )
            Row(
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .clickable(onClick = onLocationClick)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isDark) Color(0xFF6ED9C5) else Color(0xFF2C6D9E)
                )
                Text(
                    text = location.ifBlank { "حلب" },
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = colors.textPrimary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isDark) Color(0xFF6ED9C5) else Color(0xFF2C6D9E)
                )
            }
        }

        // Prayer Hero Card: Navy background, Dome of the Rock on left, Countdown and Prayer Times on right
        Surface(
            onClick = onPrayerClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F263A)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF132D48), Color(0xFF0A1E32))
                        )
                    )
                    .padding(top = 18.dp, bottom = 14.dp, start = 18.dp, end = 18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Main Prayer Content: Right = Info/Countdown, Left = Dome of the Rock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "مساحة للصلاة",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFFE4C78E)
                            )
                            Text(
                                text = if (currentPrayerName.isBlank() || currentPrayerName == "—") "في يومك خير" else "وقت $currentPrayerName",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (nextPrayerName.isBlank() || nextPrayerName == "—") "جهّز مواقيت يومك" else "الصلاة القادمة • $nextPrayerName",
                                modifier = Modifier.semantics { heading() },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            if (countdownText.isNotBlank()) {
                                Text(
                                    text = "الوقت المتبقي للصلاة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFB0C4DE)
                                )
                            }
                            val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
                            Text(
                                text = countdownText.ifBlank { "01:04:07" },
                                style = if (fontScale > 1.3f) {
                                    MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        textDirection = TextDirection.Ltr
                                    )
                                } else {
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        textDirection = TextDirection.Ltr
                                    )
                                },
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                color = Color.White
                            )
                            Text(
                                text = if (nextPrayerTime.isBlank() || nextPrayerTime == "—") "مواقيت الصلاة والإعدادات" else "موعدها $nextPrayerTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD6E2EE)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // Stylized Dome of the Rock Vector Artwork - responsive sizing
                        val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
                        val illustrationWidth = (130f / fontScale.coerceAtLeast(1f)).coerceIn(85f, 130f).dp
                        val illustrationHeight = (145f / fontScale.coerceAtLeast(1f)).coerceIn(95f, 145f).dp
                        Box(
                            modifier = Modifier.size(width = illustrationWidth, height = illustrationHeight),
                            contentAlignment = Alignment.Center
                        ) {
                            IhsanIllustration(
                                scene = IhsanScene.AQSA,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Bottom Row: Caption and Golden Chevron Indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "رسم مستوحى من قبة الصخرة • القدس",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color(0xFF90A8C0)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = Color(0xFFE4C78E),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
