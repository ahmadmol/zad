package com.example.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.R
import com.example.designsystem.theme.IhsanTheme

@Composable
fun DashboardHeader(
    currentTime: String,
    hijriDate: String,
    location: String,
    nextPrayerInfo: String,
    prayerTimes: List<Pair<String, String>>,
    activePrayerIndex: Int,
    onNotificationClick: () -> Unit,
    onPrayerClick: (Int) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") greeting: String = "",
    modifier: Modifier = Modifier
) {
    val onBrand = IhsanTheme.colors.onBrand
    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_home),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            // In RTL: first child = End (visual right) = clock; second child = Start
            // (visual left) = notifications. Greeting text was removed in the
            // Clean UI pass so the header carries only the information that
            // changes minute-to-minute (date, location, clock, next prayer).
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClockDisplay(
                    currentTime = currentTime,
                    onBrand = onBrand
                )
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(onBrand.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(id = R.string.notifications_desc),
                        tint = onBrand,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // RTL: first = date/location (visual right), second = next prayer chip (visual left/center)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = if (layoutDirection == LayoutDirection.Rtl) {
                        Alignment.Start
                    } else {
                        Alignment.End
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = hijriDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = onBrand.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = onBrand.copy(alpha = 0.78f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = location,
                            fontSize = 12.sp,
                            color = onBrand.copy(alpha = 0.78f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                NextPrayerChip(
                    nextPrayerInfo = nextPrayerInfo,
                    onBrand = onBrand
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                itemsIndexed(prayerTimes) { index, pair ->
                    PrayerTimeCard(
                        name = pair.first,
                        time = pair.second,
                        isActive = index == activePrayerIndex,
                        onClick = { onPrayerClick(index) }
                    )
                }
            }

            // Bottom fade: stitches the green hero into the white body so the
            // prayer strip flows into the cards without a hard seam.
            Spacer(modifier = Modifier.height(12.dp))
            BottomFadeToSurface()
        }
    }
}

@Composable
private fun ClockDisplay(
    currentTime: String,
    onBrand: Color
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = clockSuffix(currentTime),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = onBrand.copy(alpha = 0.88f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = clockPart(currentTime),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = onBrand,
            lineHeight = 34.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun NextPrayerChip(
    nextPrayerInfo: String,
    onBrand: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(onBrand.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.HourglassBottom,
            contentDescription = null,
            tint = onBrand,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = nextPrayerInfo.ifBlank { "—" },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = onBrand,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BottomFadeToSurface() {
    val surface = MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        surface.copy(alpha = 0.55f)
                    )
                )
            )
    )
}

@Composable
fun PrayerTimeCard(
    name: String,
    time: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onBrand = IhsanTheme.colors.onBrand
    val shape = RoundedCornerShape(15.dp)

    Box(
        modifier = modifier
            .width(78.dp)
            .height(100.dp)
            .semantics { selected = isActive }
            .clip(shape)
            .background(
                if (isActive) Color.White.copy(alpha = 0.15f)
                else Color.Black.copy(alpha = 0.2f)
            )
            .then(
                if (isActive) {
                    Modifier.border(1.5.dp, Color.White.copy(alpha = 0.85f), shape)
                } else {
                    Modifier.border(0.5.dp, onBrand.copy(alpha = 0.2f), shape)
                }
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = onBrand,
                maxLines = 1,
                softWrap = false
            )
            Icon(
                imageVector = prayerIconForNameFixed(name),
                contentDescription = null,
                tint = if (isActive) Color(0xFFFFC400) else Color(0xFFFFC400).copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = time,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = onBrand,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

private fun prayerIconForNameFixed(nameAr: String): ImageVector = when {
    nameAr.contains("\u0627\u0644\u0641\u062c\u0631") -> Icons.Default.WbSunny
    nameAr.contains("\u0627\u0644\u0634\u0631\u0648\u0642") -> Icons.Default.WbSunny
    nameAr.contains("\u0627\u0644\u0638\u0647\u0631") -> Icons.Default.WbSunny
    nameAr.contains("\u0627\u0644\u0639\u0635\u0631") -> Icons.Default.WbSunny
    nameAr.contains("\u0627\u0644\u0645\u063a\u0631\u0628") -> Icons.Default.WbTwilight
    nameAr.contains("\u0627\u0644\u0639\u0634\u0627\u0621") -> Icons.Default.DarkMode
    else -> Icons.Default.WbSunny
}

/** HH:mm portion of a clock string such as "04:23 م". */
private fun clockPart(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isEmpty() || trimmed == "—") return ""
    return trimmed.substringBeforeLast(' ', missingDelimiterValue = trimmed)
}

/** Arabic ص/م (or legacy AM/PM) suffix. */
private fun clockSuffix(value: String): String {
    val trimmed = value.trim()
    if (!trimmed.contains(' ')) return ""
    return trimmed.substringAfterLast(' ')
        .replace(Regex("AM", RegexOption.IGNORE_CASE), "ص")
        .replace(Regex("PM", RegexOption.IGNORE_CASE), "م")
}
