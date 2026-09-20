package com.example.feature.fahmanallah.presentation

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

@Composable
fun FahmAboutScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = IhsanTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "عن الفهم عن الله",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Artwork Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(IhsanTheme.colors.brand.copy(alpha = 0.1f))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ihsan_mosque_sunrise_landscape),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(primary.copy(alpha = 0.35f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "الفهم عن الله",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "الجزء الأول • رمضان 2023",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "مع د. عمرو خالد",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Description Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(0.8.dp, IhsanTheme.colors.borderSubtle.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "عن البرنامج",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "رحلة من 29 درسًا (ضمن تصور 30 قاعدة) لفهم تدبير الله في حياتنا، مستلهمة من قصص الأنبياء، والسيرة، ومبنية على منازل الروح السبعة، لنصل إلى حياة أقرب إلى الله...",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = IhsanTheme.colors.textPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            // Source Attribution Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(0.8.dp, IhsanTheme.colors.borderSubtle.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "المصدر والنسبة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "برنامج «الفهم عن الله — الجزء الأول» هو محتوى مرئي ودعوي للدكتور عمرو خالد تم بثه في رمضان 2023.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = IhsanTheme.colors.textPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(primary.copy(alpha = 0.08f))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, "https://www.youtube.com/@AmrKhaled".toUri())
                                context.startActivity(intent)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "قناة د. عمرو خالد الرسمية",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = primary
                            )

                            Text(
                                text = "زيارة القناة على YouTube",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = IhsanTheme.colors.textSecondaryMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
