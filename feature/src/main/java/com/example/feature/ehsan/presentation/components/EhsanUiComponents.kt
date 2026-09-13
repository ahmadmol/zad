package com.example.feature.ehsan.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.designsystem.theme.IhsanTheme
import com.example.designsystem.theme.PrimaryTeal
import com.example.feature.R
import com.example.feature.ehsan.domain.model.Donation

/**
 * Atmospheric Header Banner for the Ehsan Screen
 * Matches the reference design with mosque silhouettes, top bar controls, and hero text.
 */
@Composable
fun EhsanHeaderBanner(
    cityName: String = "حلب",
    islamicDate: String = "١٢ ربيع الأول ١٤٤٨",
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val darkTeal = PrimaryTeal
    val bannerShape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(bannerShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF06413E),
                        Color(0xFF095A54),
                        Color(0xFF147B73)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        // Mosque silhouette background overlay
        Image(
            painter = painterResource(id = R.drawable.bg_ehsan),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.Crop,
            alpha = 0.25f
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Actions: Notifications & Search
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "التنبيهات",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Center Title & Logo
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🌱", fontSize = 12.sp)
                            }
                        }
                        Text(
                            text = "إحسان",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "خير دائم",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                // Right Location & Date Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = cityName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = islamicDate,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hero Main Title
            Text(
                text = "يداً بيد لنشر الخير",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Hero Subtitle
            Text(
                text = "معاً نصنع مجتمعاً أكثر تماسكاً\nفي مدينتنا الحبيبة $cityName",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.88f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Leaf flourish icon
            Text(text = "🌿", fontSize = 14.sp)
        }
    }
}

/**
 * Segmented Tabs for switching between "عروض تبرع" (OFFER) and "طلبات مساعدة" (REQUEST)
 */
@Composable
fun EhsanSegmentedTabs(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTeal = PrimaryTeal

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFFF0EBE3),
        border = BorderStroke(0.5.dp, Color(0xFFE2DACF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // OFFER Tab
            val isOffer = selectedType == "OFFER"
            Surface(
                onClick = { onTypeSelected("OFFER") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(22.dp),
                color = if (isOffer) darkTeal else Color.Transparent,
                shadowElevation = if (isOffer) 2.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "عروض تبرع",
                        fontSize = 15.sp,
                        fontWeight = if (isOffer) FontWeight.Bold else FontWeight.Medium,
                        color = if (isOffer) Color.White else Color(0xFF55605E)
                    )
                }
            }

            // REQUEST Tab
            val isRequest = selectedType == "REQUEST"
            Surface(
                onClick = { onTypeSelected("REQUEST") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(22.dp),
                color = if (isRequest) darkTeal else Color.Transparent,
                shadowElevation = if (isRequest) 2.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "طلبات مساعدة",
                        fontSize = 15.sp,
                        fontWeight = if (isRequest) FontWeight.Bold else FontWeight.Medium,
                        color = if (isRequest) Color.White else Color(0xFF55605E)
                    )
                }
            }
        }
    }
}

/**
 * Rounded Search TextField matching reference
 */
@Composable
fun EhsanSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    cityName: String = "حلب",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        placeholder = {
            Text(
                text = "ابحث عن فرصة خيرية في $cityName ...",
                fontSize = 13.sp,
                color = Color(0xFF8C9694)
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF8C9694),
                modifier = Modifier.size(20.dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = PrimaryTeal,
            unfocusedBorderColor = Color(0xFFE5DFD5)
        )
    )
}

/**
 * Filter / Sorting Chips Row (الكل / الأحدث / الأقرب)
 */
@Composable
fun EhsanFilterRow(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTeal = PrimaryTeal

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            val isSelected = selectedCategory == "الكل"
            Surface(
                onClick = { onCategoryChange("الكل") },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) darkTeal else Color.White,
                border = BorderStroke(1.dp, if (isSelected) darkTeal else Color(0xFFE0D8CE))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Color(0xFF55605E),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "الكل",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF333D3B)
                    )
                }
            }
        }

        item {
            Surface(
                onClick = { onCategoryChange("الأحدث") },
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE0D8CE))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF55605E),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "الأحدث",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333D3B)
                    )
                }
            }
        }

        item {
            Surface(
                onClick = { onCategoryChange("الأقرب") },
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE0D8CE))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF55605E),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "الأقرب",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333D3B)
                    )
                }
            }
        }
    }
}

/**
 * Detailed Donation Card item matching reference
 */
@Composable
fun DonationCardItem(
    donation: Donation,
    imageModel: Any?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTeal = PrimaryTeal
    val isOffer = donation.type == "OFFER"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(0.75.dp, Color(0xFFEAE3D9)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Content Section (Title, Badge, Metadata, Action Buttons)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                // Top Tag Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOffer) Color(0xFFE4F3EC) else Color(0xFFFDECE5)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isOffer) Icons.Default.CardGiftcard else Icons.Default.Handshake,
                            contentDescription = null,
                            tint = if (isOffer) Color(0xFF1E7A5A) else Color(0xFFD65228),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isOffer) "عرض تبرع" else "طلب مساعدة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOffer) Color(0xFF1E7A5A) else Color(0xFFD65228)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = donation.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkTeal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                if (donation.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = donation.description,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7775),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Metadata (Location + Time ago)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF8A9593),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = donation.location.ifBlank { "حلب" },
                            fontSize = 11.sp,
                            color = Color(0xFF8A9593)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color(0xFF8A9593),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "منذ ٣ ساعات",
                            fontSize = 11.sp,
                            color = Color(0xFF8A9593)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Row (عرض التفاصيل + تواصل)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // View Details Button
                    Surface(
                        onClick = onClick,
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE8F2EF),
                        border = BorderStroke(0.5.dp, Color(0xFFCDE2DE))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "عرض التفاصيل",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkTeal
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = darkTeal,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Contact Button
                    Surface(
                        onClick = onClick,
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF7F5EE),
                        border = BorderStroke(0.5.dp, Color(0xFFE2DDD2))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "تواصل",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4A5654)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Comment,
                                contentDescription = null,
                                tint = Color(0xFF4A5654),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // Right Thumbnail Section
            Surface(
                modifier = Modifier
                    .size(105.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color(0xFFF2EFE9)
            ) {
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (donation.category) {
                                "طعام" -> Icons.Default.Restaurant
                                "ملابس" -> Icons.Default.Checkroom
                                "أثاث" -> Icons.Default.Weekend
                                else -> Icons.Default.Category
                            },
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFFA0AAA8)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Floating Action Button for adding offer/help
 */
@Composable
fun AddEhsanFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTeal = PrimaryTeal

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = darkTeal,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "أضف عرضاً",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "🌱",
                fontSize = 10.sp
            )
            Text(
                text = "أضف عرضاً",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/** Local board notice card preserved for safety */
@Composable
fun LocalBoardNoticeCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceWarm),
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IhsanTheme.colors.surfaceMint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "لوحة إحسان المحلية",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "القوائم محفوظة على جهازك فقط. لا يوجد تحقق رسمي من الحالات، ولا مدفوعات داخل التطبيق.",
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondaryMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EhsanEmptyContent(
    searchQuery: String,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = Color(0xFF8A9593),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "لا توجد فرص إحسان متاحة حالياً",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryTeal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddEhsanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("إضافة إحسان", fontWeight = FontWeight.Bold)
    }
}

val EhsanCityFilterOptions = listOf("الكل", "حلب", "دمشق", "حمص", "حماة")
val EhsanCategoryFilterOptions = listOf("الكل", "طعام", "ملابس", "أثاث", "أخرى")
