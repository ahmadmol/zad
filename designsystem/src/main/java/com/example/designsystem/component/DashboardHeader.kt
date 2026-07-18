package com.example.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    greeting: String = "أهلاً بك في إحسان",
    modifier: Modifier = Modifier
) {
    val brand = MaterialTheme.colorScheme.primary
    val onBrand = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(brand, IhsanTheme.colors.brandElevated)
                )
            )
            .statusBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_mosque_silhouette),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(72.dp),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleMedium,
                    color = onBrand.copy(alpha = 0.92f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(IhsanTheme.dimens.minTouchTarget)
                        .clip(RoundedCornerShape(12.dp))
                        .background(onBrand.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(id = R.string.notifications_desc),
                        tint = onBrand
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTime.ifBlank { "—" },
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = onBrand,
                        lineHeight = 44.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.semantics {
                            contentDescription = nextPrayerInfo
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = onBrand.copy(alpha = 0.75f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = nextPrayerInfo,
                            fontSize = 12.sp,
                            color = onBrand.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = hijriDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onBrand,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = onBrand.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location,
                            fontSize = 12.sp,
                            color = onBrand.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
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
        }
    }
}

@Composable
fun PrayerTimeCard(
    name: String,
    time: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onBrand = MaterialTheme.colorScheme.onPrimary
    val shape = RoundedCornerShape(14.dp)
    val description = if (isActive) "$name، $time، الصلاة الحالية أو القادمة" else "$name، $time"

    Box(
        modifier = modifier
            .widthIn(min = 72.dp)
            .semantics {
                contentDescription = description
                selected = isActive
            }
            .then(
                if (isActive) {
                    Modifier.shadow(6.dp, shape, clip = false)
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(
                if (isActive) onBrand.copy(alpha = 0.18f)
                else onBrand.copy(alpha = 0.08f)
            )
            .then(
                if (isActive) {
                    Modifier.border(1.5.dp, onBrand.copy(alpha = 0.9f), shape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .defaultMinSize(minHeight = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) onBrand else onBrand.copy(alpha = 0.75f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Icon(
                imageVector = prayerIconForName(name),
                contentDescription = null,
                tint = if (isActive) onBrand else onBrand.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = time,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) onBrand else onBrand.copy(alpha = 0.75f),
                maxLines = 1
            )
        }
    }
}

private fun prayerIconForName(nameAr: String): ImageVector {
    return when {
        nameAr.contains("فجر") -> Icons.Default.DarkMode
        nameAr.contains("شروق") -> Icons.Default.WbSunny
        nameAr.contains("ظهر") -> Icons.Default.WbSunny
        nameAr.contains("عصر") -> Icons.Default.WbTwilight
        nameAr.contains("مغرب") -> Icons.Default.WbTwilight
        nameAr.contains("عشاء") -> Icons.Default.DarkMode
        else -> Icons.Default.WbSunny
    }
}
