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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val buttonText = if (hasLastRead) {
        stringResource(R.string.home_quran_continue_title)
    } else {
        stringResource(R.string.home_quran_start_title)
    }

    val positionText = if (hasLastRead) {
        "سورة ${surahName.orEmpty()} - الآية $ayahNumber"
    } else {
        stringResource(R.string.home_quran_browse_surahs)
    }

    val handleClick = {
        if (surahId != null && ayahNumber != null && surahId > 0 && ayahNumber > 0) {
            onContinueClick(surahId, ayahNumber)
        } else {
            onStartQuranClick()
        }
    }

    val isDark = IhsanTheme.isDark
    val darkTealColor = if (isDark) Color(0xFFE2EBE5) else Color(0xFF1B3B34)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "$positionText، $buttonText"
            }
            .clickable(onClick = handleClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFF9F7F2)
        ),
        border = BorderStroke(1.dp, if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFE8E3D7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 112.dp)
        ) {
            // Background Image: Islamic pattern spanning across card
            Image(
                painter = painterResource(id = R.drawable.ihsan_quran_card_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Physical left corner showing the Quran book cover
            Image(
                painter = painterResource(id = R.drawable.ihsan_quran_book_corner),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(AbsoluteAlignment.CenterLeft)
                    .size(width = 82.dp, height = 96.dp)
            )

            if (isDark) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(Color(0x88151D1A))
                )
            }

            // Content Overlay centered with proper typography
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Calligraphy Header Text (Bismillah)
                Text(
                    text = stringResource(R.string.polish_bismillah),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = darkTealColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Last read position info (Surah Name - Ayah Number)
                Text(
                    text = positionText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = darkTealColor.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Action Button with Navy Fill from Reference 01
                Button(
                    onClick = handleClick,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 18.dp,
                        vertical = 6.dp
                    ),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF1B4E7A) else Color(0xFF15487B),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.heightIn(min = 40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

