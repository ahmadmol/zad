package com.example.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.*

@Composable
fun IhsanSectionHeader(
    title: String,
    subtitle: String? = null,
    action: String? = null,
    onAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = IhsanTheme.colors.textPrimary
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (action != null) {
            TextButton(
                onClick = onAction,
                colors = ihsanTextButtonColors(),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = action,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun IhsanIconContainer(icon: ImageVector, modifier: Modifier = Modifier, description: String? = null,
    color: Color = IhsanTheme.colors.accentContent) {
    Surface(modifier.size(48.dp), shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = if (IhsanTheme.isDark) .12f else .08f)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, description, Modifier.size(24.dp), tint = color) }
    }
}

@Composable
fun IhsanServiceCard(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isDark = IhsanTheme.isDark
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 104.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) IhsanTheme.colors.surfaceElevated else Color.White,
        border = BorderStroke(1.dp, if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFF0F0F0))
    ) {
        Column(
            Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF1E302C) else Color(0xFFEDF4FA)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isDark) Color(0xFF6ED9C5) else Color(0xFF2C6D9E)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                color = IhsanTheme.colors.textPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun IhsanGlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    // Opaque backing is a readable, inexpensive fallback on every supported API.
    Surface(modifier, shape = MaterialTheme.shapes.extraLarge, color = IhsanTheme.colors.surfaceElevated) {
        Column(Modifier.liquidGlass().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun IhsanProgressIndicator(progress: Float, modifier: Modifier = Modifier) {
    val motion = rememberIhsanMotionEnabled()
    val value by animateFloatAsState(progress.coerceIn(0f, 1f), tween(if (motion) IhsanMotion.Standard else 0), label = "progress")
    val isDark = IhsanTheme.isDark
    CircularProgressIndicator(
        progress = { value },
        modifier = modifier,
        color = if (isDark) Color(0xFF33C6AE) else Color(0xFF2C6D9E),
        trackColor = if (isDark) Color(0xFF24332F) else Color(0xFFEBF1F6),
        strokeWidth = 3.5.dp,
        strokeCap = StrokeCap.Round
    )
}

@Composable
fun IhsanDailyGoalCard(title: String, count: Int, target: Int, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val isDark = IhsanTheme.isDark
    Surface(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) IhsanTheme.colors.surfaceElevated else Color.White,
        border = BorderStroke(1.dp, if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFF0F0F0))
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                IhsanProgressIndicator(if (target > 0) count.toFloat() / target else 0f, Modifier.fillMaxSize())
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = IhsanTheme.colors.textPrimary
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = IhsanTheme.colors.textPrimary
                )
                Text(
                    text = "$count من $target • بتسجيلك",
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = null,
                tint = if (isDark) Color(0xFF6ED9C5) else Color(0xFF2C6D9E),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Immutable
data class IhsanPrayerTimelineItem(val id: String, val title: String, val time: String, val next: Boolean, val past: Boolean)

@Composable
fun IhsanPrayerTimeline(items: List<IhsanPrayerTimelineItem>, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    IhsanContentCard(modifier) {
        IhsanSectionHeader("مواقيت اليوم", action = "التفاصيل", onAction = onOpen)
        items.forEach { prayer -> key(prayer.id) {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .background(if (prayer.next) IhsanTheme.colors.accentContent.copy(alpha = .08f) else Color.Transparent,
                    MaterialTheme.shapes.small).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(if (prayer.next) 10.dp else 6.dp).background(
                    if (prayer.next) IhsanTheme.colors.accentContent else IhsanTheme.colors.textSecondary,
                    RoundedCornerShape(50)))
                Text(prayer.title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge,
                    color = if (prayer.past) IhsanTheme.colors.textSecondary else IhsanTheme.colors.textPrimary)
                if (prayer.next) Text("التالية", color = IhsanTheme.colors.accentContent, style = MaterialTheme.typography.labelMedium)
                Text(prayer.time, style = MaterialTheme.typography.titleSmall)
            }
        } }
    }
}
