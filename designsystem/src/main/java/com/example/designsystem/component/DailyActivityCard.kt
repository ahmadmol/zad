package com.example.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.component.softBrandTonalFill
import com.example.designsystem.theme.IhsanTheme

/**
 * The single, testable, count-based definition of "Daily Progress" on the Home card.
 *
 * Visual percentage, ring fill, and TalkBack announcement must all come from this
 * value for the same input, so a screen-reader user can never hear a different
 * number than the one rendered on screen.
 *
 * Definition: each item counts as 0 (not done) or 1 (done). A half-finished
 * item is **not** weighted at 0.5; that would mismatch the user-facing
 * "X من Y مكتملة" copy and the on-screen ring fill.
 */
data class DailyProgressSummary(
    val totalCount: Int,
    val doneCount: Int
) {
    /** Ring/linear progress in [0f, 1f]. Stable for empty input. */
    val overallProgress: Float
        get() = if (totalCount > 0) {
            (doneCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    /** Rounded integer percentage 0..100 used by both the visual % and TalkBack. */
    val percentage: Int
        get() = (overallProgress * 100).toInt()
}

fun summarizeDailyProgress(activities: List<DailyActivityItemData>): DailyProgressSummary =
    DailyProgressSummary(
        totalCount = activities.size,
        doneCount = activities.count { it.isCompleted }
    )

/**
 * v2 — clean, single-focal-point layout.
 *
 * v1 had a redundant LinearProgressIndicator under the subtitle AND the ring
 * around the percentage. Both were showing the same number. v2 keeps only
 * the ring (the focal visual) and removes the linear bar.
 */
@Composable
fun DailyActivityCard(
    activities: List<DailyActivityItemData>,
    onGoToChecklist: () -> Unit,
    modifier: Modifier = Modifier,
    progressAccessibilityLabel: String? = null
) {
    val summary = summarizeDailyProgress(activities)
    val totalCount = summary.totalCount
    val doneCount = summary.doneCount
    val overallProgress = summary.overallProgress
    val percentage = summary.percentage

    val semanticColors = IhsanTheme.colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val hasProgress = overallProgress > 0.001f

    val cardSemanticLabel = if (progressAccessibilityLabel.isNullOrBlank()) {
        if (totalCount > 0) {
            "النشاط اليومي، $doneCount من $totalCount مكتملة، التقدم $percentage بالمئة"
        } else {
            "النشاط اليومي، التقدم $percentage بالمئة"
        }
    } else {
        "النشاط اليومي، $progressAccessibilityLabel، التقدم $percentage بالمئة"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = cardSemanticLabel
                role = Role.Button
            }
            .clickable { onGoToChecklist() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(softBrandTonalFill())
                .border(
                    BorderStroke(1.dp, semanticColors.borderSubtle.copy(alpha = 0.5f)),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "النشاط اليومي",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (totalCount > 0) "$doneCount من $totalCount مكتملة" else "ابدأ يومك",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = primaryColor.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(60.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier.size(60.dp),
                        color = primaryColor,
                        trackColor = primaryColor.copy(alpha = 0.12f),
                        strokeWidth = 6.dp,
                        strokeCap = if (hasProgress) StrokeCap.Round else StrokeCap.Butt
                    )
                    Text(
                        text = "$percentage%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "التقدم العام",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = primaryColor.copy(alpha = 0.85f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(primaryColor.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "فتح القائمة",
                        color = primaryColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = primaryColor
                    )
                }
            }
        }
    }
}

data class DailyActivityItemData(
    val id: String,
    val title: String,
    val currentCount: Int,
    val targetCount: Int,
    val unit: String,
    val isCompleted: Boolean,
    val route: String
) {
    val progress: Float
        get() = if (targetCount > 0) {
            currentCount.coerceAtMost(targetCount).toFloat() / targetCount.toFloat()
        } else {
            0f
        }
}
