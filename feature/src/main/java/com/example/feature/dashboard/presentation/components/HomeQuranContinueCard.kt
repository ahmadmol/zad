package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

    val titleText = if (hasLastRead) {
        stringResource(R.string.home_quran_continue_title)
    } else {
        stringResource(R.string.home_quran_start_title)
    }

    val bodyText = if (hasLastRead) {
        "سورة ${surahName.orEmpty()} • الآية $ayahNumber"
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

    val darkTealColor = Color(0xFF003B46)

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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp)
        ) {
            // Background Image: Quran background spanning smoothly across card
            Image(
                painter = painterResource(id = R.drawable.ihsan_quran_card_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Calligraphy Header Text
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkTealColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Surah & Ayah / Start Info Text
                Text(
                    text = bodyText,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkTealColor.copy(alpha = 0.90f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Action Button with Quran Icon
                Button(
                    onClick = handleClick,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = darkTealColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .height(42.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ihsan_icon_quran),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = titleText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
