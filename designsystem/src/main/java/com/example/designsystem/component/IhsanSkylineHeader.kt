package com.example.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.R
import com.example.designsystem.theme.IhsanTheme

/** Shared native skyline geometry; the two themes change only its palette. */
@Composable
fun IhsanSkylineHeader(
    title: String, subtitle: String? = null, modifier: Modifier = Modifier,
    location: String? = null, hijriDate: String? = null, showBrand: Boolean = false,
    onBack: (() -> Unit)? = null, backgroundImageRes: Int = R.drawable.ihsan_reference_skyline,
    actions: @Composable RowScope.() -> Unit = {},
    compact: Boolean = false
) {
    val ink = if (IhsanTheme.isDark) IhsanTheme.colors.onBrand else Color(0xFF00565A)
    val skyInk = ink
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))) {
        Image(painterResource(backgroundImageRes), null, Modifier.matchParentSize(),
            contentScale = ContentScale.Crop, alignment = Alignment.BottomCenter)
        if (IhsanTheme.isDark) Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(
            IhsanTheme.colors.surfaceBase.copy(alpha = .50f),
            IhsanTheme.colors.surfaceBase.copy(alpha = .65f),
            IhsanTheme.colors.surfaceBase.copy(alpha = .82f)))))
        Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp)
            .padding(top = 4.dp, bottom = if (compact) 12.dp else 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (compact) {
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) IconButton(onBack, Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = ink, modifier = Modifier.size(22.dp))
                    } else Spacer(Modifier.width(48.dp))
                    Text(title, Modifier.weight(1f), color = ink, fontSize = 20.sp, lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Row(Modifier.widthIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End, content = actions)
                }
                if (!subtitle.isNullOrBlank()) Text(subtitle, color = ink, fontSize = 13.sp,
                    lineHeight = 19.sp, textAlign = TextAlign.Center)
            } else {
            Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    if (onBack != null) IconButton(onBack, Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = skyInk, modifier = Modifier.size(22.dp))
                    }
                    location?.let { Text(it, color = skyInk, fontSize = 12.sp, lineHeight = 16.sp) }
                    hijriDate?.let { Text(it, color = skyInk, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2) }
                }
                if (showBrand) Column(Modifier.width(78.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    IhsanTextWithKaabaIcon("إحسان", skyInk, logoHeight = 28.dp)
                    Text("خير دائم", color = skyInk, fontSize = 10.sp, lineHeight = 14.sp)
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    actions()
                }
            }
            Text(title, color = ink, fontSize = 25.sp, lineHeight = 33.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            if (!subtitle.isNullOrBlank()) Text(subtitle, color = ink, fontSize = 13.sp,
                lineHeight = 19.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}
