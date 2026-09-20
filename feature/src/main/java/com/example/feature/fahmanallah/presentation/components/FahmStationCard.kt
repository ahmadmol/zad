package com.example.feature.fahmanallah.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.fahmanallah.domain.model.FahmStation

@Composable
fun FahmStationCard(
    station: FahmStation,
    modifier: Modifier = Modifier
) {
    val stationIcon: ImageVector = when (station.iconName) {
        "leaf" -> Icons.Default.FilterVintage
        "lantern" -> Icons.Default.Lightbulb
        "hands" -> Icons.Default.Handshake
        "heart_shield" -> Icons.Default.Shield
        "star" -> Icons.Default.Star
        "person_pray" -> Icons.Default.Psychology
        "heart" -> Icons.Default.Favorite
        else -> Icons.Default.AutoAwesome
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.8.dp, IhsanTheme.colors.borderSubtle.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stationIcon,
                    contentDescription = station.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = IhsanTheme.colors.textPrimary
                )

                if (station.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = station.description,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = IhsanTheme.colors.textSecondaryMuted,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FahmStationChipTile(
    station: FahmStation,
    modifier: Modifier = Modifier
) {
    val stationIcon: ImageVector = when (station.iconName) {
        "leaf" -> Icons.Default.FilterVintage
        "lantern" -> Icons.Default.Lightbulb
        "hands" -> Icons.Default.Handshake
        "heart_shield" -> Icons.Default.Shield
        "star" -> Icons.Default.Star
        "person_pray" -> Icons.Default.Psychology
        "heart" -> Icons.Default.Favorite
        else -> Icons.Default.AutoAwesome
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        ),
        border = BorderStroke(0.8.dp, IhsanTheme.colors.borderSubtle.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = stationIcon,
                contentDescription = station.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = station.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = IhsanTheme.colors.textPrimary
            )
        }
    }
}
