package com.example.feature.fahmanallah.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.fahmanallah.domain.model.FahmEpisode
import com.example.feature.fahmanallah.domain.model.FahmEpisodeProgressState
import java.util.Locale

@Composable
fun FahmJourneyItem(
    episode: FahmEpisode,
    onEpisodeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedNumber = String.format(Locale.getDefault(), "%02d", episode.number)
    val isCompleted = episode.progressState == FahmEpisodeProgressState.COMPLETED
    val isInProgress = episode.progressState == FahmEpisodeProgressState.IN_PROGRESS

    val primaryColor = MaterialTheme.colorScheme.primary
    val cardBg = if (isInProgress) primaryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    val borderClr = if (isInProgress) primaryColor.copy(alpha = 0.5f) else IhsanTheme.colors.borderSubtle.copy(alpha = 0.4f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEpisodeClick(episode.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(0.8.dp, borderClr),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInProgress) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge / Number
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> primaryColor
                            isInProgress -> primaryColor.copy(alpha = 0.2f)
                            else -> IhsanTheme.colors.brand.copy(alpha = 0.08f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "مكتمل",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (isInProgress) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "في التقدم",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = formattedNumber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IhsanTheme.colors.textSecondaryMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Episode Title
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.title,
                    fontSize = 13.5.sp,
                    fontWeight = if (isInProgress || isCompleted) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCompleted) primaryColor else IhsanTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!episode.subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = episode.subtitle,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = IhsanTheme.colors.textSecondaryMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isInProgress) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "الحالي",
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
