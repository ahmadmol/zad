package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

@Composable
fun HomeQuranContinueCard(
    surahName: String?,
    surahId: Int?,
    ayahNumber: Int?,
    onContinueClick: (surahId: Int, ayahNumber: Int) -> Unit,
    onStartQuranClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasLastRead = (surahId != null) && (ayahNumber != null) && (surahId > 0) && (ayahNumber > 0)

    val titleText = if (hasLastRead) {
        stringResource(R.string.home_quran_continue_title)
    } else {
        stringResource(R.string.home_quran_start_title)
    }

    val bodyText = if (hasLastRead) {
        stringResource(R.string.polish_quran_location, surahName.orEmpty(), ayahNumber ?: 0)
    } else {
        stringResource(R.string.home_quran_browse_surahs)
    }

    val handleClick = {
        if (hasLastRead) {
            onContinueClick(surahId, ayahNumber)
        } else {
            onStartQuranClick()
        }
    }

    val darkTealColor = IhsanTheme.colors.textPrimary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "$titleText، $bodyText"
            }
            .clickable(onClick = handleClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 108.dp)
        ) {
            // Background Image: Quran background spanning smoothly across card
            Image(
                painter = painterResource(id = R.drawable.ihsan_quran_card_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            if (IhsanTheme.isDark) {
                Box(Modifier.matchParentSize().background(androidx.compose.ui.graphics.Brush.horizontalGradient(0f to IhsanTheme.colors.surfaceElevated.copy(alpha = .28f), .34f to IhsanTheme.colors.surfaceElevated.copy(alpha = .86f), 1f to IhsanTheme.colors.surfaceElevated.copy(alpha = .86f))))
            }

            // Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Calligraphy Header Text
                Text(
                    text = stringResource(R.string.polish_bismillah),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = darkTealColor,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                // Surah & Ayah / Start Info Text
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = darkTealColor.copy(alpha = 0.90f),
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                // Action Button with Quran Icon
                Button(
                    onClick = handleClick,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth(.68f)
                        .heightIn(min = 36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = titleText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
