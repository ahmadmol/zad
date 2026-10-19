package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.component.IhsanHeroScrim
import com.example.designsystem.component.IhsanHeroTopRow
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

@Composable
fun HomeContextualHero(location: String, hijriDate: String, currentPrayerName: String,
    countdownText: String, nextPrayerName: String, nextPrayerTime: String,
    dailyVerseText: String, dailyVerseSource: String, onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit, onQiblaClick: () -> Unit, modifier: Modifier = Modifier) {
    val foreground = IhsanTheme.colors.onBrand
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))) {
        Image(painterResource(R.drawable.ihsan_home_hero_background), null,
            contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        IhsanHeroScrim(Modifier.matchParentSize())
        Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp)
            .padding(top = 2.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IhsanHeroTopRow(location.ifBlank { stringResource(R.string.default_city) }, hijriDate, onSearchClick, onNotificationClick)
            Box(Modifier.fillMaxWidth()) {
                Canvas(Modifier.align(Alignment.TopCenter).fillMaxWidth(.60f).height(126.dp)) {
                    drawArc(foreground.copy(alpha = .8f), 180f, 180f, false,
                        Offset(0f, 12.dp.toPx()), Size(size.width, (size.height - 12.dp.toPx()) * 2),
                        style = Stroke(1.dp.toPx()))
                }
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Column(Modifier.weight(.22f).padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.home_hero_next_prayer), style = MaterialTheme.typography.labelSmall, color = foreground)
                        Text(nextPrayerTime.ifBlank { "—" }, style = MaterialTheme.typography.labelLarge, color = foreground)
                        if (currentPrayerName.isNotBlank() && currentPrayerName != "—")
                            Text(stringResource(R.string.home_current_prayer_format, currentPrayerName),
                                style = MaterialTheme.typography.labelSmall, color = foreground)
                    }
                    Column(Modifier.weight(.56f), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.WbSunny, null, tint = IhsanTheme.colors.accentWarm, modifier = Modifier.size(20.dp))
                        Text(stringResource(R.string.home_hero_next_prayer), style = MaterialTheme.typography.bodySmall, color = foreground)
                        Text(nextPrayerName.ifBlank { "—" }, fontSize = 30.sp, lineHeight = 38.sp,
                            fontWeight = FontWeight.Bold, color = foreground, textAlign = TextAlign.Center)
                        Text(stringResource(R.string.correction_countdown, countdownText.ifBlank { "—" }),
                            style = MaterialTheme.typography.titleMedium, color = foreground, textAlign = TextAlign.Center)
                        TextButton(onQiblaClick, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = foreground),
                            modifier = Modifier.heightIn(min = 36.dp)) {
                            Icon(Icons.Outlined.Explore, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.home_hero_qibla_btn), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Column(Modifier.weight(.22f).padding(bottom = 10.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(dailyVerseText.ifBlank { stringResource(R.string.daily_verse_default) },
                            style = MaterialTheme.typography.labelSmall, color = foreground, textAlign = TextAlign.Center)
                        Text(dailyVerseSource.ifBlank { stringResource(R.string.daily_verse_source_default) },
                            style = MaterialTheme.typography.labelSmall, color = foreground, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
