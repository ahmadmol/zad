package com.example.feature.fahmanallah.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.fahmanallah.domain.model.FahmEpisode
import java.util.Locale

@Composable
fun FahmCompletionCard(
    episode: FahmEpisode,
    onNextEpisodeClick: () -> Unit,
    onReturnToJourneyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedNumber = String.format(Locale.getDefault(), "%02d", episode.number)
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Celebration Icon Badge
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "أحسنت!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "لقد أنهيت الدرس $formattedNumber",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = IhsanTheme.colors.textSecondaryMuted
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = episode.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = IhsanTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )

        if (!episode.quote.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "«${episode.quote}»",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary,
                        textAlign = TextAlign.Center
                    )

                    if (!episode.quoteSource.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "(${episode.quoteSource})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = IhsanTheme.colors.textSecondaryMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Next Episode Button
        Button(
            onClick = onNextEpisodeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "الانتقال إلى الدرس التالي",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Return to Journey Button
        OutlinedButton(
            onClick = onReturnToJourneyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, primary)
        ) {
            Text(
                text = "العودة إلى رحلتي",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        }
    }
}
